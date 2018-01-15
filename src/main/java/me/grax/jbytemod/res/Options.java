package me.grax.jbytemod.res;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import me.grax.jbytemod.JByteMod;
import me.grax.jbytemod.res.Option.Type;
import me.grax.jbytemod.utils.ErrorDisplay;

public class Options {
  private static final File propFile = new File("jbytemod.cfg");

  public List<Option> bools = new ArrayList<>();

  public Options() {
    if (propFile.exists()) {
      System.out.println("Loading settings... ");
      try {
        Files.lines(propFile.toPath()).forEach(l -> {
          String[] split = l.split("=");
          String[] def = split[0].split(":");
          try {
            bools.add(new Option(def[0], split[1], Type.valueOf(def[1]), def[2]));
          } catch (Exception e) {
            System.err.println("Couldn't parse line: " + l);
          }
        });
        if (bools.isEmpty()) {
          System.err.println("Couldn't read file, probably empty");
          this.initWithDefaults();
          this.save();
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    } else {
      System.out.println("Property File does not exist, creating...");
      this.initWithDefaults();
      this.save();
    }
  }

  private void initWithDefaults() {
    bools = Arrays.asList(new Option("sort_methods", false, Type.BOOLEAN), new Option("tree_search_sel", false, Type.BOOLEAN),
        new Option("load_rt_startup", false, Type.BOOLEAN), new Option("primary_color", "#557799", Type.STRING, "color_group"),
        new Option("secondary_color", "#995555", Type.STRING, "color_group"));
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
    throw new RuntimeException("Option not found: " + name);
  }

}
