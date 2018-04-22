package me.grax.decompiler.code.ast;

public abstract class Expression {
  public abstract String toString();
  public abstract int size();
  public abstract Expression clone();
}
