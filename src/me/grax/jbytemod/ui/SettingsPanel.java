package me.grax.jbytemod.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.DefaultDesktopManager;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import me.grax.jbytemod.JByteMod;
import me.grax.jbytemod.ui.ifs.CNSettings;
import me.grax.jbytemod.ui.ifs.LVPFrame;
import me.grax.jbytemod.ui.ifs.MNSettings;
import me.grax.jbytemod.ui.ifs.MyInternalFrame;
import me.grax.jbytemod.ui.ifs.TCBFrame;

public class SettingsPanel extends JPanel {

  private JDesktopPane deskPane;
  private JByteMod jbm;

  public SettingsPanel(JByteMod jbm) {
    this.jbm = jbm;
    this.setLayout(new BorderLayout());
    deskPane = new JDesktopPane() {
      Color bg = new Color(0xd6d9df);

      @Override
      protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(bg);
        g.fillRect(0, 0, getWidth(), getHeight());
      }
    };
//    JInternalFrame inFrame1 = new MyInternalFrame("ts");
//    deskPane.add(inFrame1);
    deskPane.setDesktopManager(new DeskMan());
    this.add(deskPane, BorderLayout.CENTER);
  }

  @SuppressWarnings("deprecation")
  public void selectMethod(ClassNode cn, MethodNode mn) {
    for(Component c : deskPane.getComponents()) {
      if(c instanceof MyInternalFrame) {
        c.setVisible(false);
      }
    }
    deskPane.removeAll();
    deskPane.add(new TCBFrame(jbm.getTCBList()));
    deskPane.add(new LVPFrame(jbm.getLVPList()));
    deskPane.add(new MNSettings(mn));
    
    this.repaint();
  }

  public void selectClass(ClassNode cn) {
    for(Component c : deskPane.getComponents()) {
      if(c instanceof MyInternalFrame) {
        c.setVisible(false);
      }
    }
    deskPane.removeAll();
    deskPane.add(new CNSettings(cn));
    this.repaint();
  }

  class DeskMan extends DefaultDesktopManager {

    @Override
    public void beginDraggingFrame(JComponent f) {
    }

    @Override
    public void beginResizingFrame(JComponent f, int direction) {
    }

    @Override
    public void setBoundsForFrame(JComponent f, int newX, int newY, int newWidth, int newHeight) {
      if (!(f instanceof JInternalFrame)) {
        return;
      }
      boolean didResize = (f.getWidth() != newWidth || f.getHeight() != newHeight);
      if (!inBounds((JInternalFrame) f, newX, newY, newWidth, newHeight)) {
        Container parent = f.getParent();
        Dimension parentSize = parent.getSize();
        int boundedX = (int) Math.min(Math.max(0, newX), parentSize.getWidth() - newWidth);
        int boundedY = (int) Math.min(Math.max(0, newY), parentSize.getHeight() - newHeight);
        f.setBounds(boundedX, boundedY, newWidth, newHeight);
      } else {
        f.setBounds(newX, newY, newWidth, newHeight);
      }
      if (didResize) {
        f.validate();
      }
    }

    protected boolean inBounds(JInternalFrame f, int newX, int newY, int newWidth, int newHeight) {
      if (newX < 0 || newY < 0)
        return false;
      if (newX + newWidth > f.getDesktopPane().getWidth())
        return false;
      if (newY + newHeight > f.getDesktopPane().getHeight())
        return false;
      return true;
    }
  }
}
