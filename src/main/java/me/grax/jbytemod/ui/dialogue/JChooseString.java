package me.grax.jbytemod.ui.dialogue;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;

public class JChooseString extends JDialog {
  private JButton btn;
  private String selected;

  public JChooseString(String title, ArrayList<String> list) {
    setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    setBounds(100, 100, 450, 300);
    setTitle(title);
    setModalityType(ModalityType.APPLICATION_MODAL);
    JPanel contentPane = new JPanel();
    contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
    contentPane.setLayout(new BorderLayout(0, 0));
    JList<String> jlist = new JList<>(list.toArray(new String[0]));
    jlist.addListSelectionListener(l -> {
      btn.setEnabled(true);
    });
    JScrollPane scrollPane = new JScrollPane(jlist);
    contentPane.add(scrollPane, BorderLayout.CENTER);

    JPanel bpanel = new JPanel(new GridLayout(1, 6));
    bpanel.setEnabled(false);
    btn = new JButton("Select");
    btn.setEnabled(false);
    btn.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        selected = jlist.getSelectedValue();
        dispose();
      }
    });
    int i = 0;
    for (i = 0; i < 5; i++) {
      bpanel.add(new JPanel());
    }
    bpanel.add(btn);
    contentPane.add(bpanel, BorderLayout.SOUTH);
    setContentPane(contentPane);
    setModal(true);
    setAlwaysOnTop(true);
    setVisible(true);
  }

  public String getSelected() {
    return selected;
  }
  
}
