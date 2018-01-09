package me.grax.jbytemod.utils.list;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TryCatchBlockNode;

import me.grax.jbytemod.utils.InstrUtils;
import me.grax.jbytemod.utils.TextUtils;
import me.lpk.util.OpUtils;

public class TCBEntry {
  private ClassNode cn;
  private MethodNode mn;
  private TryCatchBlockNode tcbn;
  private String text;

  public ClassNode getCn() {
    return cn;
  }

  public MethodNode getMn() {
    return mn;
  }

  public TCBEntry(ClassNode cn, MethodNode mn, TryCatchBlockNode tcbn) {
    this.cn = cn;
    this.mn = mn;
    this.tcbn = tcbn;
    this.text = TextUtils.toHtml(
        InstrUtils.getDisplayType(tcbn.type != null ? tcbn.type : "Lnull;", true) + ": label " + OpUtils.getLabelIndex(tcbn.start) + " -> label "
            + OpUtils.getLabelIndex(tcbn.end) + " handler: label " + (tcbn.handler == null ? "null" : OpUtils.getLabelIndex(tcbn.handler)));
  }

  @Override
  public String toString() {
    return text;
  }

  public TryCatchBlockNode getTcbn() {
    return tcbn;
  }
}
