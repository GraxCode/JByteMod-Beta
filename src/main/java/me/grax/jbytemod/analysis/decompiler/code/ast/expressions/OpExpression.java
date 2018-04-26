package me.grax.jbytemod.analysis.decompiler.code.ast.expressions;

import me.grax.jbytemod.analysis.decompiler.code.ast.Expression;
import me.grax.jbytemod.analysis.decompiler.code.ast.Operation;
import me.grax.jbytemod.analysis.decompiler.code.ast.VarType;

public class OpExpression extends Expression {
  protected Expression left;
  private Expression right;
  protected Operation op;
  protected VarType returnType;

  public OpExpression(Expression left, Expression right, Operation op, VarType returnType) {
    super();
    this.left = left;
    this.right = right;
    this.op = op;
    this.returnType = returnType;
  }

  public Expression getLeft() {
    return left;
  }

  public void setLeft(Expression left) {
    this.left = left;
  }

  public Expression getRight() {
    return right;
  }

  public void setRight(Expression right) {
    this.right = right;
  }

  public Operation getOp() {
    return op;
  }

  public void setOp(Operation op) {
    this.op = op;
  }

  @Override
  public String toString() {
    return left.toString() + " " + op.getSymbol() + " " + right.toString();
  }

  @Override
  public int size() {
    return returnType.size(); //right side defines the size
  }

  @Override
  public Expression clone() {
    return new OpExpression(left.clone(), right.clone(), op, returnType);
  }
}
