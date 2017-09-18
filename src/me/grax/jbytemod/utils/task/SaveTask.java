package me.grax.jbytemod.utils.task;

import java.io.File;
import java.util.List;
import java.util.Map;

import javax.swing.SwingWorker;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

import me.grax.jbytemod.JByteMod;
import me.grax.jbytemod.JarFile;
import me.grax.jbytemod.ui.PageEndPanel;
import me.lpk.util.JarUtils;

public class SaveTask extends SwingWorker<Void, Integer> {

  private File output;
  private PageEndPanel jpb;
  private JByteMod jbm;
  private JarFile file;

  public SaveTask(JByteMod jbm, File output, JarFile file) {
    this.output = output;
    this.jbm = jbm;
    this.file = file;
    this.jpb = jbm.getPP();
  }

  @Override
  protected Void doInBackground() throws Exception {
    Map<String, ClassNode> classes = this.file.getClasses();
    Map<String, byte[]> outputBytes = this.file.getOutput();
    System.out.println("Writing..");
    for (String s : classes.keySet()) {
      ClassNode node = classes.get(s);
      ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
      node.accept(writer);
      outputBytes.put(s, writer.toByteArray());
    }
    System.out.println("Saving..");
    JarUtils.saveAsJar(outputBytes, output.getAbsolutePath());
    System.out.println("Done!");
    return null;
  }

  @Override
  protected void process(List<Integer> chunks) {
    int i = chunks.get(chunks.size() - 1);
    jpb.setValue(i);
    super.process(chunks);
  }

  @Override
  protected void done() {
    jbm.refreshTree();
  }
}