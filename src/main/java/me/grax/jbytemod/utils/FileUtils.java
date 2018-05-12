package me.grax.jbytemod.utils;

import java.io.File;

public class FileUtils {
  public static boolean exists(File f) {
    return f.exists() && !f.isDirectory();
  }

  public static boolean isType(File f, String... types) {
    for (String type : types) {
      if (f.getName().endsWith(type)) {
        return true;
      }
    }
    return false;
  }
}
