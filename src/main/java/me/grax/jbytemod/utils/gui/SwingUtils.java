package me.grax.jbytemod.utils.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;

public class SwingUtils {
  public static JPanel withButton(Component c, String text, ActionListener e) {
    JPanel jp = new JPanel();
    jp.setLayout(new BorderLayout());
    jp.add(c, BorderLayout.CENTER);
    JButton help = new JButton(text);
    help.addActionListener(e);
    jp.add(help, BorderLayout.EAST);
    return jp;
  }
}
