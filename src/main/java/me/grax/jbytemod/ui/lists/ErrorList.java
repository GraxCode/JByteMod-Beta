package me.grax.jbytemod.ui.lists;

import java.awt.Component;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Random;

import javax.swing.DefaultListModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import com.alee.extended.layout.VerticalFlowLayout;
import com.alee.extended.panel.SingleAlignPanel;
import com.alee.extended.window.PopOverLocation;
import com.alee.extended.window.WebPopOver;
import com.alee.laf.button.WebButton;
import com.alee.laf.label.WebLabel;

import me.grax.jbytemod.JByteMod;
import me.grax.jbytemod.utils.list.InstrEntry;
import me.grax.jbytemod.utils.list.LazyListModel;

public class ErrorList extends JList<String> {
  private MyCodeList cl;
  private ImageIcon warning;
  private ListCellRenderer<? super String> oldRenderer;

  public ErrorList(JByteMod jbm, MyCodeList cl) {
    super(new DefaultListModel<String>());
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
        System.out.println("clicked");
        int index = locationToIndex(e.getPoint());
        String error = getModel().getElementAt(index);
        if (error.length() > 1) {
          WebPopOver popOver = new WebPopOver(JByteMod.instance);
          popOver.setMargin(10);
          popOver.setMovable(false);
          popOver.setLayout(new VerticalFlowLayout());
          popOver.add(new JLabel(error));
          popOver.show(jbm, (int) jbm.getMousePosition().getX(), (int) jbm.getMousePosition().getY());
        }
      }
    });
    this.updateErrors();
  }

  public void updateErrors() {
    LazyListModel<String> lm = new LazyListModel<String>();
    LazyListModel<InstrEntry> clm = (LazyListModel<InstrEntry>) cl.getModel();
    if (clm.getSize() > 9999) {
      throw new RuntimeException("code too big");
    }
    for (int i = 0; i < clm.getSize(); i++) {
      lm.addElement(" ");
    }
    this.setModel(lm);
  }

  class CustomCellRenderer extends JLabel implements ListCellRenderer<String> {
    public Component getListCellRendererComponent(JList list, String value, int index, boolean isSelected, boolean cellHasFocus) {
      Component c = oldRenderer.getListCellRendererComponent(list, value, index, false, false); //hacky hack
      JLabel label = (JLabel) c;
      if (value.length() > 1) {
        label.setIcon(warning);
        label.setText("\u200B"); //another hacky hack
      }
      return c;
    }
  }
}
