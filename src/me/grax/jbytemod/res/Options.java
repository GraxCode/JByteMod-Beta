package me.grax.jbytemod.res;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import me.grax.jbytemod.utils.ErrorDisplay;

public class Options {
  private Properties props;
  private static final File propFile = new File("jbytemod.cfg");

  public static final List<String> bools = Arrays.asList("sort_methods", "tree_search_sel", "load_rt_startup");
  public Options() {
    props = new Properties();
    if (propFile.exists()) {
      try {
        props.load(new FileInputStream(propFile));
      } catch (IOException e) {
        e.printStackTrace();
      }
    } else {
      System.out.println("Property File does not exist");
    }
  }

  public Properties getProperties() {
    return props;
  }

  public void setProperty(String s, String o) {
    props.setProperty(s, o);
    this.save();
  }
  
  public Object getProperty(String s) {
    return props.getOrDefault(s, "false");
  }

  public boolean getBool(String s) {
    return Boolean.parseBoolean(getProperty(s).toString());
  }
  private void save() {
    new Thread(() -> {
      try {
        props.store(new FileOutputStream(propFile), "JByteMod Properties");
      } catch (Exception e) {
        new ErrorDisplay(e);
      }
    }).start();
  }
  
}
