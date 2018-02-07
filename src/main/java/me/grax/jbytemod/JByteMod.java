package me.grax.jbytemod;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.GridLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.LinkedHashMap;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.ListModel;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import me.grax.jbytemod.logging.Logging;
import me.grax.jbytemod.plugin.Plugin;
import me.grax.jbytemod.plugin.PluginManager;
import me.grax.jbytemod.res.LanguageRes;
import me.grax.jbytemod.res.Options;
import me.grax.jbytemod.ui.ClassTree;
import me.grax.jbytemod.ui.ControlFlowPanel;
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
import me.grax.jbytemod.utils.ErrorDisplay;
import me.grax.jbytemod.utils.asm.FrameGen;
import me.grax.jbytemod.utils.gui.LookUtils;
import me.grax.jbytemod.utils.task.SaveTask;
import me.grax.jbytemod.utils.tree.SortedTreeNode;
import me.lpk.util.ASMUtils;
import me.lpk.util.OpUtils;

public class JByteMod extends JFrame {

  public static final Logging LOGGER = new Logging();
  public static final LanguageRes res = new LanguageRes();
  public static final Options ops = new Options();

  private static boolean lafInit;

  private static JarArchive file;
  public static HashMap<ClassNode, MethodNode> lastSelectedTreeEntries = new LinkedHashMap<>();

  private JPanel contentPane;
  private ClassTree jarTree;
  private MyCodeList clist;
  private PageEndPanel pp;
  private SearchList slist;
  private DecompilerPanel dp;
  private TCBList tcblist;
  private MyTabbedPane tabbedPane;
  private InfoPanel sp;
  private LVPList lvplist;
  private ControlFlowPanel cfp;
  private MyMenuBar myMenuBar;

  private ClassNode currentNode;
  private MethodNode currentMethod;

  public static JByteMod instance;
  public static Color border;
  private PluginManager pluginManager;

  /**
   * Launch the application.
   */
  public static void main(String[] args) {
    EventQueue.invokeLater(new Runnable() {

      public void run() {
        try {
          if (!lafInit) {
            LookUtils.setLAF();
            lafInit = true;
          }
          JByteMod frame = new JByteMod();
          instance = frame;
          frame.setVisible(true);
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
    if (ops.get("use_rt").getBoolean()) {
      new FrameGen().start();
    }
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
    if (border == null) {
      border = new Color(146, 151, 161);
    }
    this.setBounds(100, 100, 1280, 720);
    this.setTitle("JByteMod 1.5.3");
    this.setJMenuBar(myMenuBar = new MyMenuBar(this));
    this.jarTree = new ClassTree(this);
    contentPane = new JPanel();
    contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
    contentPane.setLayout(new BorderLayout(0, 0));
    this.setContentPane(contentPane);
    this.setTCBList(new TCBList());
    this.setLVPList(new LVPList());
    JPanel border = new JPanel();
    if (!UIManager.getLookAndFeel().getName().equals("WebLookAndFeel")) {
      //looks better without border for weblaf
      border.setBorder(new LineBorder(JByteMod.border));
    }
    border.setLayout(new GridLayout());
    JSplitPane splitPane = new MySplitPane(this, jarTree);
    JPanel b2 = new JPanel();
    b2.setBorder(new EmptyBorder(5, 0, 5, 0));
    b2.setLayout(new GridLayout());
    b2.add(splitPane);
    border.add(b2);
    contentPane.add(border, BorderLayout.CENTER);
    contentPane.add(pp = new PageEndPanel(), BorderLayout.PAGE_END);
    if (file != null) {
      this.refreshTree();
    }
  }

  public void changeUI(String clazz) {
    LookUtils.changeLAF(clazz);
  }

  @Override
  public void setVisible(boolean b) {
    this.setPluginManager(new PluginManager(this));
    this.myMenuBar.addPluginMenu(pluginManager.getPlugins());
    super.setVisible(b);
  }

  public MyMenuBar getMyMenuBar() {
    return myMenuBar;
  }

  public PluginManager getPluginManager() {
    return pluginManager;
  }

  public void setPluginManager(PluginManager pluginManager) {
    this.pluginManager = pluginManager;
  }

  /**
   * Load .jar or .class file
   */
  public void loadFile(File input) {
    String ap = input.getAbsolutePath();
    if (ap.endsWith(".jar")) {
      try {
        file = new JarArchive(this, input);
      } catch (Throwable e) {
        new ErrorDisplay(e);
      }
    } else if (ap.endsWith(".class")) {
      try {
        file = new JarArchive(ASMUtils.getNode(Files.readAllBytes(input.toPath())));
        this.refreshTree();
      } catch (Throwable e) {
        new ErrorDisplay(e);
      }
    } else {
      new ErrorDisplay(new UnsupportedOperationException(res.getResource("jar_warn")));
    }
    for (Plugin p : pluginManager.getPlugins()) {
      p.loadFile(file.getClasses());
    }
  }

  public void refreshTree() {
    LOGGER.log("Building tree..");
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
    if (ops.get("select_code_tab").getBoolean()) {
      tabbedPane.setSelectedIndex(0);
    }
    OpUtils.clearLabelCache();
    this.currentNode = cn;
    this.currentMethod = mn;
    sp.selectMethod(cn, mn);
    if (!clist.loadInstructions(mn)) {
      clist.setSelectedIndex(-1);
    }
    tcblist.addNodes(cn, mn);
    lvplist.addNodes(cn, mn);
    cfp.setNode(mn);
    dp.setText("");
    tabbedPane.selectMethod(cn, mn);
    lastSelectedTreeEntries.put(cn, mn);
    if (lastSelectedTreeEntries.size() > 5) {
      lastSelectedTreeEntries.remove(lastSelectedTreeEntries.keySet().iterator().next());
    }
  }

  public void selectClass(ClassNode cn) {
    if (ops.get("select_code_tab").getBoolean()) {
      tabbedPane.setSelectedIndex(0);
    }
    this.currentNode = cn;
    sp.selectClass(cn);
    clist.loadFields(cn);
    tabbedPane.selectClass(cn);
    lastSelectedTreeEntries.put(cn, null);
    if (lastSelectedTreeEntries.size() > 5) {
      lastSelectedTreeEntries.remove(lastSelectedTreeEntries.keySet().iterator().next());
    }
  }

  public void treeSelection(ClassNode cn, MethodNode mn) {
    //selection may take some time
    new Thread(() -> {
      DefaultTreeModel tm = (DefaultTreeModel) jarTree.getModel();
      if (this.selectEntry(mn, tm, (SortedTreeNode) tm.getRoot())) {
        jarTree.repaint();
      }
    }).start();
  }

  private boolean selectEntry(MethodNode mn, DefaultTreeModel tm, SortedTreeNode node) {
    for (int i = 0; i < tm.getChildCount(node); i++) {
      SortedTreeNode child = (SortedTreeNode) tm.getChild(node, i);
      if (child.getMn() != null && child.getMn().equals(mn)) {
        TreePath tp = new TreePath(tm.getPathToRoot(child));
        jarTree.setSelectionPath(tp);
        jarTree.scrollPathToVisible(tp);
        return true;
      }
      if (!child.isLeaf()) {
        if (selectEntry(mn, tm, child)) {
          return true;
        }
      }
    }
    return false;
  }

  public ClassNode getCurrentNode() {
    return currentNode;
  }

  public MethodNode getCurrentMethod() {
    return currentMethod;
  }

  public JarArchive getFile() {
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

  public void setCFP(ControlFlowPanel cfp) {
    this.cfp = cfp;
  }

  public static void restartGUI() {
    instance.dispose();
    instance = null;
    System.gc();
    JByteMod.main(new String[0]);
  }

  public static void resetLAF() {
    lafInit = false;
  }
}
