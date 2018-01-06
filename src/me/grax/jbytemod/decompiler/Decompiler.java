package me.grax.jbytemod.decompiler;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

import me.grax.jbytemod.JByteMod;
import me.grax.jbytemod.ui.DecompilerPanel;

public abstract class Decompiler extends Thread {
  protected JByteMod jbm;
  protected ClassNode cn;
  protected DecompilerPanel dp;
  /**
   * Do not reload if we already know the output
   */
  public static ClassNode last;
  public static String lastOutput;

  public Decompiler(JByteMod jbm, DecompilerPanel dp) {
    this.jbm = jbm;
    this.dp = dp;
  }


  public Decompiler setNode(ClassNode cn) {
    this.cn = cn;
    return this;
  }

  public Decompiler deleteCache() {
    last = null;
    return this;
  }

  
  @Override
  public final void run() {
    dp.setText("Loading...");
    if(cn == null) {
      dp.setText("ClassNode is null.");
      return;
    }
    dp.setText(lastOutput = this.decompile(cn));
  }

  protected String decompile(ClassNode cn) {
    if (last != null && cn.equals(last)) {
      //same node, same output
      System.out.println("cache");
      return lastOutput;
    }
    last = cn;
    System.out.println("load");
    //do not regenerate anything here
    ClassWriter cw = new ClassWriter(0);
    cn.accept(cw);
    return decompile(cw.toByteArray());
  }

  protected abstract String decompile(byte[] b);
}
