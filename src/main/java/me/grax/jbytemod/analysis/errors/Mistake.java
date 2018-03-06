package me.grax.jbytemod.analysis.errors;

public abstract class Mistake {
  private String desc;

  public Mistake(String desc) {
    this.desc = desc;
  }

  public String getDesc() {
    return desc;
  }

  public void setDesc(String desc) {
    this.desc = desc;
  }
  
}
