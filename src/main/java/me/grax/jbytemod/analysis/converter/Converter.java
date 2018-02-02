package me.grax.jbytemod.analysis.converter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LookupSwitchInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TableSwitchInsnNode;

import me.grax.jbytemod.analysis.block.Block;

public class Converter implements Opcodes {

  private ArrayList<AbstractInsnNode> nodes;

  public Converter(MethodNode mn) {
    assert (mn.instructions != null && mn.instructions.size() > 0);
    this.nodes = new ArrayList<>(Arrays.asList(mn.instructions.toArray()));
  }

  public Converter(AbstractInsnNode[] array) {
    assert (array != null && array.length > 0);
    this.nodes = new ArrayList<>(Arrays.asList(array));
  }

  public ArrayList<Block> convert(boolean simplify) {
    ArrayList<Block> blocks = new ArrayList<>();
    HashMap<AbstractInsnNode, Block> correspBlock = new HashMap<>();
    Block block = null;
    //detect block structure & last block insns
    for (AbstractInsnNode ain : nodes) {
      if (block == null) {
        block = new Block();
      }
      if (ain instanceof LabelNode) {
        block.setLabel((LabelNode) ain);
      }
      block.getNodes().add(ain);
      correspBlock.put(ain, block);
      //end blocks
      int op = ain.getOpcode();
      if (op >= IRETURN && op <= RETURN || ain instanceof JumpInsnNode || op == ATHROW || op == LOOKUPSWITCH || op == TABLESWITCH) {
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
      } else if (end instanceof TableSwitchInsnNode) {
        ArrayList<Block> outputs = new ArrayList<>();
        TableSwitchInsnNode tsin = (TableSwitchInsnNode) end;
        if (tsin.dflt != null) {
          Block blockAtDefault = correspBlock.get(tsin.dflt);
          blockAtDefault.getInput().add(b);
          outputs.add(blockAtDefault);
        }
        for (LabelNode l : tsin.labels) {
          Block blockAtCase = correspBlock.get(l);
          blockAtCase.getInput().add(b);
          outputs.add(blockAtCase);
        }
        b.setOutput(outputs);
      } else if (end instanceof LookupSwitchInsnNode) {
        ArrayList<Block> outputs = new ArrayList<>();
        LookupSwitchInsnNode lsin = (LookupSwitchInsnNode) end;
        if (lsin.dflt != null) {
          Block blockAtDefault = correspBlock.get(lsin.dflt);
          blockAtDefault.getInput().add(b);
          outputs.add(blockAtDefault);
        }
        for (LabelNode l : lsin.labels) {
          Block blockAtCase = correspBlock.get(l);
          blockAtCase.getInput().add(b);
          outputs.add(blockAtCase);
        }
        b.setOutput(outputs);
      } else if (end instanceof LabelNode) {
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
    if (simplify) {
      for (Block b : new ArrayList<>(blocks)) {
        if (b.getInput().isEmpty()) {
          simplifyBlock(new ArrayList<>(), blocks, b);
        }
      }

    }
    return blocks;
  }

  private void simplifyBlock(ArrayList<Block> simplified, ArrayList<Block> blocks, Block b) {
    if (simplified.contains(b)) {
      return;
    }
    simplified.add(b);
    while (true) {
      if (b.getOutput().size() == 1) {
        Block to = b.getOutput().get(0);
        //also optimizes unnecessary gotos
        if (to.getInput().size() == 1) {
          assert (to.getInput().get(0) == b);
          b.getNodes().addAll(to.getNodes());
          b.setEndNode(to.getEndNode());
          b.setOutput(to.getOutput());
          blocks.remove(to);
          continue;
        }
      }
      break;
    }
    for (Block output : b.getOutput()) {
      if (!blocks.contains(output)) {
        System.out.println("failure");
      }
      simplifyBlock(simplified, blocks, output);
    }
  }
}
