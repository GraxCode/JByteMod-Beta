package me.grax.jbytemod.utils.gui;

import java.awt.Component;
import java.awt.Toolkit;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

public class CellRenderer extends DefaultTreeCellRenderer {
  Icon pack, java, file;

  public CellRenderer() {
    this.pack = new ImageIcon(Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/resources/package_obj.png")));
    this.java = new ImageIcon(Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/resources/java.png")));
    this.file = new ImageIcon(Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/resources/file.png")));
  }

  @Override
  public Component getTreeCellRendererComponent(final JTree tree, final Object value, final boolean sel, final boolean expanded, final boolean leaf,
      final int row, final boolean hasFocus) {
    super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
    final DefaultMutableTreeNode n = (DefaultMutableTreeNode) value;
    if (n.getChildCount() > 0 && !this.getFileName(n).endsWith(".jar") && !this.getFileName(n).endsWith(".class")) {
      this.setIcon(this.pack);
    } else if (this.getFileName(n).endsWith(".class")) {
      this.setIcon(this.java);
    } else {
      this.setIcon(this.file);
    }
    return this;
  }

  public String getFileName(final DefaultMutableTreeNode node) {
    return (String) node.getUserObject().toString();
  }
}
