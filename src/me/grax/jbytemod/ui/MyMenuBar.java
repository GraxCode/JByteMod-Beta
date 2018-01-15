package me.grax.jbytemod.ui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashMap;

import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.io.IOUtils;

import me.grax.jbytemod.JByteMod;
import me.grax.jbytemod.res.LanguageRes;
import me.grax.jbytemod.res.Option;
import me.grax.jbytemod.res.Options;
import me.grax.jbytemod.utils.ErrorDisplay;

public class MyMenuBar extends JMenuBar {

  private JByteMod jam;
  private File lastFile;

  public MyMenuBar(JByteMod jam) {
    this.jam = jam;
    this.initFileMenu();
  }

  private void initFileMenu() {
    JMenu file = new JMenu("File");
    JMenuItem save = new JMenuItem("Save");
    JMenuItem saveas = new JMenuItem("Save As..");
    JMenuItem load = new JMenuItem("Load");
    load.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        openLoadDialogue();
      }
    });
    save.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (lastFile != null) {
          jam.saveFile(lastFile);
        } else {
          openSaveDialogue();
        }
      }
    });
    saveas.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        openSaveDialogue();
      }
    });
    save.setAccelerator(KeyStroke.getKeyStroke('S', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
    load.setAccelerator(KeyStroke.getKeyStroke('N', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
    file.add(save);
    file.add(saveas);
    file.add(load);
    this.add(file);

    JMenu search = new JMenu("Search");
    JMenuItem ldc = new JMenuItem("Search LDC");
    ldc.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        searchLDC();
      }
    });

    search.add(ldc);
    JMenuItem field = new JMenuItem("Search FieldInsnNode");
    field.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        searchField();
      }
    });

    search.add(field);
    JMenuItem method = new JMenuItem("Search MethodInsnNode");
    method.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        searchMethod();
      }
    });

    search.add(method);
    this.add(search);
    JMenu utils = new JMenu("Utils");
    JMenuItem accman = new JMenuItem("Access Helper");
    accman.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        new JAccessHelper().setVisible(true);
      }
    });
    utils.add(accman);
    this.add(utils);
    JMenu tree = new JMenu("Tree");
    utils.add(tree);
    JMenuItem rltree = new JMenuItem("Reload Tree");
    rltree.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        jam.getJarTree().refreshTree(jam.getFile());
      }
    });
    tree.add(rltree);
    JMenuItem collapse = new JMenuItem("Collapse all");
    collapse.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        jam.getJarTree().collapseAll();
      }
    });
    tree.add(collapse);
    this.add(getSettings());
    JMenu help = new JMenu("Help");
    JMenuItem about = new JMenuItem("About");
    about.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        try {
          new JAboutFrame(jam).setVisible(true);
        } catch (Exception ex) {
          new ErrorDisplay(ex);
        }
      }
    });

    help.add(about);
    JMenuItem licenses = new JMenuItem("Licenses");
    licenses.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        try {
          JFrame jf = new JFrame();
          jf.setBounds(100, 100, 700, 800);
          jf.add(new JScrollPane(new JTextArea(IOUtils.toString(MyMenuBar.class.getResourceAsStream("/resources/LICENSES")))));
          jf.setTitle("Licenses");
          jf.setVisible(true);
        } catch (Exception ex) {
          new ErrorDisplay(ex);
        }
      }
    });

    help.add(licenses);
    this.add(help);
  }

  private JMenu getSettings() {
    JMenu settings = new JMenu("Settings");
    LanguageRes lr = JByteMod.res;
    Options o = JByteMod.ops;
    HashMap<String, JMenu> menus = new LinkedHashMap<>();
    for (Option op : o.bools) {
      String group = lr.getResource(op.getGroup());
      JMenu menu = null;
      if (menus.containsKey(group)) {
        menu = menus.get(group);
      } else {
        menus.put(group, menu = new JMenu(group));
      }
      switch (op.getType()) {
      case BOOLEAN:
        JCheckBoxMenuItem jmi = new JCheckBoxMenuItem(lr.getResource(op.getName()), op.getBoolean());
        jmi.addActionListener(new ActionListener() {

          @Override
          public void actionPerformed(ActionEvent e) {
            op.setValue(jmi.isSelected());
            o.save();
          }
        });
        menu.add(jmi);
        break;
      case STRING:
        JMenu jm = new JMenu(lr.getResource(op.getName()));
        JTextField jtf = new JTextField(op.getString());
        jm.add(jtf);
        jtf.addFocusListener(new FocusAdapter() {
          public void focusLost(FocusEvent e) {
            op.setValue(jtf.getText());
            o.save();
          }
        });
        menu.add(jm);
        break;
      default:
        break;
      }
    }
    for (JMenu m : menus.values()) {
      settings.add(m);
    }
    return settings;
  }

  protected void searchLDC() {
    final JPanel panel = new JPanel(new BorderLayout(5, 5));
    final JPanel input = new JPanel(new GridLayout(0, 1));
    final JPanel labels = new JPanel(new GridLayout(0, 1));
    panel.add(labels, "West");
    panel.add(input, "Center");
    panel.add(new JLabel("Warning: This could take some time\n on short strings!"), "South");
    labels.add(new JLabel("String Constant:"));
    JTextField cst = new JTextField();
    input.add(cst);
    JCheckBox exact = new JCheckBox("Exact");
    JCheckBox snstv = new JCheckBox("Case sensitive");
    labels.add(exact);
    input.add(snstv);
    if (JOptionPane.showConfirmDialog(this.jam, panel, "Search LDC", 2) == JOptionPane.OK_OPTION && !cst.getText().isEmpty()) {
      jam.getSearchList().searchForString(cst.getText(), exact.isSelected(), snstv.isSelected());
    }
  }

  protected void searchField() {
    final JPanel panel = new JPanel(new BorderLayout(5, 5));
    final JPanel input = new JPanel(new GridLayout(0, 1));
    final JPanel labels = new JPanel(new GridLayout(0, 1));
    panel.add(labels, "West");
    panel.add(input, "Center");
    panel.add(new JLabel("Warning: This could take some time\n on big jars!"), "South");
    labels.add(new JLabel("Owner:"));
    JTextField owner = new JTextField();
    input.add(owner);
    labels.add(new JLabel("Name:"));
    JTextField name = new JTextField();
    input.add(name);
    labels.add(new JLabel("Desc:"));
    JTextField desc = new JTextField();
    input.add(desc);
    JCheckBox exact = new JCheckBox("Exact");
    labels.add(exact);
    input.add(new JPanel());
    if (JOptionPane.showConfirmDialog(JByteMod.instance, panel, "Search FieldInsnNode", 2) == JOptionPane.OK_OPTION
        && !(name.getText().isEmpty() && owner.getText().isEmpty() && desc.getText().isEmpty())) {
      jam.getSearchList().searchForFMInsn(owner.getText(), name.getText(), desc.getText(), exact.isSelected(), true);
    }
  }

  protected void searchMethod() {
    final JPanel panel = new JPanel(new BorderLayout(5, 5));
    final JPanel input = new JPanel(new GridLayout(0, 1));
    final JPanel labels = new JPanel(new GridLayout(0, 1));
    panel.add(labels, "West");
    panel.add(input, "Center");
    panel.add(new JLabel("Warning: This could take some time\n on big jars!"), "South");
    labels.add(new JLabel("Owner:"));
    JTextField owner = new JTextField();
    input.add(owner);
    labels.add(new JLabel("Name:"));
    JTextField name = new JTextField();
    input.add(name);
    labels.add(new JLabel("Desc:"));
    JTextField desc = new JTextField();
    input.add(desc);
    JCheckBox exact = new JCheckBox("Exact");
    labels.add(exact);
    input.add(new JPanel());
    if (JOptionPane.showConfirmDialog(JByteMod.instance, panel, "Search MethodInsnNode", 2) == JOptionPane.OK_OPTION
        && !(name.getText().isEmpty() && owner.getText().isEmpty() && desc.getText().isEmpty())) {
      jam.getSearchList().searchForFMInsn(owner.getText(), name.getText(), desc.getText(), exact.isSelected(), false);
    }
  }

  protected void openSaveDialogue() {
    JFileChooser jfc = new JFileChooser(new File(System.getProperty("user.home") + File.separator + "Desktop"));
    jfc.setAcceptAllFileFilterUsed(false);
    jfc.setFileFilter(new FileNameExtensionFilter("Java Package (*.jar)", "jar"));
    int result = jfc.showSaveDialog(this);
    if (result == JFileChooser.APPROVE_OPTION) {
      File output = jfc.getSelectedFile();
      this.lastFile = output;
      System.out.println("Selected output file: " + output.getAbsolutePath());
      jam.saveFile(output);
    }
  }

  protected void openLoadDialogue() {
    JFileChooser jfc = new JFileChooser(new File(System.getProperty("user.home") + File.separator + "Desktop"));
    jfc.setAcceptAllFileFilterUsed(false);
    jfc.setFileFilter(new FileNameExtensionFilter("Java Package (*.jar)", "jar"));
    int result = jfc.showOpenDialog(this);
    if (result == JFileChooser.APPROVE_OPTION) {
      File input = jfc.getSelectedFile();
      System.out.println("Selected input file: " + input.getAbsolutePath());
      jam.loadFile(input);
    }
  }
}
