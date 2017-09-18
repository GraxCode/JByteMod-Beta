package me.lpk;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.objectweb.asm.tree.ClassNode;

import me.lpk.antis.AntiBase;
import me.lpk.antis.impl.*;
import me.lpk.log.Logger;
import me.lpk.mapping.MappedClass;
import me.lpk.mapping.MappingProcessor;
import me.lpk.util.JarUtils;
import me.lpk.util.Setup;

public class Main {

	public static void main(String[] args) {
		try {
			runAnti(new File("ZKMNew.jar"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static void runAnti(File jar) throws Exception {
		Setup.setBypassSetup();
		Setup lsm = Setup.get(jar.getAbsolutePath(), true);
		for (String className : lsm.getNodes().keySet()) {
			AntiBase anti = new AntiZKM8(jar);
			ClassNode node = lsm.getNodes().get(className);
			lsm.getNodes().put(className, anti.scan(node));
		}
		Map<String, byte[]> out = MappingProcessor.process(lsm.getNodes(), new HashMap<String, MappedClass>(), true);
		out.putAll(JarUtils.loadNonClassEntries(jar));
		Logger.logLow("Saving...");
		JarUtils.saveAsJar(out, jar.getName() + "-re.jar");
	}
}
