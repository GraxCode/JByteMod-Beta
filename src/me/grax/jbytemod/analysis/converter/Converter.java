package me.grax.jbytemod.analysis.converter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.MethodNode;

import me.grax.jbytemod.analysis.block.Block;

public class Converter implements Opcodes {

  private ArrayList<AbstractInsnNode> nodes;
  private MethodNode mn;

  public Converter(MethodNode mn) {
    assert (mn.instructions != null && mn.instructions.size() > 0);
    this.nodes = new ArrayList<>(Arrays.asList(mn.instructions.toArray()));
    this.mn = mn;
  }

  //FIXME: not really working, getBlock sometimes causes stackoverflow, or ifs don't have a label after them
  //at recoding we need no instructions
  public Collection<Block> convert() {
    HashMap<LabelNode, UnfinishedBlock> map = new LinkedHashMap<>();
    LabelNode current = null;
    //if it doesn't start with a labelnode, add one
    if (!(nodes.get(0) instanceof LabelNode)) {
      nodes.add(0, new LabelNode());
    }
    //TODO if jumps may not have a label after them, add one
    ArrayList<AbstractInsnNode> currentBlock = null;
    for (AbstractInsnNode ain : nodes) {
      if (ain instanceof LineNumberNode) {
        continue;
      }
      if (current != null) {
        if (ain instanceof LabelNode) {
          ArrayList<LabelNode> output = new ArrayList<>();
          output.add((LabelNode) ain);
          map.put(current, new UnfinishedBlock(currentBlock, output));
          current = (LabelNode) ain;
          currentBlock = new ArrayList<>();
          continue;
        }
        currentBlock.add(ain);
        if (ain instanceof JumpInsnNode) {
          //block finished
          JumpInsnNode jin = (JumpInsnNode) ain;
          ArrayList<LabelNode> output = new ArrayList<>();
          if (jin.getOpcode() == GOTO) {
            output.add(jin.label);
          } else {
            output.add(jin.label);
            output.add((LabelNode) ain.getNext());
          }
          map.put(current, new UnfinishedBlock(currentBlock, output));
          current = null;
        }
      } else if (ain instanceof LabelNode) {
        current = (LabelNode) ain;
        currentBlock = new ArrayList<>();
      }
    }
    //lastNode
    map.put(current, new UnfinishedBlock(currentBlock, new ArrayList<>()));
    
    //TODO maybe here is a bug?
    //we need to use a sorted hashmap here
    HashMap<LabelNode, Block> finishedBlocks = new LinkedHashMap<>();
    for (Entry<LabelNode, UnfinishedBlock> e : map.entrySet()) {
      LabelNode l = e.getKey();
      finishedBlocks.put(l, getBlock(map, finishedBlocks, l));
    }
    return finishedBlocks.values();
  }

  private Block getBlock(HashMap<LabelNode, UnfinishedBlock> unfinished, HashMap<LabelNode, Block> finished, LabelNode l) {
    if (finished.containsKey(l)) {
      return finished.get(l);
    }
    UnfinishedBlock ufb = unfinished.get(l);
    assert (ufb != null);
    Block b = new Block(ufb.list, null);
    ArrayList<Block> output = new ArrayList<>();
    for (LabelNode ol : ufb.output) {
      Block ob = getBlock(unfinished, finished, ol);
      ob.getInput().add(b);
      output.add(ob);
    }
    b.setOutput(output);
    finished.put(l, b);
    return b;
  }

  class UnfinishedBlock {
    ArrayList<AbstractInsnNode> list;
    ArrayList<LabelNode> output;

    public UnfinishedBlock(ArrayList<AbstractInsnNode> list, ArrayList<LabelNode> output) {
      super();
      this.list = list;
      this.output = output;
    }

  }
}
