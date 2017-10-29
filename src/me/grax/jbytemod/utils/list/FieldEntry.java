package me.grax.jbytemod.utils.list;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;

import me.grax.jbytemod.utils.InstrUtils;
import me.grax.jbytemod.utils.TextUtils;

public class FieldEntry extends InstrEntry {

  private ClassNode cn;
  private FieldNode fn;
  private String text;

  public FieldEntry(ClassNode cn, FieldNode fn) {
    super(null, null);
    this.cn = cn;
    this.fn = fn;
    this.text = TextUtils.toHtml(InstrUtils.getDisplayAccess(fn.access) + " " + InstrUtils.getDisplayType(fn.desc, true) + " "
        + InstrUtils.getDisplayClassRed(fn.name) + " = " + TextUtils.toBold(String.valueOf(fn.value)));
  }

  @Override
  public String toString() {
    return text;
  }

  public ClassNode getCn() {
    return cn;
  }

  public void setCn(ClassNode cn) {
    this.cn = cn;
  }

  public FieldNode getFn() {
    return fn;
  }

  public void setFn(FieldNode fn) {
    this.fn = fn;
  }
  
}
