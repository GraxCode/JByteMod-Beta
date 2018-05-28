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
import org.objectweb.asm.tree.MethodNode;

import me.grax.jbytemod.JByteMod;
import me.grax.jbytemod.ui.JAccessHelper;
import me.grax.jbytemod.ui.JAnnotationEditor;
import me.grax.jbytemod.ui.dialogue.ClassDialogue;
import me.grax.jbytemod.utils.gui.SwingUtils;

public class MNSettings extends MyInternalFrame {
  /**
   * Save position
   */
  private static Rectangle bounds = new Rectangle(670, 10, 1280 / 4, 720 / 3 + 80);

  public MNSettings(ClassNode cn, MethodNode mn) {
    super("Method Settings");
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
    labels.add(new JLabel("Method Name:"));
    JTextField name = new JTextField(mn.name);
    input.add(name);
    labels.add(new JLabel("Method Desc:"));
    JTextField desc = new JTextField(mn.desc);
    input.add(desc);
    labels.add(new JLabel("Method Access:"));
    JFormattedTextField access = ClassDialogue.createNumberField();
    access.setValue(mn.access);
    input.add(SwingUtils.withButton(access, "...", e -> {
      JAccessHelper jah = new JAccessHelper(mn, "access", access);
      jah.setVisible(true);
    }));
    labels.add(new JLabel("Method Signature:"));
    JTextField signature = new JTextField(mn.signature);
    input.add(signature);
    labels.add(new JLabel("Method MaxLocals:"));
    JFormattedTextField maxL = ClassDialogue.createNumberField();
    maxL.setValue(mn.maxLocals);
    input.add(maxL);
    labels.add(new JLabel("Method MaxStack:"));
    JFormattedTextField maxS = ClassDialogue.createNumberField();
    maxS.setValue(mn.maxStack);
    input.add(maxS);
    labels.add(new JLabel("Annotations:"));
    JButton annotations = new JButton(JByteMod.res.getResource("edit"));
    annotations.addActionListener(a -> {
      if (!JAnnotationEditor.isOpen("visibleAnnotations"))
        new JAnnotationEditor("Annotations", mn, "visibleAnnotations").setVisible(true);
    });
    input.add(annotations);
    labels.add(new JLabel("Invis Annotations:"));
    JButton invisAnnotations = new JButton(JByteMod.res.getResource("edit"));
    invisAnnotations.addActionListener(a -> {
      if (!JAnnotationEditor.isOpen("invisibleAnnotations"))
        new JAnnotationEditor("Invis Annotations", mn, "invisibleAnnotations").setVisible(true);
    });
    input.add(invisAnnotations);
    this.add(panel, BorderLayout.CENTER);
    JButton update = new JButton("Update");
    update.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        boolean refresh = false;
        if (!mn.name.equals(name.getText())) {
          refresh = true;
          mn.name = name.getText();
        }
        mn.desc = desc.getText();
        mn.access = (int) access.getValue();
        mn.maxLocals = (int) maxL.getValue();
        mn.maxStack = (int) maxS.getValue();
        String sig = signature.getText();
        if (sig.isEmpty()) {
          mn.signature = null;
        } else {
          mn.signature = sig;
        }
        if (refresh) {
          JByteMod.instance.getJarTree().refreshMethod(cn, mn);
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
