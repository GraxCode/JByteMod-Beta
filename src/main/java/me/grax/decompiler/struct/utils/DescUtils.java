package me.grax.decompiler.struct.utils;

import java.util.ArrayList;

public class DescUtils {
  public static ArrayList<Integer> getDescSizes(String desc) {
    ArrayList<Integer> descSizes = new ArrayList<>();
    int dims = 0;
    boolean inObject = false;
    for (char c : desc.toCharArray()) {
      if(inObject) {
        if(c == ';') {
          inObject = false;
          descSizes.add(1);
          dims = 0;
        }
        continue;
      }
      if(c == 'L') {
        inObject = true;
      } else if(c == '[') {
        dims++;
      } else {
        if(dims == 0 && (c == 'J' || c == 'D')) {
          descSizes.add(2);
        } else {
          descSizes.add(1);
        }
        dims = 0;
      }
    }
    return descSizes;
  }
  public static ArrayList<Integer> getInnerDescSizes(String desc) {
    if(desc.startsWith("()")) {
      return new ArrayList<>();
    }
    return getDescSizes(desc.substring(1, desc.lastIndexOf(')')));
  }
}
