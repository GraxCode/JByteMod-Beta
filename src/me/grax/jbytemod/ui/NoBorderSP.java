package me.grax.jbytemod.ui;

import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.JScrollPane;

public class NoBorderSP extends JScrollPane {
  public NoBorderSP(Component c) {
    super(c);
    this.setBorder(BorderFactory.createEmptyBorder());
  }
}
