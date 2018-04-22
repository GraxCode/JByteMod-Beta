package me.grax.decompiler.code.ast.expressions;

import me.grax.decompiler.code.ast.Expression;

public class TableSwitchExpression extends Expression {

  private Expression toSwitch;
  private int min;
  private int max;

  public TableSwitchExpression(Expression toSwitch, int min, int max) {
    this.toSwitch = toSwitch;
    this.min = min;
    this.max = max;
  }

  @Override
  public String toString() {
    return "<i><b>switch</b>(" + toSwitch + ") " + min + " - " + max + "</i>";
  }

  @Override
  public int size() {
    return 1;
  }

  @Override
  public Expression clone() {
    return new TableSwitchExpression(toSwitch.clone(), min, max);
  }
}
