package me.grax.jbytemod.analysis.decompiler.code.ast.expressions;

import me.grax.jbytemod.analysis.decompiler.ClassDefinition;
import me.grax.jbytemod.analysis.decompiler.code.ast.Expression;

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
