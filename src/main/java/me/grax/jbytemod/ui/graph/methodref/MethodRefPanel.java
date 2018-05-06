package me.grax.jbytemod.ui.graph.methodref;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import com.mxgraph.layout.mxOrganicLayout;
import com.mxgraph.model.mxCell;
import com.mxgraph.util.mxCellRenderer;

import me.grax.jbytemod.JByteMod;
import me.grax.jbytemod.ui.graph.methodref.MRGraph.MRGComponent;
import me.grax.jbytemod.utils.ErrorDisplay;
import me.grax.jbytemod.utils.ImageUtils;

@Deprecated
public class MethodRefPanel extends JPanel {

  private MRGraph graph;
  private MRGComponent graphComponent;
  private JScrollPane scp;

  public MethodRefPanel(JByteMod jbm) {
    this.setLayout(new BorderLayout(0, 0));
    graph = new MRGraph(jbm);
    JPanel lpad = new JPanel();
    lpad.setBorder(new EmptyBorder(1, 5, 0, 1));
    lpad.setLayout(new GridLayout());
    lpad.add(new JLabel(JByteMod.res.getResource("network")));
    JPanel rs = new JPanel();
    rs.setLayout(new GridLayout(1, 5));
    for (int i = 0; i < 3; i++)
      rs.add(new JPanel());
    JButton save = new JButton(JByteMod.res.getResource("save"));
    save.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        File parentDir = new File(System.getProperty("user.home") + File.separator + "Desktop");
        JFileChooser jfc = new JFileChooser(parentDir);
        jfc.setAcceptAllFileFilterUsed(false);
        jfc.setFileFilter(new FileNameExtensionFilter("Bitmap image file (.bmp)", "bmp"));
        jfc.addChoosableFileFilter(new FileNameExtensionFilter("Portable Network Graphics (.png)", "png"));
        jfc.setSelectedFile(new File(parentDir, "classanalysis.bmp"));
        int result = jfc.showSaveDialog(MethodRefPanel.this);
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
        generateList(jbm.getFile().getClasses().values());
      }
    });
    rs.add(reload);
    lpad.add(rs);
    this.add(lpad, BorderLayout.NORTH);

    graphComponent = graph.getComponent();
    graphComponent.setScp(scp);
    JPanel inner = new JPanel();
    inner.setBorder(new EmptyBorder(30, 30, 30, 30));
    inner.setLayout(new BorderLayout(0, 0));
    inner.setBackground(Color.WHITE);
    inner.add(graphComponent, BorderLayout.CENTER);
    scp = new JScrollPane(inner);
    scp.getVerticalScrollBar().setUnitIncrement(16);
    this.add(scp, BorderLayout.CENTER);
  }

  public void generateList(Collection<ClassNode> nodes) {
    ArrayList<MethodNode> main = new ArrayList<>();
    HashMap<String, MethodNode> methods = new HashMap<>();

    for (ClassNode cn : nodes) {
      for (MethodNode mn : cn.methods) {
        methods.put(mn.owner + "." + mn.name + mn.desc, mn);
        if (mn.name.equals("main") && mn.desc.equals("([Ljava/lang/String;)V")) {
          main.add(mn);
        }
      }
    }
    graph.getModel().beginUpdate();
    try {
      graph.removeCells(graph.getChildCells(graph.getDefaultParent(), true, true));
      HashMap<MethodNode, mxCell> cells = new HashMap<>();
      Object parent = graph.getDefaultParent();
      for (MethodNode mainMethod : main) {
        addMethodTree(methods, parent, cells, mainMethod);
      }
      JByteMod.LOGGER.log("Generated Graph... formatting");
      graph.getView().setScale(1);
      mxOrganicLayout layout = new mxOrganicLayout(graph);
      layout.setFineTuning(false);
      layout.execute(graph.getDefaultParent());
    } finally {
      graph.getModel().endUpdate();
    }
    this.revalidate();
    this.repaint();
  }

  private mxCell addMethodTree(HashMap<String, MethodNode> methods, Object parent, HashMap<MethodNode, mxCell> cells, MethodNode method) {
    if (cells.containsKey(method))
      return cells.get(method);
    mxCell v1 = (mxCell) graph.insertVertex(parent, null, method.owner + "." + method.name, 150, 10, 80, 40,
        "fillColor=#FFFFFF;fontColor=#111111;strokeColor=#9297a1");
    graph.updateCellSize(v1); //resize cell
    cells.put(method, v1);
    ArrayList<MethodNode> added = new ArrayList<>();
    for (AbstractInsnNode ain : method.instructions.toArray()) {
      if (ain instanceof MethodInsnNode) {
        MethodInsnNode min = (MethodInsnNode) ain;
        MethodNode call = methods.get(min.owner + "." + min.name + min.desc);
        if (call == null || added.contains(call))
          continue;
        added.add(call);
        if (call.equals(method)) {
          graph.insertEdge(parent, null, null, v1, v1, "strokeColor=" + getEdgeColor(min.getOpcode()) + ";");
        } else {
          mxCell vertexOut = addMethodTree(methods, parent, cells, call);
          graph.insertEdge(parent, null, null, v1, vertexOut, "strokeColor=" + getEdgeColor(min.getOpcode()) + ";");
        }
      }
    }
    return v1;
  }

  private String getEdgeColor(int opcode) {
    return "#000000";
  }

  public void clear() {
    graph.getModel().beginUpdate();
    graph.removeCells(graph.getChildCells(graph.getDefaultParent(), true, true));
    graph.getModel().endUpdate();
  }
}
