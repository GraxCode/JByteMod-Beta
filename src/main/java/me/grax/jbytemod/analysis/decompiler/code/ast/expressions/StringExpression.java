package me.grax.jbytemod.analysis.decompiler.code.ast.expressions;

import me.grax.jbytemod.analysis.decompiler.code.ast.Expression;
import me.grax.jbytemod.utils.TextUtils;

public class StringExpression extends Expression {

  private String value;

  public StringExpression(String value) {
    super();
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  @Override
  public String toString() {
    return TextUtils.addTag("\"" + value + "\"", "font color=#559955"); //TODO escape
  }

  @Override
  public int size() {
    return 1;
  }

  @Override
  public Expression clone() {
    return new StringExpression(value);
  }

}
