package me.grax.jbytemod.ui.ifs;

import javax.swing.JInternalFrame;

public class MyInternalFrame extends JInternalFrame {

  public MyInternalFrame(String title) {
    this.setTitle(title);
    this.setMaximizable(true);
    this.setResizable(true);
    this.setClosable(false);
    this.setIconifiable(true);
    getDesktopIcon().updateUI();
    updateUI();
  }

}
