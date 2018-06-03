package me.grax.jbytemod.ui.lists;

import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.ListModel;
import javax.swing.SwingUtilities;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import me.grax.jbytemod.JByteMod;
import me.grax.jbytemod.ui.JAnnotationEditor;
import me.grax.jbytemod.ui.JSearch;
import me.grax.jbytemod.ui.dialogue.InsnEditDialogue;
import me.grax.jbytemod.ui.lists.entries.FieldEntry;
import me.grax.jbytemod.ui.lists.entries.InstrEntry;
import me.grax.jbytemod.ui.lists.entries.PrototypeEntry;
import me.grax.jbytemod.utils.ErrorDisplay;
import me.grax.jbytemod.utils.HtmlSelection;
import me.grax.jbytemod.utils.list.LazyListModel;
import me.lpk.util.OpUtils;

public class MyCodeList extends JList<InstrEntry> {
  private JLabel editor;
  private AdressList adressList;
  private ErrorList errorList;
  private MethodNode currentMethod;
  private ClassNode currentClass;

  public MyCodeList(JByteMod jam, JLabel editor) {
    super(new LazyListModel<InstrEntry>());
    this.editor = editor;
    this.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
    this.addMouseListener(new MouseAdapter() {
      public void mousePressed(MouseEvent e) {
        InstrEntry entry = (InstrEntry) MyCodeList.this.getSelectedValue();
        if (entry == null) {
          createPopupForEmptyList(jam);
          return;
        }
        MethodNode mn = entry.getMethod();
        if (SwingUtilities.isRightMouseButton(e)) {
          if (mn != null) {
            AbstractInsnNode ain = entry.getInstr();
            rightClickMethod(jam, mn, ain, MyCodeList.this.getSelectedValuesList());
          } else {
            rightClickField(jam, (FieldEntry) entry, MyCodeList.this.getSelectedValuesList());
          }
        } else if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 2) {
          if (mn != null) {
            try {
              if (InsnEditDialogue.canEdit(entry.getInstr())) {
                new InsnEditDialogue(mn, entry.getInstr()).open();
              }
            } catch (Exception e1) {
              new ErrorDisplay(e1);
            }
          } else {
            FieldEntry fe = (FieldEntry) entry;
            try {
              new InsnEditDialogue(null, fe.getFn()).open();
            } catch (Exception e1) {
              new ErrorDisplay(e1);
            }
            MyCodeList.this.loadFields(fe.getCn());
          }
        }
      }
    });
    InputMap im = getInputMap(WHEN_FOCUSED);
    ActionMap am = getActionMap();

    im.put(KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_MASK), "search");
    im.put(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_MASK), "copy");
    im.put(KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.CTRL_MASK), "duplicate");
    im.put(KeyStroke.getKeyStroke(KeyEvent.VK_I, InputEvent.CTRL_MASK), "insert");
    im.put(KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, 0), "insert");
    im.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "delete");
    im.put(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, 0), "up");
    im.put(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, 0), "down");
    am.put("search", new AbstractAction() {
      @Override
      public void actionPerformed(ActionEvent e) {
        new JSearch(MyCodeList.this).setVisible(true);
      }
    });

    am.put("copy", new AbstractAction() {
      @Override
      public void actionPerformed(ActionEvent e) {
        copyToClipbord();
      }
    });

    am.put("duplicate", new AbstractAction() {
      @Override
      public void actionPerformed(ActionEvent e) {
        InstrEntry entry = getSelectedValue();
        if (entry != null && entry.getMethod() != null) {
          duplicate(entry.getMethod(), entry.getInstr());
        }
      }
    });
    am.put("insert", new AbstractAction() {
      @Override
      public void actionPerformed(ActionEvent e) {
        InstrEntry entry = getSelectedValue();
        if (entry != null && entry.getMethod() != null) {
          try {
            InsnEditDialogue.createInsertInsnDialog(entry.getMethod(), entry.getInstr(), true);
            OpUtils.clearLabelCache();
          } catch (Exception e1) {
            new ErrorDisplay(e1);
          }
        }
      }
    });
    am.put("delete", new AbstractAction() {
      @Override
      public void actionPerformed(ActionEvent e) {
        List<InstrEntry> entries = getSelectedValuesList();
        for (InstrEntry entry : entries) {
          if (entry.getMethod() != null) {
            removeNode(entry.getMethod(), entry.getInstr());
          }
        }
      }
    });
    am.put("up", new AbstractAction() {
      @Override
      public void actionPerformed(ActionEvent e) {
        InstrEntry entry = getSelectedValue();
        if (entry != null && entry.getMethod() != null) {
          int index = getSelectedIndex();
          if (moveUp(entry.getMethod(), entry.getInstr())) {
            setSelectedIndex(index - 1);
          }
        }
      }
    });
    am.put("down", new AbstractAction() {
      @Override
      public void actionPerformed(ActionEvent e) {
        InstrEntry entry = getSelectedValue();
        if (entry != null && entry.getMethod() != null) {
          int index = getSelectedIndex();
          if (moveDown(entry.getMethod(), entry.getInstr())) {
            setSelectedIndex(index + 1);
          }
        }
      }
    });
    addMouseMotionListener(new MouseMotionAdapter() {
      @Override
      public void mouseMoved(MouseEvent e) {
        if (JByteMod.ops.get("hints").getBoolean()) {
          ListModel<InstrEntry> m = getModel();
          int index = locationToIndex(e.getPoint());
          if (index > -1) {
            InstrEntry el = m.getElementAt(index);
            setToolTipText(el.getHint());
          }
        } else {
          setToolTipText(null);
        }
      }
    });
    this.setPrototypeCellValue(new PrototypeEntry());
    this.setFixedCellWidth(-1);
  }

  protected void rightClickField(JByteMod jbm, FieldEntry fle, List<InstrEntry> selected) {
	  ClassNode cn = fle.getCn();
	  JPopupMenu menu = new JPopupMenu();
	  if (selected.size() > 1) {
		  JMenuItem remove = new JMenuItem(JByteMod.res.getResource("remove_all"));
	      remove.addActionListener(new ActionListener() {
	    	 public void actionPerformed(ActionEvent e) {
	          for (InstrEntry sel : selected) {
	            cn.fields.remove(((FieldEntry) sel).getFn());
	          }
	          MyCodeList.this.loadFields(cn);
	        }
	      });
	      menu.add(remove);
	      menu.add(copyText());
	      menu.show(jbm, (int) jbm.getMousePosition().getX(), (int) jbm.getMousePosition().getY());
	  } else {
	    JMenuItem edit = new JMenuItem(JByteMod.res.getResource("edit"));
	    edit.addActionListener(new ActionListener() {
	      public void actionPerformed(ActionEvent e) {
	        try {
	          new InsnEditDialogue(null, fle.getFn()).open();
	        } catch (Exception e1) {
	          new ErrorDisplay(e1);
	        }
	        MyCodeList.this.loadFields(cn);
	      }
	    });
	    menu.add(edit);
	    JMenuItem remove = new JMenuItem(JByteMod.res.getResource("remove"));
	    remove.addActionListener(new ActionListener() {
	      public void actionPerformed(ActionEvent e) {
	        cn.fields.remove(fle.getFn());
	        MyCodeList.this.loadFields(cn);
	      }
	    });
	    menu.add(remove);
	    JMenuItem add = new JMenuItem(JByteMod.res.getResource("insert"));
	    add.addActionListener(new ActionListener() {
	      public void actionPerformed(ActionEvent e) {
	        try {
	          FieldNode fn = new FieldNode(1, "", "", "", null);
	          if (new InsnEditDialogue(null, fn).open()) {
	            cn.fields.add(fn);
	          }
	        } catch (Exception e1) {
	          new ErrorDisplay(e1);
	        }
	        MyCodeList.this.loadFields(cn);
	      }
	    });
	    menu.add(add);
	    menu.add(copyText());
	    JMenuItem annotations = new JMenuItem("Edit Annotations");
	    annotations.addActionListener(new ActionListener() {
	      public void actionPerformed(ActionEvent e) {
	    	  if (!JAnnotationEditor.isOpen("visibleAnnotations"))
	    		  new JAnnotationEditor("Annotations", fle.getFn(), "visibleAnnotations").setVisible(true);
	      }
	    });
	    menu.add(annotations);
	    JMenuItem invisAnnotations = new JMenuItem("Edit Invis Annotations");
	    invisAnnotations.addActionListener(new ActionListener() {
	      public void actionPerformed(ActionEvent e) {
	    	  if (!JAnnotationEditor.isOpen("invisibleAnnotations"))
	    		  new JAnnotationEditor("Invis Annotations", fle.getFn(), "invisibleAnnotations").setVisible(true);
	      }
	    });
	    menu.add(invisAnnotations);
	    menu.show(jbm, (int) jbm.getMousePosition().getX(), (int) jbm.getMousePosition().getY());
	  }
  }

  private JMenuItem copyText() {
    JMenuItem copy = new JMenuItem(JByteMod.res.getResource("copy_text"));
    copy.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        copyToClipbord();
        JByteMod.LOGGER.log("Copied code to clipboard!");
      }
    });
    return copy;
  }

  protected void copyToClipbord() {
    StringBuilder sb = new StringBuilder();
    boolean html = JByteMod.ops.get("copy_formatted").getBoolean();
    if (html) {
      for (InstrEntry sel : MyCodeList.this.getSelectedValuesList()) {
        sb.append(sel.toString());
        sb.append("<br>");
      }
    } else {
      for (InstrEntry sel : MyCodeList.this.getSelectedValuesList()) {
        sb.append(sel.toEasyString());
        sb.append("\n");
      }
    }
    if (sb.length() > 0) {
      HtmlSelection selection = new HtmlSelection(sb.toString());
      Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);
    }
  }

  protected void rightClickMethod(JByteMod jbm, MethodNode mn, AbstractInsnNode ain, List<InstrEntry> selected) {
    if (selected.size() > 1) {
      JPopupMenu menu = new JPopupMenu();
      JMenuItem remove = new JMenuItem(JByteMod.res.getResource("remove_all"));
      remove.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          for (InstrEntry sel : selected) {
            mn.instructions.remove(sel.getInstr());
          }
          OpUtils.clearLabelCache();
          MyCodeList.this.loadInstructions(mn);
        }
      });
      menu.add(remove);
      menu.add(copyText());
      addPopupListener(menu);
      menu.show(jbm, (int) jbm.getMousePosition().getX(), (int) jbm.getMousePosition().getY());
    } else {
      JPopupMenu menu = new JPopupMenu();
      JMenuItem insertBefore = new JMenuItem(JByteMod.res.getResource("ins_before"));
      insertBefore.addActionListener(new ActionListener() {

        public void actionPerformed(ActionEvent e) {
          try {
            InsnEditDialogue.createInsertInsnDialog(mn, ain, false);
            OpUtils.clearLabelCache();
          } catch (Exception e1) {
            new ErrorDisplay(e1);
          }
        }
      });
      menu.add(insertBefore);
      JMenuItem insert = new JMenuItem(JByteMod.res.getResource("ins_after"));
      insert.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          try {
            InsnEditDialogue.createInsertInsnDialog(mn, ain, true);
            OpUtils.clearLabelCache();
          } catch (Exception e1) {
            new ErrorDisplay(e1);
          }
        }
      });
      insert.setAccelerator(KeyStroke.getKeyStroke('I', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
      menu.add(insert);

      if (InsnEditDialogue.canEdit(ain)) {
        JMenuItem edit = new JMenuItem(JByteMod.res.getResource("edit"));
        edit.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            try {
              new InsnEditDialogue(mn, ain).open();
            } catch (Exception e1) {
              new ErrorDisplay(e1);
            }
          }
        });
        menu.add(edit);
      }
      if (ain instanceof JumpInsnNode) {
        JMenuItem edit = new JMenuItem(JByteMod.res.getResource("jump_to_label"));
        edit.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            JumpInsnNode jin = (JumpInsnNode) ain;
            ListModel<InstrEntry> model = getModel();
            for (int i = 0; i < model.getSize(); i++) {
              InstrEntry sel = model.getElementAt(i);
              if (sel.getInstr().equals(jin.label)) {
                setSelectedIndex(i);
                ensureIndexIsVisible(i);
                break;
              }
            }
          }
        });
        menu.add(edit);
      }
      if (ain instanceof MethodInsnNode) {
        JMenuItem edit = new JMenuItem(JByteMod.res.getResource("go_to_dec"));
        edit.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            MethodInsnNode min = (MethodInsnNode) ain;
            for (ClassNode cn : jbm.getFile().getClasses().values()) {

              if (cn.name.equals(min.owner)) {
                for (MethodNode mn : cn.methods) {
                  if (min.name.equals(mn.name) && min.desc.equals(mn.desc)) {
                    jbm.selectMethod(cn, mn);
                    jbm.treeSelection(cn, mn);
                    return;
                  }
                }
              }
            }
          }
        });
        menu.add(edit);
      }
      if (ain instanceof FieldInsnNode) {
        JMenuItem edit = new JMenuItem(JByteMod.res.getResource("go_to_dec"));
        edit.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            FieldInsnNode fin = (FieldInsnNode) ain;
            for (ClassNode cn : jbm.getFile().getClasses().values()) {
              if (cn.name.equals(fin.owner)) {
                jbm.selectClass(cn);
                return;
              }
            }
          }
        });
        menu.add(edit);
      }
      JMenuItem duplicate = new JMenuItem(JByteMod.res.getResource("duplicate"));
      duplicate.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          duplicate(mn, ain);
        }
      });
      duplicate.setAccelerator(KeyStroke.getKeyStroke('D', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
      menu.add(duplicate);
      JMenuItem up = new JMenuItem(JByteMod.res.getResource("move_up"));
      up.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          moveUp(mn, ain);
        }
      });
      up.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, 0));
      menu.add(up);
      JMenuItem down = new JMenuItem(JByteMod.res.getResource("move_down"));
      down.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          moveDown(mn, ain);
        }
      });
      down.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, 0));
      menu.add(down);
      JMenuItem remove = new JMenuItem(JByteMod.res.getResource("remove"));
      remove.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          removeNode(mn, ain);
        }
      });
      remove.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
      menu.add(copyText());
      menu.add(remove);
      addPopupListener(menu);
      menu.show(jbm, (int) jbm.getMousePosition().getX(), (int) jbm.getMousePosition().getY());
    }
  }

  protected void removeNode(MethodNode mn, AbstractInsnNode ain) {
    mn.instructions.remove(ain);
    OpUtils.clearLabelCache();
    loadInstructions(mn);
  }

  protected boolean moveDown(MethodNode mn, AbstractInsnNode ain) {
    AbstractInsnNode node = ain.getNext();
    if (node != null) {
      mn.instructions.remove(node);
      mn.instructions.insertBefore(ain, node);
      OpUtils.clearLabelCache();
      loadInstructions(mn);
      return true;
    }
    return false;
  }

  protected boolean moveUp(MethodNode mn, AbstractInsnNode ain) {
    AbstractInsnNode node = ain.getPrevious();
    if (node != null) {
      mn.instructions.remove(node);
      mn.instructions.insert(ain, node);
      OpUtils.clearLabelCache();
      loadInstructions(mn);
      return true;
    }
    return false;
  }

  protected void duplicate(MethodNode mn, AbstractInsnNode ain) {
    try {
      if (ain instanceof LabelNode) {
        mn.instructions.insert(ain, new LabelNode());
        OpUtils.clearLabelCache();
      } else if (ain instanceof JumpInsnNode) {
        mn.instructions.insert(ain, new JumpInsnNode(ain.getOpcode(), ((JumpInsnNode) ain).label));
      } else {
        mn.instructions.insert(ain, ain.clone(new HashMap<>()));
      }
      MyCodeList.this.loadInstructions(mn);

    } catch (Exception e1) {
      new ErrorDisplay(e1);
    }
  }

  protected void createPopupForEmptyList(JByteMod jbm) {
    JPopupMenu menu = new JPopupMenu();
    if (currentMethod != null) {
      JMenuItem add = new JMenuItem(JByteMod.res.getResource("add"));
      add.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          try {
            InsnEditDialogue.createInsertInsnDialog(currentMethod, null, true);
          } catch (Exception e1) {
            new ErrorDisplay(e1);
          }

        }
      });
      menu.add(add);
    } else if (currentClass != null) {
      JMenuItem add = new JMenuItem(JByteMod.res.getResource("add"));
      add.addActionListener(new ActionListener() {

        public void actionPerformed(ActionEvent e) {
          try {
            FieldNode fn = new FieldNode(1, "", "", "", null);
            if (new InsnEditDialogue(null, fn).open()) {
              currentClass.fields.add(fn);
            }
          } catch (Exception e1) {
            new ErrorDisplay(e1);
          }
          MyCodeList.this.loadFields(currentClass);
        }

      });
      menu.add(add);
    }
    menu.show(jbm, (int) jbm.getMousePosition().getX(), (int) jbm.getMousePosition().getY());
  }

  protected void addPopupListener(JPopupMenu menu) {
    menu.addPopupMenuListener(new PopupMenuListener() {
      public void popupMenuCanceled(PopupMenuEvent popupMenuEvent) {
        MyCodeList.this.setFocusable(true);
      }

      public void popupMenuWillBecomeInvisible(PopupMenuEvent popupMenuEvent) {
        MyCodeList.this.setFocusable(true);
      }

      public void popupMenuWillBecomeVisible(PopupMenuEvent popupMenuEvent) {
        MyCodeList.this.setFocusable(false);
      }
    });
  }

  public boolean loadInstructions(MethodNode m) {
    this.currentMethod = m;
    this.currentClass = null;
    LazyListModel<InstrEntry> lm = new LazyListModel<InstrEntry>();
    editor.setText(m.name + m.desc);
    ArrayList<InstrEntry> entries = new ArrayList<>();
    for (AbstractInsnNode i : m.instructions) {
      InstrEntry entry = new InstrEntry(m, i);
      lm.addElement(entry);
      entries.add(entry);
    }
    this.setModel(lm);
    //update sidebar
    if (adressList != null) {
      adressList.updateAdr();
    }
    if (errorList != null) {
      errorList.updateErrors();
    }
    return true;
  }

  public void setAdressList(AdressList adressList) {
    this.adressList = adressList;
  }

  public boolean loadFields(ClassNode cn) {
    this.currentClass = cn;
    this.currentMethod = null;
    LazyListModel<InstrEntry> lm = new LazyListModel<InstrEntry>();
    editor.setText(cn.name + " Fields");
    ArrayList<InstrEntry> entries = new ArrayList<>();
    for (FieldNode fn : cn.fields) {
      InstrEntry entry = new FieldEntry(cn, fn);
      lm.addElement(entry);
      entries.add(entry);
    }
    this.setModel(lm);
    //update sidebar
    if (adressList != null) {
      adressList.updateAdr();
    }
    if (errorList != null) {
      errorList.updateErrors();
    }
    return true;
  }

  public void setErrorList(ErrorList errorList) {
    this.errorList = errorList;
  }
}
