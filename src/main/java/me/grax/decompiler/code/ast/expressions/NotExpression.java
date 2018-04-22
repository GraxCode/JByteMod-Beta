package me.grax.decompiler.code.ast.expressions;

import me.grax.decompiler.code.ast.Expression;

public class NotExpression extends Expression {
  private Expression toNegate;

  public NotExpression(Expression toNegate) {
    super();
    this.toNegate = toNegate;
  }

  public Expression getToNegate() {
    return toNegate;
  }

  public void setToNegate(Expression toNegate) {
    this.toNegate = toNegate;
  }

  @Override
  public String toString() {
    return "!(" + toNegate.toString() + ")";
  }

  @Override
  public int size() {
    return toNegate.size();
  }

  @Override
  public Expression clone() {
    return new NotExpression(toNegate.clone());
  }

}
