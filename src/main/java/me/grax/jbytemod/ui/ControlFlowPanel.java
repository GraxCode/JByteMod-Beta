package me.grax.jbytemod.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodNode;

import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.util.mxConstants;
import com.mxgraph.view.mxGraph;
import com.mxgraph.view.mxStylesheet;

import me.grax.jbytemod.analysis.block.Block;
import me.grax.jbytemod.analysis.converter.Converter;
import me.grax.jbytemod.utils.ErrorDisplay;
import me.grax.jbytemod.utils.InstrUtils;

public class ControlFlowPanel extends JPanel {

  private MethodNode node;
  private ArrayList<Block> cf = new ArrayList<>();
  private mxGraph graph;
  private mxGraphComponent graphComponent;

  private static final String edgeColor = "#111111";
  private static final String jumpColor = "#39698a";

  private static final String jumpColorGreen = "#388a47";
  private static final String jumpColorRed = "#8a3e38";
  private static final String jumpColorPurple = "#71388a";
  private static final String jumpColorPink = "#8a386d";

  public ControlFlowPanel() {
    this.setBorder(new EmptyBorder(30, 30, 30, 30));
    this.setLayout(new BorderLayout(0, 0));
    this.setBackground(Color.WHITE);
    graph = new mxGraph();
    graph.setAutoOrigin(true);
    graph.setAutoSizeCells(true);
    graph.setHtmlLabels(true);
    setStyles();
    graphComponent = new mxGraphComponent(graph);
    graphComponent.getViewport().setBackground(Color.WHITE);
    graphComponent.setEnabled(false);
    graphComponent.setBorder(new EmptyBorder(0, 0, 0, 0));
    this.add(graphComponent, BorderLayout.CENTER);
  }
  
  private void setStyles() {
    Map<String, Object> edge = new HashMap<String, Object>();
    edge.put(mxConstants.STYLE_ROUNDED, true);
    edge.put(mxConstants.STYLE_ORTHOGONAL, false);
    edge.put(mxConstants.STYLE_EDGE, "elbowEdgeStyle");
    edge.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_CONNECTOR);
    edge.put(mxConstants.STYLE_ENDARROW, mxConstants.ARROW_OPEN);
    edge.put(mxConstants.STYLE_VERTICAL_ALIGN, mxConstants.ALIGN_MIDDLE);
    edge.put(mxConstants.STYLE_ALIGN, mxConstants.ALIGN_CENTER);
    //we don't need to set colors
    mxStylesheet edgeStyle = new mxStylesheet();
    edgeStyle.setDefaultEdgeStyle(edge);
    graph.setStylesheet(edgeStyle);
}

  public MethodNode getNode() {
    return node;
  }

  public void setNode(MethodNode node) {
    this.node = node;
  }

  public void generateList() {
    if (node == null)
      return;
    cf.clear();
    if (node.instructions.size() == 0) {
      this.clear();
      return;
    }
    Converter c = new Converter(node);
    try {
      cf.addAll(c.convert());
    } catch (Exception e) {
      e.printStackTrace();
      new ErrorDisplay(e);
      this.clear();
      return;
    }
    Object parent = graph.getDefaultParent();
    graph.getModel().beginUpdate();
    try {
      graph.removeCells(graph.getChildCells(graph.getDefaultParent(), true, true));
      existing.clear();
      if (!cf.isEmpty()) {
        for (Block b : cf) {
          if (b.getInput().isEmpty()) {
            addBlock(parent, b);
          }
        }
      }
      mxHierarchicalLayout layout = new mxHierarchicalLayout(graph);
      layout.setFineTuning(true);
      layout.setIntraCellSpacing(25d);
      layout.setInterRankCellSpacing(80d);
      layout.execute(graph.getDefaultParent());
    } finally {
      graph.getModel().endUpdate();
    }
    this.repaint();
  }

  private HashMap<Block, Object> existing = new HashMap<>();

  private Object addBlock(Object parent, Block b) {
    Object v1 = null;
    if (existing.containsKey(b)) {
      return existing.get(b);
    } else {
      String text = "";
      for (AbstractInsnNode ain : b.getNodes()) {
        text += InstrUtils.toString(ain) + "\n";
      }
      v1 = graph.insertVertex(parent, null, text, 150, 10, 80, 40,
          "fillColor=#FFFFFF;fontColor=#111111;strokeColor=#9297a1;");
      graph.updateCellSize(v1); //resize cell

      existing.put(b, v1);
    }
    if (v1 == null) {
      throw new RuntimeException();
    }
    ArrayList<Block> next = b.getOutput();
    for (int i = 0; i < next.size(); i++) {
      Block out = next.get(i);
      if (out.equals(b)) {
        graph.insertEdge(parent, null, null, v1, v1, "strokeColor=" + getEdgeColor(b, i) + ";");
      } else {
        assert (out.getInput().contains(b));
        graph.insertEdge(parent, null, null, v1, addBlock(parent, out), "strokeColor=" + getEdgeColor(b, i) + ";");
      }
    }
    return v1;
  }

  private String getEdgeColor(Block b, int i) {
    if (b.endsWithJump()) {
      if (b.getOutput().size() > 1) {
        if (i == 0) {
          return jumpColorGreen;
        }
        return jumpColorRed;
      }
      return jumpColor;
    }
    if (b.endsWithSwitch()) {
      if (i == 0) {
        return jumpColorPink;
      }
      return jumpColorPurple;
    }
    return edgeColor;
  }

  public void clear() {
    graph.getModel().beginUpdate();
    graph.removeCells(graph.getChildCells(graph.getDefaultParent(), true, true));
    graph.getModel().endUpdate();
  }

}
