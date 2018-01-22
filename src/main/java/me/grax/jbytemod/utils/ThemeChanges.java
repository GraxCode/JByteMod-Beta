package me.grax.jbytemod.utils;

import java.awt.Color;
import java.awt.Font;
import java.lang.reflect.Field;

import javax.swing.UIDefaults;
import javax.swing.UIManager;

public class ThemeChanges {

  public static void setDefaults() {
    try {
      UIDefaults defaults = UIManager.getLookAndFeelDefaults();
      defaults.put("nimbusOrange", new Color(0xFF01D328));
      Object o = UIManager.get("InternalFrame:InternalFrameTitlePane:\"InternalFrameTitlePane.iconifyButton\"[Enabled].backgroundPainter");
      Class<?> c = o.getClass();
      Field f = c.getDeclaredField("color3");
      f.setAccessible(true);
      f.set(o, new Color(0xFF01D328));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void changeDefaultFont(Font f) {
    UIManager.put("Button.font", f);
    UIManager.put("ToggleButton.font", f);
    UIManager.put("RadioButton.font", f);
    UIManager.put("CheckBox.font", f);
    UIManager.put("ColorChooser.font", f);
    UIManager.put("ComboBox.font", f);
    UIManager.put("Label.font", f);
    UIManager.put("List.font", f);
    UIManager.put("MenuBar.font", f);
    UIManager.put("MenuItem.font", f);
    UIManager.put("RadioButtonMenuItem.font", f);
    UIManager.put("CheckBoxMenuItem.font", f);
    UIManager.put("Menu.font", f);
    UIManager.put("PopupMenu.font", f);
    UIManager.put("OptionPane.font", f);
    UIManager.put("Panel.font", f);
    UIManager.put("ProgressBar.font", f);
    UIManager.put("ScrollPane.font", f);
    UIManager.put("Viewport.font", f);
    UIManager.put("TabbedPane.font", f);
    UIManager.put("Table.font", f);
    UIManager.put("TableHeader.font", f);
    UIManager.put("TextField.font", f);
    UIManager.put("PasswordField.font", f);
    UIManager.put("TextArea.font", f);
    UIManager.put("TextPane.font", f);
    UIManager.put("EditorPane.font", f);
    UIManager.put("TitledBorder.font", f);
    UIManager.put("ToolBar.font", f);
    UIManager.put("ToolTip.font", f);
    UIManager.put("Tree.font", f);
  }
}
