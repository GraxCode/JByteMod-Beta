package me.grax.decompiler.code.ast.expressions;

import me.grax.decompiler.code.ast.Expression;
import me.grax.decompiler.defs.Keywords;

public class ReturnExpression extends Expression {

  private Expression returnValue;

  public ReturnExpression(Expression returnValue) {
    super();
    this.returnValue = returnValue;
  }

  public ReturnExpression() {
    super();
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("<b>" + Keywords.RETURN + "</b>");
    if (returnValue != null) {
      sb.append(" ");
      sb.append(returnValue);
    }
    return sb.toString();
  }

  @Override
  public int size() {
    return 0;
  }

  @Override
  public Expression clone() {
    return new ReturnExpression(returnValue);
  }

}
