package me.grax.jbytemod.ui;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;

public class JFrameList extends JDialog {

  private List<Object> locals;
  private List<Object> stack;

  public JFrameList(List<Object> locals, List<Object> stack) {
    this.locals = locals;
    this.stack = stack;
  }

  public boolean open() {
    JPanel panel = new JPanel();
    panel.setLayout(new BorderLayout(5, 5));
    JList<Object> leftList = createList(locals);
    JList<Object> rightList = createList(stack);

    JPanel left = new JPanel();
    JPanel right = new JPanel();
    left.setLayout(new BorderLayout(5, 5));
    right.setLayout(new BorderLayout(5, 5));

    left.add(new JLabel("Locals", SwingConstants.CENTER), BorderLayout.NORTH);
    right.add(new JLabel("Stack", SwingConstants.CENTER), BorderLayout.NORTH);

    left.add(new JScrollPane(leftList), BorderLayout.CENTER);
    right.add(new JScrollPane(rightList), BorderLayout.CENTER);

    left.add(createEditBar(leftList), BorderLayout.PAGE_END);
    right.add(createEditBar(rightList), BorderLayout.PAGE_END);

    panel.add(left, BorderLayout.WEST);
    panel.add(new JSeparator(JSeparator.VERTICAL), BorderLayout.CENTER);
    panel.add(right, BorderLayout.EAST);

    if (JOptionPane.showConfirmDialog(null, panel, "Edit Frame", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
      DefaultListModel<Object> lm = (DefaultListModel<Object>) leftList.getModel();
      locals.clear();
      for (int i = 0; i < lm.getSize(); i++) {
        Object o = lm.getElementAt(i);
        if (o instanceof IntType) {
          IntType p = (IntType) o;
          locals.add(p.val);
        } else {
          locals.add(o);
        }
      }
      lm = (DefaultListModel<Object>) rightList.getModel();
      stack.clear();
      for (int i = 0; i < lm.getSize(); i++) {
        Object o = lm.getElementAt(i);
        if (o instanceof IntType) {
          IntType p = (IntType) o;
          stack.add(p.val);
        } else {
          stack.add(o);
        }
      }
      return true;
    }
    return false;
  }

  private JPanel createEditBar(JList<Object> list) {
    DefaultListModel<Object> lm = (DefaultListModel<Object>) list.getModel();
    JPanel editBar = new JPanel();
    editBar.setLayout(new GridLayout(1, 4));
    JButton addObject = new JButton("Add Object");
    addObject.addActionListener(e -> {
      String s = JOptionPane.showInputDialog(null);
      if (s != null && !s.isEmpty()) {
        lm.addElement(s);
      }
    });
    JButton addPrim = new JButton("Add Type");
    addPrim.addActionListener(e -> {
      JComboBox<String> combobox = new JComboBox<>(new String[] { "top", "int", "float", "double", "long", "null", "uninitialized_this" });
      combobox.setSelectedIndex(0);
      if (JOptionPane.showConfirmDialog(null, combobox, "Add Type", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
        lm.addElement(new IntType(combobox.getSelectedIndex()));
      }
    });

    JButton remove = new JButton("Remove");
    remove.addActionListener(e -> {
      for (int index : list.getSelectedIndices()) {
        lm.remove(index);
      }
    });
    editBar.add(addObject);
    editBar.add(addPrim);
    editBar.add(remove);
    return editBar;
  }

  private JList<Object> createList(List<Object> objs) {
    JList<Object> list = new JList<>();
    list.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
    DefaultListModel<Object> lm = new DefaultListModel<>();
    for (Object o : objs) {
      if (o instanceof Integer) {
        lm.addElement(new IntType((int) o));
      } else {
        lm.addElement(o);
      }
    }
    list.setModel(lm);
    return list;
  }

  public class IntType {
    private int val;

    public IntType(int val) {
      this.val = val;
    }

    @Override
    public String toString() {
      switch (val) {
      case 0:
        return "<html><b>top";
      case 1:
        return "<html><b>int";
      case 2:
        return "<html><b>float";
      case 3:
        return "<html><b>double";
      case 4:
        return "<html><b>long";
      case 5:
        return "<html><b>null";
      case 6:
        return "<html><b>uninitialized_this";
      default:
        return "<html><i>" + String.valueOf(val);
      }
    }
  }
}
