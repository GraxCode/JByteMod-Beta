package me.grax.jbytemod.ui.lists;

import java.awt.Component;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;

import javax.swing.DefaultListModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import org.objectweb.asm.tree.AbstractInsnNode;

import com.alee.extended.layout.VerticalFlowLayout;
import com.alee.extended.window.WebPopOver;

import me.grax.jbytemod.JByteMod;
import me.grax.jbytemod.analysis.errors.EmptyMistake;
import me.grax.jbytemod.analysis.errors.ErrorAnalyzer;
import me.grax.jbytemod.analysis.errors.InsnError;
import me.grax.jbytemod.analysis.errors.Mistake;
import me.grax.jbytemod.utils.gui.SwingUtils;
import me.grax.jbytemod.utils.list.InstrEntry;
import me.grax.jbytemod.utils.list.LazyListModel;

public class ErrorList extends JList<Mistake> {
  private MyCodeList cl;
  private ImageIcon warning;
  private ListCellRenderer<? super Mistake> oldRenderer;
  private JByteMod jbm;

  public ErrorList(JByteMod jbm, MyCodeList cl) {
    super(new DefaultListModel<Mistake>());
    this.jbm = jbm;
    this.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
    this.warning = new ImageIcon(Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/resources/warning.png")));
    this.cl = cl;
    cl.setErrorList(this);
    this.setSelectionModel(new DefaultListSelectionModel() {
      @Override
      public void setSelectionInterval(int index0, int index1) {
        super.setSelectionInterval(-1, -1);
      }
    });
    this.oldRenderer = this.getCellRenderer();
    this.setCellRenderer(new CustomCellRenderer());
    this.addMouseListener(new MouseAdapter() {

      @Override
      public void mouseClicked(MouseEvent e) {
        int index = locationToIndex(e.getPoint());
        Mistake error = getModel().getElementAt(index);
        if (!(error instanceof EmptyMistake)) {
          WebPopOver popOver = new WebPopOver(JByteMod.instance);
          popOver.setMargin(10);
          popOver.setMovable(false);
          popOver.setCloseOnFocusLoss(true);
          popOver.setLayout(new VerticalFlowLayout());
          popOver.add(new JLabel(error.getDesc()));
          popOver.show(jbm, (int) jbm.getMousePosition().getX(), (int) jbm.getMousePosition().getY());
        }
      }
    });
    this.updateErrors();
    SwingUtils.disableSelection(this);
  }

  public void updateErrors() {
    if (JByteMod.ops.get("analyze_errors").getBoolean() && jbm.getCurrentMethod() != null) {
      LazyListModel<Mistake> lm = new LazyListModel<Mistake>();
      LazyListModel<InstrEntry> clm = (LazyListModel<InstrEntry>) cl.getModel();
      if (clm.getSize() > 1000) {
        JByteMod.LOGGER.warn("Not analyzing mistakes, too many instructions!");
        return;
      }
      ErrorAnalyzer ea = new ErrorAnalyzer(jbm.getCurrentMethod());
      HashMap<AbstractInsnNode, Mistake> mistakes = ea.findErrors();
      for (int i = 0; i < clm.getSize(); i++) {
        AbstractInsnNode ain = clm.getElementAt(i).getInstr();
        if (mistakes.containsKey(ain)) {
          lm.addElement(mistakes.get(ain));
        } else {
          lm.addElement(new EmptyMistake());
        }
      }
      this.setModel(lm);
    }
  }

  class CustomCellRenderer extends JLabel implements ListCellRenderer<Mistake> {
    public Component getListCellRendererComponent(JList<? extends Mistake> list, Mistake value, int index, boolean isSelected, boolean cellHasFocus) {
      Component c = oldRenderer.getListCellRendererComponent(list, value, index, false, false); //hacky hack
      JLabel label = (JLabel) c;
      if (value.getDesc().length() > 1) {
        label.setIcon(warning);
      }
      label.setText("\u200B"); //another hacky hack
      return c;
    }
  }
}
