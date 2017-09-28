package me.grax.jbytemod.ui.lists;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodNode;

import me.grax.jbytemod.utils.list.LVPEntry;

public class LVPList extends JList<LVPEntry> {

  public void addNodes(ClassNode cn, MethodNode mn) {
    DefaultListModel<LVPEntry> model = new DefaultListModel<>();
    this.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
    if (mn.localVariables != null)
      for (LocalVariableNode lvn : mn.localVariables) {
        model.addElement(new LVPEntry(cn, mn, lvn));
      }
    this.setModel(model);
    this.addMouseListener(new MouseAdapter() {
      public void mousePressed(MouseEvent e) {
        if (SwingUtilities.isRightMouseButton(e)) {
          JPopupMenu menu = new JPopupMenu();
          JMenuItem remove = new JMenuItem("Remove");
          remove.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
              LVPEntry selected = LVPList.this.getSelectedValue();
              ClassNode cn = selected.getCn();
              MethodNode mn = selected.getMn();
              mn.tryCatchBlocks.remove(selected.getLvn());
              LVPList.this.addNodes(cn, mn);
            }
          });
          menu.add(remove);
          menu.show(LVPList.this, e.getX(), e.getY());
        }
      }
    });
  }
}
