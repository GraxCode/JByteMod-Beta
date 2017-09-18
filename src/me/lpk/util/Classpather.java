package me.lpk.util;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * Adds jar files to the classpath.
 */
public class Classpather {
	private static final Class<?>[] parameters = new Class[] { URL.class };

	/**
	 * Add a file to the classpath.
	 * 
	 * @param s
	 * @throws IOException
	 */
	public static void addFile(String s) throws IOException {
		File f = new File(s);
		addFile(f);
	}

	/**
	 * Add a file to the classpath.
	 * 
	 * @param s
	 * @throws IOException
	 */
	@SuppressWarnings("deprecation")
	public static void addFile(File f) throws IOException {
		addURL(f.toURL());
	}

	private static void addURL(URL u) throws IOException {
		URLClassLoader sysloader = (URLClassLoader) ClassLoader.getSystemClassLoader();
		Class<URLClassLoader> sysclass = URLClassLoader.class;
		try {
			Method method = sysclass.getDeclaredMethod("addURL", parameters);
			method.setAccessible(true);
			method.invoke(sysloader, new Object[] { u });
		} catch (Throwable t) {
			t.printStackTrace();
			throw new IOException("Error, could not add URL to system classloader");
		}
	}
}