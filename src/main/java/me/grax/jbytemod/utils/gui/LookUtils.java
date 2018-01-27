package me.grax.jbytemod.utils.gui;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import me.grax.jbytemod.JByteMod;

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

  public static void changeLAF(String name) {
    try {
      JByteMod.LOGGER.log("Changing UI to " + name);
      UIManager.setLookAndFeel(name);
      UIManager.getLookAndFeel().uninitialize();
      UIManager.setLookAndFeel(name);
      SwingUtilities.updateComponentTreeUI(JByteMod.instance);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
