package me.grax.jbytemod.ui.graph;

import java.util.ArrayList;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.LabelNode;

import me.grax.jbytemod.utils.InstrUtils;

public   class BlockVertex {
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