package me.grax.decompiler.code.ast.expressions;

import me.grax.decompiler.code.ast.Expression;
import me.grax.jbytemod.utils.TextUtils;

public class TextExpression extends Expression {

  private String text;

  public TextExpression(String var) {
    this.text = var;
  }

  @Override
  public String toString() {
    return "<i>" + TextUtils.escape(text) + "</i>";
  }

  @Override
  public int size() {
    return 0;
  }

  @Override
  public Expression clone() {
    return new TextExpression(text);
  }
}
