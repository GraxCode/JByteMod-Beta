package me.grax.jbytemod.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
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
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import me.grax.jbytemod.JByteMod;
import me.grax.jbytemod.JarArchive;
import me.grax.jbytemod.utils.ErrorDisplay;
import me.grax.jbytemod.utils.MethodUtils;
import me.grax.jbytemod.utils.asm.FrameGen;
import me.grax.jbytemod.utils.dialogue.InsnEditDialogue;
import me.grax.jbytemod.utils.gui.CellRenderer;
import me.grax.jbytemod.utils.tree.SortedTreeNode;
import me.lpk.util.drop.IDropUser;
import me.lpk.util.drop.JarDropHandler;

public class ClassTree extends JTree implements IDropUser {

  private JByteMod jbm;
  private DefaultTreeModel model;
  private HashMap<String, SortedTreeNode> preloadMap;

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

  private static ArrayList<Object> expandedNodes = new ArrayList<>();

  public void refreshTree(JarArchive jar) {
    DefaultTreeModel tm = this.model;
    SortedTreeNode root = (SortedTreeNode) tm.getRoot();
    root.removeAllChildren();
    tm.reload();

    preloadMap = new HashMap<>();
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
        if (preloadMap.containsKey(p)) {
          prev = preloadMap.get(p);
        } else {
          SortedTreeNode stn = new SortedTreeNode(path[i]);
          prev.add(stn);
          prev = stn;
          preloadMap.put(p, prev);
        }
        i++;
      }
      SortedTreeNode clazz = new SortedTreeNode(c);
      prev.add(clazz);
      for (MethodNode m : c.methods) {
        clazz.add(new SortedTreeNode(c, m));
      }
    }
    boolean sort = JByteMod.ops.get("sort_methods").getBoolean();
    sort(tm, root, sort);
    tm.reload();
    addListener();
    if (!expandedNodes.isEmpty()) {
      expandSaved(root);
    }
  }

  public void expandSaved(SortedTreeNode node) {
    TreePath tp = new TreePath(node.getPath());
    if (node.getCn() != null && expandedNodes.contains(node.getCn())) {
      super.expandPath(tp);
    }
    if (expandedNodes.contains(tp.toString())) {
      super.expandPath(tp);
    }
    if (node.getChildCount() >= 0) {
      for (Enumeration<?> e = node.children(); e.hasMoreElements();) {
        SortedTreeNode n = (SortedTreeNode) e.nextElement();
        expandSaved(n);
      }
    }
  }

  @Override
  public void expandPath(TreePath path) {
    SortedTreeNode stn = (SortedTreeNode) path.getLastPathComponent();
    if (stn.getCn() != null) {
      expandedNodes.add(stn.getCn());
    } else {
      expandedNodes.add(path.toString());
    }
    super.expandPath(path);
  }

  @Override
  public void collapsePath(TreePath path) {
    SortedTreeNode stn = (SortedTreeNode) path.getLastPathComponent();
    if (stn.getCn() != null) {
      expandedNodes.remove(stn.getCn());
    } else {
      expandedNodes.remove(path.toString());
    }
    super.collapsePath(path);
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
              JMenuItem edit = new JMenuItem(JByteMod.res.getResource("edit"));
              edit.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                  new InsnEditDialogue(mn, mn).open();
                  changedChilds((TreeNode) model.getRoot());
                }
              });
              menu.add(edit);
              JMenu tools = new JMenu(JByteMod.res.getResource("tools"));
              JMenuItem clear = new JMenuItem(JByteMod.res.getResource("clear"));
              clear.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                  if (JOptionPane.showConfirmDialog(JByteMod.instance, JByteMod.res.getResource("confirm_clear"), JByteMod.res.getResource("confirm"),
                      JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    MethodUtils.clear(mn);
                    jbm.selectMethod(cn, mn);
                  }
                }
              });
              tools.add(clear);

              JMenuItem lines = new JMenuItem(JByteMod.res.getResource("remove_lines"));
              lines.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                  if (JOptionPane.showConfirmDialog(JByteMod.instance, JByteMod.res.getResource("confirm_lines"), JByteMod.res.getResource("confirm"),
                      JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    MethodUtils.removeLines(mn);
                    jbm.selectMethod(cn, mn);
                  }
                }
              });
              tools.add(lines);
              JMenuItem deadcode = new JMenuItem(JByteMod.res.getResource("remove_dead_code"));
              deadcode.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                  if (JOptionPane.showConfirmDialog(JByteMod.instance, JByteMod.res.getResource("confirm_dead_code"), JByteMod.res.getResource("confirm"),
                      JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    MethodUtils.removeDeadCode(mn);
                    jbm.selectMethod(cn, mn);
                  }
                }
              });
              tools.add(deadcode);
              menu.add(tools);
              menu.show(ClassTree.this, me.getX(), me.getY());
            } else if (cn != null) {
              //class selected
              JPopupMenu menu = new JPopupMenu();
              JMenuItem insert = new JMenuItem(JByteMod.res.getResource("add_method"));
              insert.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                  MethodNode mn = new MethodNode(1, "", "()V", null, null);
                  if (new InsnEditDialogue(mn, mn).open()) {
                    if (mn.name.isEmpty() || mn.desc.isEmpty()) {
                      ErrorDisplay.error("Method name / desc cannot be empty");
                      return;
                    }
                    cn.methods.add(mn);
                    model.insertNodeInto(new SortedTreeNode(cn, mn), stn, 0);
                  }
                }
              });
              menu.add(insert);
              JMenuItem edit = new JMenuItem(JByteMod.res.getResource("edit"));
              edit.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                  if (new InsnEditDialogue(mn, cn).open()) {
                    jbm.refreshTree();
                  }
                }
              });
              menu.add(edit);
              JMenu tools = new JMenu(JByteMod.res.getResource("tools"));
              JMenuItem frames = new JMenuItem(JByteMod.res.getResource("generate_frames"));
              frames.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                  FrameGen.regenerateFrames(jbm, cn);
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

  public void refreshMethod(ClassNode cn, MethodNode mn) {
    changedChilds((TreeNode) model.getRoot());
  }

  public void changedChilds(TreeNode node) {
    model.nodeChanged(node);
    if (node.getChildCount() >= 0) {
      for (Enumeration<?> e = node.children(); e.hasMoreElements();) {
        TreeNode n = (TreeNode) e.nextElement();
        changedChilds(n);
      }
    }
  }

  public void collapseAll() {
    expandedNodes.clear();
    JByteMod.instance.refreshTree();
  }
}
