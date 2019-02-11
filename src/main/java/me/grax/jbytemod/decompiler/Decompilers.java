package me.grax.jbytemod.decompiler;

public enum Decompilers {
  PROCYON("Procyon", "0.5.32"), FERNFLOWER("Fernflower", ""), CFR("CFR", "1.39"), KRAKATAU("Krakatau", "502");
  private String version;
  private String name;

  Decompilers(String name, String version) {
    this.name = name;
    this.version = version;
  }

  public String getVersion() {
    return version;
  }

  public String getName() {
    return name;
  }

  @Override
  public String toString() {
    return name + " " + version;
  }
}
