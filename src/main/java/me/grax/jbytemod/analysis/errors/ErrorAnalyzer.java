package me.grax.jbytemod.analysis.errors;

import java.util.HashMap;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.IincInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.BasicVerifier;

import me.grax.jbytemod.JByteMod;

public class ErrorAnalyzer {
  private MethodNode mn;
  private ClassNode cn;

  public ErrorAnalyzer(ClassNode cn, MethodNode mn) {
    this.cn = cn;
    this.mn = mn;
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  public HashMap<AbstractInsnNode, Mistake> findErrors() {
    HashMap<AbstractInsnNode, Mistake> map = new HashMap<>();
    //general analysis
    for (AbstractInsnNode ain : mn.instructions.toArray()) {
      maxLocals(mn.maxLocals, ain, map);
    }
    //asm verification
    final Analyzer a = new Analyzer(new BasicVerifier());
    try {
      a.analyze(cn.name, mn);
    } catch (AnalyzerException e) {
      put(map, e.node, new InsnError(e.getMessage()));
    } catch (Exception e) {
      JByteMod.LOGGER.err("Couldn't analyze errors in bytecode (" + e.toString() + ")");
    }
    return map;
  }

  private void maxLocals(int maxLocals, AbstractInsnNode ain, HashMap<AbstractInsnNode, Mistake> map) {
    int var = -1;
    if (ain.getType() == AbstractInsnNode.VAR_INSN) {
      VarInsnNode vin = (VarInsnNode) ain;
      var = vin.var;
    }
    if (ain.getType() == AbstractInsnNode.IINC_INSN) {
      IincInsnNode iinc = (IincInsnNode) ain;
      var = iinc.var;
    }
    if (var > maxLocals) {
      put(map, ain, new InsnWarning("max locals exceeded"));
    }
  }

  private void put(HashMap<AbstractInsnNode, Mistake> map, AbstractInsnNode ain, Mistake mistake) {
    if (!map.containsKey(ain)) {
      map.put(ain, mistake);
    } else {
      Mistake present = map.get(ain);
      boolean isMoreImportant = !(present instanceof InsnError && mistake instanceof InsnWarning);
      if (isMoreImportant) {
        map.put(ain, mistake);
      }
    }
  }
}
