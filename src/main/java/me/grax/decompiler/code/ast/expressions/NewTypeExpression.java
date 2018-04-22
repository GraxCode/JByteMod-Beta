package me.grax.decompiler.code.ast.expressions;

import me.grax.decompiler.ClassDefinition;
import me.grax.decompiler.code.ast.Expression;
import me.grax.jbytemod.utils.InstrUtils;
import me.grax.jbytemod.utils.TextUtils;

public class NewTypeExpression extends Expression {
  private ClassDefinition object;
  private Expression init;

  public NewTypeExpression(ClassDefinition object) {
    super();
    this.object = object;
  }

  @Override
  public String toString() {
    if (init != null) {
      return "<b>new</b>" + TextUtils.addTag(object.getName(), "font color=" + InstrUtils.secColor.getString()) + init;
    }
    return "<b>new</b> " + TextUtils.addTag(object.getName(), "font color=" + InstrUtils.secColor.getString());
  }

  @Override
  public int size() {
    return 1;
  }

  public Expression getInit() {
    return init;
  }

  public void setInit(Expression init) {
    this.init = init;
  }

  @Override
  public Expression clone() {
    NewTypeExpression nte = new NewTypeExpression(object);
    if (init != null) {
      nte.setInit(init.clone());
    }
    return nte;
  }
}
