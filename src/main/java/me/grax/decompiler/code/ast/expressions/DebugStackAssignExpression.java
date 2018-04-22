package me.grax.decompiler.code.ast.expressions;

import me.grax.decompiler.code.ast.Expression;
import me.grax.jbytemod.utils.TextUtils;

public class DebugStackAssignExpression extends Expression {

  private int var;
  private Expression value;

  public DebugStackAssignExpression(int var, Expression value) {
    this.var = var;
    this.value = value;
  }

  @Override
  public String toString() {
    return TextUtils.addTag("<i>stack_" + var + "</i>", "font color=#909011") + " = " + value;
  }

  @Override
  public int size() {
    return 0;
  }

  @Override
  public Expression clone() {
    return new DebugStackAssignExpression(var, value.clone());
  }

}
