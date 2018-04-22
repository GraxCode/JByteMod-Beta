package me.grax.decompiler.code.ast.expressions;

import me.grax.decompiler.ClassDefinition;
import me.grax.decompiler.code.ast.Comparison;
import me.grax.decompiler.code.ast.Expression;
import me.grax.jbytemod.utils.TextUtils;

public class InstanceofExpression extends Expression {

  private Expression object;
  private ClassDefinition classDef;

  public InstanceofExpression(Expression object, ClassDefinition classDef) {
    super();
    this.object = object;
    this.classDef = classDef;
  }

  @Override
  public String toString() {
    return object.toString() + " <b>instanceof</b> " + classDef.getName();
  }

  @Override
  public int size() {
    return 1;
  }

  @Override
  public Expression clone() {
    return new InstanceofExpression(object.clone(), classDef);
  }
}
