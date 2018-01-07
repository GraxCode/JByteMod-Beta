package me.grax.jbytemod.analysis.block;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnNode;

/**
 * Separated by labels (jumps)
 * 
 * @author Admin
 *
 */
public class Block {
  private ArrayList<Block> output = new ArrayList<>();

  /**
   * when output is added automatically add input
   **/
  private ArrayList<Block> input = new ArrayList<>();

  private AbstractInsnNode endNode;

  public Block() {
    super();
  }

  public AbstractInsnNode getEndNode() {
    return endNode;
  }

  public void setEndNode(AbstractInsnNode endNode) {
    this.endNode = endNode;
  }

  public ArrayList<Block> getOutput() {
    return output;
  }

  public void setOutput(ArrayList<Block> output) {
    this.output = output;
  }

  public ArrayList<Block> getInput() {
    return input;
  }

  public void setInput(ArrayList<Block> input) {
    this.input = input;
  }

}
