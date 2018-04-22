package me.grax.decompiler.code.ast.expressions;

import me.grax.decompiler.code.ast.Comparison;
import me.grax.decompiler.code.ast.Expression;
import me.grax.jbytemod.utils.TextUtils;

public class ComparisonExpression extends Expression {

  private Expression left;
  private Comparison comparison;
  private Expression right;

  public ComparisonExpression(Expression left, Comparison comparison, Expression right) {
    super();
    this.left = left;
    this.comparison = comparison;
    this.right = right;
  }

  public Expression getLeft() {
    return left;
  }

  public void setLeft(Expression left) {
    this.left = left;
  }

  public Comparison getComparison() {
    return comparison;
  }

  public void setComparison(Comparison comparison) {
    this.comparison = comparison;
  }

  public Expression getRight() {
    return right;
  }

  public void setRight(Expression right) {
    this.right = right;
  }

  @Override
  public String toString() {
    return "<i><b>if</b> " + left.toString() + " " + TextUtils.escape(comparison.getSymbol()) + " " + right.toString() + "</i>";
  }

  @Override
  public int size() {
    return 1;
  }

  @Override
  public Expression clone() {
    return new ComparisonExpression(left.clone(), comparison, right.clone());
  }
}
