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
  private static final File propFile = new File(JByteMod.workingDir, JByteMod.configPath);

  public List<Option> bools = new ArrayList<>();
  public List<Option> defaults = Arrays.asList(new Option("sort_methods", false, Type.BOOLEAN), new Option("use_rt", false, Type.BOOLEAN),
      new Option("compute_maxs", true, Type.BOOLEAN), new Option("select_code_tab", true, Type.BOOLEAN),
      new Option("hints", false, Type.BOOLEAN, "editor_group"), new Option("copy_formatted", false, Type.BOOLEAN, "editor_group"),
      new Option("simplify_graph", true, Type.BOOLEAN, "graph_group"), new Option("remove_redundant", false, Type.BOOLEAN, "graph_group"),
      new Option("primary_color", "#557799", Type.STRING, "color_group"), new Option("secondary_color", "#995555", Type.STRING, "color_group"),
      new Option("use_weblaf", true, Type.BOOLEAN, "style_group"));

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
          if (o1 == null || o2 == null || find(o2.getName()) == null || findDefault(o1.getName()) == null) {
            JByteMod.LOGGER.warn("Option file not matching defaults, maybe from old version?");
            this.initWithDefaults(true);
            this.save();
            return;
          }
        }
        if (bools.isEmpty()) {
          JByteMod.LOGGER.warn("Couldn't read file, probably empty");
          this.initWithDefaults(false);
          this.save();
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    } else {
      JByteMod.LOGGER.warn("Property File \"" + propFile.getName() + "\" does not exist, creating...");
      this.initWithDefaults(false);
      this.save();
    }
  }

  private void initWithDefaults(boolean keepExisting) {
    if (keepExisting) {
      for (Option o : defaults) {
        if (find(o.getName()) == null) {
          bools.add(o);
        }
      }
      for (Option o : new ArrayList<>(bools)) {
        if (findDefault(o.getName()) == null) {
          bools.remove(o);
        }
      }
    } else {
      bools = new ArrayList<>();
      bools.addAll(defaults);
    }
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
    Option op = find(name);
    if (op != null) {
      return op;
    }
    JOptionPane.showMessageDialog(null, "Missing option: " + name + "\nRewriting your config file!");
    this.initWithDefaults(false);
    this.save();
    op = find(name);
    if (op != null) {
      return op;
    }
    throw new RuntimeException("Option not found: " + name);
  }

  private Option find(String name) {
    for (Option o : bools) {
      if (o.getName().equalsIgnoreCase(name)) {
        return o;
      }
    }
    return null;
  }
  private Option findDefault(String name) {
    for (Option o : defaults) {
      if (o.getName().equalsIgnoreCase(name)) {
        return o;
      }
    }
    return null;
  }
}
