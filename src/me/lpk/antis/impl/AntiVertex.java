package me.lpk.antis.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import me.lpk.analysis.Sandbox;
import me.lpk.antis.AntiBase;

public class AntiVertex extends AntiBase {

	public AntiVertex(Map<String, ClassNode> nodes) {
		super(nodes);
	}

	@Override
	public ClassNode scan(ClassNode node) {
		for (MethodNode mnode : node.methods) {
			if (mnode.access == 0xA) {
				mnode.access = Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC;
			}
		}
		for (MethodNode mnode : node.methods) {
			replace(mnode);
		}
		return node;
	}

	private void replace(MethodNode method) {
		// if (!method.name.startsWith("access")) {
		// try {
		// method.access = AntiSynthetic.inverseSynthetic(method.access);
		// } catch (Exception e) {
		// e.printStackTrace();
		// }
		// }
		ArrayList<Integer> carray = new ArrayList();
		String param2 = "";
		int param3 = 0;
		AbstractInsnNode ain = method.instructions.getFirst();
		ArrayList<Deobfuscated> deobbed = new ArrayList<>();
		while (ain != null && ain.getNext() != null) {
			AbstractInsnNode nod = ain.getNext();
			if (ain.getOpcode() == Opcodes.NEWARRAY) {
				IntInsnNode iin = (IntInsnNode) ain;
				if (iin.operand != 5) {
					ain = ain.getNext();
					continue;
				}
				if (nod.getOpcode() != Opcodes.DUP) {
					ain = ain.getNext();
					continue;
				}
				carray.clear();
				param2 = "";
				param3 = -1;

				boolean inCA = true;
				while (ain.getOpcode() != Opcodes.INVOKESTATIC) {
					if (ain.getOpcode() == Opcodes.LDC) {
						LdcInsnNode ldc = (LdcInsnNode) ain;
						if (ldc.cst instanceof Integer) {
							if (inCA) {
								carray.add((Integer) ldc.cst);
							} else {
								param3 = (int) ldc.cst;
								ain = ain.getNext();
								break;
							}
						} else {
							inCA = false;
							if (ldc.cst instanceof String) {
								param2 = ldc.cst.toString();
							}
						}
					}
					if (!inCA && param3 == -1) {
						if(ain.getOpcode() == Opcodes.BIPUSH) {
							param3 = ((IntInsnNode)ain).operand;
						}
					}
					
					ain = ain.getNext();
					if (ain == null) {
						break;
					}
				}
				if (ain == null) {
					break;
				}
				MethodInsnNode min = (MethodInsnNode) ain;
				if (!min.desc.equals("([CLjava/lang/String;I)Ljava/lang/String;")) {
					ain = ain.getNext();
					continue;
				}
				ClassNode owner = getNodes().get(min.owner);
				if (owner == null) {
					ain = ain.getNext();
					continue;
				}
				if (!carray.isEmpty()) {
					if (owner.name.equals("suicide/Suicide")) {
//						System.out.println(Arrays.toString(carray.toArray()) + " " + param2 + " " + param3);
//						System.out.println("\u0267\u02f7\u0376\u0268\u036a\u0275\u026f\u0288\u035c\u036b\u030c\u028d\u036a\u036a\u031c");
					}
					char[] ca = new char[carray.size()];
					int index = 0;
					for (Integer in : carray) {
						ca[index] = (char) ((int) in);
						index++;
					}
					try {
//						Object ret = Sandbox.getProxyIsolatedReturn(method, owner, min, new Object[] { ca, param2, param3 });
						String ret = decode(ca, param2, param3);
						if (ret != null) {
							deobbed.add(new Deobfuscated(min, ret));
//							ain = ain.getNext();
//							continue;
						} else {
							ain = ain.getNext();
							continue;
						}
					} catch (Error e) {
						e.printStackTrace();
						ain = ain.getNext();
					}
				} else {
					ain = ain.getNext();
					continue;
				}

			} else {
				ain = ain.getNext();
				continue;
			}
		}
		for(Deobfuscated deob : deobbed) {
			method.instructions.insert(deob.min, new LdcInsnNode(deob.ret));
			AbstractInsnNode a = deob.min;
			method.instructions.remove(deob.min.getPrevious().getPrevious());
			method.instructions.remove(deob.min.getPrevious());
			ArrayList<AbstractInsnNode> toRemove = new ArrayList<>();
			while(a != null && a.getOpcode() != Opcodes.NEWARRAY) {
				a = a.getPrevious();
				toRemove.add(a);
			}
//			method.instructions.set(deob.min, new InsnNode(Opcodes.POP));
			for(AbstractInsnNode insn : toRemove) {
				method.instructions.remove(insn);
			}
			method.instructions.remove(deob.min.getPrevious());
			method.instructions.remove(deob.min);
			
		}
	}

	private static String decode(char[] var0, String var1, int var2) {
		int var3 = 0;

		for (int var4 = 0; var4 < var0.length; ++var4) {
			char var5 = var0[var4];
			char var6 = var1.charAt(var3 % var1.length());
			char var7 = (char) (var5 + var2 - var6);
			var0[var4] = var7;
			++var3;
		}

		return new String(var0);
	}

	public class Deobfuscated {
		public MethodInsnNode min;
		public String ret;
		public Deobfuscated(MethodInsnNode min, String deob) {
			super();
			this.min = min;
			this.ret = deob;
		}
		
	}
}
