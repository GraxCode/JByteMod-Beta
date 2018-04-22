package me.grax.jbytemod.ui.graph;

import java.util.ArrayList;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodNode;

import me.grax.decompiler.code.ast.Expression;
import me.grax.decompiler.struct.Conversion;
import me.grax.decompiler.syntax.nodes.NodeList;
import me.grax.jbytemod.JByteMod;
import me.grax.jbytemod.analysis.block.Block;
import me.grax.jbytemod.utils.ErrorDisplay;
import me.grax.jbytemod.utils.InstrUtils;

public class BlockVertex {
  private ArrayList<AbstractInsnNode> code;
  private LabelNode label;
  private int listIndex;
  private String text;
  private Block block;
  private MethodNode mn;
  private boolean decompile = JByteMod.ops.get("decompile_graph").getBoolean();

  public BlockVertex(MethodNode mn, Block block, ArrayList<AbstractInsnNode> code, LabelNode label, int listIndex) {
    super();
    this.mn = mn;
    this.block = block;
    this.code = code;
    this.label = label;
    this.listIndex = listIndex;
    this.setupText();
  }

  private void setupText() {
    text = "";
    if (decompile) {
      try {
        NodeList list = new NodeList();
        Conversion c = new Conversion(mn, list);
        c.convert(block);
        for (Expression e : list) {
          text += e.toString() + "\n";
        }
      } catch (Exception e) {
        new ErrorDisplay(e);
      }
    }
    if (text.trim().isEmpty()) {
      for (AbstractInsnNode ain : code) {
        text += InstrUtils.toString(ain) + "\n";
      }
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

  public Block getBlock() {
    return block;
  }

  @Override
  public String toString() {
    if (text == null) {
      setupText();
    }
    return text;
  }
}