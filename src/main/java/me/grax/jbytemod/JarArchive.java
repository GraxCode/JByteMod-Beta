package me.grax.jbytemod;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.objectweb.asm.tree.ClassNode;

import me.grax.jbytemod.utils.ErrorDisplay;
import me.grax.jbytemod.utils.task.LoadTask;

public class JarArchive {
  protected Map<String, ClassNode> classes;
  protected Map<String, byte[]> output;
  private boolean singleEntry;

  public JarArchive(Map<String, ClassNode> classes, Map<String, byte[]> output) {
    super();
    this.classes = classes;
    this.output = output;
  }

  public JarArchive(ClassNode cn) {
    super();
    this.classes = new HashMap<>();
    this.singleEntry = true;
    classes.put(cn.name, cn);
  }

  public JarArchive(JByteMod jbm, File input) {
    try {
      new LoadTask(jbm, input, this).execute();
    } catch (Throwable t) {
      new ErrorDisplay(t);
    }
  } 

  public boolean isSingleEntry() {
    return singleEntry;
  }

  public Map<String, ClassNode> getClasses() {
    return classes;
  }

  public Map<String, byte[]> getOutput() {
    return output;
  }

  public void setClasses(Map<String, ClassNode> classes) {
    this.classes = classes;
  }

  public void setOutput(Map<String, byte[]> output) {
    this.output = output;
  }
}
