package me.grax.jbytemod.analysis.decompiler.code.ast.expressions;

import me.grax.jbytemod.analysis.decompiler.code.ast.Expression;
import me.grax.jbytemod.analysis.decompiler.defs.Keywords;

public class ThrowExpression extends Expression {

  private Expression throwable;

  public ThrowExpression(Expression throwable) {
    super();
    this.throwable = throwable;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("<b>" + Keywords.THROW + "</b>");
    if (throwable != null) {
      sb.append(" ");
      sb.append(throwable);
    }
    return sb.toString();
  }

  @Override
  public int size() {
    return 0;
  }

  @Override
  public Expression clone() {
    return new ThrowExpression(throwable);
  }

}
