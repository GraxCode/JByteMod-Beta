package me.grax.jbytemod.analysis.decompiler.code.ast.expressions;

import me.grax.jbytemod.analysis.decompiler.code.ast.Expression;
import me.grax.jbytemod.utils.TextUtils;

public class DebugStackAssignExpression extends Expression {

  private int var;
  private Expression value;
  private String prefix;

  public DebugStackAssignExpression(int var, Expression value) {
    this(var, value, "stack");
  }

  public DebugStackAssignExpression(int var, Expression value, String prefix) {
    this.var = var;
    this.value = value;
    this.prefix = prefix;
  }

  @Override
  public String toString() {
    return TextUtils.addTag("<i>" + prefix + var + "</i>", "font color=#909011") + " = " + value;
  }

  @Override
  public int size() {
    return 0;
  }

  public String getPrefix() {
    return prefix;
  }

  public void setPrefix(String prefix) {
    this.prefix = prefix;
  }

  @Override
  public Expression clone() {
    return new DebugStackAssignExpression(var, value.clone(), prefix);
  }

}
