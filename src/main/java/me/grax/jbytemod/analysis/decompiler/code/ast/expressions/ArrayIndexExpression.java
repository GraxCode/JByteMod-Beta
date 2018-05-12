package me.grax.jbytemod.analysis.decompiler.code.ast.expressions;

import me.grax.jbytemod.analysis.decompiler.code.ast.Expression;
import me.grax.jbytemod.utils.InstrUtils;
import me.grax.jbytemod.utils.TextUtils;

public class ArrayIndexExpression extends Expression {

  private Expression array;
  private Expression index;
  private boolean twoword;

  public ArrayIndexExpression(Expression array, Expression index, boolean twoword) {
    super();
    this.array = array;
    this.index = index;
    this.twoword = twoword;
  }

  @Override
  public String toString() {
    return TextUtils.addTag(array.toString(), "font color=" + InstrUtils.primColor.getString()) + "[" + index + "]";
  }

  @Override
  public int size() {
    return twoword ? 2 : 1;
  }

  @Override
  public Expression clone() {
    return new ArrayIndexExpression(array.clone(), index.clone(), twoword);
  }
}
