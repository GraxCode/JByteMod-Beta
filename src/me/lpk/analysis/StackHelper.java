package me.lpk.analysis;

import java.util.List;

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
import org.objectweb.asm.tree.MultiANewArrayInsnNode;
import org.objectweb.asm.tree.TableSwitchInsnNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.Value;

public class StackHelper {

	public InsnValue createConstant(AbstractInsnNode insn) throws AnalyzerException {
		switch (insn.getOpcode()) {
		case Opcodes.ACONST_NULL:
			return InsnValue.NULL_REFERENCE_VALUE;
		case Opcodes.ICONST_M1:
		case Opcodes.ICONST_0:
		case Opcodes.ICONST_1:
		case Opcodes.ICONST_2:
		case Opcodes.ICONST_3:
		case Opcodes.ICONST_4:
		case Opcodes.ICONST_5:
		case Opcodes.BIPUSH:
		case Opcodes.SIPUSH:
			return InsnValue.intValue(insn);
		case Opcodes.LCONST_0:
		case Opcodes.LCONST_1:
			return InsnValue.longValue(insn.getOpcode());
		case Opcodes.FCONST_0:
		case Opcodes.FCONST_1:
		case Opcodes.FCONST_2:
			return InsnValue.floatValue(insn.getOpcode());
		case Opcodes.DCONST_0:
		case Opcodes.DCONST_1:
			return InsnValue.doubleValue(insn.getOpcode());
		case Opcodes.LDC:
			Object obj = ((LdcInsnNode) insn).cst;
			if (obj instanceof Type) {
				return new InsnValue((Type) obj);
			} else {
				Type t = Type.getType(obj.getClass());
				int sort = t.getSort();
				// Non-included types:
				// Type.ARRAY
				// Type.VOID
				// Type.METHOD
				switch (sort) {
				case Type.BOOLEAN:
					return InsnValue.intValue((int) obj);
				case Type.CHAR:
					return InsnValue.charValue((char) obj);
				case Type.BYTE:
					return InsnValue.byteValue((byte) obj);
				case Type.SHORT:
					return InsnValue.shortValue((short) obj);
				case Type.INT:
					return InsnValue.intValue((int) obj);
				case Type.FLOAT:
					return InsnValue.floatValue((float) obj);
				case Type.LONG:
					return InsnValue.longValue((long) obj);
				case Type.DOUBLE:
					return InsnValue.doubleValue((double) obj);
				case Type.OBJECT:
					return new InsnValue(t, obj);
				}
				return new InsnValue(t);
			}
		case Opcodes.NEW:
			return new InsnValue(Type.getType(((TypeInsnNode) insn).desc));
		case Opcodes.JSR:
			// TODO: IDK if this is right.
			return InsnValue.REFERENCE_VALUE;
		}

		return null;
	}

	public InsnValue loadLocal(AbstractInsnNode insn, InsnValue value) throws AnalyzerException {
		// Pretty sure with how this is called nothing needs to be done...
		return value;
	}

	public InsnValue loadFromArray(AbstractInsnNode insn, InsnValue value1, InsnValue indexValue) throws AnalyzerException {
		Object indexObj = indexValue.getValue(), arrayObj = value1.getValue();
		int index = indexObj == null ? -1 : ((Number) indexObj).intValue();
		boolean arrayNotSimulated = arrayObj == null;
		Type t = arrayNotSimulated ? null : Type.getType(arrayObj.getClass());
		switch (insn.getOpcode()) {
		case Opcodes.IALOAD:
			if (arrayNotSimulated) {
				return InsnValue.INT_VALUE;
			}
			// Sometimes Object[] contain values. Stringer obfuscator does this.
			// TODO: Have this sort of check for others?
			boolean oarr = t.equals(InsnValue.REFERENCE_ARR_VALUE.getType());
			if (oarr) {
				int[] ia = (int[]) arrayObj;
				return InsnValue.intValue(ia[index]);
			}else{
				Object[] obarr = (Object[]) arrayObj;
				Object o = obarr[index];
				if (o == null){
					return InsnValue.INT_VALUE;
				}
				return InsnValue.intValue(((Number) o).intValue());
			}
		case Opcodes.LALOAD:
			if (arrayNotSimulated) {
				return InsnValue.LONG_VALUE;
			}
			long[] la = (long[]) arrayObj;
			return InsnValue.longValue(la[index]);
		case Opcodes.FALOAD:
			if (arrayNotSimulated) {
				return InsnValue.FLOAT_VALUE;
			}
			float[] fa = (float[]) arrayObj;
			return InsnValue.floatValue(fa[index]);
		case Opcodes.DALOAD:
			if (arrayNotSimulated) {
				return InsnValue.DOUBLE_VALUE;
			}
			double[] da = (double[]) arrayObj;
			return InsnValue.doubleValue(da[index]);
		case Opcodes.AALOAD:
			// TODO: Check if it's an object array, but the contents aren't objects
			return InsnValue.REFERENCE_VALUE;
		case Opcodes.BALOAD:
			if (arrayNotSimulated) {
				return InsnValue.BYTE_VALUE;
			}
			boolean db = t.equals(InsnValue.DOUBLE_ARR_VALUE.getType()), in = t.equals(InsnValue.INT_ARR_VALUE.getType()), saa = t.equals(InsnValue.SHORT_ARR_VALUE.getType());
			if (db) {
				double[] dba = (double[]) arrayObj;
				return InsnValue.intValue(dba[index]);
			} else if (in) {
				int[] ina = (int[]) arrayObj;
				return InsnValue.intValue(ina[index]);
			} else if (saa){
				short[] saaa = (short[]) arrayObj;
				return InsnValue.intValue(saaa[index]);
			} else {
				System.err.println("UNKNOWN TYPE BALOAD: " + t);
				throw new RuntimeException();
			}

		case Opcodes.CALOAD:
			if (arrayNotSimulated) {
				return InsnValue.CHAR_VALUE;
			}
			char[] ca = (char[]) arrayObj;
			return InsnValue.charValue(ca[index]);
		case Opcodes.SALOAD:
			if (arrayNotSimulated) {
				return InsnValue.SHORT_VALUE;
			}
			short[] sa = (short[]) arrayObj;
			return InsnValue.intValue((short) sa[index]);
		}

		return null;
	}

	public InsnValue doMath(AbstractInsnNode insn, InsnValue value1, InsnValue value2) {
		Object o1 = value1.getValue(), o2 = value2.getValue();
		switch (insn.getOpcode()) {
		case Opcodes.IADD:
		case Opcodes.ISUB:
		case Opcodes.IMUL:
		case Opcodes.IDIV:
		case Opcodes.IREM:
		case Opcodes.ISHL:
		case Opcodes.ISHR:
		case Opcodes.IUSHR:
		case Opcodes.IAND:
		case Opcodes.IOR:
		case Opcodes.IXOR:
			if (o1 == null || o2 == null) {
				return InsnValue.INT_VALUE;
			}
			int i1 = ((Number) o1).intValue(), i2 = ((Number) o2).intValue();
			switch (insn.getOpcode()) {
			case Opcodes.IADD:
				return InsnValue.intValue(i1 + i2);
			case Opcodes.ISUB:
				return InsnValue.intValue(i1 - i2);
			case Opcodes.IMUL:
				return InsnValue.intValue(i1 * i2);
			case Opcodes.IDIV:
				return InsnValue.intValue(i1 / i2);
			case Opcodes.IREM:
				return InsnValue.intValue(i1 % i2);
			case Opcodes.ISHL:
				return InsnValue.intValue(i1 << i2);
			case Opcodes.ISHR:
				return InsnValue.intValue(i1 >> i2);
			case Opcodes.IUSHR:
				return InsnValue.intValue(i1 >>> i2);
			case Opcodes.IAND:
				return InsnValue.intValue(i1 & i2);
			case Opcodes.IOR:
				return InsnValue.intValue(i1 | i2);
			case Opcodes.IXOR:
				return InsnValue.intValue(i1 ^ i2);
			}
		case Opcodes.LADD:
		case Opcodes.LSUB:
		case Opcodes.LMUL:
		case Opcodes.LDIV:
		case Opcodes.LREM:
		case Opcodes.LSHL:
		case Opcodes.LSHR:
		case Opcodes.LUSHR:
		case Opcodes.LAND:
		case Opcodes.LOR:
		case Opcodes.LXOR:
			if (o1 == null || o2 == null) {
				return InsnValue.LONG_VALUE;
			}
			long l1 = ((Number) o1).longValue(), l2 = ((Number) o2).longValue();
			switch (insn.getOpcode()) {
			case Opcodes.LADD:
				return InsnValue.longValue(l1 + l2);
			case Opcodes.LSUB:
				return InsnValue.longValue(l1 - l2);
			case Opcodes.LMUL:
				return InsnValue.longValue(l1 * l2);
			case Opcodes.LDIV:
				return InsnValue.longValue(l1 / l2);
			case Opcodes.LREM:
				return InsnValue.longValue(l1 % l2);
			case Opcodes.LSHL:
				return InsnValue.longValue(l1 << l2);
			case Opcodes.LSHR:
				return InsnValue.longValue(l1 >> l2);
			case Opcodes.LUSHR:
				return InsnValue.longValue(l1 >>> l2);
			case Opcodes.LAND:
				return InsnValue.longValue(l1 & l2);
			case Opcodes.LOR:
				return InsnValue.longValue(l1 | l2);
			case Opcodes.LXOR:
				return InsnValue.longValue(l1 ^ l2);
			}
		case Opcodes.FADD:
		case Opcodes.FSUB:
		case Opcodes.FMUL:
		case Opcodes.FDIV:
		case Opcodes.FREM:
			if (o1 == null || o2 == null) {
				return InsnValue.FLOAT_VALUE;
			}
			float f1 = (float) o1, f2 = (float) o2;
			switch (insn.getOpcode()) {
			case Opcodes.FADD:
				return InsnValue.floatValue(f1 + f2);
			case Opcodes.FSUB:
				return InsnValue.floatValue(f1 - f2);
			case Opcodes.FMUL:
				return InsnValue.floatValue(f1 * f2);
			case Opcodes.FDIV:
				return InsnValue.floatValue(f1 / f2);
			case Opcodes.FREM:
				return InsnValue.floatValue(f1 % f2);
			}
		case Opcodes.DADD:
		case Opcodes.DSUB:
		case Opcodes.DMUL:
		case Opcodes.DDIV:
		case Opcodes.DREM:
			if (o1 == null || o2 == null) {
				return InsnValue.DOUBLE_VALUE;
			}
			double d1 = (double) o1, d2 = (double) o2;
			switch (insn.getOpcode()) {
			case Opcodes.DADD:
				return InsnValue.doubleValue(d1 + d2);
			case Opcodes.DSUB:
				return InsnValue.doubleValue(d1 - d2);
			case Opcodes.DMUL:
				return InsnValue.doubleValue(d1 * d2);
			case Opcodes.DDIV:
				return InsnValue.doubleValue(d1 / d2);
			case Opcodes.DREM:
				return InsnValue.doubleValue(d1 % d2);
			}
		}

		return null;
	}

	public InsnValue storeInArray(AbstractInsnNode insn, InsnValue arrayRef, InsnValue index, InsnValue value) throws AnalyzerException {
		boolean anythingNull = arrayRef.getValue() == null || index.getValue() == null || value.getValue() == null;
		int i = anythingNull ? -1 : ((Number) index.getValue()).intValue();
		switch (insn.getOpcode()) {
		case Opcodes.IASTORE:
		case Opcodes.CASTORE:
			if (anythingNull) {
				return InsnValue.INT_ARR_VALUE;
			}
			if (arrayRef.getType().equals(InsnValue.INT_ARR_VALUE.getType())) {
				// Unsure why arrayRef when IASTORE isn't always [I
				int[] ia = (int[]) arrayRef.getValue();
				ia[i] = (int) value.getValue();
				return new InsnValue(InsnValue.INT_ARR_VALUE.getType(), ia);
			} else {
				return InsnValue.INT_ARR_VALUE;
			}

		case Opcodes.LASTORE:
			if (anythingNull) {
				return InsnValue.LONG_ARR_VALUE;
			}
			long[] la = (long[]) arrayRef.getValue();
			la[i] = (long) value.getValue();
			return new InsnValue(InsnValue.LONG_ARR_VALUE.getType(), la);
		case Opcodes.FASTORE:
			if (anythingNull) {
				return InsnValue.FLOAT_ARR_VALUE;
			}
			float[] fa = (float[]) arrayRef.getValue();
			fa[i] = (float) value.getValue();
			return new InsnValue(InsnValue.FLOAT_ARR_VALUE.getType(), fa);
		case Opcodes.DASTORE:
			if (anythingNull) {
				return InsnValue.DOUBLE_ARR_VALUE;
			}
			double[] da = (double[]) arrayRef.getValue();
			da[i] = ((Number) value.getValue()).doubleValue();
			return new InsnValue(InsnValue.DOUBLE_ARR_VALUE.getType(), da);
		case Opcodes.BASTORE:
			if (anythingNull) {
				return InsnValue.DOUBLE_ARR_VALUE;
			}
			Type t = Type.getType(arrayRef.getValue().getClass());
			boolean db = t.equals(InsnValue.DOUBLE_ARR_VALUE.getType()), in = t.equals(InsnValue.INT_ARR_VALUE.getType()), saaa = t.equals(InsnValue.SHORT_ARR_VALUE.getType());
			if (db) {
				double[] da2 = (double[]) arrayRef.getValue();
				da2[i] = ((Number) value.getValue()).doubleValue();
				return new InsnValue(InsnValue.DOUBLE_ARR_VALUE.getType(), da2);
			} else if (in) {
				int[] ia = (int[]) arrayRef.getValue();
				ia[i] = (int) value.getValue();
				return new InsnValue(InsnValue.INT_ARR_VALUE.getType(), ia);
			} else if (saaa) {
				short[] saa = (short[]) arrayRef.getValue();
				saa[i] = ((Number)value.getValue()).shortValue();
				return new InsnValue(InsnValue.SHORT_ARR_VALUE.getType(), saa);
			} else {
				System.err.println("UNKNOWN BASTORE TYPE: " + t);
				throw new RuntimeException();
			}
		case Opcodes.SASTORE:
			if (anythingNull) {
				return InsnValue.SHORT_VALUE;
			}
			short[] sa = (short[]) arrayRef.getValue();
			sa[i] = (short) value.getValue();
			return new InsnValue(InsnValue.SHORT_ARR_VALUE.getType(), sa);
		case Opcodes.AASTORE:
			return InsnValue.REFERENCE_ARR_VALUE;

		// Can't exactly cast anything to Object[] willy nilly...
		//
		// Object[] aa = (Object[]) arrayRef.getValue();
		//
		// aa[i] = value.getValue();
		//
		// return new InsnValue(InsnValue.(Find the type).getType(), ca);
		}

		return null;
	}

	public InsnValue incrementLocal(IincInsnNode iinc, InsnValue value) {
		Object obj = value.getValue();
		if (obj == null) {
			return newValue(value.getType());
		}
		if (value.getType().equals(Type.BYTE_TYPE)) {
			return InsnValue.byteValue((byte) (((Number) obj).byteValue() + iinc.incr));
		} else if (value.getType().equals(Type.INT_TYPE)) {
			return InsnValue.byteValue((int) (((Number) obj).intValue() + iinc.incr));
		} else if (value.getType().equals(Type.CHAR_TYPE)) {
			return InsnValue.charValue((char) (((Number) obj).intValue() + iinc.incr));
		} else if (value.getType().equals(Type.LONG_TYPE)) {
			return InsnValue.longValue((long) (((Number) obj).longValue() + iinc.incr));
		} else if (value.getType().equals(Type.DOUBLE_TYPE)) {
			return InsnValue.doubleValue((double) (((Number) obj).doubleValue() + iinc.incr));
		} else if (value.getType().equals(Type.FLOAT_TYPE)) {
			return InsnValue.floatValue((float) (((Number) obj).floatValue() + iinc.incr));
		} else if (value.getType().equals(Type.SHORT_TYPE)) {
			return InsnValue.shortValue((short) (((Number) obj).shortValue() + iinc.incr));
		}

		return null;
	}

	public InsnValue convertValue(AbstractInsnNode insn, InsnValue value) {
		Object obj = value.getValue();
		switch (insn.getOpcode()) {
		case Opcodes.I2L:
		case Opcodes.D2L:
		case Opcodes.F2L:
			if (obj == null) {
				return InsnValue.LONG_VALUE;
			}
			return InsnValue.longValue(((Number) obj).longValue());
		case Opcodes.I2F:
		case Opcodes.L2F:
		case Opcodes.D2F:
			if (obj == null) {
				return InsnValue.FLOAT_VALUE;
			}
			return InsnValue.floatValue(((Number) obj).floatValue());
		case Opcodes.L2I:
		case Opcodes.F2I:
		case Opcodes.D2I:
			if (obj == null) {
				return InsnValue.INT_VALUE;
			}
			return InsnValue.intValue(((Number) obj).intValue());
		case Opcodes.I2D:
		case Opcodes.L2D:
		case Opcodes.F2D:
			if (obj == null) {
				return InsnValue.DOUBLE_VALUE;
			}
			return InsnValue.doubleValue(((Number) obj).doubleValue());
		case Opcodes.I2B:
			if (obj == null) {
				return InsnValue.BYTE_VALUE;
			}
			return InsnValue.byteValue(((Number) obj).byteValue());
		case Opcodes.I2C:
			if (obj == null) {
				return InsnValue.CHAR_VALUE;
			}
			return InsnValue.charValue(((Number) obj).intValue());
		case Opcodes.I2S:
			if (obj == null) {
				return InsnValue.SHORT_VALUE;
			}
			return InsnValue.shortValue(((Number) obj).shortValue());
		}

		return null;
	}

	public InsnValue invertValue(AbstractInsnNode insn, InsnValue value) {
		Object obj = value.getValue();
		switch (insn.getOpcode()) {
		case Opcodes.INEG:
			if (obj == null) {
				return InsnValue.INT_VALUE;
			}
			return InsnValue.intValue(-1 * (int) obj);
		case Opcodes.LNEG:
			if (obj == null) {
				return InsnValue.LONG_VALUE;
			}
			return InsnValue.longValue(-1L * (long) obj);
		case Opcodes.FNEG:
			if (obj == null) {
				return InsnValue.FLOAT_VALUE;
			}
			return InsnValue.floatValue(-1F * (float) obj);
		case Opcodes.DNEG:
			if (obj == null) {
				return InsnValue.DOUBLE_VALUE;
			}
			return InsnValue.doubleValue(-1D * (double) obj);
		}

		return null;
	}

	public InsnValue getStatic(FieldInsnNode fin) {
		return new InsnValue(Type.getType(fin.desc));
	}

	/**
	 * @param fin
	 * @param value
	 *            Object instance which the fields is being retrieved from.
	 * @return
	 */
	public InsnValue getField(FieldInsnNode fin, InsnValue value) {
		return new InsnValue(Type.getType(fin.desc));
	}

	public void putStatic(FieldInsnNode fin, InsnValue value) {
		// Not much has to be done here...
	}

	public void putField(FieldInsnNode insn, InsnValue value1, InsnValue value2) {
		// Not much has to be done here...
	}

	public InsnValue onMethod(AbstractInsnNode insn, List<InsnValue> values) {
		String desc = "V";
		if (insn.getOpcode() == Opcodes.INVOKEDYNAMIC){
			desc = ((InvokeDynamicInsnNode) insn).desc;
		}else{
			desc = ((MethodInsnNode) insn).desc;
		}
		// Until I'm ready to simulate method calls the opcode here really
		// doesn't matter.
		/*
		 * switch (insn.getOpcode()) { case Opcodes.INVOKEDYNAMIC: case
		 * Opcodes.INVOKESPECIAL: case Opcodes.INVOKEINTERFACE: case
		 * Opcodes.INVOKESTATIC: case Opcodes.INVOKEVIRTUAL: }
		 */
		if (desc.endsWith("V")) {
			return null;
		}
		return new InsnValue(Type.getReturnType(desc));
	}

	public InsnValue compareConstants(AbstractInsnNode insn, InsnValue value1, InsnValue value2) {
		Object o = value1.getValue(), oo = value2.getValue();
		if (o == null || oo == null) {
			// Can't compare since the values haven't been resolved.
			return InsnValue.INT_VALUE;
		}
		int v = 0;
		switch (insn.getOpcode()) {
		case Opcodes.LCMP:
			long l1 = (long) o, l2 = (long) oo;
			v = (l1 == l2) ? 0 : (l2 < l1) ? 1 : -1;
			break;
		case Opcodes.FCMPL:
		case Opcodes.FCMPG:
			float f1 = (float) o, f2 = (float) oo;
			if (f1 == Float.NaN || f2 == Float.NaN) {
				v = insn.getOpcode() == Opcodes.FCMPG ? -1 : 1;
			} else {
				v = (f1 == f2) ? 0 : (f2 < f1) ? 1 : -1;
			}
			break;
		case Opcodes.DCMPL:
		case Opcodes.DCMPG:
			double d1 = (float) o, d2 = (double) oo;
			if (d1 == Float.NaN || d2 == Float.NaN) {
				v = insn.getOpcode() == Opcodes.DCMPG ? -1 : 1;
			} else {
				v = (d1 == d2) ? 0 : (d2 < d1) ? 1 : -1;
			}
			break;
		case Opcodes.IF_ICMPEQ:
		case Opcodes.IF_ICMPNE:
		case Opcodes.IF_ICMPLT:
		case Opcodes.IF_ICMPGE:
		case Opcodes.IF_ICMPGT:
		case Opcodes.IF_ICMPLE:
			int i1 = (int) o, i2 = (int) oo;
			switch (insn.getOpcode()) {
			case Opcodes.IF_ICMPEQ:
				v = i2 == i1 ? 1 : 0;
				break;
			case Opcodes.IF_ICMPNE:
				v = i2 != i1 ? 1 : 0;
				break;
			case Opcodes.IF_ICMPLT:
				v = i2 < i1 ? 1 : 0;
				break;
			case Opcodes.IF_ICMPLE:
				v = i2 <= i1 ? 1 : 0;
				break;
			case Opcodes.IF_ICMPGE:
				v = i2 >= i1 ? 1 : 0;
				break;
			case Opcodes.IF_ICMPGT:
				v = i2 > i1 ? 1 : 0;
				break;
			}
			break;
		case Opcodes.IF_ACMPEQ:
		case Opcodes.IF_ACMPNE:
			v = ((insn.getOpcode() == Opcodes.IF_ACMPNE) ? !o.equals(oo) : o.equals(oo)) ? 1 : 0;
			break;
		}
		return InsnValue.intValue(v);
	}

	public InsnValue compareConstant(AbstractInsnNode insn, InsnValue value) {
		if (value.getValue() == null) {
			return InsnValue.INT_VALUE;
		}
		int i = ((Number)value.getValue()).intValue();
		switch (insn.getOpcode()) {
		case Opcodes.IFEQ:
			return InsnValue.intValue(i == 0);
		case Opcodes.IFNE:
			return InsnValue.intValue(i != 0);
		case Opcodes.IFLE:
			return InsnValue.intValue(i <= 0);
		case Opcodes.IFLT:
			return InsnValue.intValue(i < 0);
		case Opcodes.IFGE:
			return InsnValue.intValue(i >= 0);
		case Opcodes.IFGT:
			return InsnValue.intValue(i > 0);
		}

		return null;
	}

	public InsnValue checkNull(AbstractInsnNode insn, InsnValue value) {
		switch (insn.getOpcode()) {
		case Opcodes.IFNULL:
			return InsnValue.intValue(value.getValue() == null);
		case Opcodes.IFNONNULL:
			return InsnValue.intValue(value.getValue() != null);
		}

		return null;
	}

	public InsnValue casting(TypeInsnNode tin, InsnValue value) {
		switch (tin.getOpcode()) {
		case Opcodes.CHECKCAST:
			return value;
		case Opcodes.INSTANCEOF:
			if (value.getValue() == null) {
				return InsnValue.intValue(0);
			}
			Class<?> clazz = value.getValue().getClass();
			try {
				Class<?> compared = Class.forName(Type.getType(tin.desc).getClassName());
				return InsnValue.intValue(clazz.isAssignableFrom(compared));
			} catch (ClassNotFoundException e) {
			}
			return InsnValue.intValue(0);
		}

		return null;
	}

	public void monitor(AbstractInsnNode insn, InsnValue value) {
		// Nothing needs to be done
	}

	public void throwException(AbstractInsnNode insn, InsnValue value) {
		// Not much has to be done here...
	}

	public InsnValue getSwitchValue(AbstractInsnNode insn, InsnValue value) {
		if (value.getValue() == null) {
			return InsnValue.intValue(-1);
		}
		int i = (int) value.getValue();
		switch (insn.getOpcode()) {
		case Opcodes.TABLESWITCH:
			TableSwitchInsnNode tsin = (TableSwitchInsnNode) insn;
			if (i < tsin.min || i > tsin.max) {
				return InsnValue.intValue(-1);
			}
			return InsnValue.intValue(i);
		case Opcodes.LOOKUPSWITCH:
			LookupSwitchInsnNode lsin = (LookupSwitchInsnNode) insn;
			for (Object o : lsin.keys) {
				if (o.equals(i)) {
					return InsnValue.intValue(i);
				}
			}
			return InsnValue.intValue(-1);
		}

		return null;
	}

	public InsnValue array(AbstractInsnNode insn, InsnValue value) {
		Object obj = value.getValue();
		int n = -1;
		switch (insn.getOpcode()) {
		case Opcodes.NEWARRAY:
			IntInsnNode iin = (IntInsnNode) insn;
			n = obj == null ? -1 : (int) obj;
			switch (iin.operand) {
			case Type.BOOLEAN:
			case Type.INT:
				if (n == -1) {
					return InsnValue.INT_ARR_VALUE;
				}
				return new InsnValue(InsnValue.INT_ARR_VALUE.getType(), new int[n]);
			case Type.CHAR:
				if (n == -1) {
					return InsnValue.CHAR_ARR_VALUE;
				}
				return new InsnValue(InsnValue.CHAR_ARR_VALUE.getType(), new int[n]);
			case Type.BYTE:
				if (n == -1) {
					return InsnValue.BYTE_ARR_VALUE;
				}
				return new InsnValue(InsnValue.BYTE_ARR_VALUE.getType(), new byte[n]);
			case Type.SHORT:
				if (n == -1) {
					return InsnValue.SHORT_ARR_VALUE;
				}
				return new InsnValue(InsnValue.SHORT_ARR_VALUE.getType(), new short[n]);
			case Type.FLOAT:
				if (n == -1) {
					return InsnValue.FLOAT_ARR_VALUE;
				}
				return new InsnValue(InsnValue.FLOAT_ARR_VALUE.getType(), new float[n]);
			case Type.LONG:
				if (n == -1) {
					return InsnValue.LONG_ARR_VALUE;
				}
				return new InsnValue(InsnValue.LONG_ARR_VALUE.getType(), new long[n]);
			case Type.DOUBLE:
				if (n == -1) {
					return InsnValue.DOUBLE_ARR_VALUE;
				}
				return new InsnValue(InsnValue.DOUBLE_ARR_VALUE.getType(), new double[n]);
			case Type.OBJECT:
				if (n == -1) {
					return InsnValue.REFERENCE_ARR_VALUE;
				}
				return new InsnValue(InsnValue.REFERENCE_ARR_VALUE.getType(), new Object[n]);
			}
			break;
		case Opcodes.ANEWARRAY:
			if (n == -1) {
				n = obj == null ? -1 : (int) obj;
			}
			TypeInsnNode tin = (TypeInsnNode) insn;
			Type t = Type.getType(tin.desc);
			if (t.getDescriptor().length() > 1) {
				return new InsnValue(Type.getType("[" + tin.desc));
			}
			switch (t.getSort()) {
			case Type.BOOLEAN:
			case Type.INT:
				if (n == -1) {
					return InsnValue.INT_ARR_VALUE;
				}
				return new InsnValue(InsnValue.INT_ARR_VALUE.getType(), new int[n]);
			case Type.CHAR:
				if (n == -1) {
					return InsnValue.CHAR_ARR_VALUE;
				}
				return new InsnValue(InsnValue.CHAR_ARR_VALUE.getType(), new int[n]);
			case Type.BYTE:
				if (n == -1) {
					return InsnValue.BYTE_ARR_VALUE;
				}
				return new InsnValue(InsnValue.BYTE_ARR_VALUE.getType(), new byte[n]);
			case Type.SHORT:
				if (n == -1) {
					return InsnValue.SHORT_ARR_VALUE;
				}
				return new InsnValue(InsnValue.SHORT_ARR_VALUE.getType(), new short[n]);
			case Type.FLOAT:
				if (n == -1) {
					return InsnValue.FLOAT_ARR_VALUE;
				}
				return new InsnValue(InsnValue.FLOAT_ARR_VALUE.getType(), new float[n]);
			case Type.LONG:
				if (n == -1) {
					return InsnValue.LONG_ARR_VALUE;
				}
				return new InsnValue(InsnValue.LONG_ARR_VALUE.getType(), new long[n]);
			case Type.DOUBLE:
				if (n == -1) {
					return InsnValue.DOUBLE_ARR_VALUE;
				}
				return new InsnValue(InsnValue.DOUBLE_ARR_VALUE.getType(), new double[n]);
			}
		case Opcodes.ARRAYLENGTH:
			int len = getLength(obj);
			if (len == -1) {
				return InsnValue.INT_VALUE;
			}
			return InsnValue.intValue(len);
		}

		return null;
	}

	public InsnValue onMultiANewArray(MultiANewArrayInsnNode insn, List<InsnValue> values) {
		Type t = Type.getType((insn).desc);
		// I have no idea how I would go about making new N-D arrays without
		// really REALLY ugly code.
		//
		// TODO: Suck it up and do the ugly.
		return new InsnValue(t);
	}

	public InsnValue newValue(Type type) {
		return new InsnValue(type);
	}

	private int getLength(Object obj) {
		// Ewww....
		if (obj instanceof Object[]) {
			return ((Object[]) obj).length;
		}
		if (obj instanceof boolean[]) {
			return ((boolean[]) obj).length;
		}
		if (obj instanceof byte[]) {
			return ((byte[]) obj).length;
		}
		if (obj instanceof short[]) {
			return ((short[]) obj).length;
		}
		if (obj instanceof char[]) {
			return ((char[]) obj).length;
		}
		if (obj instanceof int[]) {
			return ((int[]) obj).length;
		}
		if (obj instanceof long[]) {
			return ((long[]) obj).length;
		}
		if (obj instanceof float[]) {
			return ((float[]) obj).length;
		}
		if (obj instanceof double[]) {
			return ((double[]) obj).length;
		}
		return -1;
	}

	/**
	 * Determines if two frames are compatible.
	 * 
	 * @param oldFrame
	 * @param afterRET
	 * @param interpreter2
	 * @return
	 */
	public boolean isMergeCompatible(StackFrame oldFrame, StackFrame afterRET) {
		if (oldFrame == null || oldFrame.getStackSize() != afterRET.getStackSize()) {
			return false;
		}
		boolean changes = false;
		for (int i = 0; i < oldFrame.getLocals() + oldFrame.getStackSize(); ++i) {
			Value v = afterRET.getStack(i);
			if (v != null && !v.equals(oldFrame.getStack(i))) {
				changes = true;
			}
		}
		return changes;
	}
}
