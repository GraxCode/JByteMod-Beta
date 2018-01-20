package me.grax.jbytemod.ui.ifs;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import org.objectweb.asm.tree.ClassNode;

import me.grax.jbytemod.JByteMod;
import me.grax.jbytemod.ui.JListEditor;
import me.grax.jbytemod.utils.dialogue.EditDialogue;

public class CNSettings extends MyInternalFrame {
  /**
   * Save position
   */
  private static Rectangle bounds = new Rectangle(10, 10, 1280 / 4, 720 / 3 + 30);

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
    panel.add(new JLabel(JByteMod.res.getResource("ref_warn")), "South");
    labels.add(new JLabel("Class Name:"));
    JTextField name = new JTextField(cn.name);
    input.add(name);
    labels.add(new JLabel("Class SourceFile:"));
    JTextField sf = new JTextField(cn.sourceFile);
    input.add(sf);
    labels.add(new JLabel("Class Access:"));
    JFormattedTextField access = EditDialogue.createNumberField();
    access.setValue(cn.access);
    input.add(access);
    labels.add(new JLabel("Class Version:"));
    JFormattedTextField version = EditDialogue.createNumberField();
    version.setValue(cn.version);
    input.add(version);
    labels.add(new JLabel("Class Signature:"));
    JTextField signature = new JTextField(cn.signature);
    input.add(signature);
    labels.add(new JLabel("Class Parent:"));
    JTextField parent = new JTextField(cn.superName);
    input.add(parent);
    labels.add(new JLabel("Class Interfaces:"));
    JButton interfaces = new JButton(JByteMod.res.getResource("edit"));
    interfaces.addActionListener(a -> {
      if(!JListEditor.isOpen())
      new JListEditor("Interfaces", cn, "interfaces").setVisible(true);
    });
    input.add(interfaces);
    this.add(panel, BorderLayout.CENTER);
    JButton update = new JButton("Update");
    update.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        boolean refresh = false;
        if (!cn.name.equals(name.getText())) {
          refresh = true;
          cn.name = name.getText();
        }
        cn.sourceFile = sf.getText();
        cn.access = (int) access.getValue();
        cn.version = (int) version.getValue();
        String sig = signature.getText();
        if (sig.isEmpty()) {
          cn.signature = null;
        } else {
          cn.signature = sig;
        }
        String par = parent.getText();
        if (par.isEmpty()) {
          cn.superName = null;
        } else {
          cn.superName = par;
        }
        if (refresh) {
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
