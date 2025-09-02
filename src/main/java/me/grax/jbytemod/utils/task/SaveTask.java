package me.grax.jbytemod.utils.task;

import me.grax.jbytemod.JByteMod;
import me.grax.jbytemod.JarArchive;
import me.grax.jbytemod.ui.PageEndPanel;
import me.lpk.util.JarUtils;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

import javax.swing.*;
import java.io.File;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

public class SaveTask extends SwingWorker<Void, Integer> {

  private File output;
  private PageEndPanel jpb;
  private JarArchive file;

  public SaveTask(JByteMod jbm, File output, JarArchive file) {
    this.output = output;
    this.file = file;
    this.jpb = jbm.getPP();
  }

  @Override
  protected Void doInBackground() throws Exception {
    try {
      Map<String, ClassNode> classes = this.file.getClasses();
      Map<String, byte[]> outputBytes = this.file.getOutput();
      int flags = JByteMod.ops.get("compute_maxs").getBoolean() ? 1 : 0;
      JByteMod.LOGGER.log("Writing..");
      if (this.file.isSingleEntry()) {
        ClassNode node = classes.values().iterator().next();
        ClassWriter writer = new ClassWriter(flags);
        node.accept(writer);
        publish(50);
        JByteMod.LOGGER.log("Saving..");
        Files.write(this.output.toPath(), writer.toByteArray());
        publish(100);
        JByteMod.LOGGER.log("Saving successful!");
        return null;
      }

      publish(0);
      double size = classes.keySet().size();
      double i = 0;
      for (String s : classes.keySet()) {
        ClassNode node = classes.get(s);
        ClassWriter writer = new ClassWriter(flags);
        node.accept(writer);
        String name;
        if (node.innerPath != null && !node.innerPath.isEmpty()) {
          name = node.innerPath;
        } else {
          name = s + ".class";
        }
        outputBytes.put(name, writer.toByteArray());
        publish((int) ((i++ / size) * 20d));
      }
      JByteMod.LOGGER.log("Saving..");
      JarUtils.saveAsJar(outputBytes, output.getAbsolutePath(), process -> publish((int) (20d + process*80d)));
      JByteMod.LOGGER.log("Saving successful!");
    } catch (Exception e) {
      e.printStackTrace();
      JByteMod.LOGGER.log("Saving failed!");
    }
    publish(100);
    return null;
  }

  @Override
  protected void process(List<Integer> chunks) {
    int i = chunks.get(chunks.size() - 1);
    jpb.setValue(i);
    super.process(chunks);
  }

}