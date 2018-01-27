package me.grax.jbytemod.res;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JOptionPane;

import me.grax.jbytemod.JByteMod;
import me.grax.jbytemod.res.Option.Type;
import me.grax.jbytemod.utils.ErrorDisplay;

public class Options {
  private static final File propFile = new File("jbytemod.cfg");

  public List<Option> bools = new ArrayList<>();
  public List<Option> defaults = Arrays.asList(new Option("sort_methods", false, Type.BOOLEAN), new Option("use_rt", false, Type.BOOLEAN),
      new Option("compute_maxs", true, Type.BOOLEAN), new Option("hints", false, Type.BOOLEAN, "editor_group"),
      new Option("primary_color", "#557799", Type.STRING, "color_group"), new Option("secondary_color", "#995555", Type.STRING, "color_group"), new Option("use_weblaf", true, Type.BOOLEAN, "style_group"));

  public Options() {
    if (propFile.exists()) {
      JByteMod.LOGGER.log("Loading settings... ");
      try {
        Files.lines(propFile.toPath()).forEach(l -> {
          String[] split = l.split("=");
          String[] def = split[0].split(":");
          try {
            bools.add(new Option(def[0], split[1], Type.valueOf(def[1]), def[2]));
          } catch (Exception e) {
            JByteMod.LOGGER.warn("Couldn't parse line: " + l);
          }
        });
        for (int i = 0; i < bools.size(); i++) {
          Option o1 = bools.get(i);
          Option o2 = defaults.get(i);
          if (o1 == null || o2 == null || !o1.getName().equals(o2.getName())) {
            JOptionPane.showMessageDialog(null, "Corrupt option file, rewriting (#" + i + ")");
            this.initWithDefaults();
            this.save();
            return;
          }
        }
        if (bools.isEmpty()) {
          JByteMod.LOGGER.warn("Couldn't read file, probably empty");
          this.initWithDefaults();
          this.save();
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    } else {
      JByteMod.LOGGER.warn("Property File does not exist, creating...");
      this.initWithDefaults();
      this.save();
    }
  }

  private void initWithDefaults() {
    bools = new ArrayList<>();
    bools.addAll(defaults);
  }

  public void save() {
    new Thread(() -> {
      try {
        PrintWriter pw = new PrintWriter(propFile);
        for (Option o : bools) {
          pw.println(o.getName() + ":" + o.getType().name() + ":" + o.getGroup() + "=" + o.getValue());
        }
        pw.close();
      } catch (Exception e) {
        new ErrorDisplay(e);
      }
    }).start();
  }

  public Option get(String name) {
    for (Option o : bools) {
      if (o.getName().equalsIgnoreCase(name)) {
        return o;
      }
    }
    JOptionPane.showMessageDialog(null, "Missing option: " + name + "\nRewriting your config file!");
    this.initWithDefaults();
    this.save();
    for (Option o : bools) {
      if (o.getName().equalsIgnoreCase(name)) {
        return o;
      }
    }
    throw new RuntimeException("Option not found: " + name);
  }

}
