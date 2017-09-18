package me.grax.jbytemod.utils.gui;

import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

public class LookUtils {
  public static void setLAF() {
    for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
      if ("Nimbus".equals(info.getName())) {
        try {
          UIManager.setLookAndFeel(info.getClassName());
          break;
        } catch (Exception e) {
          //do nothing and continue
        }
      }
    }
  }
}
