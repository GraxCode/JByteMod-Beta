package me.grax.jbytemod.utils;

import static org.objectweb.asm.Opcodes.ACONST_NULL;
import static org.objectweb.asm.Opcodes.ARETURN;
import static org.objectweb.asm.Opcodes.DCONST_0;
import static org.objectweb.asm.Opcodes.DRETURN;
import static org.objectweb.asm.Opcodes.FCONST_0;
import static org.objectweb.asm.Opcodes.FRETURN;
import static org.objectweb.asm.Opcodes.ICONST_0;
import static org.objectweb.asm.Opcodes.IRETURN;
import static org.objectweb.asm.Opcodes.LCONST_0;
import static org.objectweb.asm.Opcodes.LRETURN;
import static org.objectweb.asm.Opcodes.RETURN;

import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodNode;

public class MethodUtils {
  public static void clear(MethodNode mn) {
    mn.instructions.clear();
    mn.instructions.add(generateReturn(mn.desc));
    mn.tryCatchBlocks.clear();
    mn.localVariables.clear();
    mn.exceptions.clear();
    mn.maxStack = 1;
    mn.maxLocals = 1;
  }

  public static InsnList generateReturn(String desc) {
    InsnList a = new InsnList();
    String after = desc.split("\\)")[1];
    a.add(new LabelNode());
    if (after.startsWith("[") || after.endsWith(";")) {
      a.add(new InsnNode(ACONST_NULL));
      a.add(new InsnNode(ARETURN));
    } else {
      switch (desc.toCharArray()[desc.length() - 1]) {
      case 'V':
        a.add(new InsnNode(RETURN));
        break;
      case 'D':
        a.add(new InsnNode(DCONST_0));
        a.add(new InsnNode(DRETURN));
        break;
      case 'F':
        a.add(new InsnNode(FCONST_0));
        a.add(new InsnNode(FRETURN));
        break;
      case 'J':
        a.add(new InsnNode(LCONST_0));
        a.add(new InsnNode(LRETURN));
        break;
      default:
        a.add(new InsnNode(ICONST_0));
        a.add(new InsnNode(IRETURN));
        break;
      }
    }
    if (desc.endsWith("V")) {
      a.size = 2;
    } else {
      a.size = 3;
    }
    return a;
  }
}
