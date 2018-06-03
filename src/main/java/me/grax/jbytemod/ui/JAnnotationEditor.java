package me.grax.jbytemod.ui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.reflect.Field;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import org.objectweb.asm.tree.AnnotationNode;

import me.grax.jbytemod.JByteMod;
import me.grax.jbytemod.ui.dialogue.ClassDialogue.JCharField;
import me.grax.jbytemod.utils.ErrorDisplay;
import me.grax.jbytemod.utils.gui.SwingUtils;

public class JAnnotationEditor extends JFrame {

  /**
   * Avoid multiple frames open
   */
  private static List<String> open = new ArrayList<>();

  @SuppressWarnings("unchecked")
  public JAnnotationEditor(String title, Object parent, String field) {
    try {
      open.add(field);
      Field flist = parent.getClass().getDeclaredField(field);
      flist.setAccessible(true);
      List<AnnotationNode> list = (List<AnnotationNode>) flist.get(parent);
      if(list == null)
    	  list = new ArrayList<>();
      List<AnnotationNode> newList = new ArrayList<>(list);
      this.setTitle(title);
      this.setBounds(100, 100, 450, 400);
      this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
      this.setLayout(new BorderLayout());

      JTable jtable = new JTable() {
        @Override
        public boolean isCellEditable(int row, int column) {
        	return false;
        };
      };
      jtable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
      jtable.getTableHeader().setReorderingAllowed(false);
      DefaultTableModel lm = new DefaultTableModel();
      lm.addColumn("#");
      lm.addColumn("Desc");
      int i = 0;
      for (AnnotationNode item : list) {
        lm.addRow(new Object[] { String.valueOf(i), item.desc });
        i++;
      }
      jtable.setModel(lm);

      //why the hell does this not work with AUTO_RESIZE_LAST_COMPONENT
      //jtable.getColumnModel().getColumn(0).setPreferredWidth(30);

      this.add(new JScrollPane(jtable), BorderLayout.CENTER);
      JPanel actions = new JPanel();
      actions.setLayout(new GridLayout(1, 4));
      JButton add = new JButton(JByteMod.res.getResource("add"));
      add.addActionListener(a -> {
    	  AnnotationNode edit = editAnnotationWindow(null);
          int row = jtable.getSelectedRow();
          if (edit != null) {
            if (row != -1) {
              lm.insertRow(row, new Object[] { -1, edit.desc});
              newList.add(row, edit);
              recalcIndex(lm);
            } else {
              lm.addRow(new Object[] { lm.getRowCount(), edit.desc });
              newList.add(edit);
            }
          }
      });
      actions.add(add);
      JButton remove = new JButton(JByteMod.res.getResource("remove"));
      remove.addActionListener(a -> {
        int[] selectedRows = jtable.getSelectedRows();
        if (selectedRows.length > 0) {
          for (int j = selectedRows.length - 1; j >= 0; j--) {
            lm.removeRow(selectedRows[j]);
            newList.remove(selectedRows[j]);
          }
          recalcIndex(lm);
        }
      });
      actions.add(remove);
      JButton edit = new JButton(JByteMod.res.getResource("edit"));
      edit.addActionListener(a -> {
    	  int row = jtable.getSelectedRow();
          if (row == -1) {
            return;
          }
          AnnotationNode node = newList.get(row);
          if (node != null) {
            editAnnotationWindow(node);
            lm.insertRow(row, new Object[] { row, node.desc });
            lm.removeRow(row + 1);
          } else {
            JOptionPane.showMessageDialog(null, "null cannot be edited!");
          }
      });
      actions.add(edit);

      this.add(actions, BorderLayout.PAGE_END);
      this.addWindowListener(new WindowAdapter() {
        @Override
        public void windowClosing(WindowEvent e) {
          try {
            JByteMod.LOGGER.log("Updating List!");
            if(newList.size() == 0)
            	flist.set(parent, null);
            else
            	flist.set(parent, newList);
          } catch (Exception e1) {
            new ErrorDisplay(e1);
          }
          open.remove(field);
        }
      });
    } catch (Throwable e1) {
      open.remove(field);
      new ErrorDisplay(e1);
      setVisible(false);
    }
  }
  
  public static void recalcIndex(DefaultTableModel lm) {
	  for (int i = 0; i < lm.getRowCount(); i++) {
        lm.setValueAt(i, i, 0);
	  }
  }

  private static AnnotationNode editAnnotationWindow(AnnotationNode node) {
	  List<Object> values = node != null ? node.values != null ? node.values : new ArrayList<>() : new ArrayList<>();
	  JPanel mainPanel = new JPanel();
      JPanel leftText = new JPanel();
      JPanel rightInput = new JPanel();
      JButton valuesButton = new JButton("Edit Values");
      valuesButton.addActionListener(e -> {
          try {
        	  ValuesEditor editor = new ValuesEditor(values);
        	  editor.open();
          } catch (Exception ex) {
            ex.printStackTrace();
          }
        });
      
      mainPanel.setLayout(new BorderLayout());
      leftText.setLayout(new GridLayout(0, 1));
      rightInput.setLayout(new GridLayout(0, 1));
      
      leftText.add(new JLabel("Type: "));
      JTextField cst = new JTextField();
      rightInput.add(cst);
      if(node != null)
    	  cst.setText(node.desc);
      mainPanel.add(leftText, BorderLayout.WEST);
      mainPanel.add(rightInput, BorderLayout.CENTER);
      mainPanel.add(valuesButton, BorderLayout.SOUTH);
      
      if (JOptionPane.showConfirmDialog(null, mainPanel, node == null ? "Add AnnotationNode" : "Edit AnnotationNode", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
    	  if(node != null)
	      {
	    	  node.desc = cst.getText();
	    	  node.values = values;
	    	  if(values.size() == 0)
	    		  node.values = null;
	    	  return node;
	      }else
	      {
	    	  node = new AnnotationNode(cst.getText());
	    	  node.values = values;
	    	  if(values.size() == 0)
	    		  node.values = null;
	    	  return node;
	      }
      }
      return node;
  }
  
  public static String getClassName(Object o) {
	  if(o instanceof String)
		  return "String";
	  if(o instanceof Byte)
		  return "Byte";
	  if(o instanceof Boolean)
		  return "Boolean";
	  if(o instanceof Character)
		  return "Character";
	  if(o instanceof Short)
		  return "Short";
	  if(o instanceof Integer)
		  return "Integer";
	  if(o instanceof Long)
		  return "Long";
	  if(o instanceof Float)
		  return "Float";
	  if(o instanceof Double)
		  return "Double";
	  if(o instanceof org.objectweb.asm.Type)
		  return "Type";
	  if(o instanceof String[])
		  return "String[]";
	  else if(o instanceof List<?>)
		  return "List";
	  else if(o instanceof AnnotationNode)
		  return "AnnotationNode";
	  return "Unknown";
  }
  
  private static class ValuesEditor {
	  private List<Object> values;
	  private List<Entry<Object, Object>> valuesMap;
	  
	  public ValuesEditor(List<Object> values) {
		  this.values = values;
		  valuesMap = new ArrayList<>();
		  for(int i = 0; i < values.size(); i += 2)
		  {
			  Object first = values.get(i);
			  Object second = values.get(i + 1);
			  valuesMap.add(new AbstractMap.SimpleEntry<>(first, second));
		  }
	  }
	  
	  public void open() {
		  JPanel frame = new JPanel();
	      frame.setBounds(100, 100, 300, 400);
	      frame.setLayout(new BorderLayout());

	      JTable jtable = new JTable() {
	        @Override
	        public boolean isCellEditable(int row, int column) {
	        	return false;
	        };
	      };
	      jtable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
	      jtable.getTableHeader().setReorderingAllowed(false);
	      DefaultTableModel lm = new DefaultTableModel();
	      lm.addColumn("#");
	      lm.addColumn("Name");
	      lm.addColumn("Type");
	      int i = 0;
	      for (Entry<Object, Object> entry : valuesMap) {
	        lm.addRow(new Object[] { String.valueOf(i), entry.getKey().toString(), getClassName(entry.getValue()) });
	        i++;
	      }
	      jtable.setModel(lm);

	      //why the hell does this not work with AUTO_RESIZE_LAST_COMPONENT
	      //jtable.getColumnModel().getColumn(0).setPreferredWidth(30);

	      frame.add(new JScrollPane(jtable), BorderLayout.CENTER);
	      JPanel actions = new JPanel();
	      actions.setLayout(new GridLayout(1, 4));
	      JButton add = new JButton(JByteMod.res.getResource("add"));
	      add.addActionListener(a -> {
	    	  Entry<Object, Object> entry = editValueWindow(null);
	          int row = jtable.getSelectedRow();
	          if (entry != null) {
	            if (row != -1) {
	              lm.insertRow(row, new Object[] { -1, entry.getKey().toString(), getClassName(entry.getValue()) });
	              valuesMap.add(row, entry);
	              recalcIndex(lm);
	            } else {
	              lm.addRow(new Object[] { lm.getRowCount(), entry.getKey().toString(), getClassName(entry.getValue()) });
	              valuesMap.add(entry);
	            }
	          }
	      });
	      actions.add(add);
	      JButton remove = new JButton(JByteMod.res.getResource("remove"));
	      remove.addActionListener(a -> {
	        int[] selectedRows = jtable.getSelectedRows();
	        if (selectedRows.length > 0) {
	          for (int j = selectedRows.length - 1; j >= 0; j--) {
	            lm.removeRow(selectedRows[j]);
	            valuesMap.remove(selectedRows[j]);
	          }
	          recalcIndex(lm);
	        }
	      });
	      actions.add(remove);
	      JButton edit = new JButton(JByteMod.res.getResource("edit"));
	      edit.addActionListener(a -> {
	    	  int row = jtable.getSelectedRow();
	          if (row == -1) {
	            return;
	          }
	          Entry<Object, Object> entry = valuesMap.get(row);
	          if (entry != null) {
	        	entry = editValueWindow(entry);
	        	valuesMap.add(row, entry);
	        	valuesMap.remove(row + 1);
	            lm.insertRow(row, new Object[] { row, entry.getKey().toString(), getClassName(entry.getValue()) });
	            lm.removeRow(row + 1);
	          } else {
	            JOptionPane.showMessageDialog(null, "null cannot be edited!");
	          }
	      });
	      actions.add(edit);

	      frame.add(actions, BorderLayout.PAGE_END);
	      if (JOptionPane.showConfirmDialog(null, frame, "Edit Values", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
	    	  try {
	              JByteMod.LOGGER.log("Updating List!");
	              values.clear();
	              for(Entry<Object, Object> entry : valuesMap)
	              {
	            	  values.add(entry.getKey());
	            	  values.add(entry.getValue());
	              }
	            } catch (Exception e1) {
	              new ErrorDisplay(e1);
	            }
	      }
	  }
	  
	  private Object value;
	  
	  private Entry<Object, Object> editValueWindow(Entry<Object, Object> entry) {
		  Object key = entry == null ? "" : entry.getKey();
		  value = entry == null ? null : entry.getValue();
		  JPanel mainPanel = new JPanel();
	      JPanel leftText = new JPanel();
	      JPanel rightInput = new JPanel();
	      
	      mainPanel.setLayout(new BorderLayout());
	      leftText.setLayout(new GridLayout(0, 1));
	      rightInput.setLayout(new GridLayout(0, 1));
	      
	      leftText.add(new JLabel("Key: "));
	      JTextField cst = new JTextField();
	      rightInput.add(cst);
	      cst.setText((String)key);
	      mainPanel.add(leftText, BorderLayout.WEST);
	      mainPanel.add(rightInput, BorderLayout.CENTER);
	      
	      leftText.add(new JLabel("Type: "));
	      JComboBox<String> type = new JComboBox<String>(new String[] { "String", "Byte", "Boolean", "Character", "Short", 
	    		  "Integer", "Long", "Float", "Double", "Type", "String[]", "AnnotationNode", "List"});
	      rightInput.add(type);
	      if(value != null)
	      {
	    	  type.setEnabled(false);
	    	  type.setSelectedItem(getClassName(value));
	      }
	      if(value != null && type.getSelectedItem().equals("Type"))
    		  value = ((org.objectweb.asm.Type)value).toString();
	      JButton valuesButton = new JButton("Edit Value");
	      valuesButton.addActionListener(e -> {
	          try {
	        	  if(value == null)
	        	  {
	        		  switch((String)type.getSelectedItem()) {
	        		  	case "String":
		        	  	case "Type":
		        	  		value = "";
		        	  		break;
		        	  	case "Byte":
		        	  		value = Byte.valueOf((byte)0);
		        	  		break;
		        	  	case "Short":
		        	  		value = Short.valueOf((short)0);
		        	  		break;
		        	  	case "Integer":
		        	  		value = 0;
		        	  		break;
		        	  	case "Character":
		        	  		value = Character.valueOf((char)0);
		        	  		break;
		        	  	case "Boolean":
		        	  		value = false;
		        	  		break;
	        		  	case "Long":
	        		  		value = 0L;
	        		  		break;
	        		  	case "Float":
	        		  		value = 0F;
	        		  		break;
	        		  	case "Double":
	        		  		value = 0D;
	        		  		break;
	        		  	case "String[]":
	        		  		value = new String[2];
	        		  		break;
	        		  	case "List":
	        		  		value = new ArrayList<>();
	        		  		break;
	        		  	case "AnnotationNode":
	        		  		value = new AnnotationNode("");
	        		  		break;
	        		  }
	        		  type.setEnabled(false);
	        	  }
	        	  if(type.getSelectedItem().equals("AnnotationNode"))
	        		  editAnnotationWindow((AnnotationNode)value);
	        	  else if(type.getSelectedItem().equals("List"))
	        		  new ListEditor((List<Object>)value).open();
	        	  else
	        		  value = editValuePair(value, (String)type.getSelectedItem());
	          } catch (Exception ex) {
	            ex.printStackTrace();
	          }
	        });
	      mainPanel.add(valuesButton, BorderLayout.SOUTH);
	      
	      if (JOptionPane.showConfirmDialog(null, mainPanel, entry == null ? "Add Value Pair" : "Edit Value Pair", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
	    	  if(value == null)
	    	  {
        		  switch((String)type.getSelectedItem()) {
        		  	case "String":
	        	  	case "Type":
	        	  		value = "";
	        	  		break;
	        	  	case "Byte":
	        	  		value = Byte.valueOf((byte)0);
	        	  		break;
	        	  	case "Short":
	        	  		value = Short.valueOf((short)0);
	        	  		break;
	        	  	case "Integer":
	        	  		value = 0;
	        	  		break;
	        	  	case "Character":
	        	  		value = Character.valueOf((char)0);
	        	  		break;
	        	  	case "Boolean":
	        	  		value = false;
	        	  		break;
        		  	case "Long":
        		  		value = 0L;
        		  		break;
        		  	case "Float":
        		  		value = 0F;
        		  		break;
        		  	case "Double":
        		  		value = 0D;
        		  		break;
        		  	case "String[]":
        		  		value = new String[2];
        		  		break;
        		  	case "List":
        		  		value = new ArrayList<>();
        		  		break;
        		  	case "AnnotationNode":
        		  		value = new AnnotationNode("");
        		  		break;
        		  }
        		  type.setEnabled(false);
        	  }
	    	  try {
	    		  if(type.getSelectedItem().equals("Type"))
	    			  value = org.objectweb.asm.Type.getType((String)value);
	    	  }catch(Exception e) {
	    		  e.printStackTrace();
	              JOptionPane.showMessageDialog(null, "Exception: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
	              value = org.objectweb.asm.Type.getType("Lnull;");
	    	  }
	    	  return new AbstractMap.SimpleEntry<>(cst.getText(), value);
	      }
	      return entry;
	  }
	  
	  public static Object editValuePair(Object value, String type) {
		  JPanel mainPanel = new JPanel();
	      JPanel leftText = new JPanel();
	      JPanel rightInput = new JPanel();
	      
	      mainPanel.setLayout(new BorderLayout());
	      leftText.setLayout(new GridLayout(0, 1));
	      rightInput.setLayout(new GridLayout(0, 1));
	      
	      switch(type)
	      {
	      	case "String":
	      		leftText.add(new JLabel("Value: "));
	            JTextField cstString = new JTextField();
	            cstString.setText((String)value);
	            rightInput.add(SwingUtils.withButton(cstString, "...", e -> {
	              JLDCEditor editor = new JLDCEditor(cstString.getText());
	              editor.setVisible(true);
	              cstString.setText(editor.getText());
	            }));
	      		break;
	      	case "Type":
	      		leftText.add(new JLabel("Type: "));
	            JTextField cstType = new JTextField();
	            cstType.setText((String)value);
	            rightInput.add(cstType);
	      		break;
	  	  	case "Character":
	  	  		leftText.add(new JLabel("Type: "));
	  	  		JCharField charType = new JCharField(value);
	  	  		rightInput.add(charType);
	  	  		break;
	  	  	case "Boolean":
	  	  		leftText.add(new JLabel("Type: "));
	  	  		JCheckBox booleanBox = new JCheckBox();
	  	  		booleanBox.setSelected((boolean)value);
  	  			rightInput.add(booleanBox);
	  	  		break;
	      	case "Byte":
	  	  	case "Short":
	  	  	case "Integer":
	  	  	case "Long":
	  	  	case "Float":
		  	case "Double":
		  		leftText.add(new JLabel("Type: "));
	            JTextField fdType = new JTextField();
	            if(type.equals("Byte"))
	            	fdType.setText(String.valueOf((byte)value));
	            else if(type.equals("Short"))
	            	fdType.setText(String.valueOf((short)value));
	            else if(type.equals("Integer"))
	            	fdType.setText(String.valueOf((int)value));
	            else if(type.equals("Long"))
	            	fdType.setText(String.valueOf((long)value));
	            else if(type.equals("Float"))
	            	fdType.setText(String.valueOf((float)value));
	            else if(type.equals("Double"))
	            	fdType.setText(String.valueOf((double)value));
	            rightInput.add(fdType);
		  		break;
	  	  	case "String[]":
	  	  		leftText.add(new JLabel("Value 1: "));
	  	  		JTextField cstArray1 = new JTextField();
	  	  		cstArray1.setText(((String[])value)[0]);
	  	  		rightInput.add(cstArray1);
	  	  		leftText.add(new JLabel("Value 2: "));
	  	  		JTextField cstArray2 = new JTextField();
	  	  		cstArray2.setText(((String[])value)[1]);
	  	  		rightInput.add(cstArray2);
	  	  		break;
	      }
	      
	      mainPanel.add(leftText, BorderLayout.WEST);
	      mainPanel.add(rightInput, BorderLayout.CENTER);
	      
	      if (JOptionPane.showConfirmDialog(null, mainPanel, "Edit Value", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
	    	  switch(type)
		      {
		      	case "String":
		      		return ((JTextField)((JPanel)rightInput.getComponent(0)).getComponent(0)).getText();
		      	case "Type":
		      		return ((JTextField)rightInput.getComponent(0)).getText();
		  	  	case "Character":
		  	  		return ((JCharField)rightInput.getComponent(0)).getCharacter();
		  	  	case "Boolean":
		  	  		return ((JCheckBox)rightInput.getComponent(0)).isSelected();
		      	case "Byte":
		      		try
		  	  		{
		  	  			return Byte.valueOf(((JTextField)rightInput.getComponent(0)).getText());
		  	  		}catch(Exception e)
		  	  		{
		  	  			e.printStackTrace();
		  	  			JOptionPane.showMessageDialog(null, "Exception: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		  	  			return (byte)0;
		  	  		}
		  	  	case "Short":
			  	  	try
		  	  		{
		  	  			return Short.valueOf(((JTextField)rightInput.getComponent(0)).getText());
		  	  		}catch(Exception e)
		  	  		{
		  	  			e.printStackTrace();
		  	  			JOptionPane.showMessageDialog(null, "Exception: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		  	  			return (short)0;
		  	  		}
		  	  	case "Integer":
			  	  	try
		  	  		{
		  	  			return Integer.valueOf(((JTextField)rightInput.getComponent(0)).getText());
		  	  		}catch(Exception e)
		  	  		{
		  	  			e.printStackTrace();
		  	  			JOptionPane.showMessageDialog(null, "Exception: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		  	  			return 0;
		  	  		}
		  	  	case "Long":
			  	  	try
		  	  		{
		  	  			return Long.valueOf(((JTextField)rightInput.getComponent(0)).getText());
		  	  		}catch(Exception e)
		  	  		{
		  	  			e.printStackTrace();
		  	  			JOptionPane.showMessageDialog(null, "Exception: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		  	  			return 0L;
		  	  		}
		  	  	case "Float":
		  	  		try
		  	  		{
		  	  			return Float.valueOf(((JTextField)rightInput.getComponent(0)).getText());
		  	  		}catch(Exception e)
		  	  		{
		  	  			e.printStackTrace();
		  	  			JOptionPane.showMessageDialog(null, "Exception: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		  	  			return 0F;
		  	  		}
			  	case "Double":
			  		try
		  	  		{
			  			return Double.valueOf(((JTextField)rightInput.getComponent(0)).getText());
		  	  		}catch(Exception e)
		  	  		{
		  	  			e.printStackTrace();
		  	  			JOptionPane.showMessageDialog(null, "Exception: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		  	  			return 0D;
		  	  		}
		  	  	case "String[]":
		  	  		return new String[] {((JTextField)rightInput.getComponent(0)).getText(),
		  	  			((JTextField)rightInput.getComponent(1)).getText()};
		      }
	      }
	      return value;
	  }
  }
  
  private static class ListEditor {
	  private List<Object> values;
	  
	  public ListEditor(List<Object> values) {
		  this.values = values;
	  }
	  
	  public void open() {
		  List<Object> newValues = new ArrayList<>(values);
		  JPanel frame = new JPanel();
	      frame.setBounds(100, 100, 300, 400);
	      frame.setLayout(new BorderLayout());

	      JTable jtable = new JTable() {
	        @Override
	        public boolean isCellEditable(int row, int column) {
	        	return false;
	        };
	      };
	      jtable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
	      jtable.getTableHeader().setReorderingAllowed(false);
	      DefaultTableModel lm = new DefaultTableModel();
	      lm.addColumn("#");
	      lm.addColumn("Type");
	      int i = 0;
	      for (Object o : values) {
	        lm.addRow(new Object[] { String.valueOf(i), getClassName(o) });
	        i++;
	      }
	      jtable.setModel(lm);

	      //why the hell does this not work with AUTO_RESIZE_LAST_COMPONENT
	      //jtable.getColumnModel().getColumn(0).setPreferredWidth(30);

	      frame.add(new JScrollPane(jtable), BorderLayout.CENTER);
	      JPanel actions = new JPanel();
	      actions.setLayout(new GridLayout(1, 4));
	      JButton add = new JButton(JByteMod.res.getResource("add"));
	      add.addActionListener(a -> {
	    	  Object o = editValueWindow(null);
	          int row = jtable.getSelectedRow();
	          if (o != null) {
	            if (row != -1) {
	              lm.insertRow(row, new Object[] { -1, getClassName(o) });
	              newValues.add(row, o);
	              recalcIndex(lm);
	            } else {
	              lm.addRow(new Object[] { lm.getRowCount(), getClassName(o) });
	              newValues.add(o);
	            }
	          }
	      });
	      actions.add(add);
	      JButton remove = new JButton(JByteMod.res.getResource("remove"));
	      remove.addActionListener(a -> {
	        int[] selectedRows = jtable.getSelectedRows();
	        if (selectedRows.length > 0) {
	          for (int j = selectedRows.length - 1; j >= 0; j--) {
	            lm.removeRow(selectedRows[j]);
	            newValues.remove(selectedRows[j]);
	          }
	          recalcIndex(lm);
	        }
	      });
	      actions.add(remove);
	      JButton edit = new JButton(JByteMod.res.getResource("edit"));
	      edit.addActionListener(a -> {
	    	  int row = jtable.getSelectedRow();
	          if (row == -1) {
	            return;
	          }
	          Object o = newValues.get(row);
	          if (o != null) {
	        	o = editValueWindow(o);
	        	newValues.add(row, o);
	        	newValues.remove(row + 1);
	            lm.insertRow(row, new Object[] { row, getClassName(o) });
	            lm.removeRow(row + 1);
	          } else {
	            JOptionPane.showMessageDialog(null, "null cannot be edited!");
	          }
	      });
	      actions.add(edit);

	      frame.add(actions, BorderLayout.PAGE_END);
	      if (JOptionPane.showConfirmDialog(null, frame, "Edit List", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
	    	  try {
	              JByteMod.LOGGER.log("Updating List!");
	              values.clear();
	              for(Object o : newValues)
	            	  values.add(o);
	            } catch (Exception e1) {
	              new ErrorDisplay(e1);
	            }
	      }
	  }
	  
	  Object value = null;
	  
	  private Object editValueWindow(Object obj) {
		  value = obj == null ? null : obj;
		  JPanel mainPanel = new JPanel();
	      JPanel leftText = new JPanel();
	      JPanel rightInput = new JPanel();
	      
	      mainPanel.setLayout(new BorderLayout());
	      leftText.setLayout(new GridLayout(0, 1));
	      rightInput.setLayout(new GridLayout(0, 1));
	      mainPanel.add(leftText, BorderLayout.WEST);
	      mainPanel.add(rightInput, BorderLayout.CENTER);
	      
	      leftText.add(new JLabel("Type: "));
	      JComboBox<String> type = new JComboBox<String>(new String[] { "String", "Byte", "Boolean", "Character", "Short", 
	    		  "Integer", "Long", "Float", "Double", "Type", "String[]", "AnnotationNode"});
	      rightInput.add(type);
	      if(value != null)
	      {
	    	  type.setEnabled(false);
	    	  type.setSelectedItem(getClassName(value));
	      }
	      if(value != null && type.getSelectedItem().equals("Type"))
    		  value = ((org.objectweb.asm.Type)value).toString();
	      JButton valuesButton = new JButton("Edit Value");
	      valuesButton.addActionListener(e -> {
	          try {
	        	  if(value == null)
	        	  {
	        		  switch((String)type.getSelectedItem()) {
	        		  	case "String":
		        	  	case "Type":
		        	  		value = "";
		        	  		break;
		        	  	case "Byte":
		        	  		value = Byte.valueOf((byte)0);
		        	  		break;
		        	  	case "Short":
		        	  		value = Short.valueOf((short)0);
		        	  		break;
		        	  	case "Integer":
		        	  		value = 0;
		        	  		break;
		        	  	case "Character":
		        	  		value = Character.valueOf((char)0);
		        	  		break;
		        	  	case "Boolean":
		        	  		value = false;
		        	  		break;
	        		  	case "Long":
	        		  		value = 0L;
	        		  		break;
	        		  	case "Float":
	        		  		value = 0F;
	        		  		break;
	        		  	case "Double":
	        		  		value = 0D;
	        		  		break;
	        		  	case "String[]":
	        		  		value = new String[2];
	        		  		break;
	        		  	case "AnnotationNode":
	        		  		value = new AnnotationNode("");
	        		  		break;
	        		  }
	        		  type.setEnabled(false);
	        	  }
	        	  if(type.getSelectedItem().equals("AnnotationNode"))
	        		  editAnnotationWindow((AnnotationNode)value);
	        	  else
	        		  value = ValuesEditor.editValuePair(value, (String)type.getSelectedItem());
	          } catch (Exception ex) {
	            ex.printStackTrace();
	          }
	        });
	      mainPanel.add(valuesButton, BorderLayout.SOUTH);
	      
	      if (JOptionPane.showConfirmDialog(null, mainPanel, obj == null ? "Add Value Pair" : "Edit Value Pair", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
	    	  if(value == null)
	    	  {
        		  switch((String)type.getSelectedItem()) {
        		  	case "String":
	        	  	case "Type":
	        	  		value = "";
	        	  		break;
	        	  	case "Byte":
	        	  		value = Byte.valueOf((byte)0);
	        	  		break;
	        	  	case "Short":
	        	  		value = Short.valueOf((short)0);
	        	  		break;
	        	  	case "Integer":
	        	  		value = 0;
	        	  		break;
	        	  	case "Character":
	        	  		value = Character.valueOf((char)0);
	        	  		break;
	        	  	case "Boolean":
	        	  		value = false;
	        	  		break;
        		  	case "Long":
        		  		value = 0L;
        		  		break;
        		  	case "Float":
        		  		value = 0F;
        		  		break;
        		  	case "Double":
        		  		value = 0D;
        		  		break;
        		  	case "String[]":
        		  		value = new String[2];
        		  		break;
        		  	case "AnnotationNode":
        		  		value = new AnnotationNode("");
        		  		break;
        		  }
        		  type.setEnabled(false);
        	  }
	    	  try {
	    		  if(type.getSelectedItem().equals("Type"))
	    			  value = org.objectweb.asm.Type.getType((String)value);
	    	  }catch(Exception e) {
	    		  e.printStackTrace();
	              JOptionPane.showMessageDialog(null, "Exception: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
	              value = org.objectweb.asm.Type.getType("Lnull;");
	    	  }
	    	  return value;
	      }
	      return obj;
	  }
  }
  
  public static boolean isOpen(String name) {
    return open.contains(name);
  }
}
