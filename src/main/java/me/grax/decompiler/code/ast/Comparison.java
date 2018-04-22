package me.grax.decompiler.code.ast;

public enum Comparison {
  IS("=="), ISNOT("!="), LOWER("<"), LOWEREQUALS("<="), GREATER(">"), GREATEREQUALS(">=");

  private String symbol;

  private Comparison(String symbol) {
    this.symbol = symbol;
  }

  public String getSymbol() {
    return symbol;
  }

  public void setSymbol(String symbol) {
    this.symbol = symbol;
  }
}
