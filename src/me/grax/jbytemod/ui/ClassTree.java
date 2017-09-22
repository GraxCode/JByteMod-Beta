package me.grax.jbytemod.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import me.grax.jbytemod.JByteMod;
import me.grax.jbytemod.JarFile;
import me.grax.jbytemod.utils.MethodUtils;
import me.grax.jbytemod.utils.dialogue.EditDialogue;
import me.grax.jbytemod.utils.gui.CellRenderer;
import me.grax.jbytemod.utils.tree.SortedTreeNode;

public class ClassTree extends JTree {

  private JByteMod jbm;

  public ClassTree(JByteMod jam) {
    this.jbm = jam;
    this.setRootVisible(false);
    this.setShowsRootHandles(true);
    this.setCellRenderer(new CellRenderer());
    this.addTreeSelectionListener(new TreeSelectionListener() {
      public void valueChanged(TreeSelectionEvent e) {
        SortedTreeNode node = (SortedTreeNode) ClassTree.this.getLastSelectedPathComponent();
        if (node == null)
          return;
        if (node.getCn() != null && node.getMn() != null) {
          jam.selectMethod(node.getCn(), node.getMn());
        } else if (node.getCn() != null) {
          jam.selectClass(node.getCn());
        } else {
          ClassTree.this.clearSelection();
          if (node.isLeaf()) {
            return;
          }
          if (ClassTree.this.isExpanded(e.getPath())) {
            ClassTree.this.collapsePath(e.getPath());
          } else {
            ClassTree.this.expandPath(e.getPath());
          }
        }
      }
    });
    this.setModel(new DefaultTreeModel(new SortedTreeNode("", null, null)));
    //TODO Transfer Handler
  }

  public void refreshTree(JarFile jar) {
    
    DefaultTreeModel tm = (DefaultTreeModel) this.getModel();
    SortedTreeNode root = (SortedTreeNode) tm.getRoot();
    root.removeAllChildren();
    tm.reload();

    for (ClassNode c : jar.getClasses().values()) {
      for (MethodNode m : c.methods) {
        String name = c.name + ".class/" + m.name;
        if (name.isEmpty())
          continue;
        if (!name.contains("/")) {
          root.add(new SortedTreeNode(name, c, m));
        } else {
          String[] names = name.split("/");
          SortedTreeNode node = root;
          int i = 1;
          for (String n : names) {
            SortedTreeNode newnode = new SortedTreeNode(n, i >= names.length - 1 ? c : null, null);
            if (i == names.length) {
              newnode.setMn(m);
              node.add(newnode);
              tm.getChildCount(node);
            } else {
              SortedTreeNode extnode = addUniqueNode(tm, node, newnode);
              if (extnode != null) {
                node = extnode;
              } else {
                node = newnode;
              }
            }
            i++;
          }
        }
      }
    }
    boolean sort = jbm.getOps().getBool("sort_methods");
    sort(tm, root, sort);
    tm.reload();
    
    this.addMouseListener(new MouseAdapter() {
      public void mousePressed(MouseEvent me) {
        if (SwingUtilities.isRightMouseButton(me)) {
          TreePath tp = ClassTree.this.getPathForLocation(me.getX(), me.getY());
          if (tp != null && tp.getParentPath() != null) {
            ClassTree.this.setSelectionPath(tp);
            if (ClassTree.this.getLastSelectedPathComponent() == null) {
              return;
            }
            MethodNode mn = ((SortedTreeNode) ClassTree.this.getLastSelectedPathComponent()).getMn();
            ClassNode cn = ((SortedTreeNode) ClassTree.this.getLastSelectedPathComponent()).getCn();
            if (mn != null) {
              JPopupMenu menu = new JPopupMenu();
              JMenuItem edit = new JMenuItem("Edit");
              edit.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                  EditDialogue.createMethodDialogue(mn);
                }
              });
              menu.add(edit);
              JMenuItem clear = new JMenuItem("Clear");
              clear.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                  if (JOptionPane.showConfirmDialog(JByteMod.instance, "Are you sure you want to clear that method?", "Confirm",
                      JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    MethodUtils.clear(mn);
                    jbm.selectMethod(cn, mn);
                  }
                }
              });
              menu.add(clear);
              menu.show(ClassTree.this, me.getX(), me.getY());
            } else if (cn != null) {
              JPopupMenu menu = new JPopupMenu();
              JMenuItem edit = new JMenuItem("Edit");
              edit.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                  EditDialogue.createClassDialogue(cn);
                }
              });
              menu.add(edit);
              menu.show(ClassTree.this, me.getX(), me.getY());
            }
          }
        }
      }
    });
  }

  private SortedTreeNode addUniqueNode(DefaultTreeModel model, SortedTreeNode node, SortedTreeNode childNode) {
    for (int i = 0; i < model.getChildCount(node); i++) {
      Object compUserObj = ((SortedTreeNode) model.getChild(node, i)).getUserObject();
      if (compUserObj.equals(childNode.getUserObject())) {
        return (SortedTreeNode) model.getChild(node, i);
      }
    }
    node.add(childNode);
    return null;
  }

  private void sort(DefaultTreeModel model, SortedTreeNode node, boolean sm) {
    if (!node.isLeaf() && (sm ? true : (!node.getUserObject().toString().endsWith(".class")))) {
      node.sort();
      for (int i = 0; i < model.getChildCount(node); i++) {
        SortedTreeNode child = ((SortedTreeNode) model.getChild(node, i));
        sort(model, child, sm);
      }
    }
  }
}
