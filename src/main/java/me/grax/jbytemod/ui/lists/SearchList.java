package me.grax.jbytemod.ui.lists;

import java.awt.Font;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import me.grax.jbytemod.JByteMod;
import me.grax.jbytemod.ui.PageEndPanel;
import me.grax.jbytemod.utils.list.SearchEntry;

public class SearchList extends JList<SearchEntry> {

  private JByteMod jbm;

  public SearchList(JByteMod jbm) {
    super(new DefaultListModel<SearchEntry>());
    this.jbm = jbm;
    this.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
    this.addMouseListener(new MouseAdapter() {
      public void mousePressed(MouseEvent e) {
        if (SwingUtilities.isRightMouseButton(e)) {
          JPopupMenu menu = new JPopupMenu();
          JMenuItem decl = new JMenuItem(JByteMod.res.getResource("go_to_dec"));
          decl.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
              ClassNode cn = SearchList.this.getSelectedValue().getCn();
              MethodNode mn = SearchList.this.getSelectedValue().getMn();
              jbm.selectMethod(cn, mn);
              jbm.treeSelection(cn, mn);
            }
          });
          menu.add(decl);
          JMenuItem copy = new JMenuItem(JByteMod.res.getResource("copy_text"));
          copy.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
              StringSelection selection = new StringSelection(SearchList.this.getSelectedValue().getFound());
              Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, selection);
            }
          });
          menu.add(copy);
          menu.show(SearchList.this, e.getX(), e.getY());
        }
      }
    });
  }

  public void searchForString(String ldc, boolean exact, boolean cs, boolean regex) {
    new TaskLDCSearch(jbm, ldc, exact, cs, regex).execute();
  }

  class TaskLDCSearch extends SwingWorker<Void, Integer> {

    private PageEndPanel jpb;
    private JByteMod jbm;
    private String ldc;
    private boolean exact;
    private boolean caseSens;
    private boolean regex;

    public TaskLDCSearch(JByteMod jbm, String ldc, boolean exact, boolean caseSens, boolean regex) {
      this.jbm = jbm;
      this.jpb = jbm.getPP();
      this.exact = exact;
      this.caseSens = caseSens;
      this.regex = regex;
      
      if (!caseSens) {
        this.ldc = ldc.toLowerCase();
      } else {
        this.ldc = ldc;
      }
    }

    @Override
    protected Void doInBackground() throws Exception {
      DefaultListModel<SearchEntry> model = new DefaultListModel<>();
      Collection<ClassNode> values = jbm.getFile().getClasses().values();
      double size = values.size();
      double i = 0;
      boolean exact = this.exact;
      boolean regex = this.regex;
      for (ClassNode cn : values) {
        for (MethodNode mn : cn.methods) {
          for (AbstractInsnNode ain : mn.instructions) {
            if (ain.getType() == AbstractInsnNode.LDC_INSN) {
              LdcInsnNode lin = (LdcInsnNode) ain;
              String cst = lin.cst.toString();
              if (!caseSens) {
                cst = cst.toLowerCase();
              }
              if (regex ? Pattern.matches(ldc, cst) : (exact ? cst.equals(ldc) : cst.contains(ldc))) {
                model.addElement(new SearchEntry(cn, mn, lin.cst.toString()));
              }
            }
          }
        }
        publish(Math.min((int) (i++ / size * 100d) + 1, 100));
      }
      SearchList.this.setModel(model);
      return null;
    }

    @Override
    protected void process(List<Integer> chunks) {
      int i = chunks.get(chunks.size() - 1);
      jpb.setValue(i);
      super.process(chunks);
    }

    @Override
    protected void done() {
      System.out.println("Search finished!");
    }
  }

  public void searchForFMInsn(String owner, String name, String desc, boolean exact, boolean field) {
    new TaskFMSearch(jbm, owner, name, desc, exact, field).execute();

  }

  class TaskFMSearch extends SwingWorker<Void, Integer> {

    private PageEndPanel jpb;
    private JByteMod jbm;
    private boolean exact;
    private String owner;
    private String name;
    private String desc;
    private boolean field;

    public TaskFMSearch(JByteMod jbm, String owner, String name, String desc, boolean exact, boolean field) {
      this.jbm = jbm;
      this.jpb = jbm.getPP();
      this.exact = exact;
      this.owner = owner;
      this.name = name;
      this.desc = desc;
      this.field = field;
    }

    @Override
    protected Void doInBackground() throws Exception {
      DefaultListModel<SearchEntry> model = new DefaultListModel<SearchEntry>() {
        @Override
        protected void fireIntervalAdded(Object source, int index0, int index1) {
        }
      };
      Collection<ClassNode> values = jbm.getFile().getClasses().values();
      double size = values.size();
      double i = 0;
      boolean exact = this.exact;
      for (ClassNode cn : values) {
        for (MethodNode mn : cn.methods) {
          for (AbstractInsnNode ain : mn.instructions) {
            if (field) {
              if (ain.getType() == AbstractInsnNode.FIELD_INSN) {
                FieldInsnNode fin = (FieldInsnNode) ain;
                if (exact) {
                  if (fin.owner.equals(owner) && fin.name.equals(name) && fin.desc.equals(desc)) {
                    model.addElement(new SearchEntry(cn, mn, fin));
                  }
                } else {
                  if (fin.owner.contains(owner) && fin.name.contains(name) && fin.desc.contains(desc)) {
                    model.addElement(new SearchEntry(cn, mn, fin));
                  }
                }
              }
            } else {
              if (ain.getType() == AbstractInsnNode.METHOD_INSN) {
                MethodInsnNode min = (MethodInsnNode) ain;
                if (exact) {
                  if (min.owner.equals(owner) && min.name.equals(name) && min.desc.equals(desc)) {
                    model.addElement(new SearchEntry(cn, mn, min));
                  }
                } else {
                  if (min.owner.contains(owner) && min.name.contains(name) && min.desc.contains(desc)) {
                    model.addElement(new SearchEntry(cn, mn, min));
                  }
                }
              }
            }
          }
        }
        publish(Math.min((int) (i++ / size * 100d) + 1, 100));
      }
      SearchList.this.setModel(model);
      publish(100);
      return null;
    }

    @Override
    protected void process(List<Integer> chunks) {
      int i = chunks.get(chunks.size() - 1);
      jpb.setValue(i);
      super.process(chunks);
    }

    @Override
    protected void done() {
      jpb.setValue(100);
      System.out.println("Search finished!");
    }
  }
}
