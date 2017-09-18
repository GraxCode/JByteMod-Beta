package me.lpk.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import org.objectweb.asm.tree.ClassNode;

import me.lpk.log.Logger;
import me.lpk.mapping.MappedClass;
import me.lpk.mapping.MappedMember;
import me.lpk.mapping.MappingFactory;

/**
 * A container for all things necessary for general usage in SkidSuite.
 * Contains:
 * <ul>
 * <li>Map of ClassNodes
 * <li>Map of MappedClasses
 * </ul>
 */
public class Setup {
	private static volatile Map<String, MappedClass> rtMappings;
	private static volatile boolean setup, bypassSetup;
	private final static String sc = Setup.class.getSimpleName();
	private final String jarName;
	private final Map<String, ClassNode> nodes;
	private final Map<String, MappedClass> mappings;
	private final Map<String, ClassNode> libNodes;
	private final Map<String, MappedClass> libMappings;

	public Setup(String name, Map<String, ClassNode> nodes, Map<String, MappedClass> mappings) {
		this.jarName = name;
		this.nodes = nodes;
		this.mappings = mappings;
		this.libNodes = new HashMap<String, ClassNode>();
		this.libMappings = new HashMap<String, MappedClass>();
	}

	public Setup(String name, Map<String, ClassNode> nodes, Map<String, MappedClass> mappings, Map<String, ClassNode> libNodes, Map<String, MappedClass> libMappings) {
		this.jarName = name;
		this.nodes = nodes;
		this.mappings = mappings;
		this.libNodes = libNodes;
		this.libMappings = libMappings;
	}

	/**
	 * Given a jar file <i>(Optional: and libraries)</i> generates everything
	 * needed for general usage in SkidSuite.
	 * 
	 * @param jarIn
	 *            Jar to read from
	 * @param readFileLibs
	 *            If libraries from the 'libraries' folder should be read
	 * @return
	 * @throws ImpatientSetupException
	 */
	public static Setup get(String jarIn, boolean readFileLibs) throws ImpatientSetupException {
		return get(jarIn, readFileLibs, null);
	}

	/**
	 * Given a jar file <i>(Optional: and libraries)</i> generates everything
	 * needed for general usage in SkidSuite.
	 * 
	 * @param jarIn
	 *            Jar to read from
	 * @param readFileLibs
	 *            If libraries from the 'libraries' folder should be read
	 * @param libs
	 *            Additional library jars
	 * @return
	 * @throws ImpatientSetupException
	 */
	public static Setup get(String jarIn, boolean readFileLibs, Collection<File> libs) throws ImpatientSetupException {
		boolean ignoredSetup = false;
		if (!setup) {
			if (!bypassSetup) {
				throw new ImpatientSetupException();
			} else {
				ignoredSetup = true;
			}
		}
		Logger.logLow("Loading: " + jarIn + " (Reading Libraries: " + readFileLibs + ")...");
		File in = new File(jarIn);
		Map<String, ClassNode> nodes = loadNodes(in);
		Map<String, ClassNode> libNodes = new HashMap<String, ClassNode>();
		if (readFileLibs) {
			if (libs == null) {
				libs = new ArrayList<File>();
			}
			libs.addAll(getLibraries());
		}
		if (libs != null && libs.size() > 0) {
			for (File lib : libs) {
				try {
					for (ClassNode cn : JarUtils.loadClasses(lib).values()) {
						libNodes.put(cn.name, cn);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		//
		//
		Logger.logLow("Generating mappings...");
		Map<String, MappedClass> mappings = MappingFactory.mappingsFromNodesNoLinking(nodes);
		Map<String, MappedClass> libMappings = new HashMap<String, MappedClass>(MappingFactory.mappingsFromNodesNoLinking(libNodes));
		if (libMappings.size() > 0) {
			Logger.logLow("Marking library nodes as read-only...");
			for (MappedClass mc : libMappings.values()) {
				mc.setIsLibrary(true);
				for (MappedMember mm : mc.getFields()) {
					mm.setIsLibrary(true);
				}
				for (MappedMember mm : mc.getMethods()) {
					mm.setIsLibrary(true);
				}
			}
		}
		//
		//
		Logger.logLow("Merging target and library mappings...");
		if (!ignoredSetup) {
			mappings.putAll(rtMappings);
		}
		if (libNodes.size() > 0) {
			mappings.putAll(libMappings);
		}
		for (MappedClass mc : mappings.values()) {
			MappingFactory.linkMappings(mc, mappings);
		}
		Logger.logLow("Completed loading from: " + jarIn);
		return new Setup(jarIn, nodes, mappings, libNodes, libMappings);
	}

	/**
	 * Gets the name of the jar file this Setup read from.
	 * 
	 * @return
	 */
	public String getJarName() {
		return jarName;
	}

	/**
	 * Gets the map of ClassNodes <i>(name to node)</i>.
	 * 
	 * @return
	 */
	public Map<String, ClassNode> getNodes() {
		return nodes;
	}

	/**
	 * Gets the map of MappedClasses<i>(name to mapping)</i>.
	 * 
	 * @return
	 */
	public Map<String, MappedClass> getMappings() {
		return mappings;
	}

	/**
	 * Gets the map of ClassNodes<i>(name to mapping)</i>, but only for library
	 * nodes.
	 * 
	 * @return
	 */
	public Map<String, ClassNode> getLibNodes() {
		return libNodes;
	}

	/**
	 * Gets the map of MappedClasses<i>(name to mapping)</i>, but only for
	 * library nodes.
	 * 
	 * @return
	 */
	public Map<String, MappedClass> getLibMappings() {
		return libMappings;
	}

	/**
	 * Reads ClasNodes from a file and returns them as a map.
	 * 
	 * @param file
	 * @return
	 */
	private static Map<String, ClassNode> loadNodes(File file) {
		Map<String, ClassNode> nodes = null;
		try {
			nodes = JarUtils.loadClasses(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (nodes == null) {
			Logger.errLow("Failed reading classes from: " + file.getAbsolutePath());
			return null;
		}
		return nodes;
	}

	/**
	 * Returns a list of libraries on loaded from the fily system. May be empty.
	 * 
	 * @return
	 */
	private static List<File> getLibraries() {
		return getLibraries(false);
	}

	/**
	 * Returns a list of libraries on loaded from the fily system. May be empty.
	 * 
	 * @param makeDir
	 * @return
	 */
	private static List<File> getLibraries(boolean makeDir) {
		List<File> files = new ArrayList<File>();
		//
		if (makeDir) {
			File libDir = new File("libraries");
			libDir.mkdirs();
			for (File lib : FileUtils.listFiles(libDir, new String[] { "jar" }, true)) {
				files.add(lib);
			}
		}
		return files;
	}

	/**
	 * This bypasses the setup process. This will skip a 5 or so second wait
	 * time but it may result in less accurate output. <i> Do not do this is
	 * classes or class members are being renamed.</i>
	 */
	public static void setBypassSetup() {
		bypassSetup = true;
	}

	/**
	 * Loads RT the first time around. When this is loaded and saved once, it
	 * makes using LazySetupMaker much quicker than doing it every time. It'd be
	 * even better to save this to a local file or something.
	 */
	public static void setup() {
		try {
			if (setup) {
				return;
			}
			Logger.logLow("Setting up " + sc + "...");
			Map<String, ClassNode> libNodes = new HashMap<String, ClassNode>();
			for (ClassNode cn : JarUtils.loadRT().values()) {
				libNodes.put(cn.name, cn);
			}
			Map<String, MappedClass> libMappings = new HashMap<String, MappedClass>(MappingFactory.mappingsFromNodesNoLinking(libNodes));

			for (MappedClass mc : libMappings.values()) {
				mc.setIsLibrary(true);
				for (MappedMember mm : mc.getFields()) {
					mm.setIsLibrary(true);
				}
				for (MappedMember mm : mc.getMethods()) {
					mm.setIsLibrary(true);
				}
			}
			for (MappedClass mc : libMappings.values()) {
				MappingFactory.linkMappings(mc, libMappings);
			}
			rtMappings = libMappings;
			setup = true;
		} catch (IOException e) {
			e.printStackTrace();
			rtMappings = new HashMap<String, MappedClass>();
		}
		Logger.logLow("Finished setting up "+sc+"!");
	}

	static class ImpatientSetupException extends Exception {
		public ImpatientSetupException() {
			super(sc + " has not finished preparing!");
		}

		private static final long serialVersionUID = 54L;
	}
}
