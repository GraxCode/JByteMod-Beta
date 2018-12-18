package me.grax.jbytemod.ui.dialogue;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.text.NumberFormat;
import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.NumberFormatter;
import javax.swing.text.PlainDocument;

import org.jfree.chart.plot.ThermometerPlot;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;

import me.grax.jbytemod.JByteMod;
import me.grax.jbytemod.ui.JLDCEditor;
import me.grax.jbytemod.utils.gui.SwingUtils;

public class ClassDialogue {

  private Object object;
  private Class<? extends Object> clazz;
  private ArrayList<Field> fields;

  private String title;

  private static final List<String> noChilds = Arrays.asList(String.class.getName(), Integer.class.getName(), int.class.getName(),
      char.class.getName(), Character.class.getName(), boolean.class.getName(), Boolean.class.getName(), char[].class.getName(),
      Type.class.getName());

  public ClassDialogue(Object object) {
    this(object, "Edit " + object.getClass().getSimpleName());
  }

  public ClassDialogue(Object object, String title) {
    if (object instanceof AbstractCollection<?>) {
      object = ((AbstractCollection<?>) object).toArray(); //we already have an editing table for that
    }
    this.object = object;
    this.clazz = object.getClass();
    this.title = title;
    this.initializeFields();
  }

  private void initializeFields() {
    fields = new ArrayList<>();
    for (Field f : clazz.getDeclaredFields()) {
      if (!Modifier.isStatic(f.getModifiers()) && !ignore(f.getName())) {
        f.setAccessible(true);
        fields.add(f);
      }
    }
  }

  protected boolean ignore(String name) {
    return false;
  }

  public boolean open() {
    JPanel panel = initializePanel();
    JPanel rightInput = (JPanel) panel.getComponent(1);
    if (JOptionPane.showConfirmDialog(null, panel, title, JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
      for (Component c : rightInput.getComponents()) {
        WrappedPanel wp = (WrappedPanel) c;
        Field f = wp.getField();
        if (f != null) {
          if (isModifiedSpecial(f.getName(), f.getType())) {
            Object o = getSpecialValue(object, f.getName(), f.getType(), wp.getObject(), wp);
            try {
              f.set(object, o);
            } catch (IllegalArgumentException | IllegalAccessException e) {
              e.printStackTrace();
            }
            continue;
          }
          Component child = wp.getComponent(0);
          if (hasNoChilds(f.getType())) {
            try {
              f.set(object, getValue(f.getType(), child));
            } catch (IllegalArgumentException | IllegalAccessException e) {
              e.printStackTrace();
            }
          }
        } else {
          Object o = wp.getObject();
          getSpecialValue(object, o.getClass().getName(), o.getClass(), o, wp); //can also be used as void
        }
      }
      return true;
    }
    return false;
  }

  protected Object getSpecialValue(Object object, String name, Class<?> type, Object object3, WrappedPanel wp) {
    return null;
  }

  private Object getValue(Class<?> type, Component child) {
    switch (noChilds.indexOf(type.getName())) {
    case 0:
      JTextField jtf = (JTextField) child;
      return jtf.getText();
    case 1:
    case 2:
      JFormattedTextField numberField = (JFormattedTextField) child;
      return numberField.getValue();
    case 3:
    case 4:
      JCharField jcf = (JCharField) child;
      return jcf.getCharacter();
    case 5:
    case 6:
      return ((JCheckBox) child).isSelected();
    case 7:
      jtf = (JTextField) child;
      return jtf.getText().toCharArray();
    case 8:
      jtf = (JTextField) child;
      return Type.getType(jtf.getText());
    }
    if (Number.class.isAssignableFrom(type) || (type.isPrimitive() && !char.class.isAssignableFrom(type))) {
      return getNumberInputValue(type, child);
    }
    throw new RuntimeException("" + noChilds.indexOf(type.getName()));
  }

  private Object getNumberInputValue(Class<?> type, Component child) {
    Object num = parseNumber(type, String.valueOf(((JFormattedTextField) child).getValue()));
    if (num != null) {
      return num;
    }
    return ((JFormattedTextField) child).getValue();
  }

  public static Object parseNumber(Class<?> type, String number) {
    for (java.lang.reflect.Method m : type.getDeclaredMethods()) {
      if (m.getName().startsWith("parse") && m.getParameterTypes().length == 1 && m.getParameterTypes()[0].getName().equals(String.class.getName())) {
        try {
          return m.invoke(null, number);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }
    return null;
  }

  private JPanel initializePanel() {
    JPanel mainPanel = new JPanel();
    JPanel leftText = new JPanel();
    JPanel rightInput = new JPanel();

    mainPanel.setLayout(new BorderLayout());
    leftText.setLayout(new GridLayout(0, 1));
    rightInput.setLayout(new GridLayout(0, 1));
    addSpecialInputs(object, leftText, rightInput);
    for (Field f : fields) {
      //determine if field has special input method
      if (isModifiedSpecial(f.getName(), f.getType())) {
        try {
          //add special input method
          rightInput.add(wrap(f, getModifiedSpecial(f.get(object), f.getName(), f.getType())));
        } catch (IllegalArgumentException | IllegalAccessException e) {
          e.printStackTrace();
        }
      } else if (hasNoChilds(f.getType())) {
        try {
          rightInput.add(wrap(f, getComponent(f)));
        } catch (IllegalArgumentException | IllegalAccessException e) {
          e.printStackTrace();
        }
      } else if (f.getType().isArray() || isList(f)) {
        JButton edit = new JButton("Edit " + f.getType().getSimpleName());
        edit.addActionListener(e -> {
          try {
            ListEditorTable t = new ListEditorTable(object, f);
            if (t.open()) {
              f.set(object, f.getType().isArray() ? t.getList().toArray() : t.getList());
            }
          } catch (Exception ex) {
            ex.printStackTrace();
          }
        });
        rightInput.add(wrap(f, edit));
      } else if (Object.class.isAssignableFrom(f.getType())) {
        JPanel panel = new JPanel();
        try {
          Object value = f.get(object);
          panel.setLayout(new BorderLayout());
          JButton edit = new JButton(JByteMod.res.getResource("edit"));
          edit.setToolTipText("Can be null");
          edit.addActionListener(e -> {
            try {
              //should still be the same class type
              ClassDialogue dialogue = ClassDialogue.this.init(value == null ? f.getType().newInstance() : value);
              if (dialogue.open()) {
                f.set(object, dialogue.getObject());
              }
            } catch (Exception ex) {
              ex.printStackTrace();
            }
          });
          panel.add(edit, BorderLayout.CENTER);
          JCheckBox jcb = new JCheckBox("", f.get(object) != null);
          jcb.addItemListener(i -> {
            try {
              if (jcb.isSelected()) {
                if (f.get(object) == null) {
                  f.set(object, f.getType().newInstance());
                }
                edit.setEnabled(true);
              } else {
                f.set(object, null);
                edit.setEnabled(false);
              }
            } catch (Exception ex) {
              ex.printStackTrace();
            }
          });
          if (f.get(object) == null) {
            edit.setEnabled(false);
          } else {
            edit.setEnabled(true);
          }
          panel.add(jcb, BorderLayout.WEST);
        } catch (IllegalArgumentException | IllegalAccessException e1) {
          e1.printStackTrace();
        }
        rightInput.add(wrap(f, panel));
      } else {
        JButton edit = new JButton(JByteMod.res.getResource("edit"));
        try {
          Object value = f.get(object);
          edit.addActionListener(e -> {
            try {
              //should still be the same class type
              ClassDialogue dialogue = ClassDialogue.this.init(value);
              if (dialogue.open()) {
                f.set(object, dialogue.getObject());
              }
            } catch (Exception ex) {
              ex.printStackTrace();
            }
          });
          if (value == null) {
            edit.setEnabled(false);
          }
        } catch (IllegalArgumentException | IllegalAccessException e1) {
          e1.printStackTrace();
        }
        rightInput.add(wrap(f, edit));
      }
      leftText.add(new JLabel(getFieldName(f.getName(), f.getType()) + ": "));
    }

    mainPanel.add(leftText, BorderLayout.WEST);
    mainPanel.add(rightInput, BorderLayout.CENTER);

    return mainPanel;
  }

  private boolean isList(Field f) {
    if (AbstractCollection.class.isAssignableFrom(f.getType()))
      return true;
    try {
      Object o = f.get(object);
      return o instanceof AbstractCollection;
    } catch (Throwable t) {
    }
    return false;
  }

  protected ClassDialogue init(Object value) {
    return new ClassDialogue(value);
  }

  protected void addSpecialInputs(Object object, JPanel leftText, JPanel rightInput) {
  }

  protected Component getModifiedSpecial(Object object, String name, Class<?> type) {
    return null;
  }

  protected boolean isModifiedSpecial(String name, Class<?> type) {
    return false;
  }

  protected String getFieldName(String name, Class<?> type) {
    return formatText(name);
  }

  protected Component wrap(Field f, Component component) {
    WrappedPanel wp = new WrappedPanel(f);
    wp.add(component);
    return wp;
  }

  protected Component wrap(Object o, Component component) {
    WrappedPanel wp = new WrappedPanel(o);
    wp.add(component);
    return wp;
  }

  public Object getObject() {
    return object;
  }

  @SuppressWarnings("unused")
  private Component wrapRight(Field f, Component component, Component component2) {
    WrappedPanel wp = new WrappedPanel(f);
    wp.add(component, BorderLayout.CENTER);
    wp.add(component2, BorderLayout.EAST);
    return wp;
  }

  private Component getComponent(Field f) throws IllegalArgumentException, IllegalAccessException {
    return getComponent(f.getType(), f.get(object));
  }

  protected Component getComponent(Class<?> c, Object o) throws IllegalArgumentException, IllegalAccessException {
    switch (noChilds.indexOf(c.getName())) {
    case 0:
      return new JTextField(String.valueOf(o));
    case 1:
    case 2:
      JFormattedTextField numberField = createNumberField(Integer.class, Integer.MIN_VALUE, Integer.MAX_VALUE);
      numberField.setValue(o);
      return numberField;
    case 3:
    case 4:
      return new JCharField(o);
    case 5:
    case 6:
      return new JCheckBox("", (boolean) o);
    case 7:
      return new JTextField(new String((char[]) o));
    case 8:
      return new JTextField(new String(((Type) o).getInternalName()));
    }
    if (Number.class.isAssignableFrom(c) || (c.isPrimitive() && !char.class.isAssignableFrom(c))) {
      try {
        return getNumberInput(o);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    throw new RuntimeException();
  }

  private Component getNumberInput(Object o) throws Exception {
    Object maxValue = parseNumber(o.getClass(), o.getClass().getDeclaredField("MAX_VALUE").get(null).toString());
    Object minValue = parseNumber(o.getClass(), o.getClass().getDeclaredField("MIN_VALUE").get(null).toString());
    JFormattedTextField numberField = createNumberField(o.getClass(), minValue, maxValue);
    numberField.setValue(o);
    return numberField;
  }

  private String formatText(String string) {
    if (string.length() < 3) {
      //may be obfuscated, do not uppercase
      return string;
    }
    return string.substring(0, 1).toUpperCase() + string.substring(1);
  }

  private boolean hasNoChilds(Class<?> type) {
    return noChilds.contains(type.getName()) || Number.class.isAssignableFrom(type) || (type.isPrimitive());
  }

  @SuppressWarnings("rawtypes")
  public static JFormattedTextField createNumberField(Class<?> type, Object minValue, Object maxValue) {
    NumberFormat format = NumberFormat.getInstance();
    format.setGroupingUsed(false);
    format.setMaximumFractionDigits(10);
    NumberFormatter formatter = new NumberFormatter(format);
    formatter.setValueClass(type);
    formatter.setMinimum((Comparable) minValue);
    formatter.setMaximum((Comparable) maxValue);
    formatter.setAllowsInvalid(false);
    formatter.setCommitsOnValidEdit(true);
    formatter.setOverwriteMode(true);
    JFormattedTextField jftf = new JFormattedTextField(formatter);
    return jftf;
  }

  public static class WrappedPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    private Field f;

    private Object o;

    public WrappedPanel(Field f) {
      super();
      this.f = f;
      this.setLayout(new BorderLayout());
    }

    public WrappedPanel(Object o) {
      super();
      this.o = o;
      this.setLayout(new BorderLayout());
    }

    public Field getField() {
      return f;
    }

    public Object getObject() {
      return o;
    }
  }

  public class ListEditorTable extends JDialog {
    private static final long serialVersionUID = 1L;

    @SuppressWarnings("rawtypes")
    private List list;

    private Class<?> type;

    private JTable jtable;

    private Field f;

    private Object bsmHandle;

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ListEditorTable(Object parent, Field f) throws IllegalArgumentException, IllegalAccessException {
      Object item = f.get(parent);
      if (item == null) {
        this.list = new ArrayList<>();
      } else if (item.getClass().isArray()) {
        int size = Array.getLength(item);
        ArrayList list = new ArrayList<>();
        for (int i = 0; i < size; i++) {
          list.add(Array.get(item, i));
        }
        this.list = list;
      } else if (item instanceof List<?>) {
        this.list = (List<?>) item;
      } else {
        throw new RuntimeException();
      }
      if (f.getType().isArray()) {
        this.type = f.getType().getComponentType();
      } else {
        java.lang.reflect.Type type = f.getGenericType();
        if (type instanceof ParameterizedType) {
          ParameterizedType pType = (ParameterizedType) type;
          this.type = (Class<?>) pType.getActualTypeArguments()[0];
        } else {
          this.type = Object.class;
        }
      }
      this.f = f;
    }

    private JPanel initializePanel() {
      JPanel mainPanel = new JPanel();
      JPanel leftText = new JPanel();
      JPanel rightInput = new JPanel();

      int size = list.size();

      mainPanel.setLayout(new BorderLayout(15, 15));
      leftText.setLayout(new GridLayout(size, 1));
      rightInput.setLayout(new GridLayout(size, 1));
      mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
      DefaultTableModel lm = new DefaultTableModel();
      lm.addColumn("#");
      lm.addColumn("Item");
      lm.addColumn("toString");
      for (int i = 0; i < size; i++) {
        Object o = list.get(i);
        if (o == null) {
          lm.addRow(new Object[] { i, "null", null });
          continue;
        }
        lm.addRow(new Object[] { i, o.getClass().getSimpleName(), o });
      }
      jtable = new JTable() {
        @Override
        public boolean isCellEditable(int row, int column) {
          return false;
        };
      };
      jtable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
      jtable.getTableHeader().setReorderingAllowed(false);
      jtable.setModel(lm);
      JPanel actions = new JPanel();
      actions.setLayout(new GridLayout(1, 4));
      if (InvokeDynamicInsnNode.class.isAssignableFrom(f.getDeclaringClass()) && f.getName().equals("bsmArgs")) {
        //Special case for bootstrap arguments
        JButton add = new JButton(JByteMod.res.getResource("add"));
        add.addActionListener(a -> {
          Object edit = bsmArgsWindow();
          int row = jtable.getSelectedRow();
          if (edit != null) {
            if (row != -1) {
              lm.insertRow(row, new Object[] { -1, edit.getClass().getSimpleName(), edit });
              recalcIndex();
            } else {
              lm.addRow(new Object[] { lm.getRowCount(), edit.getClass().getSimpleName(), edit });
            }
          }
        });
        actions.add(add);
      } else {
        JButton add = new JButton(JByteMod.res.getResource("add"));
        add.addActionListener(a -> {
          try {
            int row = jtable.getSelectedRow();
            Object o = type.getConstructor().newInstance();
            Object edit = extraEditWindow(o, -1, jtable);
            if (edit != null) {
              if (row != -1) {
                lm.insertRow(row, new Object[] { -1, edit.getClass().getSimpleName(), edit });
                recalcIndex();
              } else {
                lm.addRow(new Object[] { lm.getRowCount(), edit.getClass().getSimpleName(), edit });
              }
            }
          } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Node not supported");
          }
        });
        if (!type.getName().equals(Object.class.getName())) {
          actions.add(add);
        }
      }
      JButton remove = new JButton(JByteMod.res.getResource("remove"));
      remove.addActionListener(a -> {
        int[] selectedRows = jtable.getSelectedRows();
        if (selectedRows.length > 0) {
          for (int j = selectedRows.length - 1; j >= 0; j--) {
            lm.removeRow(selectedRows[j]);
          }
          recalcIndex();
        }
      });
      actions.add(remove);
      JButton edit = new JButton(JByteMod.res.getResource(JByteMod.res.getResource("edit")));
      edit.addActionListener(a -> {
        int row = jtable.getSelectedRow();
        if (row == -1) {
          return;
        }
        Object o = lm.getValueAt(row, 2);
        if (o != null) {
          extraEditWindow(o, row, jtable);
        } else {
          JOptionPane.showMessageDialog(null, "null cannot be edited!");
        }
      });
      actions.add(edit);

      JScrollPane jscp = new JScrollPane(jtable) {
        @Override
        public Dimension getPreferredSize() {
          int maxWidth = 500;
          int maxHeight = 300;
          Dimension dim = super.getPreferredSize();
          if (dim.width > maxWidth)
            dim.width = maxWidth;
          if (dim.height > maxHeight)
            dim.height = maxHeight;
          return dim;
        }
      };
      mainPanel.add(jscp, BorderLayout.CENTER);
      mainPanel.add(actions, BorderLayout.PAGE_END);
      return mainPanel;
    }

    public void recalcIndex() {
      DefaultTableModel lm = (DefaultTableModel) jtable.getModel();
      for (int i = 0; i < lm.getRowCount(); i++) {
        lm.setValueAt(i, i, 0);
      }
    }

    @SuppressWarnings("unchecked")
    public boolean open() {
      JPanel panel = initializePanel();
      JScrollPane scrollPane = (JScrollPane) panel.getComponent(0);
      JTable table = (JTable) scrollPane.getViewport().getView();
      if (JOptionPane.showConfirmDialog(null, panel, "Edit Array", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
        list.clear();
        for (int row = 0; row < table.getRowCount(); row++) {
          Object o = table.getValueAt(row, 2);
          list.add(o);
        }
        return true;
      }
      return false;
    }

    public List<?> getList() {
      return list;
    }

    public Object extraEditWindow(Object o, int row, JTable jtable) {
      JPanel mainPanel = new JPanel();
      JPanel leftText = new JPanel();
      JPanel rightInput = new JPanel();

      mainPanel.setLayout(new BorderLayout(15, 15));
      leftText.setLayout(new GridLayout(1, 1));
      rightInput.setLayout(new GridLayout(1, 1));
      if (isModifiedSpecial(o.getClass().getName(), o.getClass())) {
        rightInput.add(wrap(o, getModifiedSpecial(o, o.getClass().getName(), o.getClass())));
      } else if (hasNoChilds(o.getClass())) {
        try {
          rightInput.add(wrap(o, ClassDialogue.this.getComponent(o.getClass(), o)));
        } catch (IllegalArgumentException | IllegalAccessException e) {
          e.printStackTrace();
        }
      } else {
        JButton edit = new JButton(JByteMod.res.getResource("edit"));
        edit.addActionListener(e -> {
          try {
            ClassDialogue dialogue = ClassDialogue.this.init(o);
            dialogue.open();
          } catch (Exception ex) {
            ex.printStackTrace();
          }
        });
        rightInput.add(wrap(o, edit));
      }
      mainPanel.add(leftText, BorderLayout.WEST);
      mainPanel.add(rightInput, BorderLayout.CENTER);
      leftText.add(new JLabel(formatText(o.getClass().getSimpleName() + ":")));
      Object newObject = null;
      if (JOptionPane.showConfirmDialog(null, mainPanel, "Edit List Item", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
        for (Component c : rightInput.getComponents()) {
          WrappedPanel wp = (WrappedPanel) c;
          if (o != null) {
            Component child = wp.getComponent(0);
            if (isModifiedSpecial(o.getClass().getName(), o.getClass())) {
              newObject = getSpecialValue(object, o.getClass().getName(), o.getClass(), o, wp);
            } else if (hasNoChilds(o.getClass())) {
              newObject = getValue(o.getClass(), child);
            }
          }
        }
        if (newObject == null) {
          newObject = o;
        }
        if (row != -1) {
          DefaultTableModel lm = (DefaultTableModel) jtable.getModel();
          lm.insertRow(row, new Object[] { row, newObject.getClass().getSimpleName(), newObject });
          lm.removeRow(row + 1);
        }
        return newObject;
      }
      return null;
    }

    private Object bsmArgsWindow() {
      bsmHandle = new Handle(1, "", "", "", false);
      JPanel mainPanel = new JPanel();
      JPanel leftText = new JPanel();
      JPanel rightInput = new JPanel();
      JButton handleButton = new JButton("Edit Handle");

      mainPanel.setLayout(new BorderLayout());
      leftText.setLayout(new GridLayout(0, 1));
      rightInput.setLayout(new GridLayout(0, 1));

      leftText.add(new JLabel("Type: "));
      JComboBox<String> ldctype = new JComboBox<String>(new String[] { "String", "float", "double", "int", "long", "Class", "Handle" });
      handleButton.addActionListener(e -> {
        try {
          InsnEditDialogue dialogue = new InsnEditDialogue(null, bsmHandle);
          if (dialogue.open()) {
            bsmHandle = (Handle) dialogue.getObject();
          }
        } catch (Exception ex) {
          ex.printStackTrace();
        }
      });
      rightInput.add(ldctype);
      leftText.add(new JLabel("Value: "));
      JTextField cst = new JTextField();
      rightInput.add(SwingUtils.withButton(cst, "...", e -> {
        JLDCEditor editor = new JLDCEditor(cst.getText());
        editor.setVisible(true);
        cst.setText(editor.getText());
      }));
      ldctype.addItemListener(i -> {
        if (ldctype.getSelectedItem().equals("Handle")) {
          cst.setEnabled(false);
          ((JPanel) rightInput.getComponent(1)).getComponent(1).setEnabled(false);
          handleButton.setEnabled(true);
        } else {
          cst.setEnabled(true);
          ((JPanel) rightInput.getComponent(1)).getComponent(1).setEnabled(true);
          handleButton.setEnabled(false);
        }
      });
      handleButton.setEnabled(false);
      mainPanel.add(leftText, BorderLayout.WEST);
      mainPanel.add(rightInput, BorderLayout.CENTER);
      mainPanel.add(handleButton, BorderLayout.SOUTH);

      if (JOptionPane.showConfirmDialog(null, mainPanel, "Add BSM Object", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
        try {
          switch (ldctype.getSelectedItem().toString()) {
          case "String":
            return cst.getText();
          case "float":
            return Float.parseFloat(cst.getText());
          case "double":
            return Double.parseDouble(cst.getText());
          case "long":
            return Long.parseLong(cst.getText());
          case "int":
            return Integer.parseInt(cst.getText());
          case "Class":
            return org.objectweb.asm.Type.getType(cst.getText());
          case "Handle":
            return bsmHandle;
          }
        } catch (Exception e) {
          e.printStackTrace();
          JOptionPane.showMessageDialog(null, "Exception: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
          return null;
        }
      }
      return null;
    }
  }

  public static class JCharField extends JTextField {
    private static final long serialVersionUID = 1L;
    private Object c;

    public JCharField(Object c) {
      this.c = c;
      this.setText(String.valueOf(c));

    }

    public char getCharacter() {
      if (this.getText().isEmpty()) {
        return String.valueOf(c).charAt(0);
      }
      return this.getText().charAt(0);
    }

    protected Document createDefaultModel() {
      return new LimitDocument();
    }

    private class LimitDocument extends PlainDocument {
      private static final long serialVersionUID = 1L;

      @Override
      public void insertString(int offset, String str, AttributeSet attr) throws BadLocationException {
        if (str == null)
          return;

        if ((getLength() + str.length()) <= 1) {
          super.insertString(offset, str, attr);
        }
      }

    }
  }

}
