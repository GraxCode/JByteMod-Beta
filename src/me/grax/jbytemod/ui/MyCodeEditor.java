package me.grax.jbytemod.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import me.grax.jbytemod.JByteMod;
import me.grax.jbytemod.ui.lists.AdressList;
import me.grax.jbytemod.ui.lists.MyCodeList;

public class MyCodeEditor extends JPanel {
  private MyCodeList cl;

  public MyCodeEditor(JByteMod jam, JLabel editor) {
    this.setLayout(new BorderLayout());
    cl = new MyCodeList(jam, editor);
    this.add(cl, BorderLayout.CENTER);
    JPanel p = new JPanel();
    p.setLayout(new GridLayout());
    p.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Color.GRAY));
    p.add(new AdressList(cl));
    this.add(p, BorderLayout.WEST);
  }

  public MyCodeList getEditor() {
    return cl;
  }
}
