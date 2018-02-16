package me.grax.jbytemod.utils.task.search;

import java.util.Collection;
import java.util.List;

import javax.swing.SwingWorker;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import me.grax.jbytemod.JByteMod;
import me.grax.jbytemod.ui.PageEndPanel;
import me.grax.jbytemod.ui.lists.SearchList;
import me.grax.jbytemod.utils.list.LazyListModel;
import me.grax.jbytemod.utils.list.SearchEntry;

public class ReferenceTask extends SwingWorker<Void, Integer> {

  private SearchList sl;
  private PageEndPanel jpb;
  private JByteMod jbm;
  private boolean exact;
  private String owner;
  private String name;
  private String desc;
  private boolean field;

  public ReferenceTask(SearchList sl, JByteMod jbm, String owner, String name, String desc, boolean exact, boolean field) {
    this.sl = sl;
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
    LazyListModel<SearchEntry> model = new LazyListModel<SearchEntry>();
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
    jpb.setValue(100);
    JByteMod.LOGGER.log("Search finished!");
  }
}