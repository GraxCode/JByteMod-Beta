package me.grax.jbytemod.ui.lists;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodNode;

import me.grax.jbytemod.JByteMod;
import me.grax.jbytemod.utils.ErrorDisplay;
import me.grax.jbytemod.utils.dialogue.EditDialogue;
import me.grax.jbytemod.utils.dialogue.EditDialogueSpec;
import me.grax.jbytemod.utils.list.FieldEntry;
import me.grax.jbytemod.utils.list.InstrEntry;
import me.lpk.util.OpUtils;

public class MyCodeList extends JList<InstrEntry> {
  private JLabel editor;
  private AdressList adressList;
  private MethodNode currentMethod;
  private ClassNode currentClass;

  public MyCodeList(JByteMod jam, JLabel editor) {
    super(new DefaultListModel<InstrEntry>());
    this.editor = editor;
    this.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
    this.addMouseListener(new MouseAdapter() {
      public void mousePressed(MouseEvent e) {
        InstrEntry entry = (InstrEntry) MyCodeList.this.getSelectedValue();
        List<InstrEntry> selected = MyCodeList.this.getSelectedValuesList();
        if (entry == null) {
          JPopupMenu menu = new JPopupMenu();
          if (currentMethod != null) {
            JMenuItem add = new JMenuItem("Add");
            add.addActionListener(new ActionListener() {
              public void actionPerformed(ActionEvent e) {
                try {
                  EditDialogue.createInsertInsnDialog(currentMethod, null);
                } catch (Exception e1) {
                  new ErrorDisplay(e1);
                }

              }
            });
            menu.add(add);
          } else if (currentClass != null) {
            JMenuItem add = new JMenuItem("Add");
            add.addActionListener(new ActionListener() {
              public void actionPerformed(ActionEvent e) {
                try {
                  FieldNode fn = new FieldNode(1, "", "", "", null);
                  EditDialogueSpec.createEditDialogue(null, fn);
                  if (fn.signature.isEmpty()) {
                    fn.signature = null;
                  }
                  currentClass.fields.add(fn);
                } catch (Exception e1) {
                  new ErrorDisplay(e1);
                }
                MyCodeList.this.loadFields(currentClass);
              }
            });
            menu.add(add);
          }
          menu.show(jam, (int) jam.getMousePosition().getX(), (int) jam.getMousePosition().getY());
          return;
        }
        MethodNode mn = entry.getMethod();
        if (SwingUtilities.isRightMouseButton(e)) {
          AbstractInsnNode ain = entry.getInstr();
          if (mn != null) {
            if (selected.size() > 1) {
              JPopupMenu menu = new JPopupMenu();
              JMenuItem remove = new JMenuItem("Remove All");
              remove.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                  for (InstrEntry sel : selected) {
                    mn.instructions.remove(sel.getInstr());
                  }
                  MyCodeList.this.loadInstructions(mn);
                }
              });
              menu.add(remove);
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
              menu.show(jam, (int) jam.getMousePosition().getX(), (int) jam.getMousePosition().getY());
            } else {
              JPopupMenu menu = new JPopupMenu();
              JMenuItem insert = new JMenuItem("Insert after");
              insert.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                  try {
                    EditDialogue.createInsertInsnDialog(mn, ain);
                  } catch (Exception e1) {
                    new ErrorDisplay(e1);
                  }
                }
              });
              menu.add(insert);
              if(EditDialogue.canEdit(ain)) {
              JMenuItem edit = new JMenuItem("Edit");
              edit.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                  try {
                    EditDialogue.createEditInsnDialog(mn, ain);
                  } catch (Exception e1) {
                    new ErrorDisplay(e1);
                  }
                }
              });
              menu.add(edit);
              }
              JMenuItem duplicate = new JMenuItem("Duplicate");
              duplicate.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                  try {
                    if (ain instanceof LabelNode) {
                      mn.instructions.insert(ain, new LabelNode());
                    } else {
                      mn.instructions.insert(ain, ain.clone(new HashMap<>()));
                    }
                    MyCodeList.this.loadInstructions(mn);

                  } catch (Exception e1) {
                    new ErrorDisplay(e1);
                  }
                }
              });
              menu.add(duplicate);
              JMenuItem up = new JMenuItem("Move up");
              up.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                  AbstractInsnNode node = ain.getPrevious();
                  mn.instructions.remove(node);
                  mn.instructions.insert(ain, node);
                  MyCodeList.this.loadInstructions(mn);
                }
              });
              menu.add(up);
              JMenuItem down = new JMenuItem("Move down");
              down.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                  AbstractInsnNode node = ain.getNext();
                  mn.instructions.remove(node);
                  mn.instructions.insertBefore(ain, node);
                  MyCodeList.this.loadInstructions(mn);
                }
              });
              menu.add(down);
              JMenuItem remove = new JMenuItem("Remove");
              remove.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                  mn.instructions.remove(ain);
                  MyCodeList.this.loadInstructions(mn);
                }
              });
              menu.add(remove);
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
              menu.show(jam, (int) jam.getMousePosition().getX(), (int) jam.getMousePosition().getY());
            }
          } else {
            FieldEntry fle = (FieldEntry) entry;
            ClassNode cn = fle.getCn();
            JPopupMenu menu = new JPopupMenu();
            JMenuItem edit = new JMenuItem("Edit");
            edit.addActionListener(new ActionListener() {
              public void actionPerformed(ActionEvent e) {
                try {
                  EditDialogue.createEditFieldDialog(cn, fle.getFn());
                } catch (Exception e1) {
                  new ErrorDisplay(e1);
                }
                MyCodeList.this.loadFields(cn);
              }
            });
            menu.add(edit);
            JMenuItem remove = new JMenuItem("Remove");
            remove.addActionListener(new ActionListener() {
              public void actionPerformed(ActionEvent e) {
                cn.fields.remove(fle.getFn());
                MyCodeList.this.loadFields(cn);
              }
            });
            menu.add(remove);
            JMenuItem add = new JMenuItem("Insert");
            add.addActionListener(new ActionListener() {
              public void actionPerformed(ActionEvent e) {
                try {
                  FieldNode fn = new FieldNode(1, "", "", "", null);
                  if (EditDialogueSpec.createEditDialogue(null, fn)) {
                    if (fn.signature.isEmpty()) {
                      fn.signature = null;
                    }
                    cn.fields.add(fn);
                  }
                } catch (Exception e1) {
                  new ErrorDisplay(e1);
                }
                MyCodeList.this.loadFields(cn);
              }
            });
            menu.add(add);
            menu.show(jam, (int) jam.getMousePosition().getX(), (int) jam.getMousePosition().getY());
          }
        }
      }
    });
  }

  public boolean loadInstructions(MethodNode m) {
    OpUtils.clearLabelCache();
    this.currentMethod = m;
    this.currentClass = null;
    DefaultListModel<InstrEntry> lm = new DefaultListModel<InstrEntry>();
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
    return true;
  }

  public void setAdressList(AdressList adressList) {
    this.adressList = adressList;
  }

  public boolean loadFields(ClassNode cn) {
    this.currentClass = cn;
    this.currentMethod = null;
    DefaultListModel<InstrEntry> lm = new DefaultListModel<InstrEntry>();
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
    return true;
  }

}
