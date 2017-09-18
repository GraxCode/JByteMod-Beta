package me.grax.jbytemod;

import java.io.File;
import java.util.List;
import java.util.Map;

import javax.swing.SwingWorker;

import org.objectweb.asm.tree.ClassNode;

import me.grax.jbytemod.ui.PageEndPanel;
import me.grax.jbytemod.utils.ErrorDisplay;
import me.lpk.util.JarUtils;

public class JarFile {
  private Map<String, ClassNode> classes;
  private Map<String, byte[]> output;

  public JarFile(Map<String, ClassNode> classes, Map<String, byte[]> output) {
    super();
    this.classes = classes;
    this.output = output;
  }

  public JarFile(JByteMod jbm, File input) {
    try {
      //      jbm.getPP().setValue(0);
      //      this.classes = JarUtils.loadClasses(input);
      //      jbm.getPP().setValue(50);
      //      this.output = JarUtils.loadNonClassEntries(input);
      //      jbm.getPP().setValue(100);
      new TaskLoadFile(jbm, input).execute();
    } catch (Throwable t) {
      new ErrorDisplay(t);
    }
  }

  public Map<String, ClassNode> getClasses() {
    return classes;
  }

  public Map<String, byte[]> getOutput() {
    return output;
  }

  class TaskLoadFile extends SwingWorker<Void, Integer> {

    private File input;
    private PageEndPanel jpb;
    private JByteMod jbm;

    public TaskLoadFile(JByteMod jbm, File input) {
      this.input = input;
      this.jbm = jbm;
      this.jpb = jbm.getPP();
    }

    @Override
    protected Void doInBackground() throws Exception {
      publish(0);
      JarFile.this.classes = JarUtils.loadClasses(input);
      publish(50);
      JarFile.this.output = JarUtils.loadNonClassEntries(input);
      publish(90);
      jbm.refreshTree();
      publish(100);
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
}
