package me.grax.jbytemod.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.border.EmptyBorder;

import org.fife.ui.rtextarea.RTextScrollPane;

import me.grax.jbytemod.JByteMod;
import me.grax.jbytemod.decompiler.DecompileThread;
import me.grax.jbytemod.ui.lists.SearchList;
import me.grax.jbytemod.ui.lists.TCBList;

public class MyTabbedPane extends JTabbedPane {
  private JByteMod jbm;

  public MyTabbedPane(JByteMod jam) {
    this.jbm = jam;
    JLabel editor = new JLabel("Editor");
    MyCodeEditor list = new MyCodeEditor(jam, editor);
    jam.setCodeList(list.getEditor());
    this.addTab("Code", this.withBorder(editor, list));
    TCBList tcb = new TCBList();
    jam.setTCBList(tcb);
    this.addTab("TCB", this.withBorder(editor, tcb));
    DecompilerPanel dp = new DecompilerPanel();
    jam.setDP(dp);
    this.addTab("Decompiler", this.withBorder(new JPanel(), new JLabel("Procyon Decompiler"), dp));
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

  private JPanel withBorder(JPanel panel, JLabel label, DecompilerPanel c) {
    panel.setLayout(new BorderLayout(0, 0));
    JPanel lpad = new JPanel();
    lpad.setBorder(new EmptyBorder(1, 5, 0, 1));
    lpad.setLayout(new GridLayout());
    lpad.add(label);
    JPanel rs = new JPanel();
    rs.setLayout(new GridLayout(1, 5));
    for(int i = 0; i< 4; i++) rs.add(new JPanel());
    JButton reload = new JButton("Reload");
    reload.addActionListener(new ActionListener() {
      
      @Override
      public void actionPerformed(ActionEvent e) {
       DecompileThread t = new DecompileThread(jbm, DecompileThread.last, c);
       //do not load cache
       DecompileThread.last = null;
       t.start();
      }
    });
    rs.add(reload);
    lpad.add(rs);
    panel.add(lpad, BorderLayout.NORTH);
    JScrollPane scp = new RTextScrollPane(c);
    scp.getVerticalScrollBar().setUnitIncrement(16);
    panel.add(scp, BorderLayout.CENTER);
    return panel;
  }
}
