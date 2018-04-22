package me.grax.decompiler.code.ast;

import org.objectweb.asm.Opcodes;

public enum Operation implements Opcodes {
  ADD("+"), SUB("-"), MUL("*"), DIV("/"), REM("%"), SHL("<<"), SHR(">>"), USHR(">>>"), AND("&"), OR("|"), XOR("^"), NEG("-");

  private String symbol;

  Operation(String symbol) {
    this.symbol = symbol;
  }

  public String getSymbol() {
    return symbol;
  }

  public void setSymbol(String symbol) {
    this.symbol = symbol;
  }

  public static Operation of(int opc) {
    switch (opc) {
    case IADD:
    case DADD:
    case FADD:
    case LADD:
      return Operation.ADD;
    case ISUB:
    case FSUB:
    case LSUB:
    case DSUB:
      return Operation.SUB;
    case IMUL:
    case FMUL:
    case LMUL:
    case DMUL:
      return Operation.MUL;
    case IDIV:
    case FDIV:
    case LDIV:
    case DDIV:
      return Operation.DIV;
    case IREM:
    case FREM:
    case LREM:
    case DREM:
      return Operation.REM;
    case ISHL:
    case LSHL:
      return Operation.SHL;
    case ISHR:
    case LSHR:
      return Operation.SHR;
    case IUSHR:
    case LUSHR:
      return Operation.USHR;
    case IAND:
    case LAND:
      return Operation.AND;
    case IOR:
    case LOR:
      return Operation.OR;
    case IXOR:
    case LXOR:
      return Operation.XOR;
    case INEG:
    case FNEG:
    case LNEG:
    case DNEG:
      return Operation.NEG;
    }
    throw new RuntimeException("unresolved operation");
  }
}
