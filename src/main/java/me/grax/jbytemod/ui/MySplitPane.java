package me.grax.jbytemod.ui;

import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.border.LineBorder;

import me.grax.jbytemod.JByteMod;

public class MySplitPane extends JSplitPane {
  private JTabbedPane rightSide;
  private JPanel leftSide;

  public MySplitPane(JByteMod jam, ClassTree dexTree) {
    rightSide = new MyTabbedPane(jam);
    leftSide = new JPanel();
    leftSide.setLayout(new BorderLayout(0, 0));
    leftSide.add(new JLabel(" Java Archive"), BorderLayout.NORTH);
    leftSide.add(new JScrollPane(dexTree), BorderLayout.CENTER);
    JPanel border = new JPanel();
    border.setBorder(new LineBorder(JByteMod.border));
    border.setLayout(new GridLayout());
    this.setLeftComponent(leftSide);
    this.setRightComponent(rightSide);
    this.setDividerLocation(150);
    this.setContinuousLayout(true);
  }
}
