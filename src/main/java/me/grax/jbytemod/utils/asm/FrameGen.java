package me.grax.jbytemod.utils.asm;

import java.io.IOException;
import java.util.Map;

import javax.swing.JOptionPane;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

import me.grax.jbytemod.JByteMod;
import me.grax.jbytemod.utils.ErrorDisplay;
import me.lpk.util.ASMUtils;
import me.lpk.util.JarUtils;

public class FrameGen extends Thread {

  private static Map<String, ClassNode> libraries;

  public static void regenerateFrames(JByteMod jbm, ClassNode cn) {
    if (libraries == null && JByteMod.ops.get("use_rt").getBoolean()) {
      if (JOptionPane.showConfirmDialog(null,
          "It seems like rt.jar hasn't been loaded, would you like to load it now? (This could take some time)") == JOptionPane.OK_OPTION) {
        try {
          libraries = JarUtils.loadRT();
        } catch (IOException e) {
          new ErrorDisplay(e);
        }
        if (libraries == null) {
          return;
        }
      } else {
        return;
      }
    }
    ClassWriter cw = new LibClassWriter(ClassWriter.COMPUTE_FRAMES, jbm.getFile().getClasses(), libraries);
    try {
      cn.accept(cw);
      ClassNode node2 = ASMUtils.getNode(cw.toByteArray());
      cn.methods.clear();
      cn.methods.addAll(node2.methods);
      JByteMod.LOGGER.log("Successfully regenerated frames at class " + cn.name);
    } catch (Exception e) {
      e.printStackTrace();
      return;
    }
  }

  @Override
  public void run() {
    try {
      libraries = JarUtils.loadRT();
      JByteMod.LOGGER.log("Successfully loaded RT.jar");
    } catch (IOException e) {
      new ErrorDisplay(e);
    }
  }
}
