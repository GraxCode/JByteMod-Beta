package me.grax.jbytemod.ui;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JPanel;

import org.objectweb.asm.tree.MethodNode;

import com.mxgraph.layout.mxCompactTreeLayout;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.view.mxGraph;

import me.grax.jbytemod.analysis.block.Block;
import me.grax.jbytemod.analysis.converter.Converter;

public class ControlFlowPanel extends JPanel {

  private MethodNode node;
  private ArrayList<Block> cf = new ArrayList<>();
  private mxGraph graph;

  public ControlFlowPanel() {
    this.setLayout(new BorderLayout(0, 0));

    graph = new mxGraph() {
      //      @Override
      //      public boolean isCellSelectable(Object cell) {
      //        if (cell != null) {
      //          if (cell instanceof mxCell) {
      //            mxCell myCell = (mxCell) cell;
      //            if (myCell.isEdge())
      //              return false;
      //          }
      //        }
      //        return super.isCellSelectable(cell);
      //      }
      //
      //      @Override
      //      public boolean isCellEditable(Object arg0) {
      //        return false;
      //      }
      //
      //      @Override
      //      public boolean isCellResizable(Object arg0) {
      //        return false;
      //      }
    };
    graph.setAutoOrigin(true);
    graph.setAutoSizeCells(true);
    mxGraphComponent graphComponent = new mxGraphComponent(graph);
    //    graphComponent.setEnabled(false); //TODO
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
    cf.addAll(c.convert());
    System.out.println("Blocks: " + cf.size());
    int i = 0;
    for (Block b : cf) {
      System.out.println("block " + i++ + " nodes: ?" + " outputs: " + b.getOutput().size());
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
    mxCompactTreeLayout layout = new mxCompactTreeLayout(graph);
    //    layout.setFineTuning(false);
    layout.execute(graph.getDefaultParent());
    //    new mxParallelEdgeLayout(graph).execute(graph.getDefaultParent());
    //    new mxEdgeLabelLayout(graph).execute(graph.getDefaultParent());
  }

  private HashMap<Block, Object> existing = new HashMap<>();

  private Object addBlock(Object parent, Block b) {
    Object v1 = null;
    if (existing.containsKey(b)) {
      return existing.get(b);
    } else {
      v1 = graph.insertVertex(parent, null, "Block " + cf.indexOf(b), 150, 10, 80, 30);
      existing.put(b, v1);
    }
    if (v1 == null) {
      throw new RuntimeException();
    }
    ArrayList<Block> next = b.getOutput();
    for (Block out : next) {
      if (out.equals(b)) {
        System.out.println("return to same");
      } else {
        if (b == out) {
          System.out.println("loop?");
          return v1;
        }
        assert (out.getInput().contains(b));
        graph.insertEdge(parent, null, null, v1, addBlock(parent, out));
      }
    }
    return v1;
  }

}
