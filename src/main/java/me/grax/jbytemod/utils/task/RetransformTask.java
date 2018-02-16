package me.grax.jbytemod.utils.task;

import java.io.File;
import java.lang.instrument.ClassDefinition;
import java.lang.instrument.Instrumentation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import org.objectweb.asm.tree.ClassNode;

import me.grax.jbytemod.JByteMod;
import me.grax.jbytemod.JarArchive;
import me.grax.jbytemod.ui.PageEndPanel;
import me.grax.jbytemod.utils.ErrorDisplay;
import me.lpk.util.ASMUtils;

public class RetransformTask extends SwingWorker<Void, Integer> {

  private Instrumentation ins;
  private PageEndPanel jpb;
  private JarArchive file;

  public RetransformTask(JByteMod jbm, Instrumentation ins, JarArchive file) {
    this.ins = ins;
    this.file = file;
    this.jpb = jbm.getPP();
  }

  @Override
  protected Void doInBackground() throws Exception {
    publish(0);
    try {
      Map<String, ClassNode> classes = file.getClasses();
      Map<String, byte[]> original = file.getOutput();
      Map<String, byte[]> newOriginal = new HashMap<>();

      ArrayList<ClassDefinition> definitions = new ArrayList<>();
      double size = classes.size();
      if (size == 0) {
        publish(100);
        return null;
      }
      int i = 0;
      for (Entry<String, ClassNode> e : classes.entrySet()) {
        publish((int) ((i / size) * 80d));
        byte[] originalBytes = original.get(e.getKey());
        byte[] bytes = ASMUtils.getNodeBytes0(e.getValue());
        if (!Arrays.equals(bytes, originalBytes)) {
          System.out.println("Retransform " + e.getKey());
          definitions.add(new ClassDefinition(ClassLoader.getSystemClassLoader().loadClass(e.getKey().replace('/', '.')), bytes));
          newOriginal.put(e.getKey(), bytes);
        }
        i++;
      }
      if (definitions.isEmpty()) {
        JOptionPane.showMessageDialog(null, "Nothing to redefine!");
      } else {
        publish(80);
        ins.redefineClasses(definitions.toArray(new ClassDefinition[0]));
        JByteMod.LOGGER.log("Successfully retransformed " + newOriginal.size() + " classes");
        original.putAll(newOriginal);
        publish(100);
      }
    } catch (VerifyError v) {
      JOptionPane.showMessageDialog(null, "VerifyError! Make sure bytecode is valid or restart process with \"-noverify\" as argument");
    } catch (Throwable t) {
      new ErrorDisplay(t);
      t.printStackTrace();
    }
    return null;
  }

  @Override
  protected void process(List<Integer> chunks) {
    int i = chunks.get(chunks.size() - 1);
    jpb.setValue(i);
    super.process(chunks);
  }
}
