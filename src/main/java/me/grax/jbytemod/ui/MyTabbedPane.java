package me.grax.jbytemod.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.TabbedPaneUI;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import me.grax.jbytemod.JByteMod;
import me.grax.jbytemod.ui.lists.SearchList;

public class MyTabbedPane extends JTabbedPane {
  private MyEditorTab editorTab;

  public MyTabbedPane(JByteMod jbm) {
    this.editorTab = new MyEditorTab(jbm);
    this.addTab("Editor", editorTab);
    SearchList searchList = new SearchList(jbm);
    jbm.setSearchlist(searchList);
    JLabel search = new JLabel(JByteMod.res.getResource("search_results"));
    this.addTab(JByteMod.res.getResource("search"), this.withBorder(search, searchList));
    this.addTab("Opcodes", this.withBorder(new JLabel("Opcodes"), new OpcodeTable()));
    jbm.setTabbedPane(this);
    this.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent me) {
        if (me.getButton() == 3) {
          int tabNr = ((TabbedPaneUI) getUI()).tabForCoordinate(MyTabbedPane.this, me.getX(), me.getY());
          if (tabNr == 0) {
            JPopupMenu menu = new JPopupMenu();
            for (ClassNode cn : JByteMod.lastSelectedTreeEntries.keySet()) {
              String item = cn.name;
              MethodNode mn = JByteMod.lastSelectedTreeEntries.get(cn);
              if (mn != null) {
                item += "." + mn.name;
              }
              if (item.length() > 128) {
                item = "..." + item.substring(item.length() - 128);
              }
              JMenuItem remove = new JMenuItem(item);
              remove.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                  if (mn != null) {
                    jbm.selectMethod(cn, mn);
                  } else {
                    jbm.selectClass(cn);
                  }
                }
              });
              menu.add(remove);
            }
            menu.show(jbm, (int) jbm.getMousePosition().getX(), (int) jbm.getMousePosition().getY());
          }
        }
      }
    });
  }

  public void selectClass(ClassNode cn) {
    this.editorTab.selectClass(cn);
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

  public void selectMethod(ClassNode cn, MethodNode mn) {
    this.editorTab.selectMethod(cn, mn);
  }
  @Override
  public Dimension getMinimumSize() {
    return new Dimension(this.getWidth() / 2, 0);
  }
}
