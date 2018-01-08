package me.grax.jbytemod.analysis.converter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
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

  public ArrayList<Block> convert() {
    ArrayList<Block> blocks = new ArrayList<>();
    HashMap<AbstractInsnNode, Block> correspBlock = new HashMap<>();
    Block block = null;
    //detect block structure & last block insns
    for (AbstractInsnNode ain : nodes) {
      if (block == null) {
        block = new Block();
      }
      correspBlock.put(ain, block);
      //end blocks
      if (ain.getOpcode() >= IRETURN && ain.getOpcode() <= RETURN || ain instanceof JumpInsnNode || ain.getOpcode() == ATHROW) {
        block.setEndNode(ain);
        blocks.add(block);
        block = null;
        continue;
      }
      //next because the label should have a new block
      //blocks that end without a jump
      if (ain.getNext() != null && (ain.getNext() instanceof LabelNode)) {
        block.setEndNode(ain.getNext());
        blocks.add(block);
        block = null;
        continue;
      }
    }
    for (Block b : blocks) {
      AbstractInsnNode end = b.getEndNode();
      //only opc where it continues
      if (end instanceof JumpInsnNode) {
        JumpInsnNode jin = (JumpInsnNode) end;
        ArrayList<Block> outputs = new ArrayList<>();
        if (!correspBlock.containsKey(jin.label)) {
          throw new RuntimeException("label not visited");
        }
        Block blockAtLabel = correspBlock.get(jin.label);
        //blockAtLabel can be the same block!
        if (end.getOpcode() == GOTO) {
          outputs.add(blockAtLabel);
          b.setOutput(outputs);
          blockAtLabel.getInput().add(b);
        } else {
          //ifs have two outputs: either it jumps or not
          outputs.add(blockAtLabel);
          if (jin.getNext() == null) {
            throw new RuntimeException("if has no next entry");
          }
          if (correspBlock.get(jin.getNext()) == b) {
            throw new RuntimeException("next node is self?");
          }
          Block blockAfter = correspBlock.get(jin.getNext());
          outputs.add(blockAfter);
          b.setOutput(outputs);
          blockAtLabel.getInput().add(b);
          blockAfter.getInput().add(b);
        }
      }
      if (end instanceof LabelNode) {
        if (!correspBlock.containsKey(end)) {
          throw new RuntimeException("label not visited");
        }
        ArrayList<Block> outputs = new ArrayList<>();
        Block blockAtNext = correspBlock.get(end);
        outputs.add(blockAtNext);
        b.setOutput(outputs);
        blockAtNext.getInput().add(b);
      }
    }
    return blocks;
  }
}
