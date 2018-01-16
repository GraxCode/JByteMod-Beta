package me.grax.jbytemod.utils.asm;

import java.util.Map;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

import me.lpk.util.AccessHelper;

public class LibClassWriter extends ClassWriter {

  private Map<String, ClassNode> classes;
  private ParentUtils util;

  public LibClassWriter(int flags, Map<String, ClassNode> classes) {
    super(flags);
    this.classes = classes;
    this.util = new ParentUtils(classes);
  }

  @Override
  protected String getCommonSuperClass(String type1, String type2) {
    ClassNode mc1 = classes.get(type1);
    ClassNode mc2 = classes.get(type2);
    if (mc1 == null || mc2 == null) {
      System.err.println((mc1 == null ? (mc2 == null ? (type1 + " and " + type2) : type1) : type2) + " not found. Check your classpath!");
      try {
        return super.getCommonSuperClass(type1, type2);
      } catch (Exception e) {
      }
      return "java/lang/Object";
    }
    ClassNode common = findCommonParent(mc1, mc2);
    if (common == null) {
      try {
        System.err.println("Couldn't get common superclass of the classes " + type1 + " " + type2 + "");
        return super.getCommonSuperClass(type1, type2);
      } catch (Exception e) {
      }
      return "java/lang/Object";
    }
    return common.name;
  }

  public ClassNode findCommonParent(ClassNode mc1, ClassNode mc2) {
    //are they the same?
    if (mc1.name.equals(mc2.name)) {
      return mc1;
    }
    if (util.isAssignableFrom(mc1, mc2)) {
      return mc1;
    }
    if (util.isAssignableFrom(mc2, mc1)) {
      return mc2;
    }
    if (AccessHelper.isInterface(mc1.access) || AccessHelper.isInterface(mc2.access)) {
      return classes.get("java/lang/Object");
    } else {
      do {
        mc1 = classes.get(mc1.superName);
      } while (!util.isAssignableFrom(mc1, mc2));
      return mc1;
    }
  }
}
