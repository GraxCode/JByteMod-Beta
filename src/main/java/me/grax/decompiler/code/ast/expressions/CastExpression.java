package me.grax.decompiler.code.ast.expressions;

import me.grax.decompiler.ClassDefinition;
import me.grax.decompiler.code.ast.Expression;
import me.grax.decompiler.code.ast.VarType;

public class CastExpression extends Expression {
  private ClassDefinition cast;
  private Expression object;
  private VarType typeCast;

  public CastExpression(ClassDefinition cast, Expression object) {
    super();
    this.cast = cast;
    this.object = object;
  }

  public CastExpression(VarType typeCast, Expression object) {
    super();
    this.typeCast = typeCast;
    this.object = object;
  }

  public ClassDefinition getCast() {
    return cast;
  }

  public void setCast(ClassDefinition cast) {
    this.cast = cast;
  }

  public Expression getObject() {
    return object;
  }

  public void setObject(Expression object) {
    this.object = object;
  }

  public VarType getTypeCast() {
    return typeCast;
  }

  public void setTypeCast(VarType typeCast) {
    this.typeCast = typeCast;
  }

  @Override
  public String toString() {
    if (typeCast != null) {
      return "((" + typeCast.getType() + ") " + object + ")";
    }
    return "((" + cast.getShortName() + ") " + object + ")";
  }

  @Override
  public int size() {
    if (typeCast != null) {
      return typeCast.size();
    }
    return 1;
  }

  @Override
  public Expression clone() {
    if (typeCast != null) {
      return new CastExpression(typeCast, object.clone());
    }
    return new CastExpression(new ClassDefinition(cast.getName()), object.clone());
  }
}
