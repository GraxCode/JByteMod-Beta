package me.grax.jbytemod.utils.list;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import me.grax.jbytemod.utils.InstrUtils;
import me.grax.jbytemod.utils.TextUtils;
import me.lpk.util.OpUtils;

public class SearchEntry {
  private ClassNode cn;
  private MethodNode mn;
  private String found;
  private String text;

  public ClassNode getCn() {
    return cn;
  }

  public MethodNode getMn() {
    return mn;
  }

  public String getFound() {
    return found;
  }

  /**
   * Construct prototype entry
   */
  public SearchEntry() {
    this.text = " ";
  }

  public SearchEntry(ClassNode cn, MethodNode mn, String found) {
    this.cn = cn;
    this.mn = mn;
    this.found = found;
    this.text = TextUtils.toHtml(
        InstrUtils.getDisplayClass(cn.name) + "." + TextUtils.escape(mn.name) + " - " + TextUtils.addTag("\"" + found + "\"", "font color=#559955"));
  }

  public SearchEntry(ClassNode cn, MethodNode mn, FieldInsnNode fin) {
    this(cn, mn, fin.owner, fin.name, fin.desc, fin.getOpcode());
  }

  public SearchEntry(ClassNode cn, MethodNode mn, MethodInsnNode min) {
    this(cn, mn, min.owner, min.name, min.desc, min.getOpcode());
  }

  public SearchEntry(ClassNode cn, MethodNode mn, String owner, String name, String desc, int opcode) {
    this.cn = cn;
    this.mn = mn;
    this.found = owner + "." + name + desc;
    this.text = TextUtils.toHtml(InstrUtils.getDisplayClass(cn.name) + "." + TextUtils.escape(mn.name) + " - "
        + TextUtils.toBold(OpUtils.getOpcodeText(opcode).toLowerCase()) + " " + InstrUtils.getDisplayClassRed(owner) + "." + TextUtils.escape(name)
        + "(" + InstrUtils.getDisplayArgs(TextUtils.escape(desc)) + ")");
  }

  @Override
  public String toString() {
    return text;
  }
}
