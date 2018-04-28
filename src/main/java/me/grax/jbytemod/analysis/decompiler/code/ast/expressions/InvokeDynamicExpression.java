package me.grax.jbytemod.analysis.decompiler.code.ast.expressions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.objectweb.asm.Handle;

import me.grax.jbytemod.analysis.decompiler.code.ast.Expression;
import me.grax.jbytemod.utils.InstrUtils;
import me.grax.jbytemod.utils.TextUtils;

public class InvokeDynamicExpression extends Expression {

  private String name;
  private String desc;
  private Object[] bsmArgs;
  private Handle bsm;
  private Handle methodHandle;
  private List<Expression> args;

  public InvokeDynamicExpression(String name, String desc, Object[] bsmArgs, Handle bsm) {
    this.methodHandle = getMethodHandle(bsmArgs);
    this.name = name;
    this.desc = desc;
    this.bsmArgs = bsmArgs;
    this.bsm = bsm;
  }

  private Handle getMethodHandle(Object[] bsmArgs) {
    for (Object o : bsmArgs) {
      if (o instanceof Handle) {
        return (Handle) o;
      }
    }
    return null;
  }

  public Handle getMethodHandle() {
    return methodHandle;
  }

  public List<Expression> getArgs() {
    return args;
  }

  public void setArgs(List<Expression> args) {
    this.args = args;
  }

  @Override
  public String toString() {
    if (methodHandle != null) {
      StringBuilder sb = new StringBuilder();
      sb.append(TextUtils.addTag(methodHandle.getOwner(), "font color=" + InstrUtils.secColor.getString()));
      sb.append("</font>.");
      sb.append(TextUtils.addTag(TextUtils.escape(methodHandle.getName()), "font color=#8855aa"));
      sb.append("</font>(");
      if (args != null) {
        ArrayList<String> argsString = new ArrayList<>();
        for (Expression ref : args) {
          argsString.add(ref.toString());
        }
        sb.append(String.join(", ", argsString));
      } else {
        sb.append(TextUtils.escape(methodHandle.getDesc()));
      }
      sb.append(")");
      return sb.toString();
    }
    return "<b>invokedynamic</b>(" + name + " " + desc + " " + Arrays.toString(bsmArgs) + " " + bsm.getOwner() + "." + bsm.getName() + bsm.getDesc()
        + ")";
  }

  @Override
  public int size() {
    return 1;
  }

  @Override
  public Expression clone() {
    return new InvokeDynamicExpression(name, desc, bsmArgs, bsm);
  }
}
