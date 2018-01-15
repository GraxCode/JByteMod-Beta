package me.grax.jbytemod.utils;

public class TextUtils {

  public static String toHtml(String str) {
    return "<html>" + toBlack(str);
  }

  public static String toBlack(String str) {
    return addTag(str, "font color=#000000");
  }

  public static String addTag(String str, String tag) {
    return "<" + tag + ">" + str + "</" + tag.split(" ")[0] + ">";
  }

  public static String toLight(String str) {
    return addTag(str, "font color=#999999");
  }

  public static String toBold(String str) {
    return addTag(str, "b");
  }

  public static String escape(String str) {
    return str.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
  }

  public static String toItalics(String str) {
    return addTag(str, "i");
  }
}
