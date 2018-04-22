package me.grax.decompiler.code.ast.expressions;

import me.grax.decompiler.code.ast.Expression;
import me.grax.jbytemod.utils.TextUtils;

public class VarSpecialExpression extends Expression {

  private String var;

  public VarSpecialExpression(String var) {
    this.var = var;
  }

  @Override
  public String toString() {
    return TextUtils.addTag(var, "font color=#669966");
  }

  @Override
  public int size() {
    return 1;
  }

  @Override
  public Expression clone() {
    return new VarSpecialExpression(var);
  }
}
