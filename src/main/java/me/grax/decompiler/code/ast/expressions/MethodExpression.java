package me.grax.decompiler.code.ast.expressions;

import java.util.ArrayList;
import java.util.List;

import me.grax.decompiler.ClassDefinition;
import me.grax.decompiler.code.ast.Expression;
import me.grax.decompiler.code.ast.VarType;
import me.grax.jbytemod.utils.InstrUtils;
import me.grax.jbytemod.utils.TextUtils;

public class MethodExpression extends Expression {

  private Expression invokeOn;
  private ClassDefinition staticOwner;

  private String methodName;
  private List<Expression> args;
  private VarType returnType;

  public MethodExpression(Expression invokeOn, String methodName, List<Expression> args, VarType returnType) {
    super();
    this.invokeOn = invokeOn;
    this.methodName = methodName;
    this.args = args;
    this.returnType = returnType;
  }

  public MethodExpression(ClassDefinition staticOwner, String methodName, List<Expression> args, VarType returnType) {
    super();
    this.staticOwner = staticOwner;
    this.methodName = methodName;
    this.args = args;
    this.returnType = returnType;
  }

  @Override
  public String toString() {
    boolean isStatic = isStatic();
    StringBuilder sb = new StringBuilder();
    if (isStatic) {
      sb.append(TextUtils.addTag(staticOwner.getName(), "font color=" + InstrUtils.secColor.getString()));
    } else {
      sb.append(invokeOn.toString());
    }
    sb.append("</font>.");
    sb.append(TextUtils.addTag(TextUtils.escape(methodName), "font color=#8855aa"));
    sb.append("</font>(");
    ArrayList<String> argsString = new ArrayList<>();
    for (Expression ref : args) {
      argsString.add(ref.toString());
    }
    sb.append(String.join(", ", argsString));
    sb.append(")");
    return sb.toString();
  }

  public boolean isStatic() {
    return invokeOn == null;
  }

  public Expression getInvokeOn() {
    return invokeOn;
  }

  public void setInvokeOn(Expression invokeOn) {
    this.invokeOn = invokeOn;
  }

  public ClassDefinition getStaticOwner() {
    return staticOwner;
  }

  public void setStaticOwner(ClassDefinition staticOwner) {
    this.staticOwner = staticOwner;
  }

  public String getMethodName() {
    return methodName;
  }

  public void setMethodName(String methodName) {
    this.methodName = methodName;
  }

  public List<Expression> getArgs() {
    return args;
  }

  public void setArgs(List<Expression> args) {
    this.args = args;
  }

  public VarType getReturnType() {
    return returnType;
  }

  public void setReturnType(VarType returnType) {
    this.returnType = returnType;
  }

  @Override
  public int size() {
    return returnType.size();
  }

  @Override
  public Expression clone() {
    ArrayList<Expression> clonedArgs = new ArrayList<>();
    for (Expression arg : args) {
      clonedArgs.add(arg);
    }
    if (invokeOn != null) {
      return new MethodExpression(invokeOn.clone(), methodName, clonedArgs, returnType);
    } else {
      return new MethodExpression(new ClassDefinition(staticOwner.getName()), methodName, clonedArgs, returnType);
    }
  }
}
