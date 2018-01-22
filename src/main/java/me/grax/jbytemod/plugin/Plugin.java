package me.grax.jbytemod.plugin;

import java.util.Map;

import javax.swing.JMenuBar;
import javax.swing.JTree;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import me.grax.jbytemod.JByteMod;

public abstract class Plugin {
  protected String name;
  protected String version;
  protected String author;

  public Plugin(String name, String version, String author) {
    this.name = name;
    this.version = version;
    this.author = author;
  }

  public abstract void init();

  public abstract void loadFile(Map<String, ClassNode> map);

  public abstract boolean isClickable();

  public abstract void menuClick();

  protected final Map<String, ClassNode> getCurrentFile() {
    return JByteMod.instance.getFile().getClasses();
  }

  protected final void updateTree() {
    JByteMod.instance.refreshTree();
  }

  protected final JMenuBar getMenu() {
    return JByteMod.instance.getMyMenuBar();
  }

  protected final JTree gerTree() {
    return JByteMod.instance.getJarTree();
  }

  protected final ClassNode gerSelectedNode() {
    return JByteMod.instance.getCurrentNode();
  }

  protected final MethodNode gerSelectedMethod() {
    return JByteMod.instance.getCurrentMethod();
  }

  public final String getName() {
    return name;
  }

  public final String getVersion() {
    return version;
  }

  public final String getAuthor() {
    return author;
  }
}
