package me.grax.jbytemod.ui.lists.entries;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;

import me.grax.jbytemod.utils.InstrUtils;
import me.grax.jbytemod.utils.TextUtils;

public class FieldEntry extends InstrEntry {

  private ClassNode cn;
  private FieldNode fn;
  private String text;
  private String easyText;

  public FieldEntry(ClassNode cn, FieldNode fn) {
    super(null, null);
    this.cn = cn;
    this.fn = fn;
    this.text = TextUtils
        .toHtml(InstrUtils.getDisplayAccess(fn.access) + " " + InstrUtils.getDisplayType(fn.desc, true) + " " + InstrUtils.getDisplayClassRed(fn.name)
            + " = " + (fn.value instanceof String ? TextUtils.addTag("\"" + TextUtils.escape(String.valueOf(fn.value)) + "\"", "font color=#559955")
                : fn.value != null ? String.valueOf(fn.value) : TextUtils.toBold("null")));
    this.easyText = InstrUtils.getDisplayAccess(fn.access) + " " + InstrUtils.getDisplayType(fn.desc, false) + " "
        + InstrUtils.getDisplayClassEasy(fn.name) + " = "
        + (fn.value instanceof String ? "\"" + String.valueOf(fn.value) + "\"" : String.valueOf(fn.value));
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

  public String toEasyString() {
    return easyText;
  }
}
