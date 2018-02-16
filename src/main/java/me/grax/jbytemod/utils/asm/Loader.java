package me.grax.jbytemod.utils.asm;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.objectweb.asm.tree.ClassNode;

import me.grax.jbytemod.JByteMod;
import me.lpk.util.ASMUtils;

public class Loader {
  public static ClassNode classToNode(String type) throws IOException {
    return ASMUtils.getNode(classToBytes(type));
  }
  
  public static byte[] classToBytes(String type) throws IOException {
    if (type == null) {
      return null;
    }
    InputStream is = ClassLoader.getSystemClassLoader().getResourceAsStream(type + ".class");
    if (is == null) {
      JByteMod.LOGGER.err(type + " not in classpath");
      return null;
    }
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    byte[] buffer = new byte[4096];
    int n;
    while ((n = is.read(buffer)) > 0) {
      baos.write(buffer, 0, n);
    }
    return baos.toByteArray();
  }
}
