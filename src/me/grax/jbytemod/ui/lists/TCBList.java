package me.grax.jbytemod.ui.lists;

import java.awt.Font;

import javax.swing.DefaultListModel;
import javax.swing.JList;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TryCatchBlockNode;

import me.grax.jbytemod.utils.list.TCBEntry;

public class TCBList extends JList<TCBEntry> {
  
  public void addNodes(ClassNode cn, MethodNode mn) {
    DefaultListModel<TCBEntry> model = new DefaultListModel<>();
    this.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
    for(TryCatchBlockNode tcbn : mn.tryCatchBlocks) {
      model.addElement(new TCBEntry(cn, mn, tcbn));
    }
    this.setModel(model);
  }
}
