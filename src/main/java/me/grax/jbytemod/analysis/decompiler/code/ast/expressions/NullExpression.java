package me.grax.jbytemod.analysis.decompiler.code.ast.expressions;

import me.grax.jbytemod.analysis.decompiler.code.ast.Expression;
import me.grax.jbytemod.analysis.decompiler.defs.Keywords;

public class NullExpression extends Expression {

  @Override
  public String toString() {
    return "<b>" + Keywords.NULL + "</b>";
  }

  @Override
  public int size() {
    return 1;
  }

  @Override
  public Expression clone() {
    return new NullExpression();
  }

}
