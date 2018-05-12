package me.grax.jbytemod.analysis.decompiler;

public class ClassDefinition {
  private String name;

  public ClassDefinition(String name) {
    super();
    if (name.startsWith("L") && name.endsWith(";")) {
      this.name = name.substring(1, name.length() - 1);
    } else {
      this.name = name;
    }
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getShortName() {
    if (!name.contains("/")) {
      return name;
    }
    return name.substring(name.lastIndexOf("/") + 1);
  }

  public String getSourceName() {
    return name.replace('/', '.');
  }
}
