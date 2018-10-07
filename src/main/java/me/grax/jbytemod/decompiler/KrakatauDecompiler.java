package me.grax.jbytemod.decompiler;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.benf.cfr.reader.bytecode.analysis.parse.utils.Pair;
import org.objectweb.asm.tree.MethodNode;

import me.grax.jbytemod.JByteMod;
import me.grax.jbytemod.ui.DecompilerPanel;
import me.lpk.util.JarUtils;

public class KrakatauDecompiler extends Decompiler {

  private static File tempDir = new File(System.getProperty("java.io.tmpdir"));
  private static File krakatauDir = new File(tempDir, "krakatau");
  private static File decompile;

  public KrakatauDecompiler(JByteMod jbm, DecompilerPanel dp) {
    super(jbm, dp);
  }

  public String decompile(byte[] b, MethodNode mn) {
    try {
      File tempJar = createTempJar(b);
      File outputZip = new File(tempDir, b.hashCode() + ".zip");
      if (decompile == null) {
        decompile = makeTemp();
        JByteMod.LOGGER.log("Successfully created Krakatau temp folder");
      }
      File filePath = jbm.getFilePath();
      String command = getPythonPath() + " " + escape(decompile.getAbsolutePath()) + " -nauto -path " + escape(JarUtils.getRT().getAbsolutePath())
          + ";" + escape(tempJar.getAbsolutePath()) + (filePath != null ? (";" + escape(filePath.getAbsolutePath())) : "") + " -out "
          + escape(outputZip.getAbsolutePath()) + " -skip " + escape(tempJar.getAbsolutePath());
      Process p = Runtime.getRuntime().exec(command);
      BufferedReader error = new BufferedReader(new InputStreamReader(p.getErrorStream()));
      BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));

      String s = null;
      while ((s = input.readLine()) != null) {
        JByteMod.LOGGER.log(s);
      }
      while ((s = error.readLine()) != null) {
        JByteMod.LOGGER.err(s);
      }
      p.waitFor();
      JByteMod.LOGGER.log("Finished Krakatau emulation");
      ZipInputStream zis = new ZipInputStream(new FileInputStream(outputZip));
      zis.getNextEntry();
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      byte[] buffer = new byte[1024];
      int len;
      while ((len = zis.read(buffer)) > 0) {
        out.write(buffer, 0, len);
      }
      zis.close();
      tempJar.delete();
      outputZip.delete();
      return new String(out.toByteArray(), "UTF-8");
    } catch (Exception e) {
      e.printStackTrace();
      StringWriter sw = new StringWriter();
      PrintWriter pw = new PrintWriter(sw);
      e.printStackTrace(pw);
      return sw.toString() + "\n\n" + JByteMod.res.getResource("set_py_path");
    }
  }

  private String getPythonPath() {
    String pp = JByteMod.ops.get("python_path").getString();
    if (!pp.isEmpty()) {
      try {
        File path = new File(pp);
        if (!path.exists()) {
          JByteMod.LOGGER.err("Python executable does not exist");
        } else {
          return "\"" + path.getAbsolutePath() + "\"";
        }
      } catch (Exception e) {
        JByteMod.LOGGER.err("Invalid python path (" + e.toString() + ")");
      }
    }
    return "py";
  }

  private String escape(String absolutePath) {
    return "\"" + absolutePath.replace('\\', '/') + "\"";
  }

  private File createTempJar(byte[] b) {
    File temp = new File(tempDir, b.hashCode() + ".jar");
    JarUtils.saveAsJar(Collections.singletonMap(cn.name + ".class", b), temp.getAbsolutePath());
    return temp;
  }

  protected Pair<byte[], String> getSystemClass(String name, String path) throws IOException {
    InputStream is = ClassLoader.getSystemClassLoader().getResourceAsStream(path);
    if (is != null) {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      byte[] buffer = new byte[4096];
      int n;
      while ((n = is.read(buffer)) > 0) {
        baos.write(buffer, 0, n);
      }
      return Pair.make(baos.toByteArray(), name);
    }
    return null;
  }

  public static File makeTemp() throws IOException {
    byte[] buffer = new byte[1024];
    ZipInputStream zis = new ZipInputStream(KrakatauDecompiler.class.getResourceAsStream("/resources/krakatau.zip"));
    ZipEntry zipEntry = zis.getNextEntry();
    if (!krakatauDir.exists()) {
      krakatauDir.mkdirs();
    }
    while (zipEntry != null) {
      String fileName = zipEntry.getName();
      File newFile = new File(krakatauDir, fileName);
      if (zipEntry.isDirectory()) {
        newFile.mkdirs();
      } else {
        FileOutputStream fos = new FileOutputStream(newFile);
        int len;
        while ((len = zis.read(buffer)) > 0) {
          fos.write(buffer, 0, len);
        }
        fos.close();
      }
      zipEntry = zis.getNextEntry();
    }
    zis.closeEntry();
    zis.close();
    return new File(krakatauDir, "decompile.py");
  }
}
