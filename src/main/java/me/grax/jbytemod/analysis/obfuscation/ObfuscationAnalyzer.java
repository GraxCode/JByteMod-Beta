package me.grax.jbytemod.analysis.obfuscation;

import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TryCatchBlockNode;

import me.grax.jbytemod.analysis.obfuscation.enums.MethodObfType;
import me.grax.jbytemod.analysis.obfuscation.enums.NameObfType;
import me.grax.jbytemod.analysis.obfuscation.result.MethodResult;
import me.grax.jbytemod.analysis.obfuscation.result.NamesResult;
import me.lpk.util.OpUtils;

@SuppressWarnings("unused")
public class ObfuscationAnalyzer implements Opcodes {
  private Map<String, ClassNode> classes;

  private static final String NONE = "None";
  private static final String ALLATORI = "Allatori";
  private static final String STRINGER = "Stringer";
  private static final String ZKM8 = "ZKM8";
  private static final String ZKM5 = "ZKM5";

  public ObfuscationAnalyzer(Map<String, ClassNode> classes) {
    this.classes = classes;
  }

  public NamesResult analyzeNames() {
    ArrayList<NameObfType> cnames = new ArrayList<>();
    ArrayList<NameObfType> mnames = new ArrayList<>();
    ArrayList<NameObfType> fnames = new ArrayList<>();
    for (ClassNode cn : classes.values()) {
      analyzeName(cn.name, cnames);
      for (MethodNode mn : cn.methods) {
        analyzeName(mn.name, mnames);
      }
      for (FieldNode fn : cn.fields) {
        analyzeName(fn.name, fnames);
      }
    }
    return new NamesResult(cnames, mnames, fnames);
  }

  private static CharsetEncoder asciiEncoder = Charset.forName("US-ASCII").newEncoder();

  public MethodResult analyzeMethod() {
    ArrayList<MethodObfType> mobf = new ArrayList<>();
    for (ClassNode cn : classes.values()) {
      for (MethodNode mn : cn.methods) {
        boolean tcbo = false;
        if (isTCBO(mn)) {
          tcbo = true;
        }
        boolean pop2 = false;
        boolean indyn = false;
        boolean strobf = false;
        for (AbstractInsnNode ain : mn.instructions.toArray()) {
          if (ain.getOpcode() == POP2) {
            pop2 = true;
          }
          if (ain.getOpcode() == INVOKEDYNAMIC) {
            indyn = true;
          }
          if (ain.getOpcode() == LDC) {
            LdcInsnNode ldc = (LdcInsnNode) ain;
            if (ldc.cst instanceof String && isStringObf(ldc.cst.toString())) {
              strobf = true;
            }
          }
        }
        if (!tcbo && !pop2 && !indyn && !strobf) {
          mobf.add(MethodObfType.NONE);
        } else {
          if (tcbo) {
            mobf.add(MethodObfType.TCBO);
          }
          if (pop2) {
            mobf.add(MethodObfType.POP2);
          }
          if (indyn) {
            mobf.add(MethodObfType.INVOKEDYNAMIC);
          }
          if (strobf) {
            mobf.add(MethodObfType.STRING);
          }
        }
      }
    }
    return new MethodResult(mobf);
  }

  private boolean isStringObf(String string) {
    if (string.length() >= 1000) {
      return true;
    }
    int nonAscii = 0;
    for (char c : string.toCharArray()) {
      if (!asciiEncoder.canEncode(c)) {
        nonAscii++;
      }
    }
    double p = nonAscii / (double) string.length();
    return p > 0.5;
  }

  private boolean isTCBO(MethodNode mn) {
    if (mn.tryCatchBlocks.size() > mn.instructions.size() / 8 || mn.tryCatchBlocks.size() > 15) {
      return true;
    }
    for (TryCatchBlockNode tcbn : mn.tryCatchBlocks) {
      int start = OpUtils.getLabelIndex(tcbn.start);
      int end = OpUtils.getLabelIndex(tcbn.start);
      for (TryCatchBlockNode tcbn2 : mn.tryCatchBlocks) {
        int start2 = OpUtils.getLabelIndex(tcbn2.start);
        if (start2 >= start && start2 < end) {
          return true;
        }
      }
    }
    return false;
  }

  private static final List<String> keywords = Arrays
      .asList(new String[] { "abstract", "assert", "boolean", "break", "byte", "case", "catch", "char", "class", "const", "continue", "default", "do",
          "double", "else", "enum", "extends", "for", "final", "finally", "float", "goto", "if", "implements", "import", "instanceof", "int",
          "interface", "long", "native", "new", "package", "private", "protected", "public", "return", "short", "static", "strictfp", "super",
          "switch", "synchronized", "this", "throw", "throws", "transient", "try", "void", "volatile", "while", "true", "false", "null" });

  private static final List<String> windir = Arrays.asList(new String[] { "con", "nul", "aux", "prn" });

  private void analyzeName(String name, ArrayList<NameObfType> names) {
    boolean none = true;
    String sname = name.substring(name.lastIndexOf('/') + 1);
    if (sname.length() > 32) {
      names.add(NameObfType.LONG_LETTERS);
      none = false;
    } else if (sname.length() <= 2) { //actually 3 but there are "run", "put" and "get"
      names.add(NameObfType.SHORT_LETTERS);
      none = false;
    }
    if (!asciiEncoder.canEncode(sname)) {
      names.add(NameObfType.HIGH_CHAR);
      none = false;
    }
    if (keywords.contains(sname)) {
      names.add(NameObfType.JAVA_KEYWORD);
      none = false;
    } else if (windir.contains(sname.toLowerCase())) {
      names.add(NameObfType.INVALID_WINDIR);
      none = false;
    }
    if (none) {
      names.add(NameObfType.NONE);
    }
  }

}
