package me.grax.jbytemod.utils.list;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import me.grax.jbytemod.utils.InstrUtils;
import me.grax.jbytemod.utils.TextUtils;

public class SearchEntry {
  private ClassNode cn;
  private MethodNode mn;
  private String found;

  public ClassNode getCn() {
    return cn;
  }

  public MethodNode getMn() {
    return mn;
  }

  public String getFound() {
    return found;
  }

  public SearchEntry(ClassNode cn, MethodNode mn, String found) {
    super();
    this.cn = cn;
    this.mn = mn;
    this.found = found;
  }

  @Override
  public String toString() {
    return TextUtils.toHtml(
        InstrUtils.getDisplayClass(cn.name) + "." + TextUtils.escape(mn.name) + " - " + TextUtils.addTag("\"" + found + "\"", "font color=#559955"));
  }
}
