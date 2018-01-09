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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.IincInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.LookupSwitchInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.MultiANewArrayInsnNode;
import org.objectweb.asm.tree.TableSwitchInsnNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

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

  public static void removeLines(MethodNode mn) {
    int i = 0;
    for (AbstractInsnNode ain : mn.instructions.toArray()) {
      if (ain instanceof LineNumberNode) {
        mn.instructions.remove(ain);
        i++;
      }
    }
    System.out.println("Removed " + i + " nodes!");
  }

  public static InsnList clone(InsnList il, LabelNode end) { // TODO: invokedynamic
    HashMap<LabelNode, LabelNode> cloned = cloneLabels(il);
    InsnList list = new InsnList();
    Loop: for (AbstractInsnNode ain : il.toArray()) {
      switch (ain.getType()) {
      case AbstractInsnNode.FIELD_INSN:
        FieldInsnNode fin = (FieldInsnNode) ain;
        list.add(new FieldInsnNode(ain.getOpcode(), fin.owner, fin.name, fin.desc));
        break;
      case AbstractInsnNode.FRAME:
        FrameNode fn = (FrameNode) ain;
        list.add(new FrameNode(fn.type, fn.local.size(), new ArrayList(fn.local).toArray(), fn.stack.size(), new ArrayList(fn.stack).toArray()));
        break;
      case AbstractInsnNode.IINC_INSN:
        IincInsnNode iinc = (IincInsnNode) ain;
        list.add(new IincInsnNode(iinc.var, iinc.incr));
        break;
      case AbstractInsnNode.INSN:
        InsnNode in = (InsnNode) ain;
        list.add(new InsnNode(in.getOpcode()));
        break;
      case AbstractInsnNode.INT_INSN:
        IntInsnNode iin = (IntInsnNode) ain;
        list.add(new IntInsnNode(iin.getOpcode(), iin.operand));
        break;
      case AbstractInsnNode.JUMP_INSN:
        JumpInsnNode jin = (JumpInsnNode) ain;
        list.add(new JumpInsnNode(jin.getOpcode(), cloned.get(jin.label)));
        break;
      case AbstractInsnNode.LABEL:
        if (ain == end) {
          list.add(cloned.get((LabelNode) ain));
          list.add(new InsnNode(Opcodes.RETURN));
          return list;
        }
        list.add(cloned.get((LabelNode) ain));
        break;
      case AbstractInsnNode.LDC_INSN:
        LdcInsnNode ldc = (LdcInsnNode) ain;
        list.add(new LdcInsnNode(ldc.cst));
        break;
      case AbstractInsnNode.LINE:
        LineNumberNode line = (LineNumberNode) ain;
        list.add(new LineNumberNode(line.line, cloned.get(line.start)));
        break;
      case AbstractInsnNode.LOOKUPSWITCH_INSN:
        LookupSwitchInsnNode lsin = (LookupSwitchInsnNode) ain;
        list.add(new LookupSwitchInsnNode(cloned.get(lsin.dflt), toIntArray(lsin.keys), mapLabels(lsin.labels, cloned).toArray(new LabelNode[0])));
        break;
      case AbstractInsnNode.METHOD_INSN:
        MethodInsnNode min = (MethodInsnNode) ain;
        list.add(new MethodInsnNode(min.getOpcode(), min.owner, min.name, min.desc));
        break;
      case AbstractInsnNode.MULTIANEWARRAY_INSN:
        MultiANewArrayInsnNode manain = (MultiANewArrayInsnNode) ain;
        list.add(new MultiANewArrayInsnNode(manain.desc, manain.dims));
        break;
      case AbstractInsnNode.TABLESWITCH_INSN:
        TableSwitchInsnNode tsin = (TableSwitchInsnNode) ain;
        list.add(new TableSwitchInsnNode(tsin.min, tsin.max, cloned.get(tsin.dflt), mapLabels(tsin.labels, cloned).toArray(new LabelNode[0])));
        break;
      case AbstractInsnNode.TYPE_INSN:
        TypeInsnNode tin = (TypeInsnNode) ain;
        list.add(new TypeInsnNode(tin.getOpcode(), tin.desc));
        break;
      case AbstractInsnNode.VAR_INSN:
        VarInsnNode vin = (VarInsnNode) ain;
        list.add(new VarInsnNode(vin.getOpcode(), vin.var));
        break;
      }
    }
    return list;
  }

  private static int[] toIntArray(List<Integer> list) {
    int[] ret = new int[list.size()];
    int i = 0;
    for (Integer e : list)
      ret[i++] = e.intValue();
    return ret;
  }

  private static ArrayList<LabelNode> mapLabels(List<LabelNode> labels, HashMap<LabelNode, LabelNode> cloned) {
    ArrayList<LabelNode> mapped = new ArrayList<>();
    for (LabelNode l : labels) {
      mapped.add(cloned.get(l));
    }
    return mapped;
  }

  private static HashMap<LabelNode, LabelNode> cloneLabels(InsnList il) {
    HashMap<LabelNode, LabelNode> hm = new HashMap<>();
    for (AbstractInsnNode ain : il.toArray()) {
      if (ain instanceof LabelNode) {
        hm.put((LabelNode) ain, new LabelNode());
      }
    }
    return hm;
  }
}
