package me.grax.jbytemod.ui;

import java.awt.Toolkit;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JToolBar;

import com.alee.global.StyleConstants;
import com.alee.laf.button.WebButton;

import me.grax.jbytemod.JByteMod;

public class MyToolBar extends JToolBar {
  private MyMenuBar menubar;

  public MyToolBar(JByteMod jbm) {
    this.menubar = (MyMenuBar) jbm.getJMenuBar();
    this.setFloatable(false);
    if (!menubar.isAgent()) {
      this.add(makeNavigationButton(JByteMod.res.getResource("load"), getIcon("load"), e -> {
        menubar.openLoadDialogue();
      }));
      this.add(makeNavigationButton(JByteMod.res.getResource("save"), getIcon("save"), e -> {
        if (menubar.getLastFile() != null) {
          jbm.saveFile(menubar.getLastFile());
        } else {
          menubar.openSaveDialogue();
        }
      }));
    } else {
      this.add(makeNavigationButton(JByteMod.res.getResource("reload"), getIcon("reload"), e -> {
        jbm.refreshAgentClasses();
      }));
      this.add(makeNavigationButton(JByteMod.res.getResource("apply"), getIcon("save"), e -> {
        jbm.applyChangesAgent();
      }));
    }
    this.addSeparator();
    this.add(makeNavigationButton(JByteMod.res.getResource("search"), getIcon("search"), e -> {
      menubar.searchLDC();
    }));
    this.addSeparator();
    this.add(makeNavigationButton("Access Helper", getIcon("table"), e -> {
      new JAccessHelper().setVisible(true);
    }));
    this.add(makeNavigationButton("Attach to other process", getIcon("plug"), e -> {
      menubar.openProcessSelection();
    }));
  }

  private ImageIcon getIcon(String string) {
    return new ImageIcon(Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/resources/toolbar/" + string + ".png")));
  }

  protected JButton makeNavigationButton(String action, ImageIcon i, ActionListener a) {
    JButton button = WebButton.createIconWebButton(i, StyleConstants.smallRound, true);
    button.setToolTipText(action);
    button.addActionListener(a);
    button.setFocusable(false);
    button.setBorderPainted(false);
    button.setRolloverEnabled(false);
    return button;
  }
}