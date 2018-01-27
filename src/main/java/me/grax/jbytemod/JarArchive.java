package me.grax.jbytemod;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;

import javax.swing.SwingWorker;

import org.apache.commons.io.IOUtils;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import me.grax.jbytemod.ui.PageEndPanel;
import me.grax.jbytemod.utils.ErrorDisplay;
import me.lpk.util.ASMUtils;

public class JarArchive {
  private Map<String, ClassNode> classes;
  private Map<String, byte[]> output;
  private boolean singleEntry;

  public JarArchive(Map<String, ClassNode> classes, Map<String, byte[]> output) {
    super();
    this.classes = classes;
    this.output = output;
  }

  public JarArchive(ClassNode cn) {
    super();
    this.classes = new HashMap<>();
    this.singleEntry = true;
    classes.put(cn.name, cn);
  }

  public JarArchive(JByteMod jbm, File input) {
    try {
      new TaskLoadJarFile(jbm, input).execute();
    } catch (Throwable t) {
      new ErrorDisplay(t);
    }
  }

  public boolean isSingleEntry() {
    return singleEntry;
  }

  public Map<String, ClassNode> getClasses() {
    return classes;
  }

  public Map<String, byte[]> getOutput() {
    return output;
  }

  class TaskLoadJarFile extends SwingWorker<Void, Integer> {

    private JarFile input;
    private PageEndPanel jpb;
    private JByteMod jbm;
    private int jarSize; //including directories
    private int loaded;

    public TaskLoadJarFile(JByteMod jbm, File input) {
      try {
        this.jarSize = countFiles(this.input = new JarFile(input));
        JByteMod.LOGGER.log(jarSize + " files to load!");
        this.jbm = jbm;
        this.jpb = jbm.getPP();
      } catch (IOException e) {
        new ErrorDisplay(e);
      }
    }

    @Override
    protected Void doInBackground() throws Exception {
      publish(0);
      this.loadFiles(input);
      publish(100);
      return null;
    }

    public int countFiles(final JarFile zipFile) {
      final Enumeration<? extends JarEntry> entries = zipFile.entries();
      int c = 0;
      while (entries.hasMoreElements()) {
        entries.nextElement();
        ++c;
      }
      return c;
    }

    /**
     * loads both classes and other files at the same time
     */
    public void loadFiles(JarFile jar) throws IOException {
      Map<String, ClassNode> classes = new HashMap<String, ClassNode>();
      Map<String, byte[]> otherFiles = new HashMap<String, byte[]>();

      Stream<JarEntry> str = jar.stream();
      str.forEach(z -> readJar(jar, z, classes, otherFiles));
      jar.close();
      JarArchive.this.classes = classes;
      JarArchive.this.output = otherFiles;
      return;
    }

    private void readJar(JarFile jar, JarEntry en, Map<String, ClassNode> classes, Map<String, byte[]> otherFiles) {
      publish((int) (((float) loaded++ / (float) jarSize) * 100f));
      String name = en.getName();
      try (InputStream jis = jar.getInputStream(en)) {
        if (name.endsWith(".class")) {
          byte[] bytes = IOUtils.toByteArray(jis);
          String cafebabe = String.format("%02X%02X%02X%02X", bytes[0], bytes[1], bytes[2], bytes[3]);
          if (cafebabe.toLowerCase().equals("cafebabe")) {
            try {
              final ClassNode cn = ASMUtils.getNode(bytes);
              if (cn != null) { // && (cn.name.equals("java/lang/Object") ? true : cn.superName != null)
                for (MethodNode mn : cn.methods) {
                  mn.owner = cn.name;
                }
                classes.put(cn.name, cn);
              }
            } catch (Exception e) {
              e.printStackTrace();
              JByteMod.LOGGER.err("Failed loading class file " + name);
            }
          }
        } else if (!en.isDirectory()) {
          byte[] bytes = IOUtils.toByteArray(jis);
          otherFiles.put(name, bytes);
        }
      } catch (Exception e) {
        e.printStackTrace();
        JByteMod.LOGGER.err("Failed loading file");
      }
      return;
    }

    @Override
    protected void process(List<Integer> chunks) {
      int i = chunks.get(chunks.size() - 1);
      jpb.setValue(i);
      super.process(chunks);
    }

    @Override
    protected void done() {
      JByteMod.LOGGER.log("Successfully loaded file!");
      jbm.refreshTree();
    }
  }
}
