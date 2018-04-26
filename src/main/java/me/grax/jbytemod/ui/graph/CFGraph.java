package me.grax.jbytemod.ui.graph;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.Map;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import com.mxgraph.model.mxCell;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxRectangle;
import com.mxgraph.view.mxGraph;
import com.mxgraph.view.mxGraphView;
import com.mxgraph.view.mxStylesheet;

import me.grax.jbytemod.JByteMod;

public class CFGraph extends mxGraph {
  private CFGComponent component;
  private JByteMod jbm;

  public CFGraph(JByteMod jbm) {
    this.component = new CFGComponent(this);
    this.jbm = jbm;
    setAutoOrigin(true);
    setAutoSizeCells(true);
    setHtmlLabels(true);
    setAllowDanglingEdges(true);
    setStyles();
    this.resetEdgesOnMove = true;
  }

  public CFGComponent getComponent() {
    return component;
  }

  @Override
  public mxRectangle getPreferredSizeForCell(Object arg0) {
    mxRectangle size = super.getPreferredSizeForCell(arg0);
    size.setWidth(size.getWidth() + 10); //some items touch the border
    return size;
  }

  private void setStyles() {
    Map<String, Object> edgeStyle = this.getStylesheet().getDefaultEdgeStyle();
    edgeStyle.put(mxConstants.STYLE_ROUNDED, true);
    edgeStyle.put(mxConstants.STYLE_ELBOW, mxConstants.ELBOW_VERTICAL);
    edgeStyle.put(mxConstants.STYLE_ENDARROW, mxConstants.ARROW_OPEN);
    edgeStyle.put(mxConstants.STYLE_TARGET_PERIMETER_SPACING, 1d);

    Map<String, Object> vertexStyle = this.getStylesheet().getDefaultVertexStyle();
    vertexStyle.put(mxConstants.STYLE_SHADOW, true);
    mxStylesheet stylesheet = new mxStylesheet();
    stylesheet.setDefaultEdgeStyle(edgeStyle);
    stylesheet.setDefaultVertexStyle(vertexStyle);
    this.setStylesheet(stylesheet);

  }

  public class CFGComponent extends mxGraphComponent {

    private JScrollPane scp;

    public CFGComponent(mxGraph g) {
      super(g);
      this.getViewport().setBackground(Color.WHITE);
      this.setEnabled(false);
      this.setBorder(new EmptyBorder(0, 0, 0, 0));
      this.setZoomFactor(1.1);
      MouseAdapter adapter = new MouseAdapter() {
        @Override
        public void mousePressed(MouseEvent e) {
          if (SwingUtilities.isRightMouseButton(e)) {
            mxCell cell = (mxCell) getCellAt(e.getX(), e.getY());
            if (cell != null && cell.getValue() instanceof BlockVertex) {
              BlockVertex bv = (BlockVertex) cell.getValue();
              JPopupMenu menu = new JPopupMenu();
              JMenuItem edit = new JMenuItem(JByteMod.res.getResource("edit"));
              edit.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                }
              });
              //menu.add(edit); TODO  
              JMenuItem dec = new JMenuItem(JByteMod.res.getResource("go_to_dec"));
              dec.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                  jbm.getCodeList().setSelectedIndex(bv.getListIndex());
                  jbm.getTabbedPane().getEditorTab().getCodeBtn().doClick();
                }
              });
              menu.add(dec);
              menu.show(jbm, (int) jbm.getMousePosition().getX(), (int) jbm.getMousePosition().getY());
            }
          }
        }
      };
      this.getGraphControl().addMouseListener(adapter);
      this.getGraphControl().addMouseMotionListener(adapter);
      this.getGraphControl().addMouseWheelListener(new MouseWheelListener() {

        @Override
        public void mouseWheelMoved(MouseWheelEvent e) {
          if (e.isControlDown()) {
            if (e.getWheelRotation() < 0) {
              zoomIn();
            } else {
              zoomOut();
            }
            repaint();
            revalidate();
          } else if (scp != null) {
            //do we need this on linux too?
            scp.getVerticalScrollBar()
                .setValue(scp.getVerticalScrollBar().getValue() + e.getUnitsToScroll() * scp.getVerticalScrollBar().getUnitIncrement());
          }
        }
      });
    }

    @Override
    public void zoomIn() {
      mxGraphView view = graph.getView();
      double scale = view.getScale();
      if (scale < 4) {
        zoom(zoomFactor);
      }
    }

    @Override
    public void zoomOut() {
      mxGraphView view = graph.getView();
      double scale = view.getScale();
      if (scp != null && (scp.getVerticalScrollBar().isVisible() || scale >= 1) && scale > 0.3) {
        zoom(1 / zoomFactor);
      }
    }

    public void setScp(JScrollPane scp) {
      this.scp = scp;
    }
  }
}
