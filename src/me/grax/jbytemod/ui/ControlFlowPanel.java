package me.grax.jbytemod.ui;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JPanel;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodNode;

import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.view.mxGraph;

import me.grax.jbytemod.analysis.block.Block;
import me.grax.jbytemod.analysis.converter.Converter;
import me.grax.jbytemod.utils.ErrorDisplay;
import me.grax.jbytemod.utils.InstrUtils;

public class ControlFlowPanel extends JPanel {

  private MethodNode node;
  private ArrayList<Block> cf = new ArrayList<>();
  private mxGraph graph;

  //old code
  //private static final String vertexColor = "#4FC3F7";
  //private static final String singleVertexColor = "#EEEEEE";

  private static final String edgeColor = "#111111";
  private static final String jumpColor = "#39698a";

  private static final String jumpColorGreen = "#388a47";
  private static final String jumpColorRed = "#8a3e38";
  private static final String jumpColorPurple = "#71388a";
  private static final String jumpColorPink = "#8a386d";

  public ControlFlowPanel() {
    this.setLayout(new BorderLayout(0, 0));

    graph = new mxGraph();
    graph.setAutoOrigin(true);
    graph.setAutoSizeCells(true);
    graph.setHtmlLabels(true);
    mxGraphComponent graphComponent = new mxGraphComponent(graph);
    graphComponent.setEnabled(false); //TODO
    this.add(graphComponent);
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
      graph.removeCells(graph.getChildCells(graph.getDefaultParent(), true, true));
      return;
    }
    Converter c = new Converter(node);
    try {
      cf.addAll(c.convert());
    } catch (Exception e) {
      e.printStackTrace();
      new ErrorDisplay(e);
      graph.removeCells(graph.getChildCells(graph.getDefaultParent(), true, true));
      return;
    }
    Object parent = graph.getDefaultParent();
    graph.getModel().beginUpdate();
    graph.removeCells(graph.getChildCells(graph.getDefaultParent(), true, true));
    try {
      existing.clear();
      if (!cf.isEmpty()) {
        for (Block b : cf) {
          if (b.getInput().isEmpty()) {
            addBlock(parent, b);
          }
        }
      }
    } finally {
      graph.getModel().endUpdate();
    }
    new mxHierarchicalLayout(graph).execute(graph.getDefaultParent());
    this.repaint();
  }

  private HashMap<Block, Object> existing = new HashMap<>();

  private Object addBlock(Object parent, Block b) {
    Object v1 = null;
    if (existing.containsKey(b)) {
      return existing.get(b);
    } else {

      //old code for analysis without bytecode
      //boolean isNormal = b.getLabel() != null;
      //String text = "Block " + cf.indexOf(b) + (isNormal ? "\n" + "Label " + OpUtils.getLabelIndex(b.getLabel()) : "");
      //fillColor is (isNormal ? vertexColor : singleVertexColor)

      String text = "";
      for (AbstractInsnNode ain : b.getNodes()) {
        text += InstrUtils.toString(ain) + "\n";
      }
      v1 = graph.insertVertex(parent, null, text, 150, 10, 80, 40, "fillColor=#FFFFFF;fontColor=#111111;strokeColor=#9297a1;");
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

}
