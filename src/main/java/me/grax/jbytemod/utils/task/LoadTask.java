package me.grax.jbytemod.utils.task;

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

import me.grax.jbytemod.JByteMod;
import me.grax.jbytemod.JarArchive;
import me.grax.jbytemod.ui.PageEndPanel;
import me.grax.jbytemod.utils.ErrorDisplay;
import me.lpk.util.ASMUtils;

public class LoadTask extends SwingWorker<Void, Integer> {

  private JarFile input;
  private PageEndPanel jpb;
  private JByteMod jbm;
  private int jarSize; //including directories
  private int loaded;
  private JarArchive ja;
  private long maxMem;
  private boolean memoryWarning;

  public LoadTask(JByteMod jbm, File input, JarArchive ja) {
    try {
      this.jarSize = countFiles(this.input = new JarFile(input, false));
      JByteMod.LOGGER.log(jarSize + " files to load!");
      this.jbm = jbm;
      this.jpb = jbm.getPP();
      this.ja = ja;
      this.maxMem = Runtime.getRuntime().maxMemory();
      this.memoryWarning = JByteMod.ops.get("memory_warning").getBoolean();
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
    long mem = Runtime.getRuntime().totalMemory();
    if (mem / (double) maxMem > 0.75) {
      JByteMod.LOGGER.warn("Memory usage is high: " + Math.round((mem / (double) maxMem * 100d)) + "%");
    }
    System.gc();
    Map<String, ClassNode> classes = new HashMap<String, ClassNode>();
    Map<String, byte[]> otherFiles = new HashMap<String, byte[]>();

    Stream<JarEntry> str = jar.stream();
    str.forEach(z -> readJar(jar, z, classes, otherFiles));
    jar.close();
    ja.setClasses(classes);
    ja.setOutput(otherFiles);
    return;
  }

  private void readJar(JarFile jar, JarEntry en, Map<String, ClassNode> classes, Map<String, byte[]> otherFiles) {
    long ms = System.currentTimeMillis();
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
      if (memoryWarning) {
        long timeDif = System.currentTimeMillis() - ms;
        if (timeDif > 200 && Runtime.getRuntime().totalMemory() / (double) maxMem > 0.95) { //if loading class takes more than 200ms and memory is full stop
          JByteMod.LOGGER.logNotification(JByteMod.res.getResource("memory_full"));
          publish(100);
          this.cancel(true);
          return;
        }
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
