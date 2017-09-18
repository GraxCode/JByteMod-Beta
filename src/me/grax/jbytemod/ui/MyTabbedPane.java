package me.grax.jbytemod.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.border.EmptyBorder;

import me.grax.jbytemod.JByteMod;
import me.grax.jbytemod.ui.lists.SearchList;

public class MyTabbedPane extends JTabbedPane {
  public MyTabbedPane(JByteMod jam) {
    JLabel editor = new JLabel("Editor");
    MyCodeEditor list = new MyCodeEditor(jam, editor);
    jam.setCodeList(list.getEditor());
    this.addTab("Bytecode", this.withBorder(editor, list));
    SearchList searchList = new SearchList(jam);
    jam.setSearchlist(searchList);
    JLabel search = new JLabel("Search Results");
    this.addTab("Search", this.withBorder(search, searchList));
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
}
