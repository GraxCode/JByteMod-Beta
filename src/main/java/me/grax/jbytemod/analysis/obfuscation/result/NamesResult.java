package me.grax.jbytemod.analysis.obfuscation.result;

import java.util.ArrayList;

import me.grax.jbytemod.analysis.obfuscation.enums.NameObfType;

public class NamesResult {
  public ArrayList<NameObfType> cnames;
  public ArrayList<NameObfType> mnames;
  public ArrayList<NameObfType> fnames;

  public NamesResult(ArrayList<NameObfType> cnames, ArrayList<NameObfType> mnames, ArrayList<NameObfType> fnames) {
    super();
    this.cnames = cnames;
    this.mnames = mnames;
    this.fnames = fnames;
  }

}
