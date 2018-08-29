package me.lpk.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.IOUtils;
import org.objectweb.asm.tree.ClassNode;

public class JarUtils {
  /**
   * Creates a map of <String(Class name), ClassNode> for a given jar file
   * 
   * @param jarFile
   * @author Konloch (Bytecode Viewer)
   * @return
   * @throws IOException
   */
  public static Map<String, ClassNode> loadClasses(File jarFile) throws IOException {
    Map<String, ClassNode> classes = new HashMap<String, ClassNode>();
    JarFile jar = new JarFile(jarFile);
    Stream<JarEntry> str = jar.stream();
    // For some reason streaming = entries in messy jars
    // enumeration = no entries
    // Or if the jar is really big, enumeration = infinite hang
    // ...
    // Whatever. It works now!
    str.forEach(z -> readJar(jar, z, classes, null));
    jar.close();
    return classes;
  }

  public static Map<String, ClassNode> loadRT() throws IOException {
    Map<String, ClassNode> classes = new HashMap<String, ClassNode>();
    JarFile jar = new JarFile(getRT());
    Stream<JarEntry> str = jar.stream();
    // TODO: Make ignoring these packages optional
    str.forEach(z -> readJar(jar, z, classes, Arrays.asList("com/sun/", "com/oracle/", "jdk/", "sun/")));
    jar.close();
    return classes;
  }

  /**
   * This method is less fussy about the jar integrity.
   * 
   * @param jar
   * @param en
   * @param classes
   * @return
   */
  private static Map<String, ClassNode> readJar(JarFile jar, JarEntry en, Map<String, ClassNode> classes, List<String> ignored) {
    String name = en.getName();
    try (InputStream jis = jar.getInputStream(en)) {
      if (name.endsWith(".class")) {
        if (ignored != null) {
          for (String s : ignored) {
            if (name.startsWith(s)) {
              return classes;
            }
          }
        }
        byte[] bytes = IOUtils.toByteArray(jis);
        String cafebabe = String.format("%02X%02X%02X%02X", bytes[0], bytes[1], bytes[2], bytes[3]);
        if (cafebabe.toLowerCase().equals("cafebabe")) {
          try {
            final ClassNode cn = ASMUtils.getNode(bytes);
            if (cn != null && (cn.name.equals("java/lang/Object") ? true : cn.superName != null)) {
              classes.put(cn.name, cn);
            }
          } catch (Exception e) {
            e.printStackTrace();
          }
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return classes;
  }

  /**
   * Creates a map of <String(entry name), byte[]> for a given jar file
   * 
   * 
   * @param jarFile
   * @return
   * @throws IOException
   */
  public static Map<String, byte[]> loadNonClassEntries(File jarFile) throws IOException {
    Map<String, byte[]> entries = new HashMap<String, byte[]>();
    ZipInputStream jis = new ZipInputStream(new FileInputStream(jarFile));
    ZipEntry entry;
    while ((entry = jis.getNextEntry()) != null) {
      try {
        final String name = entry.getName();
        if (!name.endsWith(".class") && !entry.isDirectory()) {
          byte[] bytes = IOUtils.toByteArray(jis);
          entries.put(name, bytes);
        }
      } catch (Exception e) {
        e.printStackTrace();
      } finally {
        jis.closeEntry();
      }
    }
    jis.close();
    return entries;
  }

  /**
   * Saves a map of bytes to a jar file
   * 
   * @param outBytes
   * @param fileName
   */
  public static void saveAsJar(Map<String, byte[]> outBytes, String fileName) {
    try {
      JarOutputStream out = new JarOutputStream(new java.io.FileOutputStream(fileName));
      for (String entry : outBytes.keySet()) {
        out.putNextEntry(new ZipEntry(entry));
        if (!entry.endsWith("/"))
          out.write(outBytes.get(entry));
        out.closeEntry();
      }
      out.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @SuppressWarnings("resource")
  public static String getManifestMainClass(File jar) {
    try {
      return new JarFile(jar.getAbsolutePath()).getManifest().getMainAttributes().getValue("Main-class").replace(".", "/");
    } catch (Exception e) {
    }
    return null;
  }

  public static File getRT() {
    return new File(System.getProperty("java.home") + File.separator + "lib" + File.separator + "rt.jar");
  }
}
