package me.lpk.analysis;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.IincInsnNode;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LookupSwitchInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MultiANewArrayInsnNode;
import org.objectweb.asm.tree.TableSwitchInsnNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.Frame;
import org.objectweb.asm.tree.analysis.Interpreter;
import org.objectweb.asm.tree.analysis.Value;

import me.lpk.log.Logger;
import me.lpk.util.OpUtils;

/**
 * @editor Matt
 */
@SuppressWarnings("all")
public class StackFrame extends Frame {
	public AbstractInsnNode ain;
	public LabelNode jin;
	public boolean doJump;

	public StackFrame(Frame src, AbstractInsnNode ain) {
		super(src);
		this.ain = ain;
	}

	public StackFrame(int nLocals, int nStack) {
		super(nLocals, nStack);
		this.ain = null;
	}
	
	public FrameNode toFrame(){
		List stack = new ArrayList();
		for (int s = 0; s < this.getStackSize(); s++){
			stack.add(this.getStack(s));
		}
		List locals = new ArrayList();
		for (int l = 0; l< this.locals; l++){
			stack.add(this.getLocal(l));
		}
		return new FrameNode(Opcodes.F_FULL, stack.size(),stack.toArray(), locals.size(), locals.toArray());
	}

	public void execute(final AbstractInsnNode insn, final StackHelper interpreter) throws AnalyzerException {
		InsnValue value1, value2, value3, value4;
		List values;
		int var;
		doJump = false;
		switch (insn.getOpcode()) {
		case Opcodes.NOP:
			break;
		case Opcodes.ACONST_NULL:
		case Opcodes.ICONST_M1:
		case Opcodes.ICONST_0:
		case Opcodes.ICONST_1:
		case Opcodes.ICONST_2:
		case Opcodes.ICONST_3:
		case Opcodes.ICONST_4:
		case Opcodes.ICONST_5:
		case Opcodes.LCONST_0:
		case Opcodes.LCONST_1:
		case Opcodes.FCONST_0:
		case Opcodes.FCONST_1:
		case Opcodes.FCONST_2:
		case Opcodes.DCONST_0:
		case Opcodes.DCONST_1:
		case Opcodes.BIPUSH:
		case Opcodes.SIPUSH:
		case Opcodes.LDC:
			push(interpreter.createConstant(insn));
			break;
		case Opcodes.ILOAD:
		case Opcodes.LLOAD:
		case Opcodes.FLOAD:
		case Opcodes.DLOAD:
		case Opcodes.ALOAD:
			push(interpreter.loadLocal(insn, getLocal(((VarInsnNode) insn).var)));
			break;
		case Opcodes.IALOAD:
		case Opcodes.LALOAD:
		case Opcodes.FALOAD:
		case Opcodes.DALOAD:
		case Opcodes.AALOAD:
		case Opcodes.BALOAD:
		case Opcodes.CALOAD:
		case Opcodes.SALOAD:
			value2 = pop();
			value1 = pop();
			push(interpreter.loadFromArray(insn, value1, value2));
			break;
		case Opcodes.ISTORE:
		case Opcodes.LSTORE:
		case Opcodes.FSTORE:
		case Opcodes.DSTORE:
		case Opcodes.ASTORE:
			value1 = pop();
			value1 = interpreter.loadLocal(insn, value1);
			var = ((VarInsnNode) insn).var;
			setLocal(var, value1);
			if (value1.getSize() == 2) {
				setLocal(var + 1, interpreter.newValue(null));
			}
			if (var > 0) {
				Value local = getLocal(var - 1);
				if (local != null && local.getSize() == 2) {
					setLocal(var - 1, interpreter.newValue(null));
				}
			}
			break;
		case Opcodes.IASTORE:
		case Opcodes.LASTORE:
		case Opcodes.FASTORE:
		case Opcodes.DASTORE:
		case Opcodes.AASTORE:
		case Opcodes.BASTORE:
		case Opcodes.CASTORE:
		case Opcodes.SASTORE:
			value3 = pop();
			value2 = pop();
			value1 = pop();
			String before = value1.toString();
			// arrayRef, index, value)
			value1 = interpreter.storeInArray(insn, value1, value2, value3);
			if (value1 != null) {
				Logger.logVeryHigh("\tUpdated array value: " + before + " --> " + value1);
			} else {
				Logger.errVeryHigh("\tFailed updating array value: " + before + " --> " + value1);
			}
			break;
		case Opcodes.POP:
			if (pop().getSize() == 2) {
				throw new AnalyzerException(insn, "Illegal use of POP");
			}
			break;
		case Opcodes.POP2:
			if (pop().getSize() == 1) {
				if (pop().getSize() != 1) {
					throw new AnalyzerException(insn, "Illegal use of POP2");
				}
			}
			break;
		case Opcodes.DUP:
			value1 = pop();
			if (value1.getSize() != 1) {
				throw new AnalyzerException(insn, "Illegal use of DUP");
			}
			push(value1);
			push(interpreter.loadLocal(insn, value1));
			break;
		case Opcodes.DUP_X1:
			value1 = pop();
			value2 = pop();
			if (value1.getSize() != 1 || value2.getSize() != 1) {
				throw new AnalyzerException(insn, "Illegal use of DUP_X1");
			}
			push(interpreter.loadLocal(insn, value1));
			push(value2);
			push(value1);
			break;
		case Opcodes.DUP_X2:
			value1 = pop();
			if (value1.getSize() == 1) {
				value2 = pop();
				if (value2.getSize() == 1) {
					value3 = pop();
					if (value3.getSize() == 1) {
						push(interpreter.loadLocal(insn, value1));
						push(value3);
						push(value2);
						push(value1);
						break;
					}
				} else {
					push(interpreter.loadLocal(insn, value1));
					push(value2);
					push(value1);
					break;
				}
			}
			throw new AnalyzerException(insn, "Illegal use of DUP_X2");
		case Opcodes.DUP2:
			value1 = pop();
			if (value1.getSize() == 1) {
				value2 = pop();
				if (value2.getSize() == 1) {
					push(value2);
					push(value1);
					push(interpreter.loadLocal(insn, value2));
					push(interpreter.loadLocal(insn, value1));
					break;
				}
			} else {
				push(value1);
				push(interpreter.loadLocal(insn, value1));
				break;
			}
			throw new AnalyzerException(insn, "Illegal use of DUP2");
		case Opcodes.DUP2_X1:
			value1 = pop();
			if (value1.getSize() == 1) {
				value2 = pop();
				if (value2.getSize() == 1) {
					value3 = pop();
					if (value3.getSize() == 1) {
						push(interpreter.loadLocal(insn, value2));
						push(interpreter.loadLocal(insn, value1));
						push(value3);
						push(value2);
						push(value1);
						break;
					}
				}
			} else {
				value2 = pop();
				if (value2.getSize() == 1) {
					push(interpreter.loadLocal(insn, value1));
					push(value2);
					push(value1);
					break;
				}
			}
			throw new AnalyzerException(insn, "Illegal use of DUP2_X1");
		case Opcodes.DUP2_X2:
			value1 = pop();
			if (value1.getSize() == 1) {
				value2 = pop();
				if (value2.getSize() == 1) {
					value3 = pop();
					if (value3.getSize() == 1) {
						value4 = pop();
						if (value4.getSize() == 1) {
							push(interpreter.loadLocal(insn, value2));
							push(interpreter.loadLocal(insn, value1));
							push(value4);
							push(value3);
							push(value2);
							push(value1);
							break;
						}
					} else {
						push(interpreter.loadLocal(insn, value2));
						push(interpreter.loadLocal(insn, value1));
						push(value3);
						push(value2);
						push(value1);
						break;
					}
				}
			} else {
				value2 = pop();
				if (value2.getSize() == 1) {
					value3 = pop();
					if (value3.getSize() == 1) {
						push(interpreter.loadLocal(insn, value1));
						push(value3);
						push(value2);
						push(value1);
						break;
					}
				} else {
					push(interpreter.loadLocal(insn, value1));
					push(value2);
					push(value1);
					break;
				}
			}
			throw new AnalyzerException(insn, "Illegal use of DUP2_X2");
		case Opcodes.SWAP:
			value2 = pop();
			value1 = pop();
			if (value1.getSize() != 1 || value2.getSize() != 1) {
				throw new AnalyzerException(insn, "Illegal use of SWAP");
			}
			push(interpreter.loadLocal(insn, value2));
			push(interpreter.loadLocal(insn, value1));
			break;
		case Opcodes.IADD:
		case Opcodes.LADD:
		case Opcodes.FADD:
		case Opcodes.DADD:
		case Opcodes.ISUB:
		case Opcodes.LSUB:
		case Opcodes.FSUB:
		case Opcodes.DSUB:
		case Opcodes.IMUL:
		case Opcodes.LMUL:
		case Opcodes.FMUL:
		case Opcodes.DMUL:
		case Opcodes.IDIV:
		case Opcodes.LDIV:
		case Opcodes.FDIV:
		case Opcodes.DDIV:
		case Opcodes.IREM:
		case Opcodes.LREM:
		case Opcodes.FREM:
		case Opcodes.DREM:
		case Opcodes.ISHL:
		case Opcodes.LSHL:
		case Opcodes.ISHR:
		case Opcodes.LSHR:
		case Opcodes.IUSHR:
		case Opcodes.LUSHR:
		case Opcodes.IAND:
		case Opcodes.LAND:
		case Opcodes.IOR:
		case Opcodes.LOR:
		case Opcodes.IXOR:
		case Opcodes.LXOR:
			value2 = pop();
			value1 = pop();
			push(interpreter.doMath(insn, value1, value2));
			break;
		case Opcodes.INEG:
		case Opcodes.LNEG:
		case Opcodes.FNEG:
		case Opcodes.DNEG:
			push(interpreter.invertValue(insn, pop()));
			break;
		case Opcodes.IINC:
			IincInsnNode iinc = (IincInsnNode) insn;
			var = iinc.var;
			setLocal(var, interpreter.incrementLocal(iinc, getLocal(var)));
			break;
		case Opcodes.I2L:
		case Opcodes.I2F:
		case Opcodes.I2D:
		case Opcodes.L2I:
		case Opcodes.L2F:
		case Opcodes.L2D:
		case Opcodes.F2I:
		case Opcodes.F2L:
		case Opcodes.F2D:
		case Opcodes.D2I:
		case Opcodes.D2L:
		case Opcodes.D2F:
		case Opcodes.I2B:
		case Opcodes.I2C:
		case Opcodes.I2S:
			push(interpreter.convertValue(insn, pop()));
			break;
		case Opcodes.LCMP:
		case Opcodes.FCMPL:
		case Opcodes.FCMPG:
		case Opcodes.DCMPL:
		case Opcodes.DCMPG:
			value2 = pop();
			value1 = pop();
			push(interpreter.compareConstants(insn, value1, value2));
			break;
		case Opcodes.IFEQ:
		case Opcodes.IFNE:
		case Opcodes.IFLT:
		case Opcodes.IFGE:
		case Opcodes.IFGT:
		case Opcodes.IFLE:
			InsnValue unaryIf = interpreter.compareConstant(insn, pop());
			if (unaryIf.getValue() != null) {
				if (getInt(unaryIf.getValue()) == 1) {
					doJump = true;
					jin = ((JumpInsnNode) insn).label;
				}
			}
			break;
		case Opcodes.IF_ICMPEQ:
		case Opcodes.IF_ICMPNE:
		case Opcodes.IF_ICMPLT:
		case Opcodes.IF_ICMPGE:
		case Opcodes.IF_ICMPGT:
		case Opcodes.IF_ICMPLE:
		case Opcodes.IF_ACMPEQ:
		case Opcodes.IF_ACMPNE:
			value2 = pop();
			value1 = pop();
			InsnValue binaryIf = interpreter.compareConstants(insn, value1, value2);
			if (binaryIf.getValue() != null) {
				if (getInt(binaryIf.getValue()) == 1) {
					doJump = true;
					jin = ((JumpInsnNode) insn).label;
				}
			}
			break;
		case Opcodes.IFNULL:
		case Opcodes.IFNONNULL:
			InsnValue nullCheck = interpreter.checkNull(insn, pop());
			if (nullCheck.getValue() != null) {
				if (getInt(nullCheck.getValue()) == 1) {
					doJump = true;
					jin = ((JumpInsnNode) insn).label;
				}
			}
			break;
		case Opcodes.GOTO:
			jin = ((JumpInsnNode) insn).label;
			doJump = true;
			break;
		case Opcodes.JSR:
			push(interpreter.createConstant(insn));
			jin = ((JumpInsnNode) insn).label;
			doJump = true;
			break;
		case Opcodes.RET:
			break;
		case Opcodes.TABLESWITCH:
		case Opcodes.LOOKUPSWITCH:
			InsnValue switchValue = interpreter.getSwitchValue(insn, pop());
			int index = (int) switchValue.getValue();
			if (insn.getOpcode() == Opcodes.TABLESWITCH) {
				TableSwitchInsnNode tsin = (TableSwitchInsnNode) insn;
				jin = index == -1 ? tsin.dflt : tsin.labels.get(index);
			} else {
				LookupSwitchInsnNode lsin = (LookupSwitchInsnNode) insn;
				jin = index == -1 ? lsin.dflt : lsin.labels.get(index);
			}
			doJump = true;
			break;
		case Opcodes.IRETURN:
		case Opcodes.LRETURN:
		case Opcodes.FRETURN:
		case Opcodes.DRETURN:
		case Opcodes.ARETURN:
		case Opcodes.RETURN:
			if (getStackSize() > 0) {
				returnValue = pop();
			}
			break;
		case Opcodes.GETSTATIC:
			push(interpreter.getStatic((FieldInsnNode) insn));
			break;
		case Opcodes.PUTSTATIC:
			interpreter.putStatic((FieldInsnNode) insn, pop());
			break;
		case Opcodes.GETFIELD:
			push(interpreter.getField((FieldInsnNode) insn, pop()));
			break;
		case Opcodes.PUTFIELD:
			value2 = pop();
			value1 = pop();
			interpreter.putField((FieldInsnNode) insn, value1, value2);
			break;
		case Opcodes.INVOKEVIRTUAL:
		case Opcodes.INVOKESPECIAL:
		case Opcodes.INVOKESTATIC:
		case Opcodes.INVOKEINTERFACE: {
			// Brackets are here to prevent local variable name conflicts.
			values = new ArrayList<Value>();
			String desc = ((MethodInsnNode) insn).desc;
			for (int args = Type.getArgumentTypes(desc).length; args > 0; --args) {
				values.add(0, pop());
			}
			if (insn.getOpcode() != Opcodes.INVOKESTATIC) {
				values.add(0, pop());
			}
			if (Type.getReturnType(desc).equals(Type.VOID_TYPE)) {
				interpreter.onMethod(insn, values);
			} else {
				push(interpreter.onMethod(insn, values));
			}
			break;
		}
		case Opcodes.INVOKEDYNAMIC: {
			values = new ArrayList();
			String desc = ((InvokeDynamicInsnNode) insn).desc;
			for (int i = Type.getArgumentTypes(desc).length; i > 0; --i) {
				values.add(0, pop());
			}
			if (Type.getReturnType(desc) == Type.VOID_TYPE) {
				interpreter.onMethod(insn, values);
			} else {
				push(interpreter.onMethod(insn, values));
			}
			break;
		}
		case Opcodes.NEW:
			push(interpreter.createConstant(insn));
			break;
		case Opcodes.NEWARRAY:
		case Opcodes.ANEWARRAY:
		case Opcodes.ARRAYLENGTH:
			push(interpreter.array(insn, pop()));
			break;
		case Opcodes.ATHROW:
			interpreter.throwException(insn, pop());
			break;
		case Opcodes.CHECKCAST:
		case Opcodes.INSTANCEOF:
			push(interpreter.casting((TypeInsnNode) insn, pop()));
			break;
		case Opcodes.MONITORENTER:
		case Opcodes.MONITOREXIT:
			interpreter.monitor(insn, pop());
			break;
		case Opcodes.MULTIANEWARRAY:
			values = new ArrayList();
			for (int i = ((MultiANewArrayInsnNode) insn).dims; i > 0; --i) {
				values.add(0, pop());
			}
			push(interpreter.onMultiANewArray((MultiANewArrayInsnNode) insn, values));
			break;
		default:
			throw new RuntimeException("Illegal opcode " + insn.getOpcode());
		}
	}

	public StackFrame init(StackFrame src) {
		returnValue = src.returnValue;
		System.arraycopy(src.values, 0, values, 0, values.length);
		top = src.top;
		return this;
	}

	@Override
	public InsnValue getLocal(int i) {
		return (InsnValue) super.getLocal(i);
	}

	@Override
	public InsnValue pop() {
		return (InsnValue) super.pop();
	}

	private int getInt(Object value) {
		try {
			int i = Integer.parseInt(value.toString());
			return i;
		} catch (NumberFormatException nfe) {

		}
		return -1;
	}
}
