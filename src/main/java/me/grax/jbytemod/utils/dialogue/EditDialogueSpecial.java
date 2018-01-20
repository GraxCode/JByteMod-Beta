package me.grax.jbytemod.utils.dialogue;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import org.objectweb.asm.Handle;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodNode;

import me.grax.jbytemod.JByteMod;
import me.grax.jbytemod.utils.ErrorDisplay;

public class EditDialogueSpecial {
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

  public static Object[] createEditArrayDialog(Object[] arr) {
    try {
      JPanel editor = new JPanel();
      editor.setLayout(new BorderLayout());
      JTable jtable = new JTable() {
        @Override
        public boolean isCellEditable(int row, int column) {
          return column > 0;
        };
      };
      jtable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
      jtable.getTableHeader().setReorderingAllowed(false);
      DefaultTableModel lm = new DefaultTableModel();
      lm.addColumn("#");
      lm.addColumn("Item");
      int i = 0;
      for (Object item : arr) {
        System.out.println("clas " + item.getClass().getName());
        lm.addRow(new Object[] { String.valueOf(i), item });
        i++;
      }
      jtable.setModel(lm);
      editor.add(new JScrollPane(jtable), BorderLayout.CENTER);
      JPanel actions = new JPanel();
      actions.setLayout(new GridLayout(1, 4));
      JButton add = new JButton(JByteMod.res.getResource("add"));
      add.addActionListener(a -> {
        int c = lm.getRowCount();
        lm.addRow(new Object[] { String.valueOf(c), "" });
        jtable.setRowSelectionInterval(c, c);
      });
      actions.add(add);
      JButton remove = new JButton(JByteMod.res.getResource("remove"));
      remove.addActionListener(a -> {
        int[] selectedRows = jtable.getSelectedRows();
        if (selectedRows.length > 0) {
          for (int j = selectedRows.length - 1; j >= 0; j--) {
            lm.removeRow(selectedRows[j]);
          }
        }
      });
      actions.add(remove);
      JButton edit = new JButton(JByteMod.res.getResource("edit"));
      edit.addActionListener(a -> {
        jtable.editCellAt(jtable.getSelectedRow(), 1);
      });
      actions.add(edit);

      editor.add(actions, BorderLayout.PAGE_END);

      if (JOptionPane.showConfirmDialog(JByteMod.instance, editor, "Edit Array", 2) == JOptionPane.OK_OPTION) {
        System.out.println("Updating List!");
        TableModel model = jtable.getModel();
        ArrayList<String> list = new ArrayList<>();
        for (int j = 0; j < model.getRowCount(); j++) {
          list.add(String.valueOf(model.getValueAt(j, 1)));
        }
        return list.toArray();
      }
    } catch (Throwable e1) {
      new ErrorDisplay(e1);
    }
    return arr;
  }
}