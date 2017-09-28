package me.grax.jbytemod.utils.list;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TryCatchBlockNode;

import me.grax.jbytemod.utils.InstrUtils;
import me.grax.jbytemod.utils.TextUtils;
import me.lpk.util.OpUtils;

public class LVPEntry {
  private ClassNode cn;
  private MethodNode mn;
  private LocalVariableNode lvn;
  private String text;

  public ClassNode getCn() {
    return cn;
  }

  public MethodNode getMn() {
    return mn;
  }

  public LVPEntry(ClassNode cn, MethodNode mn, LocalVariableNode lvn) {
    this.cn = cn;
    this.mn = mn;
    this.lvn = lvn;
    this.text = TextUtils.toHtml(TextUtils.toBold("#" + lvn.index) + " ");
    if(lvn.desc != null && !lvn.desc.isEmpty()) {
      this.text += InstrUtils.getDisplayType(lvn.desc) + " ";
    }
    this.text += TextUtils.addTag(TextUtils.escape(lvn.name), "font color=#995555");
  }


  @Override
  public String toString() {
    return text;
  }
}
