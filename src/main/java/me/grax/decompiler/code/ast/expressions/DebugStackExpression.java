package me.grax.decompiler.code.ast.expressions;

import me.grax.decompiler.code.ast.Expression;
import me.grax.jbytemod.utils.TextUtils;

public class DebugStackExpression extends Expression {

  private int var;
  private int size;

  public DebugStackExpression(int var, int size) {
    this.var = var;
    this.size = size;
  }
  @Override
  public String toString() {
    return TextUtils.addTag("<i>stack_" + var + "</i>", "font color=#909011");
  }

  @Override
  public int size() {
    return size; 
  }

  @Override
  public Expression clone() {
    return new DebugStackExpression(var, size);
  }

}
