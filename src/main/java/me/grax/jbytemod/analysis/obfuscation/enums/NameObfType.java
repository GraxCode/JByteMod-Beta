package me.grax.jbytemod.analysis.obfuscation.enums;

public enum NameObfType {
  NONE("None"), LONG_LETTERS("Long Letters"), SHORT_LETTERS("Short Letters"), HIGH_CHAR("High UTF8 Values"), JAVA_KEYWORD(
      "Java Keywords"), INVALID_WINDIR("Invalid Dir Names");

  private final String type;

  private NameObfType(String type) {
    this.type = type;
  }

  public String getType() {
    return type;
  }

}