package me.grax.jbytemod.plugin;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import me.grax.jbytemod.JByteMod;

public class PluginManager {

  private File pluginFolder = new File("plugins");

  private JByteMod jbm;

  private final ArrayList<Plugin> plugins = new ArrayList<>();

  public PluginManager(JByteMod jbm) {
    this.jbm = jbm;
    if (pluginFolder.exists() && pluginFolder.isDirectory()) {
      loadPlugins();
    } else {
      System.err.println("No plugin folder found!");
    }
  }

  @SuppressWarnings("deprecation")
  private void loadPlugins() {
//    ClassLoader parent = ClassLoader.getSystemClassLoader();
    for (File f : pluginFolder.listFiles()) {
      if (f.getName().endsWith(".jar")) {
        try {
          ZipFile zip = new ZipFile(f);
          Enumeration<? extends ZipEntry> entries = zip.entries();
//          ClassLoader loader = URLClassLoader.newInstance(new URL[] { f.toURI().toURL() }, parent);
          addURL(f.toURL());
          while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            String name = entry.getName();
            if (name.endsWith(".class")) {
              try {
                Class<?> loaded = Class.forName(name.replace('/', '.').substring(0, name.length() - 6), true, ClassLoader.getSystemClassLoader());
                if (Plugin.class.isAssignableFrom(loaded)) {
                  System.out.println("loaded " + loaded.getSuperclass().getName());
                  Plugin p = (Plugin) loaded.newInstance();
                  p.init();
                  this.plugins.add(p);
                  break;
                }
              } catch (Exception e) {
                e.printStackTrace();
              }
            }
          }
//          parent = loader;
//          Thread.currentThread().setContextClassLoader(loader);
          zip.close();
        } catch (Exception e) {
          e.printStackTrace();
          System.err.println("Plugin " + f.getName() + " failed to load!");
        }
      }
    }
    try {
//      Class<?> loader = ClassLoader.class;
//      Field f2 = loader.getDeclaredField("scl");
//      f2.setAccessible(true);
//      f2.set(null, parent);
    } catch (Exception e) {
      e.printStackTrace();
    }
    System.out.println(plugins.size() + " plugins loaded!");
  }

  public static void addURL(URL u) throws IOException {
    URLClassLoader sysloader = (URLClassLoader) ClassLoader.getSystemClassLoader();
    Class<?> sysclass = URLClassLoader.class;
    try {
      Method method = sysclass.getDeclaredMethod("addURL", new Class[] { URL.class });
      method.setAccessible(true);
      method.invoke(sysloader, new Object[] { u });
    } catch (Throwable t) {
      t.printStackTrace();
    }
  }

  public ArrayList<Plugin> getPlugins() {
    return plugins;
  }

}
