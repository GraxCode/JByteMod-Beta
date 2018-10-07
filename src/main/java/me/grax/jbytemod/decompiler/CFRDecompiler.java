package me.grax.jbytemod.decompiler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.benf.cfr.reader.PluginRunner;
import org.benf.cfr.reader.api.ClassFileSource;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.Pair;
import org.benf.cfr.reader.entities.ClassFile;
import org.benf.cfr.reader.entities.Method;
import org.benf.cfr.reader.entities.constantpool.ConstantPool;
import org.benf.cfr.reader.state.ClassFileSourceImpl;
import org.benf.cfr.reader.state.DCCommonState;
import org.benf.cfr.reader.util.bytestream.BaseByteData;
import org.benf.cfr.reader.util.getopt.OptionsImpl;
import org.benf.cfr.reader.util.output.DumperFactory;
import org.benf.cfr.reader.util.output.ToStringDumper;
import org.objectweb.asm.tree.MethodNode;

import me.grax.jbytemod.JByteMod;
import me.grax.jbytemod.ui.DecompilerPanel;

public class CFRDecompiler extends Decompiler {

  public static final HashMap<String, String> options = new HashMap<>();

  public CFRDecompiler(JByteMod jbm, DecompilerPanel dp) {
    super(jbm, dp);
  }

  static {
    options.put("aexagg", "false");
    options.put("allowcorrecting", "true");
    options.put("arrayiter", "true");
    options.put("caseinsensitivefs", "false");
    options.put("clobber", "false");
    options.put("collectioniter", "true");
    options.put("commentmonitors", "false");
    options.put("decodeenumswitch", "true");
    options.put("decodefinally", "true");
    options.put("decodelambdas", "true");
    options.put("decodestringswitch", "true");
    options.put("dumpclasspath", "false");
    options.put("eclipse", "true");
    options.put("elidescala", "false");
    options.put("forcecondpropagate", "false");
    options.put("forceexceptionprune", "false");
    options.put("forcereturningifs", "false");
    options.put("forcetopsort", "false");
    options.put("forcetopsortaggress", "false");
    options.put("forloopaggcapture", "false");
    options.put("hidebridgemethods", "true");
    options.put("hidelangimports", "true");
    options.put("hidelongstrings", "false");
    options.put("hideutf", "true");
    options.put("innerclasses", "true");
    options.put("j14classobj", "false");
    options.put("labelledblocks", "true");
    options.put("lenient", "false");
    options.put("liftconstructorinit", "true");
    options.put("override", "true");
    options.put("pullcodecase", "false");
    options.put("recover", "true");
    options.put("recovertypeclash", "false");
    options.put("recovertypehints", "false");
    options.put("relinkconststring", "true");
    options.put("removebadgenerics", "true");
    options.put("removeboilerplate", "true");
    options.put("removedeadmethods", "true");
    options.put("removeinnerclasssynthetics", "true");
    options.put("rename", "false");
    options.put("renamedupmembers", "false");
    options.put("renameenumidents", "false");
    options.put("renameillegalidents", "false");
    options.put("showinferrable", "false");
    options.put("silent", "false");
    options.put("stringbuffer", "false");
    options.put("stringbuilder", "true");
    options.put("sugarasserts", "true");
    options.put("sugarboxing", "true");
    options.put("sugarenums", "true");
    options.put("tidymonitors", "true");
    options.put("usenametable", "true");
  }

  public String decompile(byte[] b, MethodNode mn) {
    try {
      HashMap<String, String> ops = new HashMap<>();
      ops.put("comments", "false");
      for (String key : options.keySet()) {
        ops.put(key, String.valueOf(JByteMod.ops.get("cfr_" + key).getBoolean()));
      }
      ClassFileSource cfs = new ClassFileSource() {

        @Override
        public void informAnalysisRelativePathDetail(String a, String b) {
        }

        @Override
        public String getPossiblyRenamedPath(String path) {
          return path;
        }

        @Override
        public Pair<byte[], String> getClassFileContent(String path) throws IOException {
          String name = path.substring(0, path.length() - 6);
          if (name.equals(cn.name)) {
            return Pair.make(b, name);
          }
          return null; //cfr loads unnecessary classes
        }

        @Override
        public Collection<String> addJar(String arg0) {
          throw new RuntimeException();
        }
      };
      PluginRunner runner = new PluginRunner(ops, cfs);
      if(mn != null) {
        BaseByteData data = new BaseByteData(b);
        ClassFile cf = new ClassFile(data, "", initDCState(ops, cfs));
      	Field cpf = Method.class.getDeclaredField("cp");
      	Field descI = Method.class.getDeclaredField("descriptorIndex");
      	descI.setAccessible(true);
      	cpf.setAccessible(true);
        for(Method m : cf.getMethodByName(mn.name)) {
        	ConstantPool cp = (ConstantPool) cpf.get(m);
        	if(cp.getUTF8Entry(descI.getInt(m)).getValue().equals(mn.desc)) {
        		ToStringDumper tsd = new ToStringDumper();
        		m.dump(tsd, true);
        		return tsd.toString();
        	}
        }
      }
      String decompilation = runner.getDecompilationFor(cn.name);
      System.gc(); //cfr has a performance bug
      return decompilation.substring(37); //small hack to remove watermark
    } catch (Exception e) {
      e.printStackTrace();
      StringWriter sw = new StringWriter();
      PrintWriter pw = new PrintWriter(sw);
      e.printStackTrace(pw);
      return sw.toString();
    }
  }

  private static DCCommonState initDCState(Map<String, String> optionsMap, ClassFileSource classFileSource) {
    OptionsImpl options = new OptionsImpl(null, null, optionsMap);
    if (classFileSource == null) classFileSource = new ClassFileSourceImpl(options);
    DCCommonState dcCommonState = new DCCommonState(options, classFileSource);
    return dcCommonState;
  }

  protected Pair<byte[], String> getSystemClass(String name, String path) throws IOException {
    InputStream is = ClassLoader.getSystemClassLoader().getResourceAsStream(path);
    if (is != null) {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      byte[] buffer = new byte[4096];
      int n;
      while ((n = is.read(buffer)) > 0) {
        baos.write(buffer, 0, n);
      }
      return Pair.make(baos.toByteArray(), name);
    }
    return null;
  }
}
