package me.grax.decompiler.code.ast.expressions;

import me.grax.decompiler.code.ast.Expression;
import me.grax.decompiler.code.ast.VarType;
import me.grax.jbytemod.utils.InstrUtils;
import me.grax.jbytemod.utils.TextUtils;

public class NewPrimArrayExpression extends Expression {
  private Expression count;
  private VarType type;

  public NewPrimArrayExpression(Expression count, VarType type) {
    super();
    this.count = count;
    this.type = type;
  }

  @Override
  public String toString() {
    return "<b>new</b> " + TextUtils.addTag(type.getType(), "font color=" + InstrUtils.secColor.getString()) + "[" + count + "]";
  }

  @Override
  public int size() {
    return 1;
  }

  @Override
  public Expression clone() {
    return new NewPrimArrayExpression(count.clone(), type);
  }
}
