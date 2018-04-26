package me.grax.jbytemod.analysis.decompiler.code.ast.expressions;

import me.grax.jbytemod.analysis.decompiler.ClassDefinition;
import me.grax.jbytemod.analysis.decompiler.code.ast.Expression;
import me.grax.jbytemod.utils.InstrUtils;
import me.grax.jbytemod.utils.TextUtils;

public class NewArrayExpression extends Expression {
  private Expression count;
  private ClassDefinition object;

  public NewArrayExpression(Expression count, ClassDefinition object) {
    super();
    this.count = count;
    this.object = object;
  }

  @Override
  public String toString() {
    return "<b>new</b> " + TextUtils.addTag(object.getName(), "font color=" + InstrUtils.secColor.getString()) + "[" + count + "]";
  }

  @Override
  public int size() {
    return 1;
  }

  @Override
  public Expression clone() {
    return new NewArrayExpression(count.clone(), object);
  }
}
