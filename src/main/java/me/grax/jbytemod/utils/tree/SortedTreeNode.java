package me.grax.jbytemod.utils.tree;

import java.util.Collections;
import java.util.Comparator;

import javax.swing.tree.DefaultMutableTreeNode;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

public class SortedTreeNode extends DefaultMutableTreeNode {

  private ClassNode c;
  private MethodNode m;
  private String className;

  public SortedTreeNode(ClassNode c, MethodNode m) {
    this.c = c;
    this.m = m;
    setClassName();
  }

  public SortedTreeNode(ClassNode c) {
    this.c = c;
    setClassName();
  }

  public SortedTreeNode(Object userObject) {
    super(userObject);
  }

  private void setClassName() {
    if (!c.name.contains("/"))
      this.className = c.name + ".class";
    String[] split = c.name.split("/");
    this.className = split[split.length - 1] + ".class";
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
        return o1.toString().compareTo(o2.toString());

      }
    };
  }

  @Override
  public String toString() {
    if (m != null) {
      return m.name;
    }
    if (c != null) {
      return className;
    }
    return userObject.toString();
  }
}