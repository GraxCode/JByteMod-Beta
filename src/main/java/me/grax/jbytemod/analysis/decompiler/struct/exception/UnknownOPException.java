package me.grax.jbytemod.analysis.decompiler.struct.exception;

import me.lpk.util.OpUtils;

public class UnknownOPException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  public UnknownOPException(int opc) {
    super("Unresolved opcode: " + OpUtils.getOpcodeText(opc) + " (" + opc + ")");
  }
}
