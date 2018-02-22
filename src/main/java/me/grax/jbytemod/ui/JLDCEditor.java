package me.grax.jbytemod.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class JLDCEditor extends JDialog {
  private JEditorPane editor;

  public JLDCEditor(String cst) {
    setTitle("LDC Editor");
    setSize(500, 300);
    setLayout(new BorderLayout());
    JPanel top = new JPanel();
    JPanel bottom = new JPanel();
    bottom.setLayout(new BorderLayout());
    top.setLayout(new GridLayout(1, 6));
    top.add(shortcutButton("\\n", "\n"));
    top.add(shortcutButton("\\b", "\b"));
    top.add(shortcutButton("\\t", "\t"));
    top.add(shortcutButton("\\r", "\r"));
    top.add(shortcutButton("\\f", "\f"));
    JButton unicode = new JButton("Hex...");
    unicode.addActionListener(e -> {
      String text = JOptionPane.showInputDialog("");
      if (text != null && !text.isEmpty()) {
        editor.setText(editor.getText() + (char) Integer.parseInt(text, 16));
      }
    });
    JLabel tip = new JLabel(" ");
    bottom.add(tip, BorderLayout.CENTER);
    top.add(unicode);
    add(top, BorderLayout.NORTH);
    add(bottom, BorderLayout.SOUTH);
    editor = new JEditorPane();
    editor.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
    editor.setText(cst);
    editor.addMouseMotionListener(new MouseMotionListener() {

      @Override
      public void mouseMoved(MouseEvent e) {
        int pos = editor.viewToModel(e.getPoint());
        String text = editor.getText();
        if (pos >= text.length()) {
          tip.setText(" ");
          return;
        }
        String val = Integer.toHexString(editor.getText().charAt(pos)).toUpperCase();
        if (val.length() > 4) {
          tip.setText("U+" + val);
        } else {
          tip.setText("U+" + "0000".substring(val.length()) + val);
        }
      }

      @Override
      public void mouseDragged(MouseEvent e) {}
    });
    add(new JScrollPane(editor), BorderLayout.CENTER);
    setModal(true);
  }

  public String getText() {
    return editor.getText();
  }
  private Component shortcutButton(String name, String letter) {
    JButton btn = new JButton(name);
    btn.addActionListener(e -> {
      editor.setText(editor.getText() + letter);
    });
    return btn;
  }
}
