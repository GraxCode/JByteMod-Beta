package me.lpk.log;

/**
 * Too lazy to setup Log4J. Made this instead.
 */
public class Logger {
	public static final int VERY_HIGH = 0, HIGH = 1, MEDIUM = 2, LOW = 3;
	private static int level = HIGH;

	public static void logVeryHigh(String s) {
		log(s, VERY_HIGH);
	}

	public static void logHigh(String s) {
		log(s, HIGH);
	}

	public static void logMedium(String s) {
		log(s, MEDIUM);
	}

	public static void logLow(String s) {
		log(s, LOW);
	}

	public static void errVeryHigh(String s) {
		err(s, VERY_HIGH);
	}

	public static void errHigh(String s) {
		err(s, HIGH);
	}

	public static void errMedium(String s) {
		err(s, MEDIUM);
	}

	public static void errLow(String s) {
		err(s, LOW);
	}

	private static void log(String s, int i) {
		if (i >= level) {
			System.out.println(s);
		}
	}

	private static void err(String s, int i) {
		if (i >= level) {
			System.err.println(s);
		}
	}
}
