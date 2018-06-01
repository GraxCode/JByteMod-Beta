package me.grax.jbytemod.ui;

import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

import org.objectweb.asm.Opcodes;

import com.alee.laf.button.WebButton;
import com.alee.managers.popup.PopupWay;
import com.alee.managers.popup.WebButtonPopup;

import me.lpk.util.AccessHelper;

public class JAccessSelectorPanel extends JPanel implements Opcodes {

  private VisibilityButton visibility;
  private ExtrasButton extras;
  private OtherButton other;
  private JButton accessHelper;

  public JAccessSelectorPanel(int accezz) {
    this.setLayout(new GridLayout(1, 4));
    this.add(visibility = new VisibilityButton(accezz));
    this.add(extras = new ExtrasButton(accezz));
    this.add(other = new OtherButton(accezz));
    accessHelper = new JButton(
        new ImageIcon(Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/resources/toolbar/table.png"))));
    accessHelper.addActionListener(e -> {
      new JAccessHelper(getAccess(), ae -> {
        setAccess(Integer.parseInt(ae.getActionCommand()));
      }).setVisible(true);
    });
    this.add(accessHelper);
  }

  public int getAccess() {
    return visibility.getVisibility() | extras.getVisibility() | other.getVisibility();
  }

  public void setAccess(int accezz) {
    visibility.updateVisibility(accezz);
    extras.updateVisibility(accezz);
    other.updateVisibility(accezz);
  }

  public class VisibilityButton extends WebButton {
    private int visibility;

    public VisibilityButton(int access) {
      updateVisibility(access);
      this.addMouseListener(new MouseAdapter() {
        public void mouseClicked(MouseEvent e) {
          WebButtonPopup popupMenu = generatePopupMenu();
          popupMenu.showPopup();
        }
      });
    }

    public void updateVisibility(int access) {
      if (AccessHelper.isPublic(access)) {
        visibility = ACC_PUBLIC;
        this.setIcon(TreeCellRenderer.mpub);
      } else if (AccessHelper.isPrivate(access)) {
        visibility = ACC_PRIVATE;
        this.setIcon(TreeCellRenderer.mpri);
      } else if (AccessHelper.isProtected(access)) {
        visibility = ACC_PROTECTED;
        this.setIcon(TreeCellRenderer.mpro);
      } else {
        visibility = 0; //default
        this.setIcon(TreeCellRenderer.mdef);
      }
    }

    private WebButtonPopup generatePopupMenu() {
      WebButtonPopup pm = new WebButtonPopup(this, PopupWay.downCenter);
      JToggleButton pub = new JToggleButton(TreeCellRenderer.mpub);
      pub.setToolTipText("public");
      JToggleButton pri = new JToggleButton(TreeCellRenderer.mpri);
      pri.setToolTipText("private");
      JToggleButton pro = new JToggleButton(TreeCellRenderer.mpro);
      pro.setToolTipText("protected");
      JToggleButton def = new JToggleButton(TreeCellRenderer.mdef);
      def.setToolTipText("none");
      pub.addActionListener(e -> {
        pub.setSelected(true);
        pri.setSelected(false);
        pro.setSelected(false);
        def.setSelected(false);
        updateVisibility(ACC_PUBLIC);
      });
      pri.addActionListener(e -> {
        pub.setSelected(false);
        pri.setSelected(true);
        pro.setSelected(false);
        def.setSelected(false);
        updateVisibility(ACC_PRIVATE);
      });
      pro.addActionListener(e -> {
        pub.setSelected(false);
        pri.setSelected(false);
        pro.setSelected(true);
        def.setSelected(false);
        updateVisibility(ACC_PROTECTED);
      });
      def.addActionListener(e -> {
        pub.setSelected(false);
        pri.setSelected(false);
        pro.setSelected(false);
        def.setSelected(true);
        updateVisibility(0);
      });
      switch (visibility) {
      case ACC_PUBLIC:
        pub.setSelected(true);
        break;
      case ACC_PRIVATE:
        pri.setSelected(true);
        break;
      case ACC_PROTECTED:
        pro.setSelected(true);
        break;
      default:
        def.setSelected(true);
        break;
      }
      JPanel list = new JPanel(new GridLayout(4, 1));
      list.add(pub);
      list.add(pri);
      list.add(pro);
      list.add(def);
      pm.setContent(list);
      return pm;
    }

    public int getVisibility() {
      return visibility;
    }

  }

  public class ExtrasButton extends WebButton {
    private int visibility;

    public ExtrasButton(int access) {
      updateVisibility(access);
      this.addMouseListener(new MouseAdapter() {
        public void mouseClicked(MouseEvent e) {
          WebButtonPopup popupMenu = generatePopupMenu();
          popupMenu.showPopup();
        }
      });
    }

    public void updateVisibility(int access) {
      visibility = 0;
      if (AccessHelper.isFinal(access)) {
        visibility |= ACC_FINAL;
      }
      if (AccessHelper.isNative(access)) {
        visibility |= ACC_NATIVE;
      }
      if (AccessHelper.isStatic(access)) {
        visibility |= ACC_STATIC;
      }
      if (AccessHelper.isSynthetic(access)) {
        visibility |= ACC_SYNTHETIC;
      }
      if (AccessHelper.isAbstract(access)) {
        visibility |= ACC_ABSTRACT;
      }
      ImageIcon preview = new ImageIcon(new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB));
      boolean empty = true;
      if (AccessHelper.isAbstract(access)) {
        preview = TreeCellRenderer.combineAccess(preview, TreeCellRenderer.abs, true);
        empty = false;
      } else {
        boolean scndRight = true;
        if (AccessHelper.isFinal(access)) {
          preview = TreeCellRenderer.combineAccess(preview, TreeCellRenderer.fin, true);
          empty = scndRight = false;
        } else if (AccessHelper.isNative(access)) { //do not allow triples
          preview = TreeCellRenderer.combineAccess(preview, TreeCellRenderer.nat, true);
          empty = scndRight = false;
        }
        if (AccessHelper.isStatic(access)) {
          preview = TreeCellRenderer.combineAccess(preview, TreeCellRenderer.stat, scndRight);
          empty = false;
        } else if (AccessHelper.isSynthetic(access)) {
          preview = TreeCellRenderer.combineAccess(preview, TreeCellRenderer.syn, scndRight);
          empty = false;
        }
      }
      this.setIcon(preview);
    }

    private WebButtonPopup generatePopupMenu() {
      WebButtonPopup pm = new WebButtonPopup(this, PopupWay.downCenter);
      JToggleButton abs = new JToggleButton(TreeCellRenderer.abs);
      abs.setToolTipText("abstract");
      abs.setSelected(AccessHelper.isAbstract(visibility));
      JToggleButton fin = new JToggleButton(TreeCellRenderer.fin);
      fin.setToolTipText("final");
      fin.setSelected(AccessHelper.isFinal(visibility));
      JToggleButton nat = new JToggleButton(TreeCellRenderer.nat);
      nat.setToolTipText("native");
      nat.setSelected(AccessHelper.isNative(visibility));
      JToggleButton stat = new JToggleButton(TreeCellRenderer.stat);
      stat.setToolTipText("static");
      stat.setSelected(AccessHelper.isStatic(visibility));
      JToggleButton syn = new JToggleButton(TreeCellRenderer.syn);
      syn.setToolTipText("synthetic");
      syn.setSelected(AccessHelper.isSynthetic(visibility));
      abs.addActionListener(e -> {
        if (AccessHelper.isAbstract(visibility)) {
          visibility -= ACC_ABSTRACT;
        } else {
          visibility |= ACC_ABSTRACT;
          if (fin.isSelected())
            fin.doClick();
          if (nat.isSelected())
            nat.doClick();
          if (stat.isSelected())
            stat.doClick();
          if (syn.isSelected())
            syn.doClick();
        }
        updateVisibility(visibility);
      });
      fin.addActionListener(e -> {
        if (AccessHelper.isFinal(visibility)) {
          visibility -= ACC_FINAL;
        } else {
          visibility |= ACC_FINAL;
          if (nat.isSelected())
            nat.doClick();
          if (abs.isSelected())
            abs.doClick();
        }
        updateVisibility(visibility);
      });
      nat.addActionListener(e -> {
        if (AccessHelper.isNative(visibility)) {
          visibility -= ACC_NATIVE;
        } else {
          visibility |= ACC_NATIVE;
          if (fin.isSelected())
            fin.doClick();
          if (abs.isSelected())
            abs.doClick();
        }
        updateVisibility(visibility);
      });
      stat.addActionListener(e -> {
        if (AccessHelper.isStatic(visibility)) {
          visibility -= ACC_STATIC;
        } else {
          visibility |= ACC_STATIC;
          if (abs.isSelected())
            abs.doClick();
        }
        updateVisibility(visibility);
      });
      syn.addActionListener(e -> {
        if (AccessHelper.isSynthetic(visibility)) {
          visibility -= ACC_SYNTHETIC;
        } else {
          visibility |= ACC_SYNTHETIC;
          if (abs.isSelected())
            abs.doClick();
        }
        updateVisibility(visibility);
      });
      JPanel list = new JPanel(new GridLayout(4, 1));
      list.add(fin);
      list.add(nat);
      list.add(stat);
      list.add(syn);
      list.add(abs);
      pm.setContent(list);
      return pm;
    }

    public int getVisibility() {
      return visibility;
    }

  }

  public class OtherButton extends WebButton {
    private int visibility;

    public OtherButton(int access) {
      try {
        for (Field d : Opcodes.class.getDeclaredFields()) {
          if (d.getName().startsWith("ACC_") && !alreadyCovered.contains(d.getName())) {
            int acc = d.getInt(null);
            otherTypes.put(d.getName().substring(4).toLowerCase(), acc);
          }
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
      updateVisibility(access);
      this.addMouseListener(new MouseAdapter() {
        public void mouseClicked(MouseEvent e) {
          WebButtonPopup popupMenu = generatePopupMenu();
          popupMenu.showPopup();
        }
      });
    }

    private final List<String> alreadyCovered = Arrays.asList("ACC_PUBLIC", "ACC_PRIVATE", "ACC_PROTECTED", "ACC_STATIC", "ACC_FINAL", "ACC_NATIVE",
        "ACC_ABSTRACT", "ACC_SYNTHETIC", "ACC_STATIC_PHASE", "ACC_TRANSITIVE");

    private final HashMap<String, Integer> otherTypes = new HashMap<>();

    public void updateVisibility(int access) {
      visibility = 0;
      for (int acc : otherTypes.values()) {
        if ((access & acc) != 0) {
          visibility |= acc;
        }
      }
      this.setText("...");
    }

    private WebButtonPopup generatePopupMenu() {
      WebButtonPopup pm = new WebButtonPopup(this, PopupWay.downCenter);
      JPanel list = new JPanel(new GridLayout(7, 1));
      for (Entry<String, Integer> acc : otherTypes.entrySet()) {
        JToggleButton jtb = new JToggleButton(
            acc.getKey().substring(0, 1).toUpperCase() + acc.getKey().substring(1, Math.min(acc.getKey().length(), 7)));
        jtb.setSelected((visibility & acc.getValue()) != 0);
        jtb.addActionListener(e -> {
          if ((visibility & acc.getValue()) != 0) {
            visibility -= acc.getValue();
          } else {
            visibility |= acc.getValue();
            System.out.println("add acc");
          }
        });
        jtb.setFont(new Font(jtb.getFont().getName(), Font.PLAIN, 10));
        list.add(jtb);
      }
      pm.setContent(list);
      return pm;
    }

    public int getVisibility() {
      return visibility;
    }
  }
}
