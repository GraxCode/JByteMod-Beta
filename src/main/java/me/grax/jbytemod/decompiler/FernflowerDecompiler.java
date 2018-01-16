package me.grax.jbytemod.decompiler;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.jar.Manifest;

import org.jetbrains.java.decompiler.main.Fernflower;
import org.jetbrains.java.decompiler.main.decompiler.PrintStreamLogger;
import org.jetbrains.java.decompiler.main.extern.IBytecodeProvider;
import org.jetbrains.java.decompiler.main.extern.IResultSaver;
import org.jetbrains.java.decompiler.struct.ContextUnit;
import org.jetbrains.java.decompiler.struct.StructClass;
import org.jetbrains.java.decompiler.struct.StructContext;
import org.jetbrains.java.decompiler.struct.lazy.LazyLoader;

import me.grax.jbytemod.JByteMod;
import me.grax.jbytemod.ui.DecompilerPanel;

public class FernflowerDecompiler extends Decompiler implements IBytecodeProvider, IResultSaver {

  private byte[] bytes;
  private String returned;

  public FernflowerDecompiler(JByteMod jbm, DecompilerPanel dp) {
    super(jbm, dp);
  }

  public String decompile(byte[] b) {
    try {
      this.bytes = b;
      HashMap<String, Object> map = new HashMap<>();
      map.put("asc", "1"); //encode non-ASCII characters in string and character literals as Unicode escapes
      Fernflower f = new Fernflower(this, this, new HashMap<>(), new PrintStreamLogger(System.out));
      StructContext sc = f.getStructContext();
      StructClass cl = new StructClass(b, true, sc.getLoader());
      sc.getClasses().put(cn.name, cl);
      //instead of loading a file use custom bridge, created a few getters
      String fakePath = new File("none.class").getAbsolutePath();
      ContextUnit unit = new ContextUnit(ContextUnit.TYPE_FOLDER, null, fakePath, true, sc.getSaver(), sc.getDecompiledData());
      sc.getUnits().put(fakePath, unit);
      unit.addClass(cl, "none.class");
      sc.getLoader().addClassLink(cn.name, new LazyLoader.Link(LazyLoader.Link.CLASS, fakePath, null));

      f.decompileContext();
      return returned;
    } catch (Exception e) {
      e.printStackTrace();
      StringWriter sw = new StringWriter();
      PrintWriter pw = new PrintWriter(sw);
      e.printStackTrace(pw);
      return sw.toString();
    }
  }

  @Override
  public byte[] getBytecode(String externalPath, String internalPath) throws IOException {
    return bytes;
  }

  //we can ignore most of those methods because we do not want to save the output as a file
  @Override
  public void saveFolder(String path) {
  }

  @Override
  public void copyFile(String source, String path, String entryName) {
  }

  @Override
  public void saveClassFile(String path, String qualifiedName, String entryName, String content, int[] mapping) {
    this.returned = content;
  }

  @Override
  public void createArchive(String path, String archiveName, Manifest manifest) {
  }

  @Override
  public void saveDirEntry(String path, String archiveName, String entryName) {
  }

  @Override
  public void copyEntry(String source, String path, String archiveName, String entry) {
  }

  @Override
  public void saveClassEntry(String path, String archiveName, String qualifiedName, String entryName, String content) {
  }

  @Override
  public void closeArchive(String path, String archiveName) {
  }
}
