package me.lpk.analysis;

import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.AnalyzerException;

public class StackUtil {

	public static StackFrame[] getFrames(MethodNode mn) {
		InsnAnalyzer a = new InsnAnalyzer(new StackHelper());
		StackFrame[] sfs = null;
		try {
			sfs = a.analyze(mn.owner, mn);
		} catch (AnalyzerException e) {
			//e.printStackTrace();
		}
		return sfs;
	}

}
