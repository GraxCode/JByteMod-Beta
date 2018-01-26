package me.grax.jbytemod.analysis.obfuscation.enums;

public enum MethodObfType {
  NONE("None"), TCBO("Unneccesary TCBs"), POP2(
      "POP2 Exploit")/* , LOCAL_VAR("Local Var Obf") */, STRING("String Obfuscation"), INVOKEDYNAMIC("Invokedynamic");

  private final String type;

  private MethodObfType(String type) {
    this.type = type;
  }

  public String getType() {
    return type;
  }

}