package me.grax.jbytemod.analysis.decompiler.struct;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.objectweb.asm.Handle;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.IincInsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.LookupSwitchInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TableSwitchInsnNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import me.grax.jbytemod.analysis.block.Block;
import me.grax.jbytemod.analysis.decompiler.ClassDefinition;
import me.grax.jbytemod.analysis.decompiler.code.ast.Comparison;
import me.grax.jbytemod.analysis.decompiler.code.ast.Expression;
import me.grax.jbytemod.analysis.decompiler.code.ast.Operation;
import me.grax.jbytemod.analysis.decompiler.code.ast.VarType;
import me.grax.jbytemod.analysis.decompiler.code.ast.expressions.ArrayIndexExpression;
import me.grax.jbytemod.analysis.decompiler.code.ast.expressions.ArrayStoreExpression;
import me.grax.jbytemod.analysis.decompiler.code.ast.expressions.CastExpression;
import me.grax.jbytemod.analysis.decompiler.code.ast.expressions.ClassTypeExpression;
import me.grax.jbytemod.analysis.decompiler.code.ast.expressions.ComparisonExpression;
import me.grax.jbytemod.analysis.decompiler.code.ast.expressions.DebugStackAssignExpression;
import me.grax.jbytemod.analysis.decompiler.code.ast.expressions.DebugStackExpression;
import me.grax.jbytemod.analysis.decompiler.code.ast.expressions.FieldAssignExpression;
import me.grax.jbytemod.analysis.decompiler.code.ast.expressions.FieldExpression;
import me.grax.jbytemod.analysis.decompiler.code.ast.expressions.IncrementExpression;
import me.grax.jbytemod.analysis.decompiler.code.ast.expressions.InstanceofExpression;
import me.grax.jbytemod.analysis.decompiler.code.ast.expressions.InvokeDynamicExpression;
import me.grax.jbytemod.analysis.decompiler.code.ast.expressions.LookupSwitchExpression;
import me.grax.jbytemod.analysis.decompiler.code.ast.expressions.MethodExpression;
import me.grax.jbytemod.analysis.decompiler.code.ast.expressions.NewArrayExpression;
import me.grax.jbytemod.analysis.decompiler.code.ast.expressions.NewPrimArrayExpression;
import me.grax.jbytemod.analysis.decompiler.code.ast.expressions.NewTypeExpression;
import me.grax.jbytemod.analysis.decompiler.code.ast.expressions.NullExpression;
import me.grax.jbytemod.analysis.decompiler.code.ast.expressions.OpExpression;
import me.grax.jbytemod.analysis.decompiler.code.ast.expressions.ReturnExpression;
import me.grax.jbytemod.analysis.decompiler.code.ast.expressions.SingleOpExpression;
import me.grax.jbytemod.analysis.decompiler.code.ast.expressions.StringExpression;
import me.grax.jbytemod.analysis.decompiler.code.ast.expressions.TableSwitchExpression;
import me.grax.jbytemod.analysis.decompiler.code.ast.expressions.TextExpression;
import me.grax.jbytemod.analysis.decompiler.code.ast.expressions.ThrowExpression;
import me.grax.jbytemod.analysis.decompiler.code.ast.expressions.ValueExpression;
import me.grax.jbytemod.analysis.decompiler.code.ast.expressions.VarAssignExpression;
import me.grax.jbytemod.analysis.decompiler.code.ast.expressions.VarLoadExpression;
import me.grax.jbytemod.analysis.decompiler.code.ast.expressions.VarSpecialExpression;
import me.grax.jbytemod.analysis.decompiler.struct.exception.StackException;
import me.grax.jbytemod.analysis.decompiler.struct.exception.UnknownOPException;
import me.grax.jbytemod.analysis.decompiler.struct.utils.DescUtils;
import me.grax.jbytemod.analysis.decompiler.syntax.nodes.NodeList;
import me.lpk.util.AccessHelper;
import me.lpk.util.OpUtils;

public class Conversion implements Opcodes {

  private NodeList list;
  private JVMStack stack;
  private int line;
  private MethodNode mn;
  private JVMStack preStack;
  private int arrayDebugIndex;

  public Conversion(MethodNode mn, NodeList list) {
    this.mn = mn;
    this.list = list;
  }

  public Conversion(MethodNode mn, NodeList list, JVMStack preStack) {
    this.mn = mn;
    this.list = list;
    this.preStack = preStack;
  }

  public int getLine() {
    return line;
  }

  public void convert(Block b) {
    this.line = 0;
    if (preStack != null) {
      this.stack = new JVMStack(preStack);
    } else {
      this.stack = new JVMStack();
    }
    for (AbstractInsnNode ain : b.getNodes()) {
      switch (ain.getType()) {
      case AbstractInsnNode.INSN:
        visitInsnNode(ain.getOpcode());
        break;
      case AbstractInsnNode.VAR_INSN:
        VarInsnNode vin = (VarInsnNode) ain;
        visitVarInsnNode(vin.getOpcode(), vin.var);
        break;
      case AbstractInsnNode.METHOD_INSN:
        MethodInsnNode min = (MethodInsnNode) ain;
        visitMethodInsnNode(min.getOpcode(), min.owner, min.name, min.desc);
        break;
      case AbstractInsnNode.FIELD_INSN:
        FieldInsnNode fin = (FieldInsnNode) ain;
        visiFieldInsnNode(fin.getOpcode(), fin.owner, fin.name, fin.desc);
        break;
      case AbstractInsnNode.TYPE_INSN:
        TypeInsnNode tin = (TypeInsnNode) ain;
        visiTypeInsnNode(tin.getOpcode(), tin.desc);
        break;
      case AbstractInsnNode.LDC_INSN:
        LdcInsnNode lin = (LdcInsnNode) ain;
        visitLdcInsnNode(lin.cst);
        break;
      case AbstractInsnNode.INT_INSN:
        IntInsnNode iin = (IntInsnNode) ain;
        visitIntInsnNode(iin.getOpcode(), iin.operand);
        break;
      case AbstractInsnNode.JUMP_INSN:
        visitJumpInsnNode(ain.getOpcode());
        break;
      case AbstractInsnNode.IINC_INSN:
        IincInsnNode iinc = (IincInsnNode) ain;
        visitIincInsnNode(iinc.var, iinc.incr);
        break;
      case AbstractInsnNode.TABLESWITCH_INSN:
        TableSwitchInsnNode sw = (TableSwitchInsnNode) ain;
        visitTableSwitch(sw.min, sw.max);
        break;
      case AbstractInsnNode.LOOKUPSWITCH_INSN:
        LookupSwitchInsnNode lsw = (LookupSwitchInsnNode) ain;
        visitLookupSwitch(lsw.keys.size());
        break;
      case AbstractInsnNode.INVOKE_DYNAMIC_INSN:
        InvokeDynamicInsnNode idin = (InvokeDynamicInsnNode) ain;
        visitInvokeDynamic(idin);
      case AbstractInsnNode.LABEL:
      case AbstractInsnNode.LINE:
      case AbstractInsnNode.FRAME:
        break;
      default:
        throw new RuntimeException("unrecognized AbstractInsnNode type: " + ain.getType());
      }
      line++;
    }
    int var = 0;
    ArrayList<Expression> left = new ArrayList<>();
    while (!stack.getList().isEmpty()) {
      Expression item = stack.getList().pop();
      if (item instanceof ComparisonExpression || item instanceof TableSwitchExpression) {
        list.add(item);
      } else {
        left.add(0, item);
      }
    }
    for (Expression debug : left) {
      int prev = -1;
      if (debug instanceof DebugStackExpression) {
        prev = ((DebugStackExpression) debug).getVar();
      }
      if (prev != var) {
        //avoid nonsense setters
        list.add(new DebugStackAssignExpression(var, debug));
      }
      stack.getList().push(debug);
      var++;
    }
  }

  private void visitInvokeDynamic(InvokeDynamicInsnNode idin) {
    InvokeDynamicExpression idye = new InvokeDynamicExpression(idin.name, idin.desc, idin.bsmArgs, idin.bsm);
    Handle methodHandle = idye.getMethodHandle();
    if (methodHandle != null) {
      ArrayList<Integer> descSizes = DescUtils.getInnerDescSizes(methodHandle.getDesc());
      List<Expression> args = new ArrayList<>();
      for (int i : descSizes) {
        if (i == 1) {
          args.add(stack.pop());
        } else {
          args.add(stack.pop2());
        }
      }
      Collections.reverse(args);
      idye.setArgs(args);
    }
    stack.push(idye);
  }

  private void visitLookupSwitch(int size) {
    stack.push(new LookupSwitchExpression(stack.pop(), size));
  }

  private void visitTableSwitch(int min, int max) {
    stack.push(new TableSwitchExpression(stack.pop(), min, max));
  }

  private void visitIincInsnNode(int var, int incr) {
    list.add(new IncrementExpression(new VarLoadExpression(var, VarType.INT), incr));
  }

  private void visitIntInsnNode(int opcode, int operand) {
    switch (opcode) {
    case BIPUSH:
    case SIPUSH:
      stack.push(new ValueExpression(VarType.INT, operand));
      break;
    case NEWARRAY:
      VarType type = null;
      switch (operand) {
      case 4:
        type = VarType.BOOLEAN;
        break;
      case 5:
        type = VarType.CHAR;
        break;
      case 6:
        type = VarType.FLOAT;
        break;
      case 7:
        type = VarType.DOUBLE;
        break;
      case 8:
        type = VarType.BYTE;
        break;
      case 9:
        type = VarType.SHORT;
        break;
      case 10:
        type = VarType.INT;
        break;
      case 11:
        type = VarType.LONG;
        break;
      default:
        throw new RuntimeException();
      }
      stack.push(new NewPrimArrayExpression(stack.pop(), type));
      break;
    default:
      throw new UnknownOPException(opcode);
    }
  }

  private void visitJumpInsnNode(int opcode) {
    switch (opcode) {
    case IFEQ:
      stack.push(new ComparisonExpression(stack.pop(), Comparison.IS, new ValueExpression(VarType.INT, 0)));
      break;
    case IFNE:
      stack.push(new ComparisonExpression(stack.pop(), Comparison.ISNOT, new ValueExpression(VarType.INT, 0)));
      break;
    case IFGE:
      stack.push(new ComparisonExpression(stack.pop(), Comparison.GREATEREQUALS, new ValueExpression(VarType.INT, 0)));
      break;
    case IFGT:
      stack.push(new ComparisonExpression(stack.pop(), Comparison.GREATER, new ValueExpression(VarType.INT, 0)));
      break;
    case IFLE:
      stack.push(new ComparisonExpression(stack.pop(), Comparison.LOWEREQUALS, new ValueExpression(VarType.INT, 0)));
      break;
    case IFLT:
      stack.push(new ComparisonExpression(stack.pop(), Comparison.LOWER, new ValueExpression(VarType.INT, 0)));
      break;
    case IF_ACMPEQ:
      Expression top = stack.pop();
      Expression second = stack.pop();
      stack.push(new ComparisonExpression(second, Comparison.IS, top));
      break;
    case IF_ACMPNE:
      top = stack.pop();
      second = stack.pop();
      stack.push(new ComparisonExpression(second, Comparison.ISNOT, top));
      break;
    case IFNULL:
      stack.push(new ComparisonExpression(stack.pop(), Comparison.IS, new NullExpression()));
      break;
    case IFNONNULL:
      stack.push(new ComparisonExpression(stack.pop(), Comparison.ISNOT, new NullExpression()));
      break;
    case GOTO:
      break;
    default:
      top = stack.pop();
      second = stack.pop();
      switch (opcode) {
      case IF_ICMPEQ:
        stack.push(new ComparisonExpression(second, Comparison.IS, top));
        break;
      case IF_ICMPNE:
        stack.push(new ComparisonExpression(second, Comparison.ISNOT, top));
        break;
      case IF_ICMPGE:
        stack.push(new ComparisonExpression(second, Comparison.GREATEREQUALS, top));
        break;
      case IF_ICMPGT:
        stack.push(new ComparisonExpression(second, Comparison.GREATER, top));
        break;
      case IF_ICMPLE:
        stack.push(new ComparisonExpression(second, Comparison.LOWEREQUALS, top));
        break;
      case IF_ICMPLT:
        stack.push(new ComparisonExpression(second, Comparison.LOWER, top));
        break;
      case JSR:
      default:
        throw new UnknownOPException(opcode);
      }
    }
  }

  private void visiTypeInsnNode(int opcode, String desc) {
    switch (opcode) {
    case NEW:
      stack.push(new NewTypeExpression(new ClassDefinition(desc)));
      break;
    case ANEWARRAY:
      NewArrayExpression nae = new NewArrayExpression(stack.pop(), new ClassDefinition(desc));
      DebugStackExpression e = new DebugStackExpression(arrayDebugIndex, 1, null, "array");
      list.add(new DebugStackAssignExpression(arrayDebugIndex++, nae, "array"));
      stack.push(e);
      break;
    case INSTANCEOF:
      stack.push(new InstanceofExpression(stack.pop(), new ClassDefinition(desc)));
      break;
    case CHECKCAST:
      stack.push(new CastExpression(new ClassDefinition(desc), stack.pop()));
      break;
    default:
      throw new UnknownOPException(opcode);
    }
  }

  private void visiFieldInsnNode(int opcode, String owner, String name, String desc) {
    if (opcode == GETSTATIC) {
      FieldExpression fe = new FieldExpression(new ClassDefinition(owner), name, VarType.ofDesc(desc));
      stack.push(fe);
    } else if (opcode == GETFIELD) {
      FieldExpression fe = new FieldExpression(stack.pop(), name, VarType.ofDesc(desc));
      stack.push(fe);
    } else {
      VarType descType = VarType.ofDesc(desc);
      if (opcode == PUTSTATIC) {
        Expression value = descType.size() == 2 ? stack.pop2() : stack.pop();
        list.add(new FieldAssignExpression(new ClassDefinition(owner), name, descType, value));
      } else if (opcode == PUTFIELD) {
        Expression value = descType.size() == 2 ? stack.pop2() : stack.pop();
        list.add(new FieldAssignExpression(stack.pop(), name, descType, value));
      }
    }
  }

  private void visitMethodInsnNode(int opcode, String owner, String name, String desc) {
    ArrayList<Expression> args = new ArrayList<>();
    ArrayList<Integer> descSizes = DescUtils.getInnerDescSizes(desc);
    if (opcode == INVOKEVIRTUAL) { //for some reason invokevirtual reads stack as arg1, arg2.... and all other read as argN ... arg1
      Collections.reverse(descSizes);
    }
    for (int i : descSizes) {
      if (i == 1) {
        args.add(stack.pop());
      } else {
        args.add(stack.pop2());
      }
    }
    Collections.reverse(args);
    if (opcode == INVOKESTATIC) {
      MethodExpression me = new MethodExpression(new ClassDefinition(owner), name, args, VarType.ofDesc(desc));
      if (me.getReturnType() == VarType.VOID) {
        list.add(me);
      } else {
        stack.push(me);
      }
    } else if (opcode == INVOKEVIRTUAL || opcode == INVOKESPECIAL) {
      MethodExpression me = new MethodExpression(stack.pop(), name, args, VarType.ofDesc(desc));
      if (me.getReturnType() == VarType.VOID) {
        if (name.equals("<init>")) {
          if (stack.size() > 0) {
            Expression e = stack.peek();
            if (e instanceof NewTypeExpression) {
              NewTypeExpression nte = (NewTypeExpression) e;
              nte.setInit(me);
              return;
            }
          }
        }
        list.add(me);
      } else {
        stack.push(me);
      }
    } else if (opcode == INVOKEINTERFACE) {
      MethodExpression me = new MethodExpression(stack.pop(), name, args, VarType.ofDesc(desc));
      if (me.getReturnType() == VarType.VOID) {
        list.add(me);
      } else {
        stack.push(me);
      }
    }
  }

  private void visitLdcInsnNode(Object cst) {
    if (cst instanceof String) {
      stack.push(new StringExpression((String) cst));
    } else if (cst instanceof Integer) {
      stack.push(new ValueExpression(VarType.INT, cst));
    } else if (cst instanceof Float) {
      stack.push(new ValueExpression(VarType.FLOAT, cst));
    } else if (cst instanceof Long) {
      stack.push(new ValueExpression(VarType.LONG, cst));
    } else if (cst instanceof Double) {
      stack.push(new ValueExpression(VarType.DOUBLE, cst));
    } else if (cst instanceof Type) {
      Type t = (Type) cst;
      stack.push(new ClassTypeExpression(t.getClassName()));
    } else {
      throw new RuntimeException(cst.getClass().getName());
    }
  }

  private void visitVarInsnNode(int opc, int index) {
    Expression top;
    switch (opc) {
    case ALOAD:
      if (!AccessHelper.isStatic(mn.access) && index == 0) {
        stack.push(new VarSpecialExpression("this"));
        break;
      }
      stack.push(new VarLoadExpression(index, VarType.OBJECT));
      break;
    case ILOAD:
      stack.push(new VarLoadExpression(index, VarType.INT));
      break;
    case FLOAD:
      stack.push(new VarLoadExpression(index, VarType.FLOAT));
      break;
    case LLOAD:
      stack.push(new VarLoadExpression(index, VarType.LONG));
      break;
    case DLOAD:
      stack.push(new VarLoadExpression(index, VarType.DOUBLE));
      break;
    case ASTORE:
      top = stack.pop();
      if (top.size() != 1) {
        throw new StackException("wrong var store size: " + top.size());
      }
      list.add(new VarAssignExpression(index, VarType.OBJECT, top));
      break;
    case ISTORE:
      top = stack.pop();
      if (top.size() != 1) {
        throw new StackException("wrong var store size: " + top.size());
      }
      list.add(new VarAssignExpression(index, VarType.INT, top));
      break;
    case FSTORE:
      top = stack.pop();
      if (top.size() != 1) {
        throw new StackException("wrong var store size: " + top.size());
      }
      list.add(new VarAssignExpression(index, VarType.FLOAT, top));
      break;
    case LSTORE:
      top = stack.pop2();
      if (top.size() != 2) {
        throw new StackException("wrong var store size: " + top.size());
      }
      list.add(new VarAssignExpression(index, VarType.LONG, top));
      break;
    case DSTORE:
      top = stack.pop2();
      if (top.size() != 2) {
        throw new StackException("wrong var store size: " + top.size());
      }
      list.add(new VarAssignExpression(index, VarType.DOUBLE, top));
      break;
    default:
      throw new UnknownOPException(opc);
    }
  }

  private void visitInsnNode(int opc) {
    if (opc >= ACONST_NULL && opc <= DCONST_1) {
      insnPush(opc);
    } else if (opc >= POP && opc <= SWAP) {
      stackOp(opc);
    } else if (opc >= IADD && opc <= LXOR) {
      operation(opc);
    } else if (opc >= I2L && opc <= I2S) {
      conversion(opc);
    } else if (opc >= IRETURN && opc <= RETURN) {
      retValue(opc);
    } else if (opc == ATHROW) {
      athrow();
    } else if (opc >= IALOAD && opc <= SALOAD) {
      arrayLoad(opc);
    } else if (opc >= IASTORE && opc <= SASTORE) {
      arrayStore(opc);
    } else if (opc == ARRAYLENGTH) {
      visiFieldInsnNode(GETFIELD, null, "length", "I");
    } else if (opc == MONITORENTER || opc == MONITOREXIT) {
      list.add(new TextExpression(OpUtils.getOpcodeText(opc).toLowerCase()));
    } else if (opc == NOP) {
      //do nothing
    } else {
      //comparison nodes
      throw new RuntimeException(OpUtils.getOpcodeText(opc));
    }
  }

  private void arrayStore(int opc) {
    Expression value;
    if (opc == LASTORE || opc == DASTORE) {
      value = stack.pop2();
    } else {
      value = stack.pop();
    }
    Expression index = stack.pop();
    Expression array = stack.pop();
    list.add(new ArrayStoreExpression(array, index, value));
  }

  private void arrayLoad(int opc) {
    Expression index = stack.pop();
    Expression array = stack.pop();
    stack.push(new ArrayIndexExpression(array, index, getVarType(array)), opc == LALOAD || opc == DALOAD); //LALOAD and DALOAD handled automatically
  }

  private VarType getVarType(Expression array) {
    for (Field f : array.getClass().getDeclaredFields()) {
      if (f.getType() == VarType.class) {
        f.setAccessible(true);
        VarType type = null;
        try {
          type = (VarType) f.get(array);
        } catch (Exception e) {
        }
        if (type == null) {
          if (array instanceof CastExpression) {
            type = VarType.OBJECT;
          } else {
            throw new RuntimeException("Expression type is null (" + array.getClass().getName() + ")");
          }
        }
        return type;
      }
    }
    throw new RuntimeException("Expression doesn't have type (" + array.getClass().getName() + ")");
  }

  private void athrow() {
    list.add(new ThrowExpression(stack.pop()));
  }

  private void retValue(int opc) {
    switch (opc) {
    case IRETURN:
    case FRETURN:
    case ARETURN:
      list.add(new ReturnExpression(stack.pop()));
      break;
    case LRETURN:
    case DRETURN:
      list.add(new ReturnExpression(stack.pop2()));
      break;
    case RETURN:
      list.add(new ReturnExpression());
      break;
    }
  }

  private void conversion(int opc) {
    switch (opc) {
    case L2I:
    case D2I:
      stack.push(new CastExpression(VarType.INT, stack.pop2()));
      break;
    case F2I:
      stack.push(new CastExpression(VarType.INT, stack.pop()));
      break;
    case I2B:
    case I2C:
    case I2S:
      //they don't need conversion
      break;
    case I2F:
      stack.push(new CastExpression(VarType.FLOAT, stack.pop()));
      break;
    case L2F:
    case D2F:
      stack.push(new CastExpression(VarType.FLOAT, stack.pop2()));
      break;
    case I2L:
    case F2L:
      stack.push(new CastExpression(VarType.LONG, stack.pop()));
      break;
    case D2L:
      stack.push(new CastExpression(VarType.LONG, stack.pop2()));
      break;
    case I2D:
    case F2D:
      stack.push(new CastExpression(VarType.DOUBLE, stack.pop()));
      break;
    case L2D:
      stack.push(new CastExpression(VarType.DOUBLE, stack.pop2()));
      break;
    default:
      throw new UnknownOPException(opc);
    }
  }

  private void insnPush(int opc) {
    switch (opc) {
    case ACONST_NULL:
      stack.push(new NullExpression());
      break;
    case ICONST_0:
    case ICONST_1:
    case ICONST_2:
    case ICONST_3:
    case ICONST_4:
    case ICONST_5:
    case ICONST_M1:
      stack.push(new ValueExpression(VarType.INT, (int) (opc - 3)));
      break;
    case FCONST_0:
    case FCONST_1:
    case FCONST_2:
      stack.push(new ValueExpression(VarType.FLOAT, (float) (opc - 11f)));
      break;
    case DCONST_0:
    case DCONST_1:
      stack.push(new ValueExpression(VarType.DOUBLE, (double) (opc - 14d)));
      break;
    case LCONST_0:
    case LCONST_1:
      stack.push(new ValueExpression(VarType.LONG, (long) (opc - 9L)));
      break;
    default:
      throw new UnknownOPException(opc);
    }
  }

  private void stackOp(int opc) {
    switch (opc) {
    case POP:
      Expression pop = stack.pop();
      if (pop instanceof MethodExpression || pop instanceof NewArrayExpression) {
        list.add(pop); //expressions were invoked, they must be added to the output
      }
      break;
    case POP2:
      Expression pop2 = stack.pop2();
      if (pop2 instanceof MethodExpression) {
        list.add(pop2); //expressions were invoked, they must be added to the output
      }
      break;
    case DUP:
      stack.push(stack.peek().clone(), false);
      break;
    case DUP_X1:
      stack.push(stack.peek().clone(), 1, false);
      break;
    case DUP_X2:
      stack.push(stack.peek().clone(), 2, false);
      break;
    case DUP2:
      if (stack.peek().size() == 2) {
        stack.push(stack.peek().clone(), true);
      } else if (stack.peek().size() == 1 && stack.peek2().size() == 1) {
        Expression o1 = stack.peek();
        Expression o2 = stack.peek2();
        stack.push(o2.clone());
        stack.push(o1.clone());
      }
      break;
    case DUP2_X1:
      stack.push(stack.peek().clone(), 1, true);
      break;
    case DUP2_X2:
      stack.push(stack.peek().clone(), 2, true);
      break;
    case SWAP:
      if (stack.peek().size() == 2) {
        break;
      } else if (stack.peek().size() == 1 && stack.peek2().size() == 1) {
        Expression o1 = stack.pop();
        Expression o2 = stack.pop();
        stack.push(o1);
        stack.push(o2);
      } else {
        throw new StackException("cannot swap");
      }
      break;
    default:
      throw new UnknownOPException(opc);
    }
  }

  private void operation(int opc) {
    Expression e1;
    Expression e2;

    switch (opc) {
    case IADD:
    case ISUB:
    case IMUL:
    case IDIV:
    case IREM:
    case ISHL:
    case ISHR:
    case IUSHR:
    case IAND:
    case IOR:
    case IXOR:
      e1 = stack.pop();
      e2 = stack.pop();
      stack.push(new OpExpression(e2, e1, Operation.of(opc), VarType.INT));
      break;
    case FADD:
    case FSUB:
    case FMUL:
    case FDIV:
    case FREM:
      e1 = stack.pop();
      e2 = stack.pop();
      stack.push(new OpExpression(e2, e1, Operation.of(opc), VarType.FLOAT));
      break;
    case LADD:
    case LSUB:
    case LMUL:
    case LDIV:
    case LREM:
    case LAND:
    case LOR:
    case LXOR:
      e1 = stack.pop2();
      e2 = stack.pop2();
      stack.push(new OpExpression(e2, e1, Operation.of(opc), VarType.LONG));
      break;
    case LSHL:
    case LSHR:
    case LUSHR:
      e1 = stack.pop();
      e2 = stack.pop2();
      stack.push(new OpExpression(e2, e1, Operation.of(opc), VarType.LONG));
      break;
    case DADD:
    case DSUB:
    case DMUL:
    case DDIV:
    case DREM:
      e1 = stack.pop2();
      e2 = stack.pop2();
      stack.push(new OpExpression(e2, e1, Operation.of(opc), VarType.DOUBLE));
      break;
    case INEG:
      e1 = stack.pop();
      stack.push(new SingleOpExpression(e1, Operation.of(opc), VarType.INT));
      break;
    case FNEG:
      e1 = stack.pop();
      stack.push(new SingleOpExpression(e1, Operation.of(opc), VarType.FLOAT));
      break;
    case LNEG:
      e1 = stack.pop2();
      stack.push(new SingleOpExpression(e1, Operation.of(opc), VarType.LONG));
      break;
    case DNEG:
      e1 = stack.pop2();
      stack.push(new SingleOpExpression(e1, Operation.of(opc), VarType.DOUBLE));
      break;
    default:
      throw new UnknownOPException(opc);
    }
  }

  public JVMStack getStack() {
    return stack;
  }
}
