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
public class Block implements Iterable<AbstractInsnNode> {
  private ArrayList<Block> output = new ArrayList<>();

  /**
   * when output is added automatically add input
   **/
  private ArrayList<Block> input = new ArrayList<>();

  private List<AbstractInsnNode> nodes;

  public Block(List<AbstractInsnNode> list, ArrayList<Block> output) {
    super();
    this.output = output;
    this.nodes = list;
  }

  public List<AbstractInsnNode> getNodes() {
    return nodes;
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

  @Override
  public Iterator<AbstractInsnNode> iterator() {
    return nodes.iterator();
  }

}
