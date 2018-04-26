package me.grax.jbytemod.analysis.decompiler.code.ast.expressions;

import me.grax.jbytemod.analysis.decompiler.code.ast.Expression;
import me.grax.jbytemod.analysis.decompiler.code.ast.VarType;
import me.grax.jbytemod.utils.TextUtils;

public class DebugStackUnknownExpression extends Expression {

  private int var;
  private int size;
  private VarType type;

  public DebugStackUnknownExpression(int var, int size, VarType type) {
    this.var = var;
    this.size = size;
    this.type = type;
  }

  @Override
  public String toString() {
    return TextUtils.addTag("<i>unkn_stack_" + var + "</i>", "font color=#997755");
  }

  @Override
  public int size() {
    return size;
  }

  @Override
  public Expression clone() {
    return new DebugStackUnknownExpression(var, size, type);
  }

  public int getVar() {
    return var;
  }

  public void setVar(int var) {
    this.var = var;
  }

  public int getSize() {
    return size;
  }

  public void setSize(int size) {
    this.size = size;
  }

  public VarType getType() {
    return type;
  }

  public void setType(VarType type) {
    this.type = type;
  }

}
