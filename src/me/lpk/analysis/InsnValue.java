package me.lpk.analysis;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.analysis.Value;

import org.objectweb.asm.Opcodes;

import me.lpk.util.OpUtils;

/**
 * A {@link Value} that is represented by types with a possibly null value. This
 * type system distinguishes the UNINITIALZED, INT, FLOAT, LONG, DOUBLE, CHAR,
 * SHORT REFERENCE and RETURNADDRESS types. Arrays for these types are also
 * included.
 * 
 * @author Eric Bruneton
 * @editor Matt
 */
public class InsnValue implements Value {

	public static final InsnValue UNINITIALIZED_VALUE = new InsnValue(null);
	public static final InsnValue INT_VALUE = new InsnValue(Type.INT_TYPE);
	public static final InsnValue FLOAT_VALUE = new InsnValue(Type.FLOAT_TYPE);
	public static final InsnValue LONG_VALUE = new InsnValue(Type.LONG_TYPE);
	public static final InsnValue DOUBLE_VALUE = new InsnValue(Type.DOUBLE_TYPE);
	public static final InsnValue BYTE_VALUE = new InsnValue(Type.BYTE_TYPE);
	public static final InsnValue CHAR_VALUE = new InsnValue(Type.CHAR_TYPE);
	public static final InsnValue SHORT_VALUE = new InsnValue(Type.SHORT_TYPE);
	public static final InsnValue REFERENCE_VALUE = new InsnValue(Type.getObjectType("java/lang/Object"));
	public static final InsnValue NULL_REFERENCE_VALUE = new InsnValue(Type.getType("java/lang/Object"));

	//
	public static final InsnValue CHAR_ARR_VALUE = new InsnValue(Type.getObjectType("[C"));
	public static final InsnValue DOUBLE_ARR_VALUE = new InsnValue(Type.getObjectType("[D"));
	public static final InsnValue INT_ARR_VALUE = new InsnValue(Type.getObjectType("[I"));
	public static final InsnValue FLOAT_ARR_VALUE = new InsnValue(Type.getObjectType("[F"));
	public static final InsnValue BOOLEAN_ARR_VALUE = new InsnValue(Type.getObjectType("[Z"));
	public static final InsnValue LONG_ARR_VALUE = new InsnValue(Type.getObjectType("[J"));
	public static final InsnValue SHORT_ARR_VALUE = new InsnValue(Type.getObjectType("[S"));
	public static final InsnValue BYTE_ARR_VALUE = new InsnValue(Type.getObjectType("[B"));
	public static final InsnValue REFERENCE_ARR_VALUE = new InsnValue(Type.getObjectType("[java/lang/Object"));

	//
	public static final InsnValue RETURNADDRESS_VALUE = new InsnValue(Type.VOID_TYPE);

	private final Type type;
	private final Object value;

	public InsnValue(final Type type) {
		this(type, null);
	}

	public InsnValue(final Type type, final Object value) {
		this.type = type;
		this.value = value;
	}

	public Object getValue() {
		return value;
	}

	public Type getType() {
		return type;
	}

	public int getSize() {
		return type == Type.LONG_TYPE || type == Type.DOUBLE_TYPE ? 2 : 1;
	}

	public boolean isReference() {
		return type != null && (type.getSort() == Type.OBJECT || type.getSort() == Type.ARRAY);
	}

	@Override
	public boolean equals(final Object value) {
		if (value == this) {
			return true;
		} else if (value instanceof InsnValue) {
			if (type == null) {
				return ((InsnValue) value).type == null;
			} else {
				return type.equals(((InsnValue) value).type);
			}
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return type == null ? 0 : type.hashCode();
	}

	@Override
	public String toString() {
		if (value != null) {
			return type.getDescriptor() + " " + (value.toString()).trim();
		}
		if (type == null || this == UNINITIALIZED_VALUE) {
			return "Uninitialized Null";
		} else if (this == RETURNADDRESS_VALUE) {
			return "Return Address";
		} else if (this == REFERENCE_VALUE) {
			return "Misc. Ref Value";
			
		} else if (this == NULL_REFERENCE_VALUE){
			return "Null";
		}
		else if (type != null) {
			return type.getDescriptor();
		} else {
			return "ERROR";
		}
	}

	public static InsnValue intValue(AbstractInsnNode opcode) {
		return new InsnValue(Type.INT_TYPE, OpUtils.getIntValue(opcode));
	}

	public static InsnValue longValue(int opcode) {
		switch (opcode) {
		case Opcodes.LCONST_0:
			return new InsnValue(Type.LONG_TYPE, 0L);
		case Opcodes.LCONST_1:
			return new InsnValue(Type.LONG_TYPE, 1L);
		}
		return InsnValue.LONG_VALUE;
	}

	public static InsnValue doubleValue(int opcode) {
		switch (opcode) {
		case Opcodes.DCONST_0:
			return new InsnValue(Type.DOUBLE_TYPE, 0.0);
		case Opcodes.DCONST_1:
			return new InsnValue(Type.DOUBLE_TYPE, 1.0);
		}
		return InsnValue.DOUBLE_VALUE;
	}

	public static InsnValue floatValue(int opcode) {
		switch (opcode) {
		case Opcodes.FCONST_0:
			return new InsnValue(Type.FLOAT_TYPE, 0.0f);
		case Opcodes.FCONST_1:
			return new InsnValue(Type.FLOAT_TYPE, 1.0f);
		case Opcodes.FCONST_2:
			return new InsnValue(Type.FLOAT_TYPE, 2.0f);
		}
		return InsnValue.FLOAT_VALUE;
	}
	
	public static InsnValue intValue(Object cst) {
		return new InsnValue(Type.INT_TYPE, cst);
	}

	public static InsnValue charValue(Object cst) {
		return new InsnValue(Type.CHAR_TYPE, cst);
	}

	public static InsnValue byteValue(Object cst) {
		return new InsnValue(Type.BYTE_TYPE, cst);
	}

	public static InsnValue longValue(Object cst) {
		return new InsnValue(Type.LONG_TYPE, cst);
	}

	public static InsnValue doubleValue(Object cst) {
		return new InsnValue(Type.DOUBLE_TYPE, cst);
	}

	public static InsnValue floatValue(Object cst) {
		return new InsnValue(Type.FLOAT_TYPE, cst);
	}

	public static InsnValue shortValue(Object cst) {
		return new InsnValue(Type.SHORT_TYPE, cst);
	}

	public static InsnValue stringValue(Object cst) {
		return new InsnValue(Type.getType(String.class), cst);
	}
}
