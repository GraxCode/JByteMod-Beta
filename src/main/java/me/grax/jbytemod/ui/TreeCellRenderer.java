package me.grax.jbytemod.ui;

import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.util.HashMap;

import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import me.grax.jbytemod.ui.tree.SortedTreeNode;
import me.lpk.util.AccessHelper;

public class TreeCellRenderer extends DefaultTreeCellRenderer implements Opcodes {
  private ImageIcon pack, clazz, enu, itf, file;
  public static ImageIcon mpri, mpro, mpub, mdef; //method access
  public static ImageIcon abs, fin, nat, stat, syn; //general access

  private HashMap<Integer, ImageIcon> methodIcons = new HashMap<>();

  static {
    TreeCellRenderer.mpri = new ImageIcon(Toolkit.getDefaultToolkit().getImage(TreeCellRenderer.class.getResource("/resources/method/methpri.png")));
    TreeCellRenderer.mpro = new ImageIcon(Toolkit.getDefaultToolkit().getImage(TreeCellRenderer.class.getResource("/resources/method/methpro.png")));
    TreeCellRenderer.mpub = new ImageIcon(Toolkit.getDefaultToolkit().getImage(TreeCellRenderer.class.getResource("/resources/method/methpub.png")));
    TreeCellRenderer.mdef = new ImageIcon(Toolkit.getDefaultToolkit().getImage(TreeCellRenderer.class.getResource("/resources/method/methdef.png")));

    TreeCellRenderer.abs = new ImageIcon(Toolkit.getDefaultToolkit().getImage(TreeCellRenderer.class.getResource("/resources/access/abstract.png")));
    TreeCellRenderer.fin = new ImageIcon(Toolkit.getDefaultToolkit().getImage(TreeCellRenderer.class.getResource("/resources/access/final.png")));
    TreeCellRenderer.nat = new ImageIcon(Toolkit.getDefaultToolkit().getImage(TreeCellRenderer.class.getResource("/resources/access/native.png")));
    TreeCellRenderer.stat = new ImageIcon(Toolkit.getDefaultToolkit().getImage(TreeCellRenderer.class.getResource("/resources/access/static.png")));
    TreeCellRenderer.syn = new ImageIcon(Toolkit.getDefaultToolkit().getImage(TreeCellRenderer.class.getResource("/resources/access/synthetic.png")));
  }

  public TreeCellRenderer() {
    this.pack = new ImageIcon(Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/resources/package.png")));
    this.file = new ImageIcon(Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/resources/file.png")));

    this.clazz = new ImageIcon(Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/resources/java.png")));
    this.enu = combine(clazz, new ImageIcon(Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/resources/classtype/enum.png"))));
    this.itf = combine(clazz, new ImageIcon(Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/resources/classtype/interface.png"))));

    methodIcons.put(ACC_PUBLIC, mpub);
    methodIcons.put(ACC_PROTECTED, mpro);
    methodIcons.put(ACC_PRIVATE, mpri);
    methodIcons.put(0, mdef);
  }

  @Override
  public Component getTreeCellRendererComponent(final JTree tree, final Object value, final boolean sel, final boolean expanded, final boolean leaf,
      final int row, final boolean hasFocus) {
    super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
    final DefaultMutableTreeNode n = (DefaultMutableTreeNode) value;
    if (n.getChildCount() > 0 && !this.getFileName(n).endsWith(".jar") && !this.getFileName(n).endsWith(".class")) {
      this.setIcon(this.pack);
    } else if (this.getFileName(n).endsWith(".class")) {
      SortedTreeNode stn = (SortedTreeNode) n;
      ClassNode cn = stn.getCn();
      if (cn != null) {
        if (AccessHelper.isInterface(cn.access)) {
          this.setIcon(this.itf);
        } else if (AccessHelper.isEnum(cn.access)) {
          this.setIcon(this.enu);
        } else {
          this.setIcon(this.clazz);
        }
      }
    } else if (n.getParent() != null && this.getFileName((DefaultMutableTreeNode) n.getParent()).endsWith(".class")) {
      SortedTreeNode stn = (SortedTreeNode) n;
      MethodNode mn = stn.getMn();
      if (mn != null) {
        ImageIcon icon;
        if (methodIcons.containsKey(mn.access)) {
          icon = methodIcons.get(mn.access);
        } else {
          icon = generateIcon(mn.access);
        }
        this.setIcon(icon);
      } else {
        throw new IllegalArgumentException();
      }
    } else {
      this.setIcon(this.file);
    }
    return this;
  }

  private ImageIcon generateIcon(int access) {
    ImageIcon template = null;
    if (AccessHelper.isPublic(access)) {
      template = mpub;
    } else if (AccessHelper.isPrivate(access)) {
      template = mpri;
    } else if (AccessHelper.isProtected(access)) {
      template = mpro;
    } else {
      template = mdef;
    }
    if (AccessHelper.isAbstract(access)) {
      template = combineAccess(template, abs, true);
    } else {
      boolean scndRight = true;
      if (AccessHelper.isFinal(access)) {
        template = combineAccess(template, fin, true);
        scndRight = false;
      } else if (AccessHelper.isNative(access)) { //do not allow triples
        template = combineAccess(template, nat, true);
        scndRight = false;
      }
      if (AccessHelper.isStatic(access)) {
        template = combineAccess(template, stat, scndRight);
      } else if (AccessHelper.isSynthetic(access)) {
        template = combineAccess(template, syn, scndRight);
      }
    }
    methodIcons.put(access, template);
    return template;
  }

  public static ImageIcon combineAccess(ImageIcon icon1, ImageIcon icon2, boolean right) {
    Image img1 = icon1.getImage();
    Image img2 = icon2.getImage();

    int w = icon1.getIconWidth();
    int h = icon1.getIconHeight();
    BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2 = image.createGraphics();
    g2.drawImage(img1, 0, 0, null);
    g2.drawImage(img2, right ? w / 4 : w / -4, h / -4, null);
    g2.dispose();

    return new ImageIcon(image);
  }

  public static ImageIcon combine(ImageIcon icon1, ImageIcon icon2) {
    Image img1 = icon1.getImage();
    Image img2 = icon2.getImage();

    int w = icon1.getIconWidth();
    int h = icon1.getIconHeight();
    BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2 = image.createGraphics();
    g2.drawImage(img1, 0, 0, null);
    g2.drawImage(img2, 0, 0, null);
    g2.dispose();

    return new ImageIcon(image);
  }

  public String getFileName(final DefaultMutableTreeNode node) {
    return node.toString();
  }
}
