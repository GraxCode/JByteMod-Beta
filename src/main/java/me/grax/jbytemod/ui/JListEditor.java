package me.grax.jbytemod.ui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import me.grax.jbytemod.JByteMod;
import me.grax.jbytemod.utils.ErrorDisplay;

public class JListEditor extends JFrame {

  /**
   * Avoid multiple frames open
   */
  private static boolean open = false;

  @SuppressWarnings({ "rawtypes", "unchecked" })
  public JListEditor(String title, Object parent, String field) {
    try {
      open = true;
      Field flist = parent.getClass().getDeclaredField(field);
      flist.setAccessible(true);
      List<String> list = (List<String>) flist.get(parent);
      this.setTitle(title);
      this.setBounds(100, 100, 300, 400);
      this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
      this.setLayout(new BorderLayout());

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
      for (String item : list) {
        lm.addRow(new Object[] { String.valueOf(i), item });
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

      this.add(actions, BorderLayout.PAGE_END);
      this.addWindowListener(new WindowAdapter() {
        @Override
        public void windowClosing(WindowEvent e) {
          try {
            System.out.println("Updating List!");
            TableModel model = jtable.getModel();
            ArrayList<String> list = new ArrayList<>();
            for (int i = 0; i < model.getRowCount(); i++) {
              list.add(String.valueOf(model.getValueAt(i, 1)));
            }
            flist.set(parent, list);
          } catch (Exception e1) {
            new ErrorDisplay(e1);
          }
          open = false;
        }
      });
    } catch (Throwable e1) {
      open = false;
      new ErrorDisplay(e1);
      setVisible(false);
    }
  }

  public static boolean isOpen() {
    return open;
  }
}
