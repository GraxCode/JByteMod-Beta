package me.grax.jbytemod.utils.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.plaf.ListUI;

import com.alee.laf.list.WebListUI;

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

  public static void disableRollover(JList<?> jl) {
    ListUI ui = (ListUI) jl.getUI();
    if(ui instanceof WebListUI) {
      WebListUI wlui = (WebListUI) ui;
      wlui.setHighlightRolloverCell(false);
    }
    
  }
}
