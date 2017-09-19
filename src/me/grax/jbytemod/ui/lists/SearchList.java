package me.grax.jbytemod.ui.lists;

import java.awt.Font;
import java.io.File;
import java.util.Collection;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JList;
import javax.swing.SwingWorker;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodNode;

import me.grax.jbytemod.JByteMod;
import me.grax.jbytemod.JarFile;
import me.grax.jbytemod.ui.PageEndPanel;
import me.grax.jbytemod.utils.list.SearchEntry;
import me.lpk.util.JarUtils;

public class SearchList extends JList<SearchEntry> {

  private JByteMod jbm;

  public SearchList(JByteMod jbm) {
    super(new DefaultListModel<SearchEntry>());
    this.jbm = jbm;
    this.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
  }

  public void searchForString(String ldc, boolean exact, boolean cs) {
    new TaskSearch(jbm, ldc, exact, cs).execute();
  }

  class TaskSearch extends SwingWorker<Void, Integer> {

    private PageEndPanel jpb;
    private JByteMod jbm;
    private String ldc;
    private boolean exact;
    private boolean caseSens;

    public TaskSearch(JByteMod jbm, String ldc, boolean exact, boolean caseSens) {
      this.jbm = jbm;
      this.jpb = jbm.getPP();
      this.exact = exact;
      this.caseSens = caseSens;
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
      for (ClassNode cn : values) {
        for (MethodNode mn : cn.methods) {
          for (AbstractInsnNode ain : mn.instructions) {
            if (ain.getType() == AbstractInsnNode.LDC_INSN) {
              LdcInsnNode lin = (LdcInsnNode) ain;
              String cst = lin.cst.toString();
              if (!caseSens) {
                cst = cst.toLowerCase();
              }
              if (exact ? cst.equals(ldc) : cst.contains(ldc)) {
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
      //      jbm.refreshTree();
    }
  }
}
