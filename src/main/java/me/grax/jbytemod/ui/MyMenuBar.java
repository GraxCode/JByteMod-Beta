package me.grax.jbytemod.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
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
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.util.CheckClassAdapter;

import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;

import android.util.Patterns;
import me.grax.jbytemod.JByteMod;
import me.grax.jbytemod.plugin.Plugin;
import me.grax.jbytemod.res.LanguageRes;
import me.grax.jbytemod.res.Option;
import me.grax.jbytemod.res.Options;
import me.grax.jbytemod.ui.dialogue.ClassDialogue;
import me.grax.jbytemod.ui.lists.entries.SearchEntry;
import me.grax.jbytemod.utils.ErrorDisplay;
import me.grax.jbytemod.utils.TextUtils;
import me.grax.jbytemod.utils.attach.AttachUtils;
import me.grax.jbytemod.utils.list.LazyListModel;
import me.itzsomebody.radon.utils.BytecodeUtils;
import me.lpk.analysis.Sandbox;
import me.lpk.util.OpUtils;
import sun.tools.attach.WindowsAttachProvider;

public class MyMenuBar extends JMenuBar {

  private JByteMod jbm;
  private File lastFile;
  private boolean agent;
  private static final Icon searchIcon = new ImageIcon(MyMenuBar.class.getResource("/resources/search.png"));

  public MyMenuBar(JByteMod jam, boolean agent) {
    this.jbm = jam;
    this.agent = agent;
    this.initFileMenu();
  }

  private void initFileMenu() {
    JMenu file = new JMenu(JByteMod.res.getResource("file"));
    if (!agent) {
      JMenuItem save = new JMenuItem(JByteMod.res.getResource("save"));
      JMenuItem saveas = new JMenuItem(JByteMod.res.getResource("save_as"));
      JMenuItem load = new JMenuItem(JByteMod.res.getResource("load"));
      load.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          openLoadDialogue();
        }
      });
      save.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          if (lastFile != null) {
            jbm.saveFile(lastFile);
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
    } else {
      JMenuItem refresh = new JMenuItem(JByteMod.res.getResource("refresh"));
      refresh.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          jbm.refreshAgentClasses();
        }
      });
      file.add(refresh);
      JMenuItem apply = new JMenuItem(JByteMod.res.getResource("apply"));
      apply.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          jbm.applyChangesAgent();
        }
      });
      file.add(apply);
    }
    this.add(file);

    JMenu search = new JMenu(JByteMod.res.getResource("search"));
    JMenuItem ldc = new JMenuItem(JByteMod.res.getResource("search_ldc"));
    ldc.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        searchLDC();
      }
    });

    search.add(ldc);
    JMenuItem field = new JMenuItem(JByteMod.res.getResource("search_field"));
    field.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        searchField();
      }
    });

    search.add(field);
    JMenuItem method = new JMenuItem(JByteMod.res.getResource("search_method"));
    method.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        searchMethod();
      }
    });

    search.add(method);
    JMenuItem replace = new JMenuItem(JByteMod.res.getResource("replace_ldc"));
    replace.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        replaceLDC();
      }
    });

    search.add(replace);
    this.add(search);
    JMenu utils = new JMenu(JByteMod.res.getResource("utils"));
    JMenuItem accman = new JMenuItem("Access Helper");
    accman.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        new JAccessHelper().setVisible(true);
      }
    });
    utils.add(accman);
    JMenuItem attach = new JMenuItem(JByteMod.res.getResource("attach"));
    attach.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        openProcessSelection();
      }
    });
    utils.add(attach);
    JMenu obf = new JMenu("Obfuscation Analysis");
    utils.add(obf);
    JMenuItem nameobf = new JMenuItem("Name Obfuscation");
    nameobf.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        if (jbm.getFile() != null)
          new JNameObfAnalysis(jbm.getFile().getClasses()).setVisible(true);
      }
    });
    obf.add(nameobf);
    JMenuItem methodobf = new JMenuItem("Method Obfuscation");
    methodobf.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        if (jbm.getFile() != null)
          new JMethodObfAnalysis(jbm.getFile().getClasses()).setVisible(true);
      }
    });
    obf.add(methodobf);
    this.add(utils);
    JMenu tree = new JMenu("Tree");
    utils.add(tree);
    JMenuItem rltree = new JMenuItem(JByteMod.res.getResource("tree_reload"));
    rltree.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        jbm.getJarTree().refreshTree(jbm.getFile());
      }
    });
    tree.add(rltree);
    JMenuItem collapse = new JMenuItem(JByteMod.res.getResource("collapse_all"));
    collapse.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        jbm.getJarTree().collapseAll();
      }
    });
    tree.add(collapse);
    JMenu searchUtils = new JMenu(JByteMod.res.getResource("search"));
    utils.add(searchUtils);
    JMenuItem url = new JMenuItem(JByteMod.res.getResource("url_search"));
    url.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        jbm.getSearchList().searchForPatternRegex(Patterns.AUTOLINK_WEB_URL);
      }
    });
    searchUtils.add(url);
    JMenuItem email = new JMenuItem(JByteMod.res.getResource("email_search"));
    email.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        jbm.getSearchList().searchForPatternRegex(Patterns.EMAIL_ADDRESS);
      }
    });
    searchUtils.add(email);
    //Utils:
    JMenu Utils2 = new JMenu(JByteMod.res.getResource("Utils"));
    utils.add(Utils2);
    
    //From old version of JbyteMod by Grax
    JMenuItem source_debug = new JMenuItem("Rename SourceFiles");
    source_debug.addActionListener(new ActionListener() {

        @Override
        public void actionPerformed(final ActionEvent e) {
        	if(jbm.getFile().getClasses() == null)
        		return;
            if (JOptionPane.showConfirmDialog(null, "Are you sure that you want to rename all SourceFile attributes to debug names?", "Confirm", 0) == 0) {
                int i = 0;
                for (final ClassNode c : jbm.getFile().getClasses().values()) {
                    c.sourceFile = "Class" + i++ + ".java";
                }
            }
        }
    });
    Utils2.add(source_debug);
    
   //From old version of JbyteMod by Grax
    JMenuItem source_debug2 = new JMenuItem("Find SourceFiles");
    source_debug2.addActionListener(new ActionListener() {

        @Override
        public void actionPerformed(final ActionEvent e) {
            if (jbm.getFile().getClasses() == null) {
            	return;
            }
            final JPanel panel = new JPanel(new BorderLayout(5, 5));
            final JPanel input = new JPanel(new GridLayout(0, 1));
            final JPanel labels = new JPanel(new GridLayout(0, 1));
            panel.add(labels, "West");
            panel.add(input, "Center");
            panel.add(new JLabel("Warning: This could take some time\n on big jars!"), "South");
            labels.add(new JLabel("SourceFile:"));
            final JTextField cst = new JTextField();
            input.add(cst);
            if (JOptionPane.showConfirmDialog(JByteMod.instance, panel, "Select Class by SourceFile", 2) == 0 && !cst.getText().isEmpty()) {
                for (final ClassNode cn : jbm.getFile().getClasses().values()) {
                	//Use Contains is Better
                    if (cn.sourceFile != null && cn.sourceFile.contains(cst.getText())) {
                    	jbm.selectClass(cn);
                        break;
                    }
                }
            }
        }
    });
    Utils2.add(source_debug2);
    
  //From old version of JbyteMod by Grax
    JMenuItem clazz_find = new JMenuItem("Find Class by Name");
    clazz_find.addActionListener(new ActionListener() {

        @Override
        public void actionPerformed(final ActionEvent e) {
            if (jbm.getFile().getClasses() == null) {
                return;
            }
            final JPanel panel = new JPanel(new BorderLayout(5, 5));
            final JPanel input = new JPanel(new GridLayout(0, 1));
            final JPanel labels = new JPanel(new GridLayout(0, 1));
            panel.add(labels, "West");
            panel.add(input, "Center");
            panel.add(new JLabel("Warning: This could take some time\n on big jars!"), "South");
            labels.add(new JLabel("Class Name:"));
            final JTextField cst = new JTextField();
            input.add(cst);
            if (JOptionPane.showConfirmDialog(JByteMod.instance, panel, "Select Class by Name", 2) == 0 && !cst.getText().isEmpty()) {
                for (final ClassNode cn : jbm.getFile().getClasses().values()) {
                    if (cn.name != null && cn.name.contains(cst.getText())) {
                        LazyListModel<SearchEntry> model = new LazyListModel<>();
                        model.addElement(new SearchEntry(cn, cn.methods.get(0), TextUtils.escape(TextUtils.max(cn.name, 100))));
                        jbm.getSearchList().setModel(model);
                        break;
                    }
                }
            }
        }
    });
    Utils2.add(clazz_find);
    
  //From old version of JbyteMod by Grax
    JMenuItem clazz_main = new JMenuItem("Find MainClass");
    clazz_main.addActionListener(new ActionListener() {

        @Override
        public void actionPerformed(final ActionEvent e) {
            if (jbm.getFile().getClasses() == null) {
                return;
            }
            for (final ClassNode c : jbm.getFile().getClasses().values()) {
                for (final MethodNode m : c.methods) {
                    if (m.name.equals("main") && m.desc.equals("([Ljava/lang/String;)V")) {
                    	 LazyListModel<SearchEntry> model = new LazyListModel<>();
                         model.addElement(new SearchEntry(c, m, TextUtils.escape(TextUtils.max(c.name, 100))));
                         jbm.getSearchList().setModel(model);
                    }
                }
            }
        }
    });
    Utils2.add(clazz_main);
    
  //From https://github.com/java-deobfuscator
    JMenuItem HeadSign_fix = new JMenuItem("SignatureFix");
    HeadSign_fix.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(final ActionEvent e) {
				if (jbm.getFile().getClasses() == null) {
					return;
				}
				try {
					for (final ClassNode classNode : jbm.getFile().getClasses().values()) {
						if (classNode.signature != null) {
							try {
								CheckClassAdapter.checkClassSignature(classNode.signature);
							} catch (IllegalArgumentException IAE) {
								classNode.signature = null;
							} catch (Throwable x) {
								x.printStackTrace();
							}
						}
						classNode.methods.forEach(methodNode -> {
							if (methodNode.signature != null) {
								try {
									CheckClassAdapter.checkMethodSignature(methodNode.signature);
								} catch (IllegalArgumentException IAE) {
									methodNode.signature = null;
								} catch (Throwable x) {
									x.printStackTrace();
								}
							}
						});
						classNode.fields.forEach(fieldNode -> {
							if (fieldNode.signature != null) {
								try {
									CheckClassAdapter.checkFieldSignature(fieldNode.signature);
								} catch (IllegalArgumentException IAE) {
									fieldNode.signature = null;
								} catch (Throwable x) {
									x.printStackTrace();
								}
							}
						});

					}
				} catch (Throwable x) {
					x.printStackTrace();
				}
				JOptionPane.showConfirmDialog(null, "Finished", "SignatureFix", 1);
        }
    });
    Utils2.add(HeadSign_fix);
    
    //From https://github.com/java-deobfuscator and https://github.com/ItzSomebody/Radon/
    JMenuItem access_fix = new JMenuItem("AccessFixer");
    access_fix.addActionListener(new ActionListener() {

        @Override
        public void actionPerformed(final ActionEvent e) {
				if (jbm.getFile().getClasses() == null) {
					return;
				}
				try {
					for (final ClassNode classNode : jbm.getFile().getClasses().values()) {

						if (!hasAnnotations(classNode))
							classNode.access &= ~(Opcodes.ACC_SYNTHETIC | Opcodes.ACC_BRIDGE);

						classNode.methods.forEach(methodNode -> {
							if (!(methodNode == null) && !hasAnnotations(methodNode)
									&& !methodNode.name.startsWith("<"))
								methodNode.access &= ~(Opcodes.ACC_SYNTHETIC | Opcodes.ACC_BRIDGE);
						});
						classNode.fields.forEach(fieldNode -> {
							if (!(fieldNode == null) && !hasAnnotations(fieldNode))
								fieldNode.access &= ~(Opcodes.ACC_SYNTHETIC | Opcodes.ACC_BRIDGE);
						});
					}
				} catch (Throwable x) {
					x.printStackTrace();
				}
				JOptionPane.showConfirmDialog(null, "Finished", "AccessFixer", 1);
        }
    });
    Utils2.add(access_fix);
    
    this.add(getSettings());
    JMenu help = new JMenu(JByteMod.res.getResource("help"));
    JMenuItem about = new JMenuItem(JByteMod.res.getResource("about"));
    about.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        try {
          new JAboutFrame(jbm).setVisible(true);
        } catch (Exception ex) {
          new ErrorDisplay(ex);
        }
      }
    });

    help.add(about);
    JMenuItem licenses = new JMenuItem(JByteMod.res.getResource("licenses"));
    licenses.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        try {
          JFrame jf = new JFrame();
          jf.setBounds(100, 100, 700, 800);
          jf.add(new JScrollPane(new JTextArea(IOUtils.toString(MyMenuBar.class.getResourceAsStream("/resources/LICENSES")))));
          jf.setTitle(JByteMod.res.getResource("licenses"));
          jf.setVisible(true);
        } catch (Exception ex) {
          new ErrorDisplay(ex);
        }
      }
    });

    help.add(licenses);
    this.add(help);
  }
  
	private void replace(MethodNode method,boolean proxy) {
			//todo
	}
  
	//From https://github.com/ItzSomebody/Radon/
	public static boolean hasAnnotations(ClassNode classNode) {
		return (classNode.visibleAnnotations != null && !classNode.visibleAnnotations.isEmpty())
				|| (classNode.invisibleAnnotations != null && !classNode.invisibleAnnotations.isEmpty());
	}

	//From https://github.com/ItzSomebody/Radon/
	public static boolean hasAnnotations(MethodNode methodNode) {
		return (methodNode.visibleAnnotations != null && !methodNode.visibleAnnotations.isEmpty())
				|| (methodNode.invisibleAnnotations != null && !methodNode.invisibleAnnotations.isEmpty());
	}

	//From https://github.com/ItzSomebody/Radon/
	public static boolean hasAnnotations(FieldNode fieldNode) {
		return (fieldNode.visibleAnnotations != null && !fieldNode.visibleAnnotations.isEmpty())
				|| (fieldNode.invisibleAnnotations != null && !fieldNode.invisibleAnnotations.isEmpty());
	}

  protected void openProcessSelection() {
	// I don't know why this can get none   
    //List<VirtualMachineDescriptor> list = VirtualMachine.list();
	  List<VirtualMachineDescriptor> list = new WindowsAttachProvider().listVirtualMachines();
    VirtualMachine vm = null;
    try {
      if (list.isEmpty()) {
        String pid = JOptionPane.showInputDialog(JByteMod.res.getResource("no_vm_found"));
        if (pid != null && !pid.isEmpty()) {
          vm = AttachUtils.getVirtualMachine(Integer.parseInt(pid));
        }
      } else {
        JProcessSelection gui = new JProcessSelection(list);
        gui.setVisible(true);
        if (gui.getPid() != 0) {
          vm = AttachUtils.getVirtualMachine(gui.getPid());
        }
      }
      if (vm != null) {
        jbm.attachTo(vm);
      }
    } catch (Throwable t) {
      if (t.getMessage() != null) {
        JOptionPane.showMessageDialog(null, t.getMessage()
        		 + "Attach Error Do you really put attach.dll in right place?");
      } else {
        new ErrorDisplay(t);
      }
    }
  }

  private JMenu getSettings() {
    JMenu settings = new JMenu(JByteMod.res.getResource("settings"));
    LanguageRes lr = JByteMod.res;
    Options o = JByteMod.ops;
    HashMap<String, JMenu> menus = new LinkedHashMap<>();
    HashMap<String, JMenu> roots = new LinkedHashMap<>();
    for (Option op : o.bools) {
      String group = op.getGroup();
      String[] groups = group.split("_");
      JMenu menu = null;
      if (menus.containsKey(group)) {
        menu = menus.get(group);
      } else {
        String full = "";
        for (String g : groups) {
          if (!full.isEmpty()) {
            full += "_";
          }
          full += g;
          if (menus.containsKey(full)) {
            menu = menus.get(full);
            continue;
          }
          if (menu == null) {
            menu = new JMenu(lr.getResource(g + "_group"));
            roots.put(full, menu);
            menus.put(full, menu);
          } else {
            JMenu subMenu = new JMenu(lr.getResource(g + "_group"));
            menu.add(subMenu);
            menu = subMenu;
            menus.put(full, menu);
          }
        }
      }
      switch (op.getType()) {
      case BOOLEAN:
        JCheckBoxMenuItem jmi = new JCheckBoxMenuItem(lr.getResource(op.getName()), op.getBoolean());
        jmi.addActionListener(new ActionListener() {

          @Override
          public void actionPerformed(ActionEvent e) {
            op.setValue(jmi.isSelected());
            o.save();
            if (op.getName().equals("use_weblaf")) {
              JByteMod.resetLAF();
              JByteMod.restartGUI();
            }
          }
        });
        menu.add(jmi);
        break;
      case STRING:
        JMenu jm = new JMenu(lr.getResource(op.getName()));
        JTextField jtf = new JTextField(op.getString());
        jtf.setPreferredSize(new Dimension(Math.max((int) jtf.getPreferredSize().getWidth(), 128), (int) jtf.getPreferredSize().getHeight()));
        jm.add(Box.createHorizontalGlue());
        jm.add(jtf);
        jtf.addFocusListener(new FocusAdapter() {
          public void focusLost(FocusEvent e) {
            op.setValue(jtf.getText());
            o.save();
          }
        });
        menu.add(jm);
        break;
      case INT:
        jm = new JMenu(lr.getResource(op.getName()));
        JFormattedTextField jnf = ClassDialogue.createNumberField(Integer.class, 0, Integer.MAX_VALUE);
        jnf.setValue(op.getInteger());
        jnf.setPreferredSize(new Dimension(Math.max((int) jnf.getPreferredSize().getWidth(), 64), (int) jnf.getPreferredSize().getHeight()));
        jm.add(jnf);
        jnf.addFocusListener(new FocusAdapter() {
          public void focusLost(FocusEvent e) {
            op.setValue((int) jnf.getValue());
            o.save();
          }
        });
        menu.add(jm);
        break;
      default:
        break;
      }
    }
    for (JMenu m : roots.values()) {
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
    panel.add(new JLabel(JByteMod.res.getResource("big_string_warn")), "South");
    labels.add(new JLabel(JByteMod.res.getResource("find")));
    JTextField cst = new JTextField();
    input.add(cst);
    JCheckBox exact = new JCheckBox(JByteMod.res.getResource("exact"));
    JCheckBox regex = new JCheckBox("Regex");
    JCheckBox snstv = new JCheckBox(JByteMod.res.getResource("case_sens"));
    labels.add(exact);
    labels.add(regex);
    input.add(snstv);
    input.add(new JPanel());
    if (JOptionPane.showConfirmDialog(this.jbm, panel, "Search LDC", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE,
        searchIcon) == JOptionPane.OK_OPTION && !cst.getText().isEmpty()) {
      jbm.getSearchList().searchForConstant(cst.getText(), exact.isSelected(), snstv.isSelected(), regex.isSelected());
    }
  }

  protected void replaceLDC() {
    final JPanel panel = new JPanel(new BorderLayout(5, 5));
    final JPanel input = new JPanel(new GridLayout(0, 1));
    final JPanel labels = new JPanel(new GridLayout(0, 1));
    panel.add(labels, "West");
    panel.add(input, "Center");
    panel.add(new JLabel(JByteMod.res.getResource("big_string_warn")), "South");
    labels.add(new JLabel("Find: "));
    JTextField find = new JTextField();
    input.add(find);
    labels.add(new JLabel("Replace with: "));
    JTextField with = new JTextField();
    input.add(with);
    JComboBox<String> ldctype = new JComboBox<String>(new String[] { "String", "float", "double", "int", "long" });
    ldctype.setSelectedIndex(0);
    labels.add(new JLabel("Ldc Type: "));
    input.add(ldctype);
    JCheckBox exact = new JCheckBox(JByteMod.res.getResource("exact"));
    JCheckBox cases = new JCheckBox(JByteMod.res.getResource("case_sens"));
    labels.add(exact);
    input.add(cases);
    if (JOptionPane.showConfirmDialog(this.jbm, panel, "Replace LDC", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE,
        searchIcon) == JOptionPane.OK_OPTION && !find.getText().isEmpty()) {
      int expectedType = ldctype.getSelectedIndex();
      boolean equal = exact.isSelected();
      boolean ignoreCase = !cases.isSelected();
      String findCst = find.getText();
      if (ignoreCase) {
        findCst = findCst.toLowerCase();
      }
      String replaceWith = with.getText();
      int i = 0;
      for (ClassNode cn : jbm.getFile().getClasses().values()) {
        for (MethodNode mn : cn.methods) {
          for (AbstractInsnNode ain : mn.instructions) {
            if (ain.getType() == AbstractInsnNode.LDC_INSN) {
              LdcInsnNode lin = (LdcInsnNode) ain;
              Object cst = lin.cst;
              int type;
              if (cst instanceof String) {
                type = 0;
              } else if (cst instanceof Float) {
                type = 1;
              } else if (cst instanceof Double) {
                type = 2;
              } else if (cst instanceof Long) {
                type = 3;
              } else if (cst instanceof Integer) {
                type = 4;
              } else {
                type = -1;
              }
              String cstStr = cst.toString();
              if (ignoreCase) {
                cstStr = cstStr.toLowerCase();
              }
              if (type == expectedType) {
                if (equal ? cstStr.equals(findCst) : cstStr.contains(findCst)) {
                  switch (type) {
                  case 0:
                    lin.cst = replaceWith;
                    break;
                  case 1:
                    lin.cst = Float.parseFloat(replaceWith);
                    break;
                  case 2:
                    lin.cst = Double.parseDouble(replaceWith);
                    break;
                  case 3:
                    lin.cst = Long.parseLong(replaceWith);
                    break;
                  case 4:
                    lin.cst = Integer.parseInt(replaceWith);
                    break;
                  }
                  i++;
                }
              }
            }
          }
        }
      }
      JByteMod.LOGGER.log(i + " ldc's replaced");
    }
  }

  protected void searchField() {
    final JPanel panel = new JPanel(new BorderLayout(5, 5));
    final JPanel input = new JPanel(new GridLayout(0, 1));
    final JPanel labels = new JPanel(new GridLayout(0, 1));
    panel.add(labels, "West");
    panel.add(input, "Center");
    panel.add(new JLabel(JByteMod.res.getResource("big_jar_warn")), "South");
    labels.add(new JLabel("Owner:"));
    JTextField owner = new JTextField();
    input.add(owner);
    labels.add(new JLabel("Name:"));
    JTextField name = new JTextField();
    input.add(name);
    labels.add(new JLabel("Desc:"));
    JTextField desc = new JTextField();
    input.add(desc);
    JCheckBox exact = new JCheckBox(JByteMod.res.getResource("exact"));
    labels.add(exact);
    input.add(new JPanel());
    if (JOptionPane.showConfirmDialog(JByteMod.instance, panel, "Search FieldInsnNode", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE,
        searchIcon) == JOptionPane.OK_OPTION && !(name.getText().isEmpty() && owner.getText().isEmpty() && desc.getText().isEmpty())) {
      jbm.getSearchList().searchForFMInsn(owner.getText(), name.getText(), desc.getText(), exact.isSelected(), true);
    }
  }

  protected void searchMethod() {
    final JPanel panel = new JPanel(new BorderLayout(5, 5));
    final JPanel input = new JPanel(new GridLayout(0, 1));
    final JPanel labels = new JPanel(new GridLayout(0, 1));
    panel.add(labels, "West");
    panel.add(input, "Center");
    panel.add(new JLabel(JByteMod.res.getResource("big_jar_warn")), "South");
    labels.add(new JLabel("Owner:"));
    JTextField owner = new JTextField();
    input.add(owner);
    labels.add(new JLabel("Name:"));
    JTextField name = new JTextField();
    input.add(name);
    labels.add(new JLabel("Desc:"));
    JTextField desc = new JTextField();
    input.add(desc);
    JCheckBox exact = new JCheckBox(JByteMod.res.getResource("exact"));
    labels.add(exact);
    input.add(new JPanel());
    if (JOptionPane.showConfirmDialog(JByteMod.instance, panel, "Search MethodInsnNode", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE,
        searchIcon) == JOptionPane.OK_OPTION && !(name.getText().isEmpty() && owner.getText().isEmpty() && desc.getText().isEmpty())) {
      jbm.getSearchList().searchForFMInsn(owner.getText(), name.getText(), desc.getText(), exact.isSelected(), false);
    }
  }

  protected void openSaveDialogue() {
    if (jbm.getFile() != null) {
      boolean isClass = jbm.getFile().isSingleEntry();
      JFileChooser jfc = new JFileChooser(new File(System.getProperty("user.home") + File.separator + "Desktop"));
      jfc.setAcceptAllFileFilterUsed(false);
      jfc.setDialogTitle("Save");
      jfc.setFileFilter(new FileNameExtensionFilter(isClass ? "Java Class (*.class)" : "Java Package (*.jar)", isClass ? "class" : "jar"));
      int result = jfc.showSaveDialog(this);
      if (result == JFileChooser.APPROVE_OPTION) {
        File output = jfc.getSelectedFile();
        this.lastFile = output;
        JByteMod.LOGGER.log("Selected output file: " + output.getAbsolutePath());
        jbm.saveFile(output);
      }
    }
  }

  protected void openLoadDialogue() {
    JFileChooser jfc = new JFileChooser(new File(System.getProperty("user.home") + File.separator + "Desktop"));
    jfc.setAcceptAllFileFilterUsed(false);
    jfc.setFileFilter(new FileNameExtensionFilter("Java Package (*.jar) or Java Class (*.class)", "jar", "class"));
    int result = jfc.showOpenDialog(this);
    if (result == JFileChooser.APPROVE_OPTION) {
      File input = jfc.getSelectedFile();
      JByteMod.LOGGER.log("Selected input file: " + input.getAbsolutePath());
      jbm.loadFile(input);
    }
  }

  public void addPluginMenu(ArrayList<Plugin> plugins) {
    if (!plugins.isEmpty()) {
      JMenu pluginMenu = new JMenu("Plugins");
      for (Plugin p : plugins) {
        JMenuItem jmi = new JMenuItem(p.getName() + " " + p.getVersion());
        jmi.setEnabled(p.isClickable());
        jmi.addActionListener(e -> {
          p.menuClick();
        });
        pluginMenu.add(jmi);
      }
      this.add(pluginMenu);
    }
  }

  public boolean isAgent() {
    return agent;
  }

  public File getLastFile() {
    return lastFile;
  }

  public void setLastFile(File lastFile) {
    this.lastFile = lastFile;
  }
}
