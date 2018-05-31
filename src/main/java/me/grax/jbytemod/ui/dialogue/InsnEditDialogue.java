package me.grax.jbytemod.ui.dialogue;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.objectweb.asm.Handle;
import org.objectweb.asm.Type;
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
import org.objectweb.asm.tree.MultiANewArrayInsnNode;
import org.objectweb.asm.tree.TableSwitchInsnNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import me.grax.jbytemod.JByteMod;
import me.grax.jbytemod.ui.JAccessHelper;
import me.grax.jbytemod.ui.JAccessSelectorPanel;
import me.grax.jbytemod.ui.JFrameList;
import me.grax.jbytemod.ui.JLDCEditor;
import me.grax.jbytemod.utils.InstrUtils;
import me.grax.jbytemod.utils.gui.SwingUtils;
import me.lpk.util.OpUtils;

public class InsnEditDialogue extends ClassDialogue {

  private static final HashMap<String, String[]> opc = new LinkedHashMap<>();
  private static final String[] handles;
  private static final String[] frames;
  private static final List<String> canBeNull;
  private static final HashMap<String, String> textFieldToolTips;
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
    opc.put(MultiANewArrayInsnNode.class.getSimpleName(), new String[] { "multianewarray" });
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
    handles = new String[] { "h_getfield", "h_getstatic", "h_putfield", "h_putstatic", "h_invokevirtual", "h_invokestatic", "h_invokespecial",
        "h_newinvokespecial", "h_invokeinterface" };
    frames = new String[] { "f_new", "f_full", "f_append", "f_chop", "f_same", "f_same1" };
    canBeNull = Arrays.asList("signature", "sourceFile", "sourceDebug", "outerClass", "outerMethod", "outerMethodDesc");
    textFieldToolTips = new HashMap<>();
    textFieldToolTips.put("desc", "e.g. ()V");
  }

  private MethodNode mn;
  private Handle handle;

  public InsnEditDialogue(MethodNode mn, Object object) {
    super(object);
    this.mn = mn;
  }

  @Override
  public boolean open() {
    Object object = getObject();
    if (object instanceof LdcInsnNode) {
      //special case for LdcInsnNode
      LdcInsnNode ldc = (LdcInsnNode) object;
      JPanel mainPanel = new JPanel();
      JPanel leftText = new JPanel();
      JPanel rightInput = new JPanel();
      JButton handleButton = new JButton("Edit Handle");
      handle = new Handle(1, "", "", "", false);

      mainPanel.setLayout(new BorderLayout());
      leftText.setLayout(new GridLayout(0, 1));
      rightInput.setLayout(new GridLayout(0, 1));

      leftText.add(new JLabel("Ldc Type: "));
      JComboBox<String> ldctype = new JComboBox<String>(new String[] { "String", "float", "double", "int", "long", "Class", "Handle" });
      if (ldc.cst instanceof String) {
        ldctype.setSelectedItem("String");
      } else if (ldc.cst instanceof Float) {
        ldctype.setSelectedItem("float");
      } else if (ldc.cst instanceof Double) {
        ldctype.setSelectedItem("double");
      } else if (ldc.cst instanceof Long) {
        ldctype.setSelectedItem("long");
      } else if (ldc.cst instanceof Integer) {
        ldctype.setSelectedItem("int");
      } else if (ldc.cst instanceof Type) {
        ldctype.setSelectedItem("Class");
      } else if (ldc.cst instanceof Handle) {
        ldctype.setSelectedItem("Handle");
        handle = (Handle) ldc.cst;
      }
      handleButton.addActionListener(e -> {
        try {
          InsnEditDialogue dialogue = new InsnEditDialogue(mn, handle);
          if (dialogue.open()) {
            handle = (Handle) dialogue.getObject();
          }
        } catch (Exception ex) {
          ex.printStackTrace();
        }
      });
      rightInput.add(ldctype);
      leftText.add(new JLabel("Ldc Value: "));
      JTextField cst = new JTextField();
      if (ldc.cst instanceof Type) {
        cst.setText(((Type) ldc.cst).getDescriptor());
      } else if (!(ldc.cst instanceof Handle)) {
        cst.setText(ldc.cst.toString());
      }
      if (ldc.cst instanceof String) {
        rightInput.add(SwingUtils.withButton(cst, "...", e -> {
          JLDCEditor editor = new JLDCEditor(cst.getText());
          editor.setVisible(true);
          cst.setText(editor.getText());
        }));
      } else {
        rightInput.add(cst);
      }
      ldctype.addItemListener(i -> {
        if (ldctype.getSelectedItem().equals("Handle")) {
          cst.setEnabled(false);
          if (rightInput.getComponent(1) instanceof JPanel)
            ((JPanel) rightInput.getComponent(1)).getComponent(1).setEnabled(false);
          handleButton.setEnabled(true);
        } else {
          cst.setEnabled(true);
          if (rightInput.getComponent(1) instanceof JPanel)
            ((JPanel) rightInput.getComponent(1)).getComponent(1).setEnabled(true);
          handleButton.setEnabled(false);
        }
      });
      if (!ldctype.getSelectedItem().equals("Handle")) {
        handleButton.setEnabled(false);
      } else {
        cst.setEnabled(false);
        if (rightInput.getComponent(1) instanceof JPanel)
          ((JPanel) rightInput.getComponent(1)).getComponent(1).setEnabled(false);
      }
      mainPanel.add(leftText, BorderLayout.WEST);
      mainPanel.add(rightInput, BorderLayout.CENTER);
      mainPanel.add(handleButton, BorderLayout.SOUTH);

      if (JOptionPane.showConfirmDialog(null, mainPanel, "Edit LdcInsnNode", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
        try {
          switch (ldctype.getSelectedItem().toString()) {
          case "String":
            ldc.cst = cst.getText();
            break;
          case "float":
            ldc.cst = Float.parseFloat(cst.getText());
            break;
          case "double":
            ldc.cst = Double.parseDouble(cst.getText());
            break;
          case "long":
            ldc.cst = Long.parseLong(cst.getText());
            break;
          case "int":
            ldc.cst = Integer.parseInt(cst.getText());
            break;
          case "Class":
            ldc.cst = Type.getType(cst.getText());
            break;
          case "Handle":
            ldc.cst = handle;
            break;
          }
        } catch (Exception e) {
          e.printStackTrace();
          JOptionPane.showMessageDialog(null, "Exception: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
          return false;
        }
        return true;
      }
      return false;
    }

    return super.open();
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
    return name.equals("itf") || name.toLowerCase().contains("annotation") || name.equals("visited") || name.equals("tryCatchBlocks")
        || name.equals("localVariables") || name.equals("instructions") || name.equals("preLoad") || name.equals("attrs") || name.equals("extraBytes")
        || name.equals("methods") || name.equals("fields") || name.equals("local") || name.equals("stack") || name.equals("hash")
        || name.equals("parameters") || name.equals("exceptions") || name.equals("innerClasses") || name.equals("module");
  }

  @Override
  protected void addSpecialInputs(Object obj, JPanel leftText, JPanel rightInput) {
    if (obj instanceof AbstractInsnNode) {
      AbstractInsnNode ain = (AbstractInsnNode) obj;
      String[] arr = opc.get(ain.getClass().getSimpleName());
      if (arr != null) {
        leftText.add(new JLabel("Opcode: "));
        JComboBox<String> opcode = new JComboBox<String>(arr);
        opcode.setSelectedItem(OpUtils.getOpcodeText(ain.getOpcode()).toLowerCase());
        rightInput.add(wrap("opc", opcode));
      }
    }
    if (obj instanceof FrameNode) {
      FrameNode fn = (FrameNode) obj;
      leftText.add(new JLabel("Local / Stack: "));
      JButton edit = new JButton(JByteMod.res.getResource("edit"));
      edit.addActionListener(e -> {
        new JFrameList(fn.local, fn.stack).open();
      });
      rightInput.add(wrap("editframe", edit));
    }
  }

  /**
   * Always gets called even if the field isn't even special
   */
  @SuppressWarnings("unchecked")
  @Override
  protected Object getSpecialValue(Object object, String name, Class<?> type, Object o, WrappedPanel wp) {
    if (o != null && o.equals("opc")) {
      JComboBox<String> opcode = (JComboBox<String>) wp.getComponent(0);
      AbstractInsnNode ain = (AbstractInsnNode) object;
      ain.setOpcode(OpUtils.getOpcodeIndex(String.valueOf(opcode.getSelectedItem()).toUpperCase()));
      return null;
    } else if (type.getName().equals(LabelNode.class.getName())) {
      JComboBox<LabelNode> label = (JComboBox<LabelNode>) wp.getComponent(0);
      return label.getSelectedItem();
    } else if (name.equals("tag") && type.getName().equals(int.class.getName())) {
      JComboBox<String> label = (JComboBox<String>) wp.getComponent(0);
      return label.getSelectedIndex() + 1;
    } else if (name.equals("type") && type.getName().equals(int.class.getName())) {
      JComboBox<String> label = (JComboBox<String>) wp.getComponent(0);
      return label.getSelectedIndex() - 1;
    } else if (canBeNull.contains(name)) {
      JPanel panel = (JPanel) wp.getComponent(0);
      JTextField jtf = (JTextField) panel.getComponent(0);
      JCheckBox jcb = (JCheckBox) panel.getComponent(1);
      if (!jcb.isSelected()) {
        return null;
      }
      return jtf.getText();
    } else if ("access".equals(name)) {
      JAccessSelectorPanel panel = (JAccessSelectorPanel) wp.getComponent(0);
      return (int) panel.getAccess();
    } else if (textFieldToolTips.containsKey(name)) {
      JTextField jtf = (JTextField) wp.getComponent(0);
      return jtf.getText();
    }
    return null;
  }

  @Override
  protected boolean isModifiedSpecial(String name, Class<?> type) {
    return type.getName().equals(LabelNode.class.getName()) || (name.equals("tag") && type.getName().equals(int.class.getName())) //invokedynamic tag
        || (name.equals("type") && type.getName().equals(int.class.getName())) //frame type
        || (canBeNull.contains(name)) || (name.equals("access")) || (textFieldToolTips.containsKey(name));
  }

  /**
   * Only gets called if name is modified special
   */
  @Override
  protected Component getModifiedSpecial(Object o, String name, Class<?> type) {
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
    } else if (name.equals("tag")) {
      JComboBox<String> jcb = new JComboBox<>(handles);
      jcb.setSelectedIndex(((int) o) - 1);
      return jcb;
    } else if (name.equals("type")) {
      JComboBox<String> jcb = new JComboBox<>(frames);
      jcb.setSelectedIndex(((int) o) + 1);
      return jcb;
    } else if (canBeNull.contains(name)) {
      JPanel panel = new JPanel();
      panel.setLayout(new BorderLayout());
      JTextField jtf = new JTextField(String.valueOf(o));
      jtf.setToolTipText("Can be null");
      panel.add(jtf, BorderLayout.CENTER);
      JCheckBox jcb = new JCheckBox("", o != null);
      jcb.addItemListener(i -> {
        if (jcb.isSelected()) {
          jtf.setEnabled(true);
        } else {
          jtf.setText("");
          jtf.setEnabled(false);
        }
      });
      if (o == null) {
        jtf.setEnabled(false);
        jtf.setText("");
      } else {
        jtf.setEnabled(true);
      }
      panel.add(jcb, BorderLayout.WEST);
      return panel;
    } else if ("access".equals(name)) {
      int accezz = Integer.parseInt(String.valueOf(o));
      return new JAccessSelectorPanel(accezz);
    } else if (textFieldToolTips.containsKey(name)) {
      JTextField jtf = new JTextField((String) o);
      jtf.setToolTipText(textFieldToolTips.get(name));
      if (name.equals("desc")) {
        jtf.addMouseListener(new MouseAdapter() {
          @Override
          public void mouseEntered(MouseEvent me) {
            try {
              String val = jtf.getText();
              if (val.startsWith("(")) {
                jtf.setToolTipText(InstrUtils.getDisplayType(val.split("\\)")[1], false) + " (" + InstrUtils.getDisplayArgsEasy(val) + ")");
              } else if (!val.isEmpty()) {
                jtf.setToolTipText(InstrUtils.getDisplayType(val, false));
              } else {
                jtf.setToolTipText(textFieldToolTips.get(name));
              }
            } catch (Throwable t) {
              t.printStackTrace();
              jtf.setToolTipText(textFieldToolTips.get(name));
            }
          }
        });
      }
      return jtf;
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
