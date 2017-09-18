package me.lpk.util;

import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

public class JarClassLoader extends URLClassLoader {

	public JarClassLoader(String... jar) throws MalformedURLException {
		super(getURLArr(jar));
	}

	@Override
	public InputStream getResourceAsStream(String name) {
		return super.getResourceAsStream(name);
	}
	private static URL[] getURLArr(String[] jar) throws MalformedURLException {
		URL[] arr = new URL[jar.length];
		for (int i = 0; i < jar.length; i++) {
			arr[i] = new File(jar[i]).toURI().toURL();
		}
		return arr;
	}
}