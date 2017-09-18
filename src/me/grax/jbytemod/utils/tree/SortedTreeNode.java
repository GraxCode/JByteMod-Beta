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
        if (o1.isLeaf() && !o2.isLeaf()) {
          return 1;
        }
        if (!o1.isLeaf() && o2.isLeaf()) {
          return -1;
        }
        return o1.getUserObject().toString().compareTo(o2.getUserObject().toString());
      }
    };
  }
}