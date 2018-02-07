package me.grax.jbytemod.ui;

import java.awt.BorderLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;

import me.grax.jbytemod.JByteMod;

public class MySplitPane extends JSplitPane {
  private JTabbedPane rightSide;
  private JPanel leftSide;

  public MySplitPane(JByteMod jbm, ClassTree classTree) {
    rightSide = new MyTabbedPane(jbm);
    leftSide = new JPanel();
    leftSide.setLayout(new BorderLayout(0, 0));
    leftSide.add(new JLabel(" " + JByteMod.res.getResource("java_archive")), BorderLayout.NORTH);
    leftSide.add(new JScrollPane(classTree), BorderLayout.CENTER);
    this.setLeftComponent(leftSide);
    this.setRightComponent(rightSide);
    this.setDividerLocation(150);
    this.setContinuousLayout(true);
  }
}
