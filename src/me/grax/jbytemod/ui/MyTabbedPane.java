package me.grax.jbytemod.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.fife.ui.rtextarea.RTextScrollPane;
import org.objectweb.asm.tree.ClassNode;

import me.grax.jbytemod.JByteMod;
import me.grax.jbytemod.decompiler.Decompiler;
import me.grax.jbytemod.decompiler.Decompilers;
import me.grax.jbytemod.decompiler.ProcyonDecompiler;
import me.grax.jbytemod.ui.lists.SearchList;

public class MyTabbedPane extends JTabbedPane {
  private JByteMod jbm;
  private DecompilerTab dt;
  private ControlFlowPanel cfp;

  public MyTabbedPane(JByteMod jbm) {
    this.jbm = jbm;
    JLabel editor = new JLabel("Editor");
    MyCodeEditor list = new MyCodeEditor(jbm, editor);
    jbm.setCodeList(list.getEditor());
    this.addTab("Code", this.withBorder(editor, list));
    InfoPanel sp = new InfoPanel(jbm);
    jbm.setSP(sp);
    this.addTab("Info", this.withBorder(new JLabel("Settings"), sp));
    String decompiler = "Decompiler";
    this.dt = new DecompilerTab(jbm);
    this.addTab(decompiler, dt); 
    SearchList searchList = new SearchList(jbm);
    jbm.setSearchlist(searchList);
    JLabel search = new JLabel("Search Results");
    this.addTab("Search", this.withBorder(search, searchList));
    this.cfp = new ControlFlowPanel();
    this.addTab("Analysis", this.withBorder(new JLabel("Control flow visualisation"), cfp)); 
    jbm.setCFP(cfp);
    jbm.setTabbedPane(this);
    ChangeListener changeListener = new ChangeListener() {
      public void stateChanged(ChangeEvent changeEvent) {
        JTabbedPane sourceTabbedPane = (JTabbedPane) changeEvent.getSource();
        int index = sourceTabbedPane.getSelectedIndex();
        if (sourceTabbedPane.getTitleAt(index).equals(decompiler)) {
          dt.decompile(jbm.getCurrentNode(), false);
        }
        if (sourceTabbedPane.getTitleAt(index).equals("Analysis")) {
          cfp.generateList();
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
    if (this.getTitleAt(index).equals("Analysis")) {
      cfp.generateList();
    }
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
