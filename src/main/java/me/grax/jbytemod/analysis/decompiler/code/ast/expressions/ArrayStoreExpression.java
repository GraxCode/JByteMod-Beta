package me.grax.jbytemod.analysis.decompiler.code.ast.expressions;

import me.grax.jbytemod.analysis.decompiler.code.ast.Expression;
import me.grax.jbytemod.utils.InstrUtils;
import me.grax.jbytemod.utils.TextUtils;

public class ArrayStoreExpression extends Expression {
  private Expression array;
  private Expression index;
  private Expression value;

  public ArrayStoreExpression(Expression array, Expression index, Expression value) {
    super();
    this.array = array;
    this.index = index;
    this.value = value;

  }

  @Override
  public String toString() {
    return TextUtils.addTag(array.toString(), "font color=" + InstrUtils.primColor.getString()) + "[" + index + "]" + " = " + value;
  }

  @Override
  public int size() {
    return 0;
  }

  @Override
  public Expression clone() {
    return new ArrayStoreExpression(array.clone(), index.clone(), value.clone());
  }
}
