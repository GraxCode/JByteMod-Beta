package me.grax.jbytemod.analysis.decompiler.code.ast;

public enum VarType {
  INT("int"), FLOAT("float"), LONG("long"), DOUBLE("double"), OBJECT("Object"), VOID("void"), BOOLEAN("boolean"), CHAR("char"), BYTE("byte"), SHORT(
      "short");
  private String type;

  VarType(String type) {
    this.type = type;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public int size() {
    return (this == VarType.LONG || this == VarType.DOUBLE) ? 2 : 1;
  }

  public static VarType ofDesc(String desc) {
    if (desc.contains(")")) {
      desc = desc.substring(desc.lastIndexOf(')') + 1);
    }
    if (desc.endsWith(";") || desc.contains("["))
      return OBJECT;
    if (desc.endsWith("V"))
      return VOID;
    if (desc.endsWith("F"))
      return FLOAT;
    if (desc.endsWith("J"))
      return LONG;
    if (desc.endsWith("D"))
      return DOUBLE;
    if (desc.endsWith("Z"))
      return BOOLEAN;
    if (desc.endsWith("C"))
      return CHAR;
    if (desc.endsWith("B"))
      return BYTE;
    if (desc.endsWith("S"))
      return SHORT;
    return INT;
  }

}
