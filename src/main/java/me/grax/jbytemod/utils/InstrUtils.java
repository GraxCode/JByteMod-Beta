package me.grax.jbytemod.utils;

import org.objectweb.asm.Handle;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.IincInsnNode;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MultiANewArrayInsnNode;
import org.objectweb.asm.tree.TableSwitchInsnNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import me.grax.jbytemod.JByteMod;
import me.grax.jbytemod.res.Option;
import me.lpk.util.OpUtils;

public class InstrUtils {

	public static Option primColor = JByteMod.ops.get("primary_color");
	public static Option secColor = JByteMod.ops.get("secondary_color");

	public static String toString(AbstractInsnNode ain) {
		String opc = TextUtils.toBold(OpUtils.getOpcodeText(ain.getOpcode()).toLowerCase()) + " ";
		switch (ain.getType()) {
		case AbstractInsnNode.LABEL:
			opc = TextUtils.toLight("label " + OpUtils.getLabelIndex((LabelNode) ain));
			break;
		case AbstractInsnNode.LINE:
			opc = TextUtils.toLight("line " + ((LineNumberNode) ain).line);
			break;
		case AbstractInsnNode.FIELD_INSN:
			FieldInsnNode fin = (FieldInsnNode) ain;
			opc += getDisplayType(TextUtils.escape(fin.desc), true) + " " + getDisplayClassRed(TextUtils.escape(fin.owner))
					+ "." + fin.name;
			break;
		case AbstractInsnNode.METHOD_INSN:
			MethodInsnNode min = (MethodInsnNode) ain;
			if (min.desc.contains(")")) {
				opc += getDisplayType(min.desc.split("\\)")[1], true);
			} else {
				opc += min.desc;
			}
			opc += " " + getDisplayClassRed(TextUtils.escape(min.owner)) + "." + TextUtils.escape(min.name) + "("
					+ getDisplayArgs(TextUtils.escape(min.desc)) + ")";
			break;
		case AbstractInsnNode.VAR_INSN:
			VarInsnNode vin = (VarInsnNode) ain;
			opc += vin.var;
			break;
		case AbstractInsnNode.TYPE_INSN:
			TypeInsnNode tin = (TypeInsnNode) ain;
			String esc = TextUtils.escape(tin.desc);
			if (esc.endsWith(";") && esc.startsWith("L")) {
				opc += TextUtils.addTag(esc, "font color=" + primColor.getString());
			} else {
				opc += getDisplayClass(esc);
			}
			break;
		case AbstractInsnNode.MULTIANEWARRAY_INSN:
			MultiANewArrayInsnNode mnin = (MultiANewArrayInsnNode) ain;
			opc += mnin.dims + " " + getDisplayType(TextUtils.escape(mnin.desc), true);
			break;
		case AbstractInsnNode.JUMP_INSN:
			JumpInsnNode jin = (JumpInsnNode) ain;
			opc += OpUtils.getLabelIndex(jin.label);
			break;
		case AbstractInsnNode.LDC_INSN:
			LdcInsnNode ldc = (LdcInsnNode) ain;
			opc += TextUtils.addTag(ldc.cst.getClass().getSimpleName(), "font color=" + primColor.getString()) + " ";
			if (ldc.cst instanceof String)
				opc += TextUtils.addTag("\"" + TextUtils.escape(ldc.cst.toString()) + "\"", "font color=#559955");
			else {
				opc += ldc.cst.toString();
			}
			break;
		case AbstractInsnNode.INT_INSN:
			opc += OpUtils.getIntValue(ain);
			break;
		case AbstractInsnNode.IINC_INSN:
			IincInsnNode iinc = (IincInsnNode) ain;
			opc += iinc.var + " " + iinc.incr;
			break;
		case AbstractInsnNode.FRAME:
			FrameNode fn = (FrameNode) ain;
			opc = TextUtils
					.toLight(OpUtils.getFrameType(fn.type).toLowerCase() + " " + fn.local.size() + " " + fn.stack.size());
			break;
		case AbstractInsnNode.TABLESWITCH_INSN:
			TableSwitchInsnNode tsin = (TableSwitchInsnNode) ain;
			if (tsin.dflt != null) {
				opc += TextUtils.addTag("L" + OpUtils.getLabelIndex(tsin.dflt), "font color=" + secColor.getString());
			}
			if (tsin.labels.size() < 20) {
				for (LabelNode l : tsin.labels) {
					opc += " " + TextUtils.addTag("L" + OpUtils.getLabelIndex(l), "font color=" + primColor.getString());
				}
			} else {
				opc += " " + TextUtils.addTag(tsin.labels.size() + " cases", "font color=" + primColor.getString());
			}
			break;
		case AbstractInsnNode.INVOKE_DYNAMIC_INSN:
			InvokeDynamicInsnNode idin = (InvokeDynamicInsnNode) ain;
			Object[] arr = idin.bsmArgs;
			if (arr.length > 1) {
				Object o = arr[1];
				if (o instanceof Handle) {
					Handle h = (Handle) o;
					opc += getDisplayType(h.getDesc().split("\\)")[1], true) + " "
							+ getDisplayClassRed(TextUtils.escape(h.getOwner())) + "." + TextUtils.escape(h.getName()) + "("
							+ getDisplayArgs(TextUtils.escape(h.getDesc())) + ")";

				}
			} else {
				opc += TextUtils.addTag(TextUtils.escape(idin.name), "font color=" + primColor.getString()) + " "
						+ TextUtils.escape(idin.desc);
			}
			break;
		}
		return opc;
	}

	public static String getDisplayClass(String str) {
		String[] spl = str.split("/");
		if (spl.length > 1) {
			return TextUtils.addTag(spl[spl.length - 1], "font color=" + primColor.getString());
		}
		return TextUtils.addTag(str, "font color=" + primColor.getString());
	}

	public static String getDisplayClassRed(String str) {
		String[] spl = str.split("/");
		if (spl.length > 1) {
			return TextUtils.addTag(spl[spl.length - 1], "font color=" + secColor.getString());
		}
		return TextUtils.addTag(str, "font color=" + secColor.getString());
	}

	public static String getDisplayArgs(String rawType) {
		if (!rawType.contains(")"))
			return "?";
		return getDisplayType(rawType.split("\\)")[0].substring(1), true);
	}

	public static String getDisplayType(String rawType, boolean tag) {
		String result = "";
		String tmpArg = "";
		String argSuffix = "";
		boolean isFullyQualifiedClass = false;
		for (char chr : rawType.toCharArray()) {
			if (isFullyQualifiedClass) {
				if (chr == ';') {
					String[] spl = tmpArg.split("/");
					result += spl[spl.length - 1] + argSuffix + ", ";
					argSuffix = "";
					isFullyQualifiedClass = false;
					tmpArg = "";
				} else {
					tmpArg += chr;
				}
			} else if (chr == '[') {
				argSuffix += "[]";
			} else if (chr == 'L') {
				isFullyQualifiedClass = true;
			} else {
				if (chr == 'Z') {
					result += "boolean";
				} else if (chr == 'B') {
					result += "byte";
				} else if (chr == 'C') {
					result += "char";
				} else if (chr == 'S') {
					result += "short";
				} else if (chr == 'I') {
					result += "int";
				} else if (chr == 'J') {
					result += "long";
				} else if (chr == 'F') {
					result += "float";
				} else if (chr == 'D') {
					result += "double";
				} else if (chr == 'V') {
					result += "void";
				} else {
					isFullyQualifiedClass = true;
					continue;
				}

				result += argSuffix;
				argSuffix = "";
				result += ", ";
			}
		}

		if (tmpArg.length() != 0) {
			String[] spl = tmpArg.split("/");
			result += spl[spl.length - 1] + argSuffix + ", ";
		}

		if (result.length() >= 2) {
			result = result.substring(0, result.length() - 2);
		}
		if (tag)
			return TextUtils.addTag(result, "font color=" + primColor.getString());
		return result;
	}

	public final static String getDisplayAccess(int var1) {
		String var2 = "";
		if ((var1 & 1) != 0) {
			var2 = var2 + "public ";
		}

		if ((var1 & 2) != 0) {
			var2 = var2 + "private ";
		}

		if ((var1 & 4) != 0) {
			var2 = var2 + "protected ";
		}

		if ((var1 & 8) != 0) {
			var2 = var2 + "static ";
		}

		if ((var1 & 16) != 0) {
			var2 = var2 + "final ";
		}

		if ((var1 & 1024) != 0) {
			var2 = var2 + "abstract ";
		}
		if (var2.length() > 0) {
			var2 = var2.substring(0, var2.length() - 1);
		}

		return var2;
	}

	public static String toEasyString(AbstractInsnNode ain) {
		String opc = OpUtils.getOpcodeText(ain.getOpcode()).toLowerCase() + " ";
		switch (ain.getType()) {
		case AbstractInsnNode.LABEL:
			opc = "label " + OpUtils.getLabelIndex((LabelNode) ain);
			break;
		case AbstractInsnNode.LINE:
			opc = "line " + ((LineNumberNode) ain).line;
			break;
		case AbstractInsnNode.FIELD_INSN:
			FieldInsnNode fin = (FieldInsnNode) ain;
			opc += getDisplayType(fin.desc, false) + " " + getDisplayClassEasy(fin.owner) + "." + fin.name;
			break;
		case AbstractInsnNode.METHOD_INSN:
			MethodInsnNode min = (MethodInsnNode) ain;
			opc += getDisplayType(min.desc.split("\\)")[1], false) + " " + getDisplayClassEasy(min.owner) + "." + min.name
					+ "(" + getDisplayArgsEasy(min.desc) + ")";
			break;
		case AbstractInsnNode.VAR_INSN:
			VarInsnNode vin = (VarInsnNode) ain;
			opc += vin.var;
			break;
		case AbstractInsnNode.MULTIANEWARRAY_INSN:
			MultiANewArrayInsnNode mnin = (MultiANewArrayInsnNode) ain;
			opc += mnin.dims + " " + getDisplayType(mnin.desc, false);
			break;
		case AbstractInsnNode.TYPE_INSN:
			TypeInsnNode tin = (TypeInsnNode) ain;
			String esc = tin.desc;
			if (esc.endsWith(";") && esc.startsWith("L")) {
				opc += esc;
			} else {
				opc += getDisplayClassEasy(esc);
			}
			break;
		case AbstractInsnNode.JUMP_INSN:
			JumpInsnNode jin = (JumpInsnNode) ain;
			opc += OpUtils.getLabelIndex(jin.label);
			break;
		case AbstractInsnNode.LDC_INSN:
			LdcInsnNode ldc = (LdcInsnNode) ain;
			opc += ldc.cst.getClass().getSimpleName() + " ";
			if (ldc.cst instanceof String)
				opc += "\"" + ldc.cst.toString() + "\"";
			else {
				opc += ldc.cst.toString();
			}
			break;
		case AbstractInsnNode.INT_INSN:
			opc += OpUtils.getIntValue(ain);
			break;
		case AbstractInsnNode.IINC_INSN:
			IincInsnNode iinc = (IincInsnNode) ain;
			opc += iinc.var + " " + iinc.incr;
			break;
		case AbstractInsnNode.FRAME:
			FrameNode fn = (FrameNode) ain;
			opc = OpUtils.getOpcodeText(fn.type).toLowerCase() + " " + fn.local.size() + " " + fn.stack.size();
			break;
		case AbstractInsnNode.TABLESWITCH_INSN:
			TableSwitchInsnNode tsin = (TableSwitchInsnNode) ain;
			if (tsin.dflt != null) {
				opc += "L" + OpUtils.getLabelIndex(tsin.dflt);
			}
			if (tsin.labels.size() < 20) {
				for (LabelNode l : tsin.labels) {
					opc += " " + "L" + OpUtils.getLabelIndex(l);
				}
			} else {
				opc += " " + tsin.labels.size() + " cases";
			}
			break;
		case AbstractInsnNode.INVOKE_DYNAMIC_INSN:
			InvokeDynamicInsnNode idin = (InvokeDynamicInsnNode) ain;
			opc += idin.name + " " + idin.desc;
			break;
		}
		return opc;
	}

	public static String getDisplayClassEasy(String str) {
		String[] spl = str.split("/");
		if (spl.length > 1) {
			return spl[spl.length - 1];
		}
		return str;
	}

	public static String getDisplayArgsEasy(String rawType) {
		return getDisplayType(rawType.split("\\)")[0].substring(1), false);
	}
}
