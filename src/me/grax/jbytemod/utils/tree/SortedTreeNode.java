package me.grax.jbytemod.utils.tree;

import java.util.Collections;
import java.util.Comparator;

import javax.swing.tree.DefaultMutableTreeNode;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

public class SortedTreeNode extends DefaultMutableTreeNode {

  private ClassNode c;
  private MethodNode m;

  public SortedTreeNode(Object userObject, ClassNode c, MethodNode m) {
    super(userObject);
    this.c = c;
    this.m = m;
  }

  public ClassNode getCn() {
    return c;
  }

  public void setCn(ClassNode c) {
    this.c = c;
  }

  public MethodNode getMn() {
    return m;
  }

  public void setMn(MethodNode m) {
    this.m = m;
  }

  @SuppressWarnings("unchecked")
  public void sort() {
    Collections.sort(children, compare());
  }

  private Comparator<DefaultMutableTreeNode> compare() {
    return new Comparator<DefaultMutableTreeNode>() {
      @Override
      public int compare(DefaultMutableTreeNode o1, DefaultMutableTreeNode o2) {
        boolean leaf1 = o1.toString().endsWith(".class");
        boolean leaf2 = o2.toString().endsWith(".class");

        if (leaf1 && !leaf2) {
          return 1;
        }
        if (!leaf1 && leaf2) {
          return -1;
        }
        return o1.getUserObject().toString().compareTo(o2.getUserObject().toString());

      }
    };
  }
}