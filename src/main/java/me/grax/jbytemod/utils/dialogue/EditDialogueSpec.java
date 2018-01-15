package me.grax.jbytemod.utils.dialogue;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodNode;

import me.grax.jbytemod.JByteMod;

public class EditDialogueSpec {
  public static boolean createInsertDialogue(MethodNode mn, Object obj) throws Exception {
    final JPanel panel = new JPanel(new BorderLayout(5, 5));
    final JPanel input = new JPanel(new GridLayout(0, 1));
    final JPanel labels = new JPanel(new GridLayout(0, 1));
    panel.add(labels, "West");
    panel.add(input, "Center");
    LinkedHashMap<String, String> fieldNames = new LinkedHashMap<>();
    for (Field f : obj.getClass().getDeclaredFields()) {
      f.setAccessible(true);
      if (f.getGenericType().getTypeName().equals("java.lang.String")) {
        fieldNames.put(f.getName(), "String");
        labels.add(new JLabel(EditDialogue.toUp(f.getName()) + ": "));
        final JTextField value = new JTextField((String) f.get(obj));
        input.add(value);
      } else if (f.getGenericType().getTypeName().equals("int")) {
        fieldNames.put(f.getName(), "int");
        labels.add(new JLabel(EditDialogue.toUp(f.getName()) + ": "));
        JFormattedTextField field = EditDialogue.createNumberField();
        field.setValue(f.get(obj));
        input.add(field);
      } else if (f.getGenericType().getTypeName().equals("org.objectweb.asm.tree.LabelNode")) {
        ArrayList<LabelNode> ln = new ArrayList<>();
        for (AbstractInsnNode nod : mn.instructions.toArray()) {
          if (nod instanceof LabelNode) {
            ln.add((LabelNode) nod);
          }
        }
        fieldNames.put(f.getName(), "label");
        labels.add(new JLabel(EditDialogue.toUp(f.getName()) + ": "));
        JComboBox<LabelNode> jcb = new JComboBox<>(ln.toArray(new LabelNode[0]));
        jcb.setSelectedItem(f.get(obj));
        input.add(jcb);
      }
    }

    if (JOptionPane.showConfirmDialog(JByteMod.instance, panel, "Insert " + obj.getClass().getSimpleName(), 2) == JOptionPane.OK_OPTION) {
      int i = 0;
      for (String fn : fieldNames.keySet()) {
        String type = fieldNames.get(fn);
        if (type.equals("String")) {
          JTextField jtf = (JTextField) input.getComponent(i);
          String text = jtf.getText();
          Field f = obj.getClass().getDeclaredField(fn);
          f.set(obj, text);
          i++;
        } else if (type.equals("int")) {
          JFormattedTextField jtf = (JFormattedTextField) input.getComponent(i);
          int val = (int) jtf.getValue();
          Field f = obj.getClass().getDeclaredField(fn);
          f.set(obj, val);
          i++;
        } else if (type.equals("label")) {
          @SuppressWarnings("unchecked")
          JComboBox<String> jcb = (JComboBox<String>) input.getComponent(i);
          Field f = obj.getClass().getDeclaredField(fn);
          f.set(obj, jcb.getSelectedItem());
          i++;
        }
      }
      return true;
    }
    return false;
  }
}