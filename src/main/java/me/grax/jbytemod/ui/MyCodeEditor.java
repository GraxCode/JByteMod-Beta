package me.grax.jbytemod.ui;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

import me.grax.jbytemod.JByteMod;
import me.grax.jbytemod.ui.lists.AdressList;
import me.grax.jbytemod.ui.lists.ErrorList;
import me.grax.jbytemod.ui.lists.MyCodeList;

public class MyCodeEditor extends JPanel {
  private MyCodeList cl;

  public MyCodeEditor(JByteMod jbm, JLabel editor) {
    this.setLayout(new BorderLayout());
    cl = new MyCodeList(jbm, editor);
    this.add(cl, BorderLayout.CENTER);
    JPanel p = new JPanel();
    p.setLayout(new BorderLayout());
    p.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, JByteMod.border));
    p.add(new AdressList(cl), BorderLayout.CENTER);
    JPanel west = new JPanel();
    west.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, JByteMod.border));
    west.setLayout(new BorderLayout(0, 0));
    west.add(new ErrorList(jbm, cl), BorderLayout.CENTER);
    p.add(west, BorderLayout.WEST);
    
    this.add(p, BorderLayout.WEST);
  }

  public MyCodeList getEditor() {
    return cl;
  }
}
