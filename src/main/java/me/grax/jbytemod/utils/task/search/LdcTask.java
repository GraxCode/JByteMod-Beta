package me.grax.jbytemod.utils.task.search;

import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

import javax.swing.SwingWorker;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodNode;

import me.grax.jbytemod.JByteMod;
import me.grax.jbytemod.ui.PageEndPanel;
import me.grax.jbytemod.ui.lists.SearchList;
import me.grax.jbytemod.utils.TextUtils;
import me.grax.jbytemod.utils.list.LazyListModel;
import me.grax.jbytemod.utils.list.SearchEntry;

public class LdcTask extends SwingWorker<Void, Integer> {

  private PageEndPanel jpb;
  private JByteMod jbm;
  private String ldc;
  private boolean exact;
  private boolean caseSens;
  private Pattern pattern;
  private SearchList sl;

  public LdcTask(SearchList sl, JByteMod jbm, String ldc, boolean exact, boolean caseSens, boolean regex) {
    this.sl = sl;
    this.jbm = jbm;
    this.jpb = jbm.getPP();
    this.exact = exact;
    this.caseSens = caseSens;
    if (regex) {
      this.pattern = Pattern.compile(ldc);
    }

    if (!caseSens) {
      this.ldc = ldc.toLowerCase();
    } else {
      this.ldc = ldc;
    }
  }

  public LdcTask(SearchList sl, JByteMod jbm, Pattern p) {
    this.sl = sl;
    this.jbm = jbm;
    this.jpb = jbm.getPP();
    this.pattern = p;
  }

  @Override
  protected Void doInBackground() throws Exception {
    LazyListModel<SearchEntry> model = new LazyListModel<>();
    Collection<ClassNode> values = jbm.getFile().getClasses().values();
    double size = values.size();
    double i = 0;
    boolean exact = this.exact;
    boolean regex = this.pattern != null;
    for (ClassNode cn : values) {
      for (MethodNode mn : cn.methods) {
        for (AbstractInsnNode ain : mn.instructions) {
          if (ain.getType() == AbstractInsnNode.LDC_INSN) {
            LdcInsnNode lin = (LdcInsnNode) ain;
            String cst = lin.cst.toString();
            if (!caseSens) {
              cst = cst.toLowerCase();
            }
            if (regex ? pattern.matcher(cst).matches() : (exact ? cst.equals(ldc) : cst.contains(ldc))) {
              model.addElement(new SearchEntry(cn, mn, TextUtils.escape(TextUtils.max(lin.cst.toString(), 100))));
            }
          }
        }
      }
      publish(Math.min((int) (i++ / size * 100d) + 1, 100));
    }
    sl.setModel(model);
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
    JByteMod.LOGGER.log("Search finished!");
  }
}