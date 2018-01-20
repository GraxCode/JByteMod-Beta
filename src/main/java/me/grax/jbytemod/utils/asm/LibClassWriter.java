package me.grax.jbytemod.utils.asm;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

import me.lpk.util.ASMUtils;
import me.lpk.util.AccessHelper;

public class LibClassWriter extends ClassWriter {

  private Map<String, ClassNode> classes;
  private ParentUtils util;

  public LibClassWriter(int flags, Map<String, ClassNode> classes, Map<String, ClassNode> libraries) {
    super(flags);
    this.classes = new HashMap<>(classes);
    this.util = new ParentUtils(classes);
    if(libraries != null) {
      classes.putAll(libraries);
    }
  }

  
  @Override
  protected String getCommonSuperClass(String type1, String type2) {
    System.out.println("getcommonsuperclass");
    ClassNode mc1 = get(type1);
    ClassNode mc2 = get(type2);
    System.out.println(mc1.name + " " + mc2.name);
    if (mc1 == null || mc2 == null) {
      System.err.println((mc1 == null ? (mc2 == null ? (type1 + " and " + type2) : type1) : type2) + " not found. Check your classpath!");
      try {
        return super.getCommonSuperClass(type1, type2);
      } catch (Exception e) {
      }
      return "java/lang/Object";
    }
    System.out.println("Finding common parent of " + mc1.name + " " + mc2.name);
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

  private ClassNode toRTNode(String type) throws IOException {
    if (type == null) {
      return null;
    }
    InputStream is = ClassLoader.getSystemClassLoader().getResourceAsStream(type + ".class");
    if (is == null) {
      return null;
    }
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    byte[] buffer = new byte[4096];
    int n;
    while ((n = is.read(buffer)) > 0) {
      baos.write(buffer, 0, n);
    }
    return ASMUtils.getNode(baos.toByteArray());
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
      return get("java/lang/Object");
    } else {
      do {
        mc1 = get(mc1.superName);
      } while (mc1.superName != null && !util.isAssignableFrom(mc1, mc2));
      return mc1;
    }
  }

  private ClassNode get(String name) {
    if (classes.containsKey(name)) {
      return classes.get(name);
    }
    try {
      //load from rt
      ClassNode cn = toRTNode(name);
      if (cn != null) {
        classes.put(name, cn);
        return cn;
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    throw new RuntimeException(name + " not found in your classpath");
  }
}
