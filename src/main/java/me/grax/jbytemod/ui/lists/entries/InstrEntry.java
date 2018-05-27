package me.grax.jbytemod.ui.lists.entries;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodNode;

import me.grax.jbytemod.utils.InstrUtils;
import me.grax.jbytemod.utils.TextUtils;
import me.grax.jbytemod.utils.asm.Hints;

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

  public String toEasyString() {
    return InstrUtils.toEasyString(i);
  }

  public String getHint() {
    if (i != null && i.getOpcode() >= 0) {
      return Hints.hints[i.getOpcode()];
    }
    return null;
  }
}
