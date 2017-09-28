package me.grax.jbytemod.ui.ifs;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;

import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.text.NumberFormatter;

import org.objectweb.asm.tree.ClassNode;

import me.grax.jbytemod.JByteMod;
import me.grax.jbytemod.ui.NoBorderSP;
import me.grax.jbytemod.ui.lists.LVPList;

public class CNSettings extends MyInternalFrame {
  /**
   * Save position
   */
  private static Rectangle bounds = new Rectangle(10, 10, 1280 / 4, 720 / 4);

  public CNSettings(ClassNode cn) {
    super("Class Settings");
    this.setBounds(bounds);
    this.setLayout(new BorderLayout(0, 0));
    JPanel panel = new JPanel();
    panel.setLayout(new BorderLayout(5, 5));
    panel.setBorder(new EmptyBorder(10, 10, 10, 10));
    final JPanel input = new JPanel(new GridLayout(0, 1));
    final JPanel labels = new JPanel(new GridLayout(0, 1));
    panel.add(labels, "West");
    panel.add(input, "Center");
    panel.add(new JLabel("Warning: References will not be updated!"), "South");
    labels.add(new JLabel("Class Name:"));
    JTextField name = new JTextField(cn.name);
    input.add(name);
    labels.add(new JLabel("Class SourceFile:"));
    JTextField sf = new JTextField(cn.sourceFile);
    input.add(sf);
    labels.add(new JLabel("Class Access:"));
    NumberFormat format = NumberFormat.getInstance();
    format.setGroupingUsed(false);
    NumberFormatter formatter = new NumberFormatter(format);
    formatter.setValueClass(Integer.class);
    formatter.setMinimum(0);
    formatter.setMaximum(Integer.MAX_VALUE);
    formatter.setAllowsInvalid(false);
    formatter.setCommitsOnValidEdit(true);
    formatter.setOverwriteMode(true);
    JFormattedTextField access = new JFormattedTextField(formatter);
    access.setValue(cn.access);
    input.add(access);

    //    
    //    if (JOptionPane.showConfirmDialog(JByteMod.instance, panel, "Edit Class " + cname, 2) == JOptionPane.OK_OPTION) {
    //      cn.name = name.getText();
    //      cn.sourceFile = sf.getText();
    //      cn.access = (int) access.getValue();
    //      JByteMod.instance.refreshTree();
    //    }
    this.add(panel, BorderLayout.CENTER);
    JButton update = new JButton("Update");
    update.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        boolean refresh = false;
        if(!cn.name.equals(name.getText())) {
          refresh = true;
          cn.name = name.getText();
        }
        cn.sourceFile = sf.getText();
        cn.access = (int) access.getValue();
        if(refresh) {
          JByteMod.instance.refreshTree();
        }
      }
    });
    this.add(update, BorderLayout.PAGE_END);
    this.show();
  }

  @Override
  public void setVisible(boolean aFlag) {
    if (!aFlag && !(getLocation().getY() == 0 && getLocation().getX() == 0)) {
      bounds = getBounds();
    }
    super.setVisible(aFlag);
  }
}
