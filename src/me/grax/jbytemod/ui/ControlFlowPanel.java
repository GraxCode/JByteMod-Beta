package me.grax.jbytemod.ui;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JPanel;

import org.objectweb.asm.tree.MethodNode;

import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.util.mxConstants;
import com.mxgraph.view.mxGraph;

import me.grax.jbytemod.analysis.block.Block;
import me.grax.jbytemod.analysis.converter.Converter;
import me.grax.jbytemod.utils.ErrorDisplay;
import me.lpk.util.OpUtils;

public class ControlFlowPanel extends JPanel {

  private MethodNode node;
  private ArrayList<Block> cf = new ArrayList<>();
  private mxGraph graph;
  private static final String vertexColor = "#4FC3F7";
  private static final String singleVertexColor = "#03A9F4";

  private static final String edgeColor = "#111111";
  private static final String jumpColor = "#555555";

  public ControlFlowPanel() {
    this.setLayout(new BorderLayout(0, 0));

    graph = new mxGraph();
    graph.setAutoOrigin(true);
    graph.setAutoSizeCells(true);
    mxGraphComponent graphComponent = new mxGraphComponent(graph);
    graphComponent.setEnabled(false); //TODO
    this.add(graphComponent);
  }

  public MethodNode getNode() {
    return node;
  }

  public void setNode(MethodNode node) {
    this.node = node;
    this.generateList();
    this.repaint();
  }

  private void generateList() {
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
          if (b.getInput().isEmpty()) { //there could be more than 1 (dead code) FIXME: multiple startpoints overlap
            addBlock(parent, b);
            break; //TODO
          }
        }
      }
    } finally {
      graph.getModel().endUpdate();
    }
    new mxHierarchicalLayout(graph).execute(graph.getDefaultParent());
    //    new mxFastOrganicLayout(graph).execute(graph.getDefaultParent());

  }

  private HashMap<Block, Object> existing = new HashMap<>();

  private Object addBlock(Object parent, Block b) {
    Object v1 = null;
    if (existing.containsKey(b)) {
      return existing.get(b);
    } else {
      boolean isNormal = b.getLabel() != null;
      v1 = graph.insertVertex(parent, null, "Block " + cf.indexOf(b) + (isNormal ? "\n" + "Label " + OpUtils.getLabelIndex(b.getLabel()) : ""), 150,
          10, 80, 40, "fillColor=" + (isNormal ? vertexColor : singleVertexColor) + ";fontColor=#111111;");
      existing.put(b, v1);
    }
    if (v1 == null) {
      throw new RuntimeException();
    }
    ArrayList<Block> next = b.getOutput();
    for (Block out : next) {
      if (out.equals(b)) {
        System.out.println("return to same");
        graph.insertEdge(parent, null, null, v1, v1);
      } else {
        assert (out.getInput().contains(b));
        graph.insertEdge(parent, null, null, v1, addBlock(parent, out), "strokeColor=" + (b.endsWithJump() ? jumpColor : edgeColor) + ";");
      }
    }
    return v1;
  }

}
