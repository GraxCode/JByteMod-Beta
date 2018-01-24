package me.grax.jbytemod.utils.dialogue;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.text.NumberFormat;
import java.util.AbstractCollection;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.NumberFormatter;
import javax.swing.text.PlainDocument;

public class ClassDialogue {

  private Object object;
  private Class<? extends Object> clazz;
  private ArrayList<Field> fields;

  private String title;

  private static final List<String> noChilds = Arrays.asList(String.class.getName(), Integer.class.getName(), int.class.getName(),
      char.class.getName(), Character.class.getName(), boolean.class.getName(), Boolean.class.getName(), char[].class.getName());

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
    default:
      throw new RuntimeException("" + noChilds.indexOf(type));
    }
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
      if (isModifiedSpecial(f.getName(), f.getType())) {
        try {
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
      } else {
        JButton edit = new JButton("Edit");
        Object value;
        try {
          value = f.get(object);
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
      leftText.add(new JLabel(formatText(f.getName()) + ": "));
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

  protected Component wrap(Field f, Component... component) {
    WrappedPanel wp = new WrappedPanel(f);
    for (Component c : component)
      wp.add(c);
    return wp;
  }

  protected Component wrap(Object o, Component... component) {
    WrappedPanel wp = new WrappedPanel(o);
    for (Component c : component)
      wp.add(c);
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
      JFormattedTextField numberField = createNumberField();
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
    default:
      throw new RuntimeException();
    }
  }

  private String formatText(String string) {
    if (string.length() < 3) {
      //may be obfuscated, do not uppercase
      return string;
    }
    return string.substring(0, 1).toUpperCase() + string.substring(1);
  }

  private boolean hasNoChilds(Class<?> type) {
    return noChilds.contains(type.getName());
  }

  private static NumberFormatter formatter = null;

  public static JFormattedTextField createNumberField() {
    if (formatter == null) {
      NumberFormat format = NumberFormat.getInstance();
      format.setGroupingUsed(false);
      formatter = new NumberFormatter(format);
      formatter.setValueClass(Integer.class);
      formatter.setMinimum(0);
      formatter.setMaximum(Integer.MAX_VALUE);
      formatter.setAllowsInvalid(false);
      formatter.setCommitsOnValidEdit(true);
      formatter.setOverwriteMode(true);
    }
    return new JFormattedTextField(formatter);
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
    }

    private JScrollPane initializePanel() {
      JPanel mainPanel = new JPanel();
      JPanel leftText = new JPanel();
      JPanel rightInput = new JPanel();

      int size = list.size();

      mainPanel.setLayout(new BorderLayout(15, 15));
      leftText.setLayout(new GridLayout(size, 1));
      rightInput.setLayout(new GridLayout(size, 1));
      mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
      for (int i = 0; i < size; i++) {
        Object o = list.get(i);
        if (isModifiedSpecial(o.getClass().getName(), o.getClass())) {
          rightInput.add(wrap(o, getModifiedSpecial(o, o.getClass().getName(), o.getClass())));
        } else if (hasNoChilds(o.getClass())) {
          try {
            rightInput.add(wrap(o, ClassDialogue.this.getComponent(o.getClass(), o)));
          } catch (IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
          }
        } else {
          JButton edit = new JButton("Edit");
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
        leftText.add(new JLabel(formatText("#" + i + " " + o.getClass().getSimpleName() + ":")));
      }

      mainPanel.add(leftText, BorderLayout.WEST);
      mainPanel.add(rightInput, BorderLayout.EAST);
      JPanel actions = new JPanel();
      actions.setLayout(new GridLayout(1, 3));
//      actions.add(new JButton());
//      actions.add(new JButton());
//      actions.add(new JButton());
//      mainPanel.add(actions, BorderLayout.PAGE_END);
      
      JScrollPane jscp = new JScrollPane(mainPanel) {
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
      return jscp;
    }

    @SuppressWarnings("unchecked")
    public boolean open() {
      JScrollPane jscp = initializePanel();
      JPanel panel = (JPanel) jscp.getViewport().getView();
      JPanel rightInput = (JPanel) panel.getComponent(1);
      if (JOptionPane.showConfirmDialog(null, jscp, "Edit Array", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
        int i = 0;
        for (Component c : rightInput.getComponents()) {
          WrappedPanel wp = (WrappedPanel) c;
          Object o = wp.getObject();
          if (o != null) {
            Component child = wp.getComponent(0);
            if (isModifiedSpecial(o.getClass().getName(), o.getClass())) {
              list.set(i, getSpecialValue(object, o.getClass().getName(), o.getClass(), o, wp));
            } else if (hasNoChilds(o.getClass())) {
              list.set(i, getValue(o.getClass(), child));
            } else {
              list.set(i, o);
            }
          }
          i++;
        }
        return true;
      }
      return false;
    }

    public List<?> getList() {
      return list;
    }

  }

  public class JCharField extends JTextField {
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
