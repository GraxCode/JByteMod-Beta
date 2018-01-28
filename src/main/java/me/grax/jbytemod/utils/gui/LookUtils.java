package me.grax.jbytemod.utils.gui;

import javax.swing.UIManager;

import com.alee.laf.WebLookAndFeel;

import me.grax.jbytemod.JByteMod;
import me.grax.jbytemod.utils.ThemeChanges;

public class LookUtils {
  public static void setLAF() {
    try {
      JByteMod.LOGGER.log("Setting default Look and Feel");
      if (JByteMod.ops.get("use_weblaf").getBoolean()) {
        WebLookAndFeel.install();
      } else {
        if (!changeLAF("javax.swing.plaf.nimbus.NimbusLookAndFeel")) {
          JByteMod.LOGGER.err("Failed to set Nimbus Look and Feel, trying to use WebLaF");
          WebLookAndFeel.install();
        }
      }
    } catch (Throwable t) {
      t.printStackTrace();
      JByteMod.LOGGER.err("Failed to set Look and Feel");
    }
  }

  public static boolean changeLAF(String name) {
    try {
      JByteMod.LOGGER.log("Changing UI to " + name);
      UIManager.setLookAndFeel(name);
      UIManager.getLookAndFeel().uninitialize();
      UIManager.setLookAndFeel(name);
      if (name.equals("javax.swing.plaf.nimbus.NimbusLookAndFeel")) {
        ThemeChanges.setDefaults();
      }
      if (JByteMod.instance != null) {
        JByteMod.restartGUI();
      }
      return true;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return false;
  }
}
