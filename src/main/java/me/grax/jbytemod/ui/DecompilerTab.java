package me.grax.jbytemod.ui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;

import org.fife.ui.rtextarea.RTextScrollPane;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import me.grax.jbytemod.JByteMod;
import me.grax.jbytemod.decompiler.CFRDecompiler;
import me.grax.jbytemod.decompiler.Decompiler;
import me.grax.jbytemod.decompiler.Decompilers;
import me.grax.jbytemod.decompiler.FernflowerDecompiler;
import me.grax.jbytemod.decompiler.KrakatauDecompiler;
import me.grax.jbytemod.decompiler.ProcyonDecompiler;

public class DecompilerTab extends JPanel {
  private DecompilerPanel dp;
  private JLabel label;
  protected Decompilers decompiler = Decompilers.PROCYON; //TODO setting
  private JByteMod jbm;

  public DecompilerTab(JByteMod jbm) {
    this.jbm = jbm;
    this.dp = new DecompilerPanel();
    this.label = new JLabel(decompiler + " Decompiler");
    jbm.setDP(dp);
    this.setLayout(new BorderLayout(0, 0));
    JPanel lpad = new JPanel();
    lpad.setBorder(new EmptyBorder(1, 5, 0, 1));
    lpad.setLayout(new GridLayout());
    lpad.add(label);
    JPanel rs = new JPanel();
    rs.setLayout(new GridLayout(1, 5));
    for (int i = 0; i < 3; i++)
      rs.add(new JPanel());
    JComboBox<Decompilers> decompilerCombo = new JComboBox<Decompilers>(Decompilers.values());
    decompilerCombo.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent e) {
        DecompilerTab.this.decompiler = (Decompilers) decompilerCombo.getSelectedItem();
        label.setText(decompiler.getName() + " " + decompiler.getVersion());
        decompile(Decompiler.last, Decompiler.lastMn, true);
      }
    });
    rs.add(decompilerCombo);
    JButton reload = new JButton(JByteMod.res.getResource("reload"));
    reload.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        decompile(Decompiler.last, Decompiler.lastMn, true);
      }
    });
    rs.add(reload);
    lpad.add(rs);
    this.add(lpad, BorderLayout.NORTH);
    JScrollPane scp = new RTextScrollPane(dp);
    scp.getVerticalScrollBar().setUnitIncrement(16);
    this.add(scp, BorderLayout.CENTER);
  }

  public void decompile(ClassNode cn, MethodNode mn, boolean deleteCache) {
    if (cn == null) {
      return;
    }
    Decompiler d = null;
    switch (decompiler) {
    case PROCYON:
      d = new ProcyonDecompiler(jbm, dp);
      break;
    case FERNFLOWER:
      d = new FernflowerDecompiler(jbm, dp);
      break;
    case CFR:
      d = new CFRDecompiler(jbm, dp);
      break;
    case KRAKATAU:
      d = new KrakatauDecompiler(jbm, dp);
      break;
    }
    d.setNode(cn, mn);
    if (deleteCache) {
      d.deleteCache();
    }
    d.start();
  }
}
