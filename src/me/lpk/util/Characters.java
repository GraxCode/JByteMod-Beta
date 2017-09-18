package me.lpk.util;
import java.util.ArrayList;
import java.util.List;

public class Characters {
	public static final String[] ALPHABET_BOTH = { "A", "a", "B", "b", "C", "c", "D", "d", "E", "e", "F", "f", "G", "g", "H", "h", "I", "i", "J", "j", "K", "k", "L", "l", "M", "m", "N", "n", "O", "o", "P", "p", "Q", "q", "R", "r", "S", "s", "T", "t", "U", "u", "V", "v", "W", "w", "X", "x", "Y", "y", "Z", "z" };
	public static final String[] ALPHABET_UPPER = { "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", };
	public static final String[] ALPHABET_LOWER = { "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z", };
	// Aside from normal english text, unicode begins at 160
	public static final String[] UNICODE = genUnicode(1000, 9000);

	private static String[] genUnicode(int min, int max) {
		List<String> list = new ArrayList<String>();
		for (int i = min; i < max; i++) {
			char c = (char) i;
			list.add(String.valueOf(c));
		}
		return list.toArray(new String[list.size()]);
	}
}