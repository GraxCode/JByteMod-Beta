package me.grax.jbytemod.utils.gui;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import com.alee.laf.WebLookAndFeel;

import me.grax.jbytemod.JByteMod;
import me.grax.jbytemod.utils.ThemeChanges;

public class LookUtils {
  public static void setLAF() {
    if (JByteMod.ops.get("use_weblaf").getBoolean()) {
      WebLookAndFeel.install();
    } else {
      for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
        if ("Nimbus".equals(info.getName())) {
          try {
            UIManager.setLookAndFeel(info.getClassName());
            ThemeChanges.setDefaults();
            break;
          } catch (Exception e) {
            //do nothing and continue
          }
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
