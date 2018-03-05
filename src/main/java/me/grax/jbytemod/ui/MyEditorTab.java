package me.grax.jbytemod.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import com.alee.extended.breadcrumb.WebBreadcrumb;
import com.alee.extended.breadcrumb.WebBreadcrumbToggleButton;
import com.alee.utils.SwingUtils;

import me.grax.jbytemod.JByteMod;

public class MyEditorTab extends JPanel {
  private MyCodeEditor codeEditor;
  private JLabel label;

  private JPanel code, info;
  private DecompilerTab decompiler;
  private ControlFlowPanel analysis;
  private JPanel center;
  private WebBreadcrumbToggleButton decompilerBtn;
  private WebBreadcrumbToggleButton analysisBtn;
  
  private boolean classSelected = false;
  
  private static String analysisText = JByteMod.res.getResource("analysis");

  public MyEditorTab(JByteMod jbm) {
    setLayout(new BorderLayout());
    this.center = new JPanel();
    center.setLayout(new GridLayout());
    this.label = new JLabel("Test");

    this.codeEditor = new MyCodeEditor(jbm, label);
    jbm.setCodeList(codeEditor.getEditor());
    this.code = withBorder(label, codeEditor);

    InfoPanel sp = new InfoPanel(jbm);
    jbm.setSP(sp);

    this.info = this.withBorder(new JLabel(JByteMod.res.getResource("settings")), sp);

    this.decompiler = new DecompilerTab(jbm);

    jbm.setCFP(this.analysis = new ControlFlowPanel());

    center.add(code);

    WebBreadcrumb selector = new WebBreadcrumb(true);
    WebBreadcrumbToggleButton codeBtn = new WebBreadcrumbToggleButton("Code");
    codeBtn.setSelected(true);
    codeBtn.addActionListener(e -> {
      center.removeAll();
      center.add(code);
      center.revalidate();
      repaint();
    });
    WebBreadcrumbToggleButton infoBtn = new WebBreadcrumbToggleButton("Info");
    infoBtn.addActionListener(e -> {
      center.removeAll();
      center.add(info);
      center.revalidate();
      repaint();
    });
    decompilerBtn = new WebBreadcrumbToggleButton("Decompiler");
    decompilerBtn.addActionListener(e -> {
      center.removeAll();
      center.add(decompiler);
      center.revalidate();
      repaint();
      decompiler.decompile(jbm.getCurrentNode(), false);
    });
    analysisBtn = new WebBreadcrumbToggleButton(analysisText);
    analysisBtn.addActionListener(e -> {
      center.removeAll();
      center.add(analysis);
      center.revalidate();
      repaint();
      if (!classSelected) {
        analysis.generateList();
      } else {
        analysis.clear();
      }
    });
    selector.add(codeBtn);
    selector.add(infoBtn);
    selector.add(decompilerBtn);
    selector.add(analysisBtn);
    SwingUtils.groupButtons(selector);
    JPanel south = new JPanel();
    south.setLayout(new FlowLayout(FlowLayout.LEFT));
    south.add(selector);
    this.add(center, BorderLayout.CENTER);
    this.add(south, BorderLayout.PAGE_END);
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

  public void selectClass(ClassNode cn) {
    if (decompilerBtn.isSelected()) {
      decompiler.decompile(cn, false);
    }
    if (analysisBtn.isSelected()) {
      analysis.clear();
    }
    this.classSelected = true;
  }

  public void selectMethod(ClassNode cn, MethodNode mn) {
    if (decompilerBtn.isSelected()) {
      decompiler.decompile(cn, false);
    }
    if (analysisBtn.isSelected()) {
      analysis.generateList();
    }
    this.classSelected = false;
  }
}
