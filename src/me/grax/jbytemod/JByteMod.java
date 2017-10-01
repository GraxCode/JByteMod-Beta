package me.grax.jbytemod;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import me.grax.jbytemod.res.LanguageRes;
import me.grax.jbytemod.res.Options;
import me.grax.jbytemod.ui.ClassTree;
import me.grax.jbytemod.ui.DecompilerPanel;
import me.grax.jbytemod.ui.InfoPanel;
import me.grax.jbytemod.ui.MyMenuBar;
import me.grax.jbytemod.ui.MySplitPane;
import me.grax.jbytemod.ui.MyTabbedPane;
import me.grax.jbytemod.ui.PageEndPanel;
import me.grax.jbytemod.ui.lists.LVPList;
import me.grax.jbytemod.ui.lists.MyCodeList;
import me.grax.jbytemod.ui.lists.SearchList;
import me.grax.jbytemod.ui.lists.TCBList;
import me.grax.jbytemod.utils.ThemeChanges;
import me.grax.jbytemod.utils.ErrorDisplay;
import me.grax.jbytemod.utils.gui.LookUtils;
import me.grax.jbytemod.utils.task.SaveTask;
import me.grax.jbytemod.utils.tree.SortedTreeNode;
import me.lpk.util.OpUtils;

public class JByteMod extends JFrame {

  private JPanel contentPane;

  private final LanguageRes res = new LanguageRes();
  private final Options ops = new Options();

  private JarFile file;

  private ClassTree jarTree;

  private MyCodeList clist;

  private PageEndPanel pp;

  private SearchList slist;

  private DecompilerPanel dp;

  private TCBList tcblist;

  private ClassNode currentNode;

  private MyTabbedPane tabbedPane;

  private InfoPanel sp;

  private LVPList lvplist;

  public static JByteMod instance;
  public static Color border;
  /**
   * Launch the application.
   */
  public static void main(String[] args) {
    EventQueue.invokeLater(new Runnable() {

      public void run() {
        try {
          LookUtils.setLAF();
          ThemeChanges.setDefaults();
          JByteMod frame = new JByteMod();
          frame.setVisible(true);
          instance = frame;
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    });
  }

  /**
   * Create the frame.
   */
  public JByteMod() {
    this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    this.addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent we) {
        if (JOptionPane.showConfirmDialog(JByteMod.this, res.getResource("exit_warn"), res.getResource("is_sure"),
            JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
          Runtime.getRuntime().exit(0);
        }
      }
    });
    border = UIManager.getColor("nimbusBorder");
    this.setBounds(100, 100, 1280, 720);
    this.setTitle("JByteMod 1.2.0");
    this.setJMenuBar(new MyMenuBar(this));
    this.jarTree = new ClassTree(this);
    contentPane = new JPanel();
    contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
    contentPane.setLayout(new BorderLayout(0, 0));
    this.setContentPane(contentPane);
    this.setTCBList(new TCBList());
    this.setLVPList(new LVPList());
    JPanel border = new JPanel();
    border.setBorder(new LineBorder(JByteMod.border));
    border.setLayout(new GridLayout());
    JSplitPane splitPane = new MySplitPane(this, jarTree);
    JPanel b2 = new JPanel();
    b2.setBorder(new EmptyBorder(5, 0, 5, 0));
    b2.setLayout(new GridLayout());
    b2.add(splitPane);
    border.add(b2);
    contentPane.add(border, BorderLayout.CENTER);
    contentPane.add(pp = new PageEndPanel(), BorderLayout.PAGE_END);
  }

  /**
   * Load .jar file
   */
  public void loadFile(File input) {
    String ap = input.getAbsolutePath();
    if (ap.endsWith(".jar")) {
      try {
        this.file = new JarFile(this, input);
      } catch (Throwable e) {
        new ErrorDisplay(e);
      }
    } else {
      new ErrorDisplay(new UnsupportedOperationException(res.getResource("jar_warn")));
    }
  }

  public void refreshTree() {
    System.out.println("Successfully loaded file!");
    System.out.println("Building tree..");
    this.jarTree.refreshTree(file);
  }

  public void saveFile(File output) {
    try {
      new SaveTask(this, output, file).execute();
    } catch (Throwable t) {
      new ErrorDisplay(t);
    }
  }

  public void selectMethod(ClassNode cn, MethodNode mn) {
    this.currentNode = cn;
    sp.selectMethod(cn, mn);
    if (!clist.loadInstructions(mn)) {
      clist.setSelectedIndex(-1);
    }
    tcblist.addNodes(cn, mn);
    lvplist.addNodes(cn, mn);
    dp.setText("");
    tabbedPane.selectClass(cn);
  }
  public void selectClass(ClassNode cn) {
    this.currentNode = cn;
    sp.selectClass(cn);
    clist.loadFields(cn);
    tabbedPane.selectClass(cn);
  }
  public void treeSelection(ClassNode cn, MethodNode mn) {
    if (ops.getBool("tree_search_sel")) {
      //selection may take some time
      new Thread(() -> {
        DefaultTreeModel tm = (DefaultTreeModel) jarTree.getModel();
        this.selectEntry(mn, tm, (SortedTreeNode) tm.getRoot());
      }).start();
    }
  }

  private void selectEntry(MethodNode mn, DefaultTreeModel tm, SortedTreeNode node) {
    for (int i = 0; i < tm.getChildCount(node); i++) {
      SortedTreeNode child = (SortedTreeNode) tm.getChild(node, i);
      if (child.getMn() != null && child.getMn().equals(mn)) {
        TreePath tp = new TreePath(tm.getPathToRoot(child));
        jarTree.setSelectionPath(tp);
        jarTree.scrollPathToVisible(tp);
        break;
      }
      if (!child.isLeaf()) {
        selectEntry(mn, tm, child);
      }
    }
  }

  public ClassNode getCurrentNode() {
    return currentNode;
  }

  public JarFile getFile() {
    return file;
  }

  public MyCodeList getCodeList() {
    return clist;
  }

  public void setCodeList(MyCodeList list) {
    this.clist = list;
  }

  public PageEndPanel getPP() {
    return pp;
  }

  public void setSearchlist(SearchList searchList) {
    this.slist = searchList;
  }

  public SearchList getSearchList() {
    return slist;
  }

  public ClassTree getJarTree() {
    return jarTree;
  }

  public Options getOps() {
    return ops;
  }

  public LanguageRes getRes() {
    return res;
  }

  public void setDP(DecompilerPanel dp) {
    this.dp = dp;
  }

  public void setTCBList(TCBList tcb) {
    this.tcblist = tcb;
  }

  private void setLVPList(LVPList lvp) {
    this.lvplist = lvp;
  }

  public LVPList getLVPList() {
    return lvplist;
  }

  public void setTabbedPane(MyTabbedPane tp) {
    this.tabbedPane = tp;
  }

  public TCBList getTCBList() {
    return tcblist;
  }

  public void setSP(InfoPanel sp) {
    this.sp = sp;
  }
}
