package me.grax.jbytemod.ui.lists;

import java.awt.Font;

import javax.swing.DefaultListModel;
import javax.swing.JList;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TryCatchBlockNode;

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
  }
}
