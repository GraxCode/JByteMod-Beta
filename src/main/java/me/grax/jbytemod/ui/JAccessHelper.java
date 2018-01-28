package me.grax.jbytemod.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Field;

import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.objectweb.asm.Opcodes;

public class JAccessHelper extends JDialog {
  private JTextField jtf;

  public JAccessHelper() {
    setBounds(100, 100, 420, 220);
    setResizable(false);
    setTitle("Access Helper");
    JPanel cp = new JPanel();
    cp.setLayout(new BorderLayout(10, 10));
    JPanel access = new JPanel();
    access.setLayout(new GridLayout(5, 4, 5, 5));
    try {
      for (Field d : Opcodes.class.getDeclaredFields()) {
        if (d.getName().startsWith("ACC_")) {
          int acc = d.getInt(null);

          JCheckBox jcb = new JAccCheckBox(d.getName().substring(4).toLowerCase(), acc);
          access.add(jcb);
          jcb.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
              int acc = 0;
              for (Component c : access.getComponents()) {
                JAccCheckBox jacb = (JAccCheckBox) c;
                if (jacb.isSelected())
                  acc |= jacb.getAccess();
              }
              jtf.setText(String.valueOf(acc));
            }
          });
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    cp.add(access, BorderLayout.CENTER);
    jtf = new JTextField();
    jtf.setEditable(false);
    jtf.setHorizontalAlignment(JTextField.CENTER);
    jtf.setPreferredSize(new Dimension(60, 30));
    JPanel jpn = new JPanel();
    jpn.setLayout(new FlowLayout());
    jpn.add(jtf);
    cp.add(jpn, BorderLayout.SOUTH);
    this.add(cp);
    setAlwaysOnTop(true);
  }

  public JAccessHelper(Object owner, String field, JFormattedTextField tf) {
    try {
      Field f = owner.getClass().getDeclaredField(field);
      int accezz = f.getInt(owner);
      setBounds(100, 100, 420, 220);
      setResizable(false);
      setTitle("Access (" + owner.getClass().getSimpleName() + ")");
      JPanel cp = new JPanel();
      cp.setLayout(new BorderLayout(10, 10));
      JPanel access = new JPanel();
      access.setLayout(new GridLayout(5, 4, 5, 5));
      for (Field d : Opcodes.class.getDeclaredFields()) {
        if (d.getName().startsWith("ACC_")) {
          int acc = d.getInt(null);
          JCheckBox jcb = new JAccCheckBox(d.getName().substring(4).toLowerCase(), acc);
          access.add(jcb);
          jcb.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
              int acc = 0;
              for (Component c : access.getComponents()) {
                JAccCheckBox jacb = (JAccCheckBox) c;
                if (jacb.isSelected())
                  acc |= jacb.getAccess();
              }
              jtf.setText(String.valueOf(acc));
              try {
                f.setInt(owner, acc);
                if (tf != null) {
                  tf.setValue(acc);
                }
              } catch (Exception e1) {
                e1.printStackTrace();
              }
            }
          });
          if ((accezz & acc) != 0) {
            jcb.setSelected(true);
          }
        }
      }
      cp.add(access, BorderLayout.CENTER);
      jtf = new JTextField();
      jtf.setEditable(false);
      jtf.setHorizontalAlignment(JTextField.CENTER);
      jtf.setPreferredSize(new Dimension(60, 30));
      jtf.setText(String.valueOf(accezz));
      JPanel jpn = new JPanel();
      jpn.add(jtf);
      cp.add(jpn, BorderLayout.SOUTH);
      this.add(cp);
      setAlwaysOnTop(true);
      setModal(true);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public JAccessHelper(int accezz, JFormattedTextField tf) {
    try {
      setBounds(100, 100, 420, 220);
      setResizable(false);
      setTitle("Access Helper");
      JPanel cp = new JPanel();
      cp.setLayout(new BorderLayout(10, 10));
      JPanel access = new JPanel();
      access.setLayout(new GridLayout(5, 4, 5, 5));
      for (Field d : Opcodes.class.getDeclaredFields()) {
        if (d.getName().startsWith("ACC_")) {
          int acc = d.getInt(null);
          JCheckBox jcb = new JAccCheckBox(d.getName().substring(4).toLowerCase(), acc);
          access.add(jcb);
          jcb.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
              int acc = 0;
              for (Component c : access.getComponents()) {
                JAccCheckBox jacb = (JAccCheckBox) c;
                if (jacb.isSelected())
                  acc |= jacb.getAccess();
              }
              jtf.setText(String.valueOf(acc));
              if (tf != null) {
                tf.setValue(acc);
              }
            }
          });
          if ((accezz & acc) != 0) {
            jcb.setSelected(true);
          }
        }
      }
      cp.add(access, BorderLayout.CENTER);
      jtf = new JTextField();
      jtf.setEditable(false);
      jtf.setHorizontalAlignment(JTextField.CENTER);
      jtf.setPreferredSize(new Dimension(60, 30));
      jtf.setText(String.valueOf(accezz));
      JPanel jpn = new JPanel();
      jpn.add(jtf);
      cp.add(jpn, BorderLayout.SOUTH);
      this.add(cp);
      setAlwaysOnTop(true);
      setModal(true);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  public void setVisible(boolean b) {
    super.setVisible(b);
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        requestFocusInWindow();
      }
    });
  }

  public static void main(String[] args) {
    new JAccessHelper().setVisible(true);
  }

  private class JAccCheckBox extends JCheckBox {
    private int access;

    public JAccCheckBox(String name, int access) {
      super(name);
      this.access = access;
    }

    public int getAccess() {
      return access;
    }

  }
}
