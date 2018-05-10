package me.grax.jbytemod.utils.attach;

import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.objectweb.asm.tree.ClassNode;

import me.grax.jbytemod.JByteMod;
import me.grax.jbytemod.JarArchive;
import me.grax.jbytemod.utils.asm.Loader;
import me.lpk.util.ASMUtils;
import me.lpk.util.JarUtils;

public class RuntimeJarArchive extends JarArchive {

  private Instrumentation ins;
  private ArrayList<String> systemClasses;

  public RuntimeJarArchive(Instrumentation ins) {
    super(new HashMap<>(), new HashMap<>());
    this.ins = ins;
    systemClasses = new ArrayList<String>();
    try {
      loadNames(JByteMod.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
      loadNames(JarUtils.getRT().getAbsolutePath());
      JByteMod.LOGGER.log("Successfully loaded system class names");
    } catch (Exception e1) {
      e1.printStackTrace();
    }
  }

  private void loadNames(String path) throws IOException {
    JarFile self = new JarFile(path);
    Enumeration<JarEntry> e = self.entries();
    while (e.hasMoreElements()) {
      JarEntry entry = (JarEntry) e.nextElement();
      String name = entry.getName();
      if (name.endsWith(".class")) {
        systemClasses.add(entry.getName().substring(0, name.length() - 6));
      }
    }
    self.close();
  }

  @Override
  public Map<String, ClassNode> getClasses() {
    for (Class<?> c : ins.getAllLoadedClasses()) {
      String name = c.getName().replace('.', '/');
      if (!isRT(name) && !classes.containsKey(name)) {
        if (name.contains("$$") || systemClasses.contains(name) || name.contains("[") || !ins.isModifiableClass(c)) {
          continue;
        }
        try {
          ClassNode cn = Loader.classToNode(name);
          if (cn != null) {
            classes.put(name, cn);
            output.put(name, ASMUtils.getNodeBytes0(cn));
          }
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
    return classes;
  }

  private boolean isRT(String name) {
    return name.startsWith("java/") || name.startsWith("sun/") || name.startsWith("com/sun") || name.startsWith("jdk");
  }
}
