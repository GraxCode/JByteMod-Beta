package me.grax.jbytemod.ui;

import java.awt.Font;
import java.io.IOException;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.Theme;

import me.grax.jbytemod.utils.ErrorDisplay;

public class DecompilerPanel extends RSyntaxTextArea {

  public DecompilerPanel() {
    this.setSyntaxEditingStyle("text/java");
    this.setCodeFoldingEnabled(true);
    this.setAntiAliasingEnabled(true);
    this.setFont(new Font("Monospaced", Font.PLAIN, 12));
    this.setEditable(false);
    //change theme to java 
    try {
      Theme theme = Theme.load(getClass().getResourceAsStream("/org/fife/ui/rsyntaxtextarea/themes/idea.xml"));
      theme.apply(this);
    } catch (IOException e1) {
      new ErrorDisplay(e1);
    }
  }
}
