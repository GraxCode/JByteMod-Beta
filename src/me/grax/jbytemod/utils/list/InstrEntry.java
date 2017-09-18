package me.grax.jbytemod.utils.list;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodNode;

import me.grax.jbytemod.utils.InstrUtils;
import me.grax.jbytemod.utils.TextUtils;

public class InstrEntry {
  private MethodNode m;
  private AbstractInsnNode i;

  public MethodNode getMethod() {
    return m;
  }

  public void setM(MethodNode m) {
    this.m = m;
  }

  public AbstractInsnNode getInstr() {
    return i;
  }

  public void setI(AbstractInsnNode i) {
    this.i = i;
  }

  public InstrEntry(MethodNode m, AbstractInsnNode i) {
    this.m = m;
    this.i = (AbstractInsnNode) i;
  }

  @Override
  public String toString() {
    return TextUtils.toHtml(InstrUtils.toString(i));
  }
}
