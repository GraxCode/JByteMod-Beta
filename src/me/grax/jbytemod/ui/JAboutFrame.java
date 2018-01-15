package me.grax.jbytemod.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.UIDefaults;
import javax.swing.border.EmptyBorder;

import me.grax.jbytemod.JByteMod;
import me.grax.jbytemod.utils.TextUtils;

public class JAboutFrame extends JDialog {

  public JAboutFrame(JByteMod jbm) {
    this.setTitle(JByteMod.res.getResource("about") + " " + jbm.getTitle());
    this.setModal(true);
    setBounds(100, 100, 400, 300);
    JPanel cp = new JPanel();
    cp.setLayout(new BorderLayout());
    cp.setBorder(new EmptyBorder(10, 10, 10, 10));
    setResizable(false);
    JButton close = new JButton("Close");
    close.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        JAboutFrame.this.dispose();
      }
    });
    JPanel jp = new JPanel(new GridLayout(1, 4));
    for (int i = 0; i < 3; i++)
      jp.add(new JPanel());

    jp.add(close);
    cp.add(jp, BorderLayout.PAGE_END);

    JTextPane title = new JTextPane();
    title.setContentType("text/html");
    title.setText(TextUtils.toHtml(jbm.getTitle()
        + "<br/>Copyright © 2016-2018 noverify<br/><font color=\"#0000EE\"><u>https://github.com/GraxCode/JByteMod-Beta</u></font><br/>Donate LTC: <font color=\"#333333\">LhwXLVASzb6t4vHSssA9FQwq2X5gAg8EKX</font>"));
    Color bgColor = new Color(0xd6d9df);
    UIDefaults defaults = new UIDefaults();
    defaults.put("TextPane[Enabled].backgroundPainter", bgColor);
    title.putClientProperty("Nimbus.Overrides", defaults);
    title.putClientProperty("Nimbus.Overrides.InheritDefaults", true);
    title.setBackground(bgColor);
    title.setEditable(false);
    title.setBorder(null);
    cp.add(title, BorderLayout.CENTER);

    getContentPane().add(cp);
  }
}
