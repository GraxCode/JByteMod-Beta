package me.grax.decompiler.struct;

import java.util.EmptyStackException;
import java.util.Stack;

import me.grax.decompiler.code.ast.Expression;
import me.grax.decompiler.code.ast.expressions.DebugStackExpression;
import me.grax.decompiler.struct.exception.StackException;

public class JVMStack {

  private Stack<Expression> list;

  public JVMStack() {
    this.list = new Stack<Expression>();

  }

  public void push(Expression o, boolean twoword) {
    if (twoword) {
      if (o.size() != 2) {
        throw new StackException("not a wide value: " + o.size());
      }
    } else {
      if (o.size() != 1) {
        throw new StackException("not a 1-size: " + o.size());
      }
    }
    push(o);
  }

  public void push(Expression o) {
    list.push(o);
  }

  public Expression pop() {
    Expression top = peek();
    if (top.size() != 1) {
      throw new StackException("Top is " + top.size() + "-word value, cannot pop");
    }
    return list.pop();
  }

  private int belowStackCount = 0;

  public Expression peek() {
    try {
      return list.peek();
    } catch (EmptyStackException e) {
      list.add(new DebugStackExpression(belowStackCount++, 1));
      return list.peek();
    }
  }

  public void push(Expression o, int i, boolean twoword) {
    if (twoword) {
      if (o.size() != 2) {
        throw new StackException("not a wide value: " + o.size());
      }
    } else {
      if (o.size() != 1) {
        throw new StackException("not a 1-size: " + o.size());
      }
    }
    push(o, i);
  }

  public void push(Expression o, int i) {
    list.add(list.size() - 1 - i, o);
  }

  public int size() {
    return list.size();
  }

  public Expression pop2() {
    Expression top = peek();
    int size = top.size();
    if (size == 2) {
      return list.pop();
    } else if (size == 1) {
      list.pop();
      if (peek().size() != 1) {
        throw new StackException("Second value is " + top.size() + "-word, cannot pop2");
      }
      return list.pop();
    }
    throw new StackException(String.valueOf(size));
  }

  public Expression peek2() {
    return list.elementAt(list.size() - 2);
  }

}
