package me.grax.jbytemod.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.border.EmptyBorder;

import org.fife.ui.rtextarea.RTextScrollPane;

import me.grax.jbytemod.JByteMod;
import me.grax.jbytemod.ui.lists.SearchList;

public class MyTabbedPane extends JTabbedPane {
  public MyTabbedPane(JByteMod jam) {
    JLabel editor = new JLabel("Editor");
    MyCodeEditor list = new MyCodeEditor(jam, editor);
    jam.setCodeList(list.getEditor());
    this.addTab("Code", this.withBorder(editor, list));
    SearchList searchList = new SearchList(jam);
    jam.setSearchlist(searchList);
    JLabel search = new JLabel("Search Results");
    this.addTab("Search", this.withBorder(search, searchList));
    DecompilerPanel dp = new DecompilerPanel();
    jam.setDP(dp);
    this.addTab("Decompiler", this.withBorder(new JPanel(), new JLabel("Procyon Decompiler"), dp));
  }

  private JPanel withBorder(JLabel label, Component c) {
    JPanel panel = new JPanel();
    panel.setLayout(new BorderLayout(0, 0));
    JPanel lpad = new JPanel();
    lpad.setBorder(new EmptyBorder(1, 5, 0, 5));
    lpad.setLayout(new GridLayout());
    lpad.add(label);
    panel.add(lpad, BorderLayout.NORTH);
    JScrollPane scp = new JScrollPane(c);
    scp.getVerticalScrollBar().setUnitIncrement(16);
    panel.add(scp, BorderLayout.CENTER);
    return panel;
  }

  private JPanel withBorder(JPanel panel, JLabel label, Component c) {
    panel.setLayout(new BorderLayout(0, 0));
    JPanel lpad = new JPanel();
    lpad.setBorder(new EmptyBorder(1, 5, 0, 5));
    lpad.setLayout(new GridLayout());
    lpad.add(label);
    panel.add(lpad, BorderLayout.NORTH);
    JScrollPane scp = new RTextScrollPane(c);
    scp.getVerticalScrollBar().setUnitIncrement(16);
    panel.add(scp, BorderLayout.CENTER);
    return panel;
  }
}
