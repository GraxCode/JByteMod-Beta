package me.grax.jbytemod.analysis.decompiler.code.ast.expressions;

import me.grax.jbytemod.analysis.decompiler.code.ast.Expression;
import me.grax.jbytemod.analysis.decompiler.code.ast.VarType;
import me.grax.jbytemod.utils.TextUtils;

public class DebugStackExpression extends Expression {

  private int var;
  private int size;
  private VarType type;
  private String prefix;

  public DebugStackExpression(int var, int size, VarType type) {
    this(var, size, type, "stack");
  }

  public DebugStackExpression(int var, int size, VarType type, String prefix) {
    this.var = var;
    this.size = size;
    this.type = type;
    this.prefix = prefix;
  }

  @Override
  public String toString() {
    return TextUtils.addTag("<i>" + prefix + var + "</i>", "font color=#909011");
  }

  @Override
  public int size() {
    return size;
  }

  @Override
  public Expression clone() {
    return new DebugStackExpression(var, size, type, prefix);
  }

  public int getVar() {
    return var;
  }

  public void setVar(int var) {
    this.var = var;
  }

  public int getSize() {
    return size;
  }

  public void setSize(int size) {
    this.size = size;
  }

  public VarType getType() {
    return type;
  }

  public void setType(VarType type) {
    this.type = type;
  }

  public String getPrefix() {
    return prefix;
  }

  public void setPrefix(String prefix) {
    this.prefix = prefix;
  }

}
