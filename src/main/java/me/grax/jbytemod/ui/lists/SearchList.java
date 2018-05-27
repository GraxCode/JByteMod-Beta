package me.grax.jbytemod.ui.lists;

import java.awt.Font;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.regex.Pattern;

import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import me.grax.jbytemod.JByteMod;
import me.grax.jbytemod.ui.lists.entries.SearchEntry;
import me.grax.jbytemod.utils.list.LazyListModel;
import me.grax.jbytemod.utils.task.search.LdcTask;
import me.grax.jbytemod.utils.task.search.ReferenceTask;

public class SearchList extends JList<SearchEntry> {

  private JByteMod jbm;

  public SearchList(JByteMod jbm) {
    super(new LazyListModel<SearchEntry>());
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
            }
          });
          menu.add(decl);
          JMenuItem treeEntry = new JMenuItem(JByteMod.res.getResource("select_tree"));
          treeEntry.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
              ClassNode cn = SearchList.this.getSelectedValue().getCn();
              MethodNode mn = SearchList.this.getSelectedValue().getMn();
              jbm.treeSelection(cn, mn);
            }
          });
          menu.add(treeEntry);
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
    this.setPrototypeCellValue(new SearchEntry());
  }

  public void searchForConstant(String ldc, boolean exact, boolean cs, boolean regex) {
    new LdcTask(this, jbm, ldc, exact, cs, regex).execute();
  }

  public void searchForPatternRegex(Pattern p) {
    new LdcTask(this, jbm, p).execute();
  }

  public void searchForFMInsn(String owner, String name, String desc, boolean exact, boolean field) {
    new ReferenceTask(this, jbm, owner, name, desc, exact, field).execute();
  }
}
