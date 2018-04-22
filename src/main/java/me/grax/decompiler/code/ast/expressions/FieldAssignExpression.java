package me.grax.decompiler.code.ast.expressions;

import me.grax.decompiler.ClassDefinition;
import me.grax.decompiler.code.ast.Expression;
import me.grax.decompiler.code.ast.VarType;
import me.grax.jbytemod.utils.InstrUtils;
import me.grax.jbytemod.utils.TextUtils;

public class FieldAssignExpression extends Expression {
  private Expression owner;
  private ClassDefinition staticOwner;

  private String name;
  private VarType type;
  private Expression value;

  public FieldAssignExpression(Expression owner, String name, VarType type, Expression value) {
    super();
    this.owner = owner;
    this.name = name;
    this.type = type;
    this.value = value;
  }

  public FieldAssignExpression(ClassDefinition staticOwner, String name, VarType type, Expression value) {
    super();
    this.staticOwner = staticOwner;
    this.name = name;
    this.type = type;
    this.value = value;
  }

  public boolean isStatic() {
    return owner == null;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    if (owner == null) {
      sb.append(TextUtils.addTag(staticOwner.getName(), "font color=" + InstrUtils.secColor.getString()));
    } else {
      sb.append(TextUtils.addTag(owner.toString(), "font color=" + InstrUtils.secColor.getString()));
    }
    sb.append(".");
    sb.append(TextUtils.addTag(name, "font color=" + InstrUtils.primColor.getString()));
    sb.append(" = ");
    sb.append(value.toString());
    return sb.toString();
  }

  public Expression getOwner() {
    return owner;
  }

  public void setOwner(Expression owner) {
    this.owner = owner;
  }

  public ClassDefinition getStaticOwner() {
    return staticOwner;
  }

  public void setStaticOwner(ClassDefinition staticOwner) {
    this.staticOwner = staticOwner;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public VarType getType() {
    return type;
  }

  public void setType(VarType type) {
    this.type = type;
  }

  public Expression getValue() {
    return value;
  }

  public void setValue(Expression value) {
    this.value = value;
  }

  @Override
  public int size() {
    return 0;
  }

  @Override
  public Expression clone() {
    if (owner != null) {
      return new FieldAssignExpression(owner.clone(), name, type, value.clone());
    } else {
      return new FieldAssignExpression(new ClassDefinition(staticOwner.getName()), name, type, value.clone());
    }
  }
}
