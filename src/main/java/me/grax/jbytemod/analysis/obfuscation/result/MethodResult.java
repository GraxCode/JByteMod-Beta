package me.grax.jbytemod.analysis.obfuscation.result;

import java.util.ArrayList;

import me.grax.jbytemod.analysis.obfuscation.enums.MethodObfType;

public class MethodResult {
  public ArrayList<MethodObfType> mobf;

  public MethodResult(ArrayList<MethodObfType> mobf) {
    super();
    this.mobf = mobf;
  }

}
