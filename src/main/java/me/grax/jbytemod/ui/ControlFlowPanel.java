package me.grax.jbytemod.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodNode;

import com.mxgraph.layout.mxCircleLayout;
import com.mxgraph.layout.mxCompactTreeLayout;
import com.mxgraph.layout.mxEdgeLabelLayout;
import com.mxgraph.layout.mxFastOrganicLayout;
import com.mxgraph.layout.mxParallelEdgeLayout;
import com.mxgraph.layout.mxPartitionLayout;
import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.model.mxCell;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.util.mxCellRenderer;
import com.mxgraph.util.mxConstants;
import com.mxgraph.view.mxGraph;
import com.mxgraph.view.mxGraphView;
import com.mxgraph.view.mxStylesheet;

import me.grax.jbytemod.JByteMod;
import me.grax.jbytemod.analysis.block.Block;
import me.grax.jbytemod.analysis.converter.Converter;
import me.grax.jbytemod.utils.ErrorDisplay;
import me.grax.jbytemod.utils.ImageUtils;
import me.grax.jbytemod.utils.InstrUtils;

public class ControlFlowPanel extends JPanel {

  private MethodNode node;
  private ArrayList<Block> cf = new ArrayList<>();
  private mxGraph graph;
  private mxGraphComponent graphComponent;
  private JScrollPane scp;

  private static final String edgeColor = "#111111";
  private static final String jumpColor = "#39698a";

  private static final String jumpColorGreen = "#388a47";
  private static final String jumpColorRed = "#8a3e38";
  private static final String jumpColorPurple = "#71388a";
  private static final String jumpColorPink = "#8a386d";

  public ControlFlowPanel(JByteMod jbm) {
    this.setLayout(new BorderLayout(0, 0));
    graph = new mxGraph();
    graph.setAutoOrigin(true);
    graph.setAutoSizeCells(true);
    graph.setHtmlLabels(true);
    setStyles();

    JPanel lpad = new JPanel();
    lpad.setBorder(new EmptyBorder(1, 5, 0, 1));
    lpad.setLayout(new GridLayout());
    lpad.add(new JLabel(JByteMod.res.getResource("ctrl_flow_vis")));
    JPanel rs = new JPanel();
    rs.setLayout(new GridLayout(1, 5));
    for (int i = 0; i < 3; i++)
      rs.add(new JPanel());
    JButton save = new JButton(JByteMod.res.getResource("save"));
    save.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        if (node == null) {
          return;
        }
        File parentDir = new File(System.getProperty("user.home") + File.separator + "Desktop");
        JFileChooser jfc = new JFileChooser(parentDir);
        jfc.setAcceptAllFileFilterUsed(false);
        jfc.setFileFilter(new FileNameExtensionFilter("Bitmap image file (.bmp)", "bmp"));
        jfc.addChoosableFileFilter(new FileNameExtensionFilter("Portable Network Graphics (.png)", "png"));
        if (node.name.length() < 32) {
          jfc.setSelectedFile(new File(parentDir, node.name + ".bmp"));
        } else {
          jfc.setSelectedFile(new File(parentDir, "method.bmp"));
        }
        int result = jfc.showSaveDialog(ControlFlowPanel.this);
        if (result == JFileChooser.APPROVE_OPTION) {
          File output = jfc.getSelectedFile();
          String type = ((FileNameExtensionFilter) jfc.getFileFilter()).getExtensions()[0];
          JByteMod.LOGGER.log("Saving graph as " + type + " file (" + output.getName() + ")");
          BufferedImage image = mxCellRenderer.createBufferedImage(graph, null, 1, Color.WHITE, true, null);
          try {
            ImageIO.write(ImageUtils.watermark(image), type, output);
          } catch (IOException e1) {
            new ErrorDisplay(e1);
          }
        }
      }
    });
    rs.add(save);
    JButton reload = new JButton(JByteMod.res.getResource("reload"));
    reload.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        generateList();
      }
    });
    rs.add(reload);
    lpad.add(rs);
    this.add(lpad, BorderLayout.NORTH);

    graphComponent = new mxGraphComponent(graph) {
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
        if ((scp.getVerticalScrollBar().isVisible() || scale >= 1) && scale > 0.3) {
          zoom(1 / zoomFactor);
        }
      }
    };
    graphComponent.getViewport().setBackground(Color.WHITE);
    graphComponent.setEnabled(false);
    graphComponent.setBorder(new EmptyBorder(0, 0, 0, 0));
    graphComponent.setZoomFactor(1.1);
    graphComponent.getGraphControl().addMouseListener(new MouseAdapter() {
      @Override
      public void mousePressed(MouseEvent e) {
        if (SwingUtilities.isRightMouseButton(e)) {
          mxCell cell = (mxCell) graphComponent.getCellAt(e.getX(), e.getY());
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
    });
    graphComponent.getGraphControl().addMouseWheelListener(new MouseWheelListener() {

      @Override
      public void mouseWheelMoved(MouseWheelEvent e) {
        if (e.isControlDown()) {
          if (e.getWheelRotation() < 0) {
            graphComponent.zoomIn();
          } else {
            graphComponent.zoomOut();
          }
          repaint();
          revalidate();
        } else {
          //do we need this on linux too?
          scp.getVerticalScrollBar()
              .setValue(scp.getVerticalScrollBar().getValue() + e.getUnitsToScroll() * scp.getVerticalScrollBar().getUnitIncrement());
        }
      }
    });
    JPanel inner = new JPanel();
    inner.setBorder(new EmptyBorder(30, 30, 30, 30));
    inner.setLayout(new BorderLayout(0, 0));
    inner.setBackground(Color.WHITE);
    inner.add(graphComponent, BorderLayout.CENTER);
    graphComponent.removeMouseWheelListener(graphComponent.getMouseWheelListeners()[0]);
    scp = new JScrollPane(inner);

    scp.getVerticalScrollBar().setUnitIncrement(16);
    this.add(scp, BorderLayout.CENTER);
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
      cf.addAll(c.convert(JByteMod.ops.get("simplify_graph").getBoolean(), JByteMod.ops.get("remove_redundant").getBoolean(), true));
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
        boolean first = true;
        for (Block b : cf) {
          if (b.getInput().isEmpty() || first) {
            addBlock(parent, b);
            first = false;
          }
        }
      }
      graph.getView().setScale(1);
      mxHierarchicalLayout layout = new mxHierarchicalLayout(graph);
      layout.setFineTuning(true);
      layout.setIntraCellSpacing(25d);
      layout.setInterRankCellSpacing(80d);
      layout.execute(graph.getDefaultParent());
      //      mxPartitionLayout ct = new mxPartitionLayout(graph);
      //      ct.execute(graph.getDefaultParent());
      //      new mxCircleLayout(graph).execute(graph.getDefaultParent());
      //      new mxParallelEdgeLayout(graph).execute(graph.getDefaultParent());
      //      mxFastOrganicLayout first = new mxFastOrganicLayout(graph);
      //      mxParallelEdgeLayout second = new mxParallelEdgeLayout(graph);
    } finally {
      graph.getModel().endUpdate();
    }
    this.revalidate();
    this.repaint();
  }

  private HashMap<Block, Object> existing = new HashMap<>();

  private Object addBlock(Object parent, Block b) {
    Object v1 = null;
    if (existing.containsKey(b)) {
      return existing.get(b);
    } else {
      v1 = graph.insertVertex(parent, null, new BlockVertex(b.getNodes(), b.getLabel(), node.instructions.indexOf(b.getNodes().get(0))), 150, 10, 80,
          40, "fillColor=#FFFFFF;fontColor=#111111;strokeColor=#9297a1");
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

  class BlockVertex {
    private ArrayList<AbstractInsnNode> code;
    private LabelNode label;
    private int listIndex;
    private String text;

    public BlockVertex(ArrayList<AbstractInsnNode> code, LabelNode label, int listIndex) {
      super();
      this.code = code;
      this.label = label;
      this.listIndex = listIndex;
      this.setupText();
    }

    private void setupText() {
      text = "";
      for (AbstractInsnNode ain : code) {
        text += InstrUtils.toString(ain) + "\n";
      }
    }

    public ArrayList<AbstractInsnNode> getCode() {
      return code;
    }

    public void setCode(ArrayList<AbstractInsnNode> code) {
      this.code = code;
    }

    public LabelNode getLabel() {
      return label;
    }

    public void setLabel(LabelNode label) {
      this.label = label;
    }

    public int getListIndex() {
      return listIndex;
    }

    public void setListIndex(int listIndex) {
      this.listIndex = listIndex;
    }

    @Override
    public String toString() {
      if (text == null) {
        setupText();
      }
      return text;
    }
  }
}
