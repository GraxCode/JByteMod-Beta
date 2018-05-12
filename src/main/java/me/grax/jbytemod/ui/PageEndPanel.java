package me.grax.jbytemod.ui;

import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import com.alee.extended.statusbar.WebMemoryBar;

public class PageEndPanel extends JPanel {

  private JProgressBar pb;
  private JLabel percent;
  private JLabel label;
  private WebMemoryBar memoryBar;
  private static final String copyright = "\u00A9 GraxCode 2016 - 2018";

  public PageEndPanel() {
    this.pb = new JProgressBar() {
      @Override
      public void setValue(int n) {
        if (n == 100) {
          super.setValue(0);
          percent.setText("");
        } else {
          super.setValue(n);
          percent.setText(n + "%");
        }
      }
    };
    this.setLayout(new GridLayout(1, 4, 10, 10));
    this.setBorder(new EmptyBorder(3, 0, 0, 0));
    this.add(pb);
    this.add(percent = new JLabel());
    percent.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
    label = new JLabel(copyright);
    label.setHorizontalAlignment(SwingConstants.RIGHT);
    label.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
    this.add(label);
    memoryBar = new WebMemoryBar();
    memoryBar.setShowMaximumMemory(false);
    this.add(memoryBar);

  }

  public void setValue(int n) {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        pb.setValue(n);
        pb.repaint();
      }
    });
  }

  public void setTip(String s) {
    if (s != null) {
      label.setText(s);
    } else {
      label.setText(copyright);
    }
  }
}
