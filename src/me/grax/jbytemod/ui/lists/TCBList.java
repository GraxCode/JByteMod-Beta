package me.grax.jbytemod.ui.lists;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TryCatchBlockNode;

import me.grax.jbytemod.utils.ErrorDisplay;
import me.grax.jbytemod.utils.dialogue.EditDialogue;
import me.grax.jbytemod.utils.dialogue.EditDialogueSpec;
import me.grax.jbytemod.utils.list.TCBEntry;

public class TCBList extends JList<TCBEntry> {

  public void addNodes(ClassNode cn, MethodNode mn) {
    DefaultListModel<TCBEntry> model = new DefaultListModel<>();
    this.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
    for (TryCatchBlockNode tcbn : mn.tryCatchBlocks) {
      model.addElement(new TCBEntry(cn, mn, tcbn));
    }
    this.setModel(model);
    this.addMouseListener(new MouseAdapter() {
      public void mousePressed(MouseEvent e) {
        if (SwingUtilities.isRightMouseButton(e)) {
          TCBEntry selected = TCBList.this.getSelectedValue();
          JPopupMenu menu = new JPopupMenu();
          if (selected != null) {
            JMenuItem remove = new JMenuItem("Remove");
            remove.addActionListener(new ActionListener() {
              public void actionPerformed(ActionEvent e) {
                ClassNode cn = selected.getCn();
                MethodNode mn = selected.getMn();
                mn.tryCatchBlocks.remove(selected.getTcbn());
                TCBList.this.addNodes(cn, mn);
              }
            });
            menu.add(remove);
            JMenuItem edit = new JMenuItem("Edit");
            edit.addActionListener(new ActionListener() {
              public void actionPerformed(ActionEvent e) {
                try {
                  EditDialogueSpec.createInsertDialogue(mn, selected.getTcbn());
                } catch (Exception ex) {
                  new ErrorDisplay(ex);
                }
                TCBList.this.addNodes(cn, mn);
              }
            });
            menu.add(edit);
          }
          JMenuItem insert = new JMenuItem("Insert");
          insert.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
              try {
                TryCatchBlockNode tcbn = new TryCatchBlockNode(null, null, null, "");
                if (EditDialogueSpec.createInsertDialogue(mn, tcbn))
                  if (tcbn.handler != null && tcbn.start != null && tcbn.end != null) {
                    mn.tryCatchBlocks.add(tcbn);
                  }
              } catch (Exception ex) {
                new ErrorDisplay(ex);
              }
              TCBList.this.addNodes(cn, mn);
            }
          });
          menu.add(insert);
          menu.show(TCBList.this, e.getX(), e.getY());
        }
      }
    });
  }
}
