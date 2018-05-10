package me.grax.jbytemod.ui.graph.methodref;

import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.Map;

import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;

import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxRectangle;
import com.mxgraph.view.mxGraph;
import com.mxgraph.view.mxGraphView;
import com.mxgraph.view.mxStylesheet;

import me.grax.jbytemod.JByteMod;

@Deprecated
public class MRGraph extends mxGraph {

  private MRGComponent component;
  private JByteMod jbm;

  public MRGraph(JByteMod jbm) {
    this.component = new MRGComponent(this);
    this.jbm = jbm;
    setAutoOrigin(true);
    setAutoSizeCells(true);
    setHtmlLabels(true);
    setAllowDanglingEdges(true);
    setStyles();
    this.resetEdgesOnMove = true;
  }

  public MRGComponent getComponent() {
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

  public class MRGComponent extends mxGraphComponent {

    private JScrollPane scp;

    public MRGComponent(mxGraph g) {
      super(g);
      this.getViewport().setBackground(Color.WHITE);
      this.setEnabled(false);
      this.setBorder(new EmptyBorder(0, 0, 0, 0));
      this.setZoomFactor(1.1);
      MouseAdapter adapter = new MouseAdapter() {
        @Override
        public void mousePressed(MouseEvent e) {
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
