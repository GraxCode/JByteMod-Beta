package me.grax.jbytemod.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import me.grax.jbytemod.JByteMod;
import me.grax.jbytemod.ui.lists.SearchList;

public class MyTabbedPane extends JTabbedPane {
  private JByteMod jbm;
  private DecompilerTab dt;
  private ControlFlowPanel cfp;
  private boolean classSelected = false;
  private static String analysis = JByteMod.res.getResource("analysis");

  public MyTabbedPane(JByteMod jbm) {
    this.jbm = jbm;
    JLabel editor = new JLabel("Editor");
    MyCodeEditor list = new MyCodeEditor(jbm, editor);
    jbm.setCodeList(list.getEditor());
    this.addTab("Code", this.withBorder(editor, list));
    InfoPanel sp = new InfoPanel(jbm);
    jbm.setSP(sp);
    this.addTab("Info", this.withBorder(new JLabel(JByteMod.res.getResource("settings")), sp));
    String decompiler = "Decompiler";
    this.dt = new DecompilerTab(jbm);
    this.addTab(decompiler, dt);
    SearchList searchList = new SearchList(jbm);
    jbm.setSearchlist(searchList);
    JLabel search = new JLabel(JByteMod.res.getResource("search_results"));
    this.addTab("Search", this.withBorder(search, searchList));
    this.cfp = new ControlFlowPanel();
    this.addTab(analysis, this.withBorder(new JLabel(JByteMod.res.getResource("control_flow_visualisation")), cfp));
    jbm.setCFP(cfp);
    jbm.setTabbedPane(this);
    ChangeListener changeListener = new ChangeListener() {
      public void stateChanged(ChangeEvent changeEvent) {
        JTabbedPane sourceTabbedPane = (JTabbedPane) changeEvent.getSource();
        int index = sourceTabbedPane.getSelectedIndex();
        if (sourceTabbedPane.getTitleAt(index).equals(decompiler)) {
          dt.decompile(jbm.getCurrentNode(), false);
        }
        if (sourceTabbedPane.getTitleAt(index).equals(analysis)) {
          if (!classSelected) {
            cfp.generateList();
          } else {
            cfp.clear();
          }
        }
      }

    };
    this.addChangeListener(changeListener);
  }

  public void selectClass(ClassNode cn) {
    int index = this.getSelectedIndex();
    if (this.getTitleAt(index).equals("Decompiler")) {
      dt.decompile(cn, false);
    }
    if (this.getTitleAt(index).equals(analysis)) {
      cfp.clear();
    }
    this.classSelected = true;
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
    int index = this.getSelectedIndex();
    if (this.getTitleAt(index).equals(analysis)) {
      cfp.generateList();
    }
    this.classSelected = false;
  }
}
