package me.grax.jbytemod.ui.graph;

import java.util.ArrayList;

import org.apache.commons.io.IOUtils;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodNode;

import com.strobel.core.ExceptionUtilities;

import me.grax.jbytemod.JByteMod;
import me.grax.jbytemod.analysis.block.Block;
import me.grax.jbytemod.analysis.decompiler.code.ast.Expression;
import me.grax.jbytemod.analysis.decompiler.struct.Conversion;
import me.grax.jbytemod.analysis.decompiler.struct.JVMStack;
import me.grax.jbytemod.analysis.decompiler.syntax.nodes.NodeList;
import me.grax.jbytemod.utils.ErrorDisplay;
import me.grax.jbytemod.utils.InstrUtils;

public class BlockVertex {
  private ArrayList<AbstractInsnNode> code;
  private LabelNode label;
  private int listIndex;
  private String text = null;
  private Block block;
  private MethodNode mn;
  private boolean decompile = JByteMod.ops.get("decompile_graph").getBoolean();
  private ArrayList<BlockVertex> input = new ArrayList<>();
  private JVMStack leftOverStack;
  private boolean setupText;

  public BlockVertex(MethodNode mn, Block block, ArrayList<AbstractInsnNode> code, LabelNode label, int listIndex) {
    super();
    this.mn = mn;
    this.block = block;
    this.code = code;
    this.label = label;
    this.listIndex = listIndex;
  }

  public void addInput(BlockVertex v) {
    if (!input.contains(v)) {
      this.input.add(v);
    }
  }

  public static int index = 0;

  public void setupText() {
    if (setupText) {
      return;
    }
    text = "";
    if (decompile) {
      ++index;
      try {
        NodeList list = new NodeList();
        JVMStack inputStack = null;
        if (!input.isEmpty()) {
          inputStack = input.get(0).getLeftOverStack();
        }
        Conversion c = new Conversion(mn, list, inputStack);
        c.convert(block);
        leftOverStack = c.getStack();
        for (Expression e : list) {
          text += e.toString() + "\n";
        }
      } catch (Exception e) {
        for (AbstractInsnNode ain : code) {
          text += InstrUtils.toString(ain) + "\n";
        }
        text += "\n<i>";
        text += ExceptionUtilities.getStackTraceString(e);
      }
    }
    if (text.trim().isEmpty()) {
      for (AbstractInsnNode ain : code) {
        text += InstrUtils.toString(ain) + "\n";
      }
    }
    setupText = true;
  }

  public JVMStack getLeftOverStack() {
    return leftOverStack;
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