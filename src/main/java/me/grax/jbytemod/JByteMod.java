package me.grax.jbytemod;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.LinkedHashMap;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import com.sun.tools.attach.VirtualMachine;

import me.grax.jbytemod.logging.Logging;
import me.grax.jbytemod.plugin.Plugin;
import me.grax.jbytemod.plugin.PluginManager;
import me.grax.jbytemod.res.LanguageRes;
import me.grax.jbytemod.res.Options;
import me.grax.jbytemod.ui.ClassTree;
import me.grax.jbytemod.ui.DecompilerPanel;
import me.grax.jbytemod.ui.InfoPanel;
import me.grax.jbytemod.ui.MyMenuBar;
import me.grax.jbytemod.ui.MySplitPane;
import me.grax.jbytemod.ui.MyTabbedPane;
import me.grax.jbytemod.ui.MyToolBar;
import me.grax.jbytemod.ui.PageEndPanel;
import me.grax.jbytemod.ui.graph.ControlFlowPanel;
import me.grax.jbytemod.ui.lists.LVPList;
import me.grax.jbytemod.ui.lists.MyCodeList;
import me.grax.jbytemod.ui.lists.SearchList;
import me.grax.jbytemod.ui.lists.TCBList;
import me.grax.jbytemod.ui.tree.SortedTreeNode;
import me.grax.jbytemod.utils.ErrorDisplay;
import me.grax.jbytemod.utils.FileUtils;
import me.grax.jbytemod.utils.asm.FrameGen;
import me.grax.jbytemod.utils.attach.RuntimeJarArchive;
import me.grax.jbytemod.utils.gui.LookUtils;
import me.grax.jbytemod.utils.task.AttachTask;
import me.grax.jbytemod.utils.task.RetransformTask;
import me.grax.jbytemod.utils.task.SaveTask;
import me.lpk.util.ASMUtils;
import me.lpk.util.OpUtils;

public class JByteMod extends JFrame {

  public static File workingDir = new File(".");
  public static String configPath = "jbytemod.cfg";
  public static Logging LOGGER;
  public static LanguageRes res;
  public static Options ops;

  private static boolean lafInit;

  private static JarArchive file;
  public static HashMap<ClassNode, MethodNode> lastSelectedTreeEntries = new LinkedHashMap<>();

  private static Instrumentation agentInstrumentation;

  public static JByteMod instance;
  public static Color border;
  private static final String jbytemod = "JByteMod 1.8.1";
  static {
    try {
      System.loadLibrary("attach");
    } catch (Throwable t) {
      t.printStackTrace();
    }
  }

  public static void agentmain(String agentArgs, Instrumentation ins) {
    if (!ins.isRedefineClassesSupported()) {
      JOptionPane.showMessageDialog(null, "Class redefinition is disabled, cannot attach!");
      return;
    }
    agentInstrumentation = ins;
    workingDir = new File(agentArgs);
    initialize();
    if (!lafInit) {
      LookUtils.setLAF();
      lafInit = true;
    }
    JByteMod.file = new RuntimeJarArchive(ins);
    JByteMod frame = new JByteMod(true);
    frame.setTitleSuffix("Agent");
    instance = frame;
    frame.setVisible(true);
  }

  public static void initialize() {
    LOGGER = new Logging();
    res = new LanguageRes();
    ops = new Options();
    try {
      System.setProperty("file.encoding", "UTF-8");
      Field charset = Charset.class.getDeclaredField("defaultCharset");
      charset.setAccessible(true);
      charset.set(null, null);
    } catch (Throwable t) {
      JByteMod.LOGGER.err("Failed to set encoding to UTF-8 (" + t.getMessage() + ")");
    }
  }

  /**
   * Launch the application.
   */
  public static void main(String[] args) {
    org.apache.commons.cli.Options options = new org.apache.commons.cli.Options();
    options.addOption("f", "file", true, "File to open");
    options.addOption("d", "dir", true, "Working directory");
    options.addOption("c", "config", true, "Config file name");
    options.addOption("?", "help", false, "Prints this help");

    CommandLineParser parser = new DefaultParser();
    CommandLine line;
    try {
      line = parser.parse(options, args);
    } catch (org.apache.commons.cli.ParseException e) {
      e.printStackTrace();
      throw new RuntimeException("An error occurred while parsing the commandline ");
    }
    if (line.hasOption("help")) {
      HelpFormatter formatter = new HelpFormatter();
      formatter.printHelp(jbytemod, options);
      return;
    }
    if (line.hasOption("d")) {
      workingDir = new File(line.getOptionValue("d"));
      if (!(workingDir.exists() && workingDir.isDirectory())) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(jbytemod, options);
        return;
      }
      JByteMod.LOGGER.err("Specified working dir set");
    }
    if (line.hasOption("c")) {
      configPath = line.getOptionValue("c");
    }
    initialize();
    EventQueue.invokeLater(new Runnable() {

      public void run() {
        try {
          if (!lafInit) {
            LookUtils.setLAF();
            lafInit = true;
          }
          JByteMod frame = new JByteMod(false);
          instance = frame;
          frame.setVisible(true);
          if (line.hasOption("f")) {
            File input = new File(line.getOptionValue("f"));
            if (FileUtils.exists(input) && FileUtils.isType(input, ".jar", ".class")) {
              frame.loadFile(input);
              JByteMod.LOGGER.log("Specified file loaded");
            } else {
              JByteMod.LOGGER.err("Specified file not found");
            }
          }
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    });
  }

  public static void resetLAF() {
    lafInit = false;
  }

  public static void restartGUI() {
    instance.dispose();
    instance = null;
    System.gc();
    JByteMod.main(new String[0]);
  }

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

  private PluginManager pluginManager;

  private File filePath;

  /**
   * Create the frame.
   */
  public JByteMod(boolean agent) {
    if (ops.get("use_rt").getBoolean()) {
      new FrameGen().start();
    }
    this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    this.addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent we) {
        if (JOptionPane.showConfirmDialog(JByteMod.this, res.getResource("exit_warn"), res.getResource("is_sure"),
            JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
          if (agent) {
            dispose();
          } else {
            Runtime.getRuntime().exit(0);
          }
        }
      }
    });
    border = UIManager.getColor("nimbusBorder");
    if (border == null) {
      border = new Color(146, 151, 161);
    }
    this.setBounds(100, 100, 1280, 720);
    this.setTitle(jbytemod);
    this.setJMenuBar(myMenuBar = new MyMenuBar(this, agent));
    this.jarTree = new ClassTree(this);
    contentPane = new JPanel();
    contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
    contentPane.setLayout(new BorderLayout(5, 5));
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
    contentPane.add(new MyToolBar(this), BorderLayout.PAGE_START);
    if (file != null) {
      this.refreshTree();
    }
  }

  public void applyChangesAgent() {
    if (agentInstrumentation == null) {
      throw new RuntimeException();
    }
    new RetransformTask(this, agentInstrumentation, file).execute();
  }

  public void attachTo(VirtualMachine vm) throws Exception {
    if (JOptionPane.showConfirmDialog(JByteMod.this, res.getResource("exit_warn"), res.getResource("is_sure"),
        JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
      new AttachTask(this, vm).execute();
    }
  }

  public void changeUI(String clazz) {
    LookUtils.changeLAF(clazz);
  }

  public ControlFlowPanel getCFP() {
    return this.cfp;
  }

  public MyCodeList getCodeList() {
    return clist;
  }

  public MethodNode getCurrentMethod() {
    return currentMethod;
  }

  public ClassNode getCurrentNode() {
    return currentNode;
  }

  public JarArchive getFile() {
    return file;
  }

  public File getFilePath() {
    return filePath;
  }

  public ClassTree getJarTree() {
    return jarTree;
  }

  public LVPList getLVPList() {
    return lvplist;
  }

  public MyMenuBar getMyMenuBar() {
    return myMenuBar;
  }

  public PluginManager getPluginManager() {
    return pluginManager;
  }

  public PageEndPanel getPP() {
    return pp;
  }

  public SearchList getSearchList() {
    return slist;
  }

  public MyTabbedPane getTabbedPane() {
    return tabbedPane;
  }

  public TCBList getTCBList() {
    return tcblist;
  }

  /**
   * Load .jar or .class file
   */
  public void loadFile(File input) {
    this.filePath = input;
    String ap = input.getAbsolutePath();
    if (ap.endsWith(".jar")) {
      try {
        file = new JarArchive(this, input);
        this.setTitleSuffix(input.getName());
      } catch (Throwable e) {
        new ErrorDisplay(e);
      }
    } else if (ap.endsWith(".class")) {
      try {
        file = new JarArchive(ASMUtils.getNode(Files.readAllBytes(input.toPath())));
        this.setTitleSuffix(input.getName());
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

  public void refreshAgentClasses() {
    if (agentInstrumentation == null) {
      throw new RuntimeException();
    }
    this.refreshTree();
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

  public void selectClass(ClassNode cn) {
    if (ops.get("select_code_tab").getBoolean()) {
      tabbedPane.setSelectedIndex(0);
    }
    this.currentNode = cn;
    this.currentMethod = null;
    sp.selectClass(cn);
    clist.loadFields(cn);
    tabbedPane.selectClass(cn);
    lastSelectedTreeEntries.put(cn, null);
    if (lastSelectedTreeEntries.size() > 5) {
      lastSelectedTreeEntries.remove(lastSelectedTreeEntries.keySet().iterator().next());
    }
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

  public void setCFP(ControlFlowPanel cfp) {
    this.cfp = cfp;
  }

  public void setCodeList(MyCodeList list) {
    this.clist = list;
  }

  public void setDP(DecompilerPanel dp) {
    this.dp = dp;
  }

  private void setLVPList(LVPList lvp) {
    this.lvplist = lvp;
  }

  public void setPluginManager(PluginManager pluginManager) {
    this.pluginManager = pluginManager;
  }

  public void setSearchlist(SearchList searchList) {
    this.slist = searchList;
  }

  public void setSP(InfoPanel sp) {
    this.sp = sp;
  }

  public void setTabbedPane(MyTabbedPane tp) {
    this.tabbedPane = tp;
  }

  public void setTCBList(TCBList tcb) {
    this.tcblist = tcb;
  }

  private void setTitleSuffix(String suffix) {
    this.setTitle(jbytemod + " - " + suffix);
  }

  @Override
  public void setVisible(boolean b) {
    this.setPluginManager(new PluginManager(this));
    this.myMenuBar.addPluginMenu(pluginManager.getPlugins());
    super.setVisible(b);
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
}
