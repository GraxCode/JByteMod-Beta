package me.grax.jbytemod.analysis.decompiler.code.ast.expressions;

import me.grax.jbytemod.analysis.decompiler.ClassDefinition;
import me.grax.jbytemod.analysis.decompiler.code.ast.Expression;
import me.grax.jbytemod.analysis.decompiler.code.ast.VarType;
import me.grax.jbytemod.utils.InstrUtils;
import me.grax.jbytemod.utils.TextUtils;

public class FieldExpression extends Expression {

  private Expression owner;
  private ClassDefinition staticOwner;

  private String fieldName;
  private VarType returnType;

  public FieldExpression(Expression owner, String fieldName, VarType returnType) {
    super();
    this.owner = owner;
    this.fieldName = fieldName;
    this.returnType = returnType;
  }

  public FieldExpression(ClassDefinition staticOwner, String fieldName, VarType returnType) {
    super();
    this.staticOwner = staticOwner;
    this.fieldName = fieldName;
    this.returnType = returnType;
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

  public String getFieldName() {
    return fieldName;
  }

  public void setFieldName(String fieldName) {
    this.fieldName = fieldName;
  }

  public VarType getReturnType() {
    return returnType;
  }

  public void setReturnType(VarType returnType) {
    this.returnType = returnType;
  }

  public boolean isStatic() {
    return owner == null;
  }

  @Override
  public String toString() {
    boolean isStatic = isStatic();
    StringBuilder sb = new StringBuilder();
    if (isStatic) {
      sb.append(TextUtils.addTag(staticOwner.getName(), "font color=" + InstrUtils.secColor.getString()));
    } else {
      sb.append(owner.toString());
    }
    sb.append(".");
    sb.append(TextUtils.addTag(fieldName, "font color=" + InstrUtils.primColor.getString()));
    return sb.toString();
  }

  @Override
  public int size() {
    return returnType.size();
  }

  @Override
  public Expression clone() {
    if (owner != null) {
      return new FieldExpression(owner.clone(), fieldName, returnType);
    } else {
      return new FieldExpression(new ClassDefinition(staticOwner.getName()), fieldName, returnType);
    }
  }
}
