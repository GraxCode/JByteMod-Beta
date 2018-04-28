package me.grax.jbytemod.analysis.decompiler.code.ast.expressions;

import me.grax.jbytemod.analysis.decompiler.code.ast.Expression;

public class LookupSwitchExpression extends Expression {

  private Expression toSwitch;
  private int size;

  public LookupSwitchExpression(Expression toSwitch, int size) {
    this.toSwitch = toSwitch;
    this.size = size;
  }

  @Override
  public String toString() {
    return "<i><b>switch</b>(" + toSwitch + ") " + size + " cases</i>";
  }

  @Override
  public int size() {
    return 1;
  }

  @Override
  public Expression clone() {
    return new LookupSwitchExpression(toSwitch.clone(), size);
  }
}
