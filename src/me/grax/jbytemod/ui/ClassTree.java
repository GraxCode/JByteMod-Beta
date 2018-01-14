package me.grax.jbytemod.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.HashMap;

import javax.swing.JMenu;
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
import me.grax.jbytemod.JarArchive;
import me.grax.jbytemod.utils.MethodUtils;
import me.grax.jbytemod.utils.asm.FrameGen;
import me.grax.jbytemod.utils.dialogue.EditDialogue;
import me.grax.jbytemod.utils.gui.CellRenderer;
import me.grax.jbytemod.utils.tree.SortedTreeNode;
import me.lpk.util.drop.IDropUser;
import me.lpk.util.drop.JarDropHandler;

public class ClassTree extends JTree implements IDropUser {

  private JByteMod jbm;
  private DefaultTreeModel model;

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
    this.model = new DefaultTreeModel(new SortedTreeNode(""));
    this.setModel(model);
    this.setTransferHandler(new JarDropHandler(this, 0));
  }

  public void refreshTree(JarArchive jar) {
    DefaultTreeModel tm = this.model;
    SortedTreeNode root = (SortedTreeNode) tm.getRoot();
    root.removeAllChildren();
    tm.reload();

    HashMap<String, SortedTreeNode> map = new HashMap<>();
    for (ClassNode c : jar.getClasses().values()) {
      String name = c.name;
      String[] path = name.split("/");
      int i = 0;
      int slashIndex = 0;
      SortedTreeNode prev = root;
      while (true) {
        slashIndex = name.indexOf("/", slashIndex + 1);
        if (slashIndex == -1) {
          break;
        }
        String p = name.substring(0, slashIndex);
        if (map.containsKey(p)) {
          prev = map.get(p);
        } else {
          SortedTreeNode stn = new SortedTreeNode(path[i]);
          prev.add(stn);
          prev = stn;
          map.put(p, prev);
        }
        i++;
      }
      SortedTreeNode clazz = new SortedTreeNode(c);
      prev.add(clazz);
      for (MethodNode m : c.methods) {
        clazz.add(new SortedTreeNode(c, m));
      }
    }
    boolean sort = jbm.getOps().getBool("sort_methods");
    sort(tm, root, sort);
    tm.reload();
    addListener();
  }

  private void addListener() {
    this.addMouseListener(new MouseAdapter() {
      public void mousePressed(MouseEvent me) {
        if (SwingUtilities.isRightMouseButton(me)) {
          TreePath tp = ClassTree.this.getPathForLocation(me.getX(), me.getY());
          if (tp != null && tp.getParentPath() != null) {
            ClassTree.this.setSelectionPath(tp);
            if (ClassTree.this.getLastSelectedPathComponent() == null) {
              return;
            }
            SortedTreeNode stn = (SortedTreeNode) ClassTree.this.getLastSelectedPathComponent();
            MethodNode mn = stn.getMn();
            ClassNode cn = stn.getCn();
            if (mn != null) {
              //method selected
              JPopupMenu menu = new JPopupMenu();
              JMenuItem edit = new JMenuItem("Edit");
              edit.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                  EditDialogue.createMethodDialogue(mn);
                }
              });
              menu.add(edit);
              JMenu tools = new JMenu("Tools");
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
              tools.add(clear);

              JMenuItem lines = new JMenuItem("Remove Lines");
              lines.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                  if (JOptionPane.showConfirmDialog(JByteMod.instance, "Are you sure you want to remove all LineNumberNodes?", "Confirm",
                      JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    MethodUtils.removeLines(mn);
                    jbm.selectMethod(cn, mn);
                  }
                }
              });
              tools.add(lines);
              menu.add(tools);
              menu.show(ClassTree.this, me.getX(), me.getY());
            } else if (cn != null) {
              //class selected
              JPopupMenu menu = new JPopupMenu();
              JMenuItem insert = new JMenuItem("Insert");
              insert.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                }
              });
              menu.add(insert);
              JMenuItem edit = new JMenuItem("Edit");
              edit.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                  EditDialogue.createClassDialogue(cn);
                  refreshTreeOpen(stn);
                }

              });
              menu.add(edit);
              JMenu tools = new JMenu("Tools");
              JMenuItem frames = new JMenuItem("Regenerate Frames");
              frames.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                  FrameGen.regenerateFrames(cn);
                }
              });
              tools.add(frames);
              menu.add(tools);
              menu.show(ClassTree.this, me.getX(), me.getY());
            }
          }
        }
      }
    });
  }

  public void refreshTreeOpen(SortedTreeNode stn) {
    jbm.refreshTree();
  }

  private void sort(DefaultTreeModel model, SortedTreeNode node, boolean sm) {
    if (!node.isLeaf() && (sm ? true : (!node.toString().endsWith(".class")))) {
      node.sort();
      for (int i = 0; i < model.getChildCount(node); i++) {
        SortedTreeNode child = ((SortedTreeNode) model.getChild(node, i));
        sort(model, child, sm);
      }
    }
  }

  @Override
  public void preLoadJars(int id) {

  }

  @Override
  public void onJarLoad(int id, File input) {
    jbm.loadFile(input);
  }
}
