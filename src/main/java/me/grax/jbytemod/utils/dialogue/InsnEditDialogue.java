package me.grax.jbytemod.utils.dialogue;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.IincInsnNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.LookupSwitchInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TableSwitchInsnNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import me.grax.jbytemod.JByteMod;
import me.lpk.util.OpUtils;

public class InsnEditDialogue extends ClassDialogue {

  private static final HashMap<String, String[]> opc = new LinkedHashMap<>();

  static {
    opc.put(InsnNode.class.getSimpleName(),
        new String[] { "nop", "aconst_null", "iconst_m1", "iconst_0", "iconst_1", "iconst_2", "iconst_3", "iconst_4", "iconst_5", "lconst_0",
            "lconst_1", "fconst_0", "fconst_1", "fconst_2", "dconst_0", "dconst_1", "iaload", "laload", "faload", "daload", "aaload", "baload",
            "caload", "saload", "iastore", "lastore", "fastore", "dastore", "aastore", "bastore", "castore", "sastore", "pop", "pop2", "dup",
            "dup_x1", "dup_x2", "dup2", "dup2_x1", "dup2_x2", "swap", "iadd", "ladd", "fadd", "dadd", "isub", "lsub", "fsub", "dsub", "imul", "lmul",
            "fmul", "dmul", "idiv", "ldiv", "fdiv", "ddiv", "irem", "lrem", "frem", "drem", "ineg", "lneg", "fneg", "dneg", "ishl", "lshl", "ishr",
            "lshr", "iushr", "lushr", "iand", "land", "ior", "lor", "ixor", "lxor", "i2l", "i2f", "i2d", "l2i", "l2f", "l2d", "f2i", "f2l", "f2d",
            "d2i", "d2l", "d2f", "i2b", "i2c", "i2s", "lcmp", "fcmpl", "fcmpg", "dcmpl", "dcmpg", "ireturn", "lreturn", "freturn", "dreturn",
            "areturn", "return", "arraylength", "athrow", "monitorenter", "monitorexit" });
    opc.put(MethodInsnNode.class.getSimpleName(), new String[] { "invokestatic", "invokevirtual", "invokespecial", "invokeinterface" });
    opc.put(FieldInsnNode.class.getSimpleName(), new String[] { "getstatic", "putstatic", "getfield", "putfield" });
    opc.put(VarInsnNode.class.getSimpleName(),
        new String[] { "iload", "lload", "fload", "dload", "aload", "istore", "lstore", "fstore", "dstore", "astore", "ret" });
    opc.put(TypeInsnNode.class.getSimpleName(), new String[] { "new", "anewarray", "checkcast", "instanceof" });
    opc.put(LdcInsnNode.class.getSimpleName(), new String[] { "ldc" });
    opc.put(IincInsnNode.class.getSimpleName(), new String[] { "iinc" });
    opc.put(JumpInsnNode.class.getSimpleName(), new String[] { "ifeq", "ifne", "iflt", "ifge", "ifgt", "ifle", "if_icmpeq", "if_icmpne", "if_icmplt",
        "if_icmpge", "if_icmpgt", "if_icmple", "if_acmpeq", "if_acmpne", "goto", "jsr", "ifnull", "ifnonnull" });
    opc.put(IntInsnNode.class.getSimpleName(), new String[] { "bipush", "sipush", "newarray" });
    opc.put(InvokeDynamicInsnNode.class.getSimpleName(), new String[] { "invokedynamic" });
    opc.put(TableSwitchInsnNode.class.getSimpleName(), new String[] { "tableswitch" });
    opc.put(LookupSwitchInsnNode.class.getSimpleName(), new String[] { "lookupswitch" });
    opc.put(LabelNode.class.getSimpleName(), null);
    opc.put(LineNumberNode.class.getSimpleName(), null);
    opc.put(FrameNode.class.getSimpleName(), null);
  }

  private MethodNode mn;

  public InsnEditDialogue(MethodNode mn, Object object) {
    super(object);
    this.mn = mn;
  }

  public static void createInsertInsnDialog(MethodNode mn, AbstractInsnNode ain, boolean after) {
    final JPanel panel = new JPanel(new BorderLayout(5, 5));
    final JPanel input = new JPanel(new GridLayout(0, 1));
    final JPanel labels = new JPanel(new GridLayout(0, 1));
    panel.add(labels, "West");
    panel.add(input, "Center");
    labels.add(new JLabel("Type"));
    JComboBox<String> clazz = new JComboBox<String>(new ArrayList<String>(opc.keySet()).toArray(new String[0]));
    input.add(clazz);
    if (JOptionPane.showConfirmDialog(JByteMod.instance, panel, "Insert " + (after ? "after" : "before"), 2) == JOptionPane.OK_OPTION) {
      try {
        //only works because i created constructors for those nodes
        Class<?> node = Class.forName("org.objectweb.asm.tree" + "." + clazz.getSelectedItem().toString());
        AbstractInsnNode newnode = (AbstractInsnNode) node.getConstructor().newInstance();
        //TODO initialize some values
        //we need no edit for LabelNode
        if (!hasSettings(newnode) || new InsnEditDialogue(mn, newnode).open()) {
          if (ain != null) {
            if (after) {
              mn.instructions.insert(ain, newnode);
            } else {
              mn.instructions.insertBefore(ain, newnode);
            }
          } else {
            mn.instructions.add(newnode);
          }
          JByteMod.instance.getCodeList().loadInstructions(mn);
        }
      } catch (Exception e) {
        e.printStackTrace();
      }

    }
  }

  @Override
  protected boolean ignore(String name) {
    return name.equals("itf");
  }

  @Override
  protected void addSpecial(Object obj, JPanel leftText, JPanel rightInput) {
    if (obj instanceof AbstractInsnNode) {
      AbstractInsnNode ain = (AbstractInsnNode) obj;
      leftText.add(new JLabel("Opcode: "));
      String[] arr = opc.get(ain.getClass().getSimpleName());
      if (arr == null) {
        return;
      }
      JComboBox<String> opcode = new JComboBox<String>(arr);
      opcode.setSelectedItem(OpUtils.getOpcodeText(ain.getOpcode()).toLowerCase());
      rightInput.add(wrap("opc", opcode));
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  protected Object getSpecialValue(Object object, Class<?> type, Object o, WrappedPanel wp) {
    if (o != null && o.equals("opc")) {
      JComboBox<String> opcode = (JComboBox<String>) wp.getComponent(0);
      AbstractInsnNode ain = (AbstractInsnNode) object;
      ain.setOpcode(OpUtils.getOpcodeIndex(String.valueOf(opcode.getSelectedItem()).toUpperCase()));
      return null;
    } else if(type.getName().equals(LabelNode.class.getName())) {
      JComboBox<LabelNode> label = (JComboBox<LabelNode>) wp.getComponent(0);
      return label.getSelectedItem();
    }
    return null;
  }

  @Override
  protected boolean isSpecial(String name, Class<?> type) {
    return type.getName().equals(LabelNode.class.getName());
  }

  @Override
  protected Component getSpecial(Object o, String name, Class<?> type) {
    if (type.getName().equals(LabelNode.class.getName())) {
      ArrayList<LabelNode> ln = new ArrayList<>();
      for (AbstractInsnNode nod : mn.instructions.toArray()) {
        if (nod instanceof LabelNode) {
          ln.add((LabelNode) nod);
        }
      }
      JComboBox<LabelNode> jcb = new JComboBox<>(ln.toArray(new LabelNode[0]));
      jcb.setSelectedItem(o);
      return jcb;
    }
    return null;
  }

  public static boolean canEdit(AbstractInsnNode ain) {
    String sn = ain.getClass().getSimpleName();
    return opc.keySet().contains(sn) && hasSettings(ain);
  }

  private static boolean hasSettings(AbstractInsnNode ain) {
    return !(ain instanceof LabelNode);
  }
  
  @Override
  protected ClassDialogue init(Object value) {
    return new InsnEditDialogue(mn, value);
  }
}
