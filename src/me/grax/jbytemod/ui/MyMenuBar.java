package me.grax.jbytemod.ui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import me.grax.jbytemod.JByteMod;
import me.grax.jbytemod.res.LanguageRes;
import me.grax.jbytemod.res.Options;
import me.grax.jbytemod.ui.lists.SearchList;

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
        if(lastFile != null) {
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
    this.add(search);
    this.add(getSettings());
  }

  private JMenu getSettings() {
    JMenu settings = new JMenu("Settings");
    Options o = jam.getOps();
    LanguageRes lr = jam.getRes();
    for(String s : Options.bools) {
      JCheckBoxMenuItem jmi = new JCheckBoxMenuItem(lr.getResource(s), o.getBool(s));
      jmi.addActionListener(new ActionListener() {
        
        @Override
        public void actionPerformed(ActionEvent e) {
          o.setProperty(s, String.valueOf(jmi.isSelected()));
        }
      });
      settings.add(jmi);
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
    if (JOptionPane.showConfirmDialog(this.jam, panel, "Search LDC", 2) == JOptionPane.OK_OPTION
        && !cst.getText().isEmpty()) {
      jam.getSearchList().searchForString(cst.getText(), exact.isSelected(), snstv.isSelected());
    }    
  }

  protected void openSaveDialogue() {
    JFileChooser jfc = new JFileChooser(new File(System.getProperty("user.home") + File.separator + "Desktop"));
    jfc.setAcceptAllFileFilterUsed(false);
    jfc.setFileFilter(new FileNameExtensionFilter("Java Package", "jar"));
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
    jfc.setFileFilter(new FileNameExtensionFilter("Java Package", "jar"));
    int result = jfc.showOpenDialog(this);
    if (result == JFileChooser.APPROVE_OPTION) {
      File input = jfc.getSelectedFile();
      System.out.println("Selected input file: " + input.getAbsolutePath());
      jam.loadFile(input);
    }
  }
}
