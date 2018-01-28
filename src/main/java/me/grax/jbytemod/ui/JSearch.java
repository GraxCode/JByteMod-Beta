package me.grax.jbytemod.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import me.grax.jbytemod.ui.lists.MyCodeList;
import me.grax.jbytemod.utils.list.InstrEntry;

public class JSearch extends JDialog implements ActionListener {

  private MyCodeList list;
  private JTextField tf;
  private JCheckBox mc;
  private JCheckBox ww;

  public JSearch(MyCodeList list) {
    this.list = list;
    this.setTitle("Code List Search");
    this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    this.setLocationRelativeTo(null);
    this.setBounds(100, 100, 300, 220);
    this.setResizable(false);
    this.setAlwaysOnTop(true);
    this.setFocusable(false);
    JPanel cp = new JPanel(new BorderLayout());
    cp.setBorder(new EmptyBorder(16, 16, 16, 16));
    this.setContentPane(cp);
    JPanel find = new JPanel(new BorderLayout(10, 10));
    find.setBorder(new EmptyBorder(16, 16, 16, 16));
    JPanel border = new JPanel(new BorderLayout());
    border.setBorder(BorderFactory.createLineBorder(Color.lightGray));
    border.add(find, BorderLayout.NORTH);
    JPanel boxes = new JPanel(new GridLayout(2, 2));
    find.add(boxes, BorderLayout.SOUTH);
    boxes.add(mc = new JCheckBox("Match Case"));
    boxes.add(ww = new JCheckBox("Whole Word"));
    cp.add(border, BorderLayout.NORTH);
    JPanel bottom = new JPanel(new GridLayout(1, 0, 10, 10));
    JButton findBtn = new JButton("Find next");
    findBtn.addActionListener(this);
    bottom.add(findBtn);
    JButton closeBtn = new JButton("Close");
    closeBtn.addActionListener(e -> {
      this.dispose();
    });
    bottom.add(closeBtn);
    cp.add(bottom, BorderLayout.SOUTH);
    final JPanel center = new JPanel(new GridLayout());
    final JPanel left = new JPanel(new GridLayout());
    find.add(left, "West");
    find.add(center, "Center");
    left.add(new JLabel("Find: "));
    tf = new JTextField();
    center.add(tf);
  }

  public void actionPerformed(ActionEvent e) {
    //TODO: go forward and backward
    boolean mcase = mc.isSelected();
    String key = tf.getText().trim();
    if (ww.isSelected()) {
      //TODO words at beginning or surrounded by quote
      key = " " + key + " ";
    }
    if (!mcase) {
      key = key.toLowerCase();
    }
    if (!key.isEmpty()) {
      DefaultListModel<InstrEntry> model = (DefaultListModel<InstrEntry>) list.getModel();
      if (!searchNextFrom(list.getSelectedIndex() + 1, mcase, key, model)) {
        searchNextFrom(0, mcase, key, model);
      }
    }
  }

  private boolean searchNextFrom(int index, boolean mcase, String key, DefaultListModel<InstrEntry> model) {
    for (int i = index; i < model.getSize(); i++) {
      InstrEntry entry = model.getElementAt(i);
      String easy = entry.toEasyString();
      if (!mcase) {
        easy = easy.toLowerCase();
      }
      if (easy.contains(key)) {
        list.setSelectedIndex(i);
        list.scrollRectToVisible(list.getCellBounds(list.getMinSelectionIndex(), list.getMaxSelectionIndex()));
        return true;
      }
    }
    return false;
  }

}
