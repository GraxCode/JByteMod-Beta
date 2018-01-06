package me.grax.jbytemod.decompiler;

import java.io.StringWriter;

import com.strobel.assembler.InputTypeLoader;
import com.strobel.assembler.metadata.Buffer;
import com.strobel.assembler.metadata.ITypeLoader;
import com.strobel.assembler.metadata.MetadataSystem;
import com.strobel.assembler.metadata.TypeDefinition;
import com.strobel.assembler.metadata.TypeReference;
import com.strobel.decompiler.DecompilationOptions;
import com.strobel.decompiler.DecompilerSettings;
import com.strobel.decompiler.PlainTextOutput;

import me.grax.jbytemod.JByteMod;
import me.grax.jbytemod.ui.DecompilerPanel;
import me.grax.jbytemod.utils.ErrorDisplay;

public class ProcyonDecompiler extends Decompiler {

  public ProcyonDecompiler(JByteMod jbm, DecompilerPanel dp) {
    super(jbm, dp);
  }

  public String decompile(byte[] b) {
    try {
      DecompilerSettings settings = new DecompilerSettings();
      MetadataSystem metadataSystem = new MetadataSystem(new ITypeLoader() {
        private InputTypeLoader backLoader = new InputTypeLoader();

        @Override
        public boolean tryLoadType(String s, Buffer buffer) {
          if (s.equals(cn.name)) {
            buffer.putByteArray(b, 0, b.length);
            buffer.position(0);
            return true;
          } else {
            return backLoader.tryLoadType(s, buffer);
          }
        }
      });
      TypeReference type = metadataSystem.lookupType(cn.name);
      DecompilationOptions decompilationOptions = new DecompilationOptions();
      decompilationOptions.setSettings(DecompilerSettings.javaDefaults());
      decompilationOptions.setFullDecompilation(true);
      TypeDefinition resolvedType = null;
      if (type == null || ((resolvedType = type.resolve()) == null)) {
        new ErrorDisplay("Unable to resolve type.");
        return "error";
      }
      StringWriter stringwriter = new StringWriter();
      settings.getLanguage().decompileType(resolvedType, new PlainTextOutput(stringwriter), decompilationOptions);
      String decompiledSource = stringwriter.toString();
      return decompiledSource;
    } catch (Exception e) {
      return e.toString();
    }
  }
}
