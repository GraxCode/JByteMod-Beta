package me.grax.jbytemod.utils.task;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import com.sun.tools.attach.VirtualMachine;

import me.grax.jbytemod.JByteMod;
import me.grax.jbytemod.ui.PageEndPanel;
import me.grax.jbytemod.utils.attach.InjectUtils;

public class AttachTask extends SwingWorker<Void, Integer> {

  private VirtualMachine vm;
  private PageEndPanel jpb;

  public AttachTask(JByteMod jbm, VirtualMachine vm) {
    this.vm = vm;
    this.jpb = jbm.getPP();
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

  @Override
  protected Void doInBackground() throws Exception {
    publish(0);
    File temp = File.createTempFile("jvm", ".jar");

    File self = new File(JByteMod.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
    if (self.getAbsolutePath().endsWith(".jar")) {
      JOptionPane.showMessageDialog(null, "Injecting... this could take a while.");
      JarFile jbytemod = new JarFile(self);
      double size = countFiles(jbytemod);
      ZipOutputStream output = new ZipOutputStream(new FileOutputStream(temp));
      Enumeration<? extends ZipEntry> entries = jbytemod.entries();
      int i = 0;
      while (entries.hasMoreElements()) {
        publish((int) ((i / size) * 100d));
        ZipEntry e = entries.nextElement();
        if (!e.getName().equals("META-INF/MANIFEST.MF")) {
          output.putNextEntry(e);
          if (!e.isDirectory()) {
            InjectUtils.copy(jbytemod.getInputStream(e), output);
          }
          output.closeEntry();
        }
        i++;
      }
      ZipEntry e = new ZipEntry("META-INF/MANIFEST.MF");
      output.putNextEntry(e);
      output.write(("Manifest-Version: 1.0\nAgent-Class: " + JByteMod.class.getName()
          + "\nCan-Redefine-Classes: true\nCan-Retransform-Classes: true\nCan-Set-Native-Method-Prefix: false\n").getBytes());
      output.closeEntry();
      jbytemod.close();
      output.close();
      publish(100);
      JByteMod.instance.dispose();
      vm.loadAgent(temp.getAbsolutePath(), self.getParent());
      temp.deleteOnExit();
    } else {
      JOptionPane.showMessageDialog(null, "Couldn't find itself as jar!");
    }
    publish(100);
    return null;
  }

  @Override
  protected void process(List<Integer> chunks) {
    int i = chunks.get(chunks.size() - 1);
    jpb.setValue(i);
    super.process(chunks);
  }
}
