package me.grax.decompiler.code.ast.expressions;

import me.grax.decompiler.code.ast.Expression;
import me.grax.decompiler.code.ast.Operation;
import me.grax.decompiler.code.ast.VarType;

public class SingleOpExpression extends OpExpression {

  public SingleOpExpression(Expression ref, Operation op, VarType returnType) {
    super(ref, null, op, returnType);
  }

  @Override
  public Expression getRight() {
    throw new IllegalArgumentException("called getRight on single exp reference");
  }

  @Override
  public String toString() {
    return op.getSymbol() + "(" + left.toString() + ")";
  }

  @Override
  public Expression clone() {
    return new SingleOpExpression(left.clone(), op, returnType);
  }
}
