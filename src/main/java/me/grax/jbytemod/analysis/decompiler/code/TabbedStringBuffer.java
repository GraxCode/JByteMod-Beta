package me.grax.jbytemod.analysis.decompiler.code;

public class TabbedStringBuffer {
  public int tabs;
  private StringBuilder sb = new StringBuilder();

  public void append(String s) {
    sb.append(s);
  }

  public void indent() {
    for (int i = 0; i < tabs; i++) {
      sb.append("  ");
    }
  }
  @Override
  public String toString() {
    return sb.toString();
  }
}
