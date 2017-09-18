package me.lpk.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexUtils {
	/**
	 * Returns a list of all class matches in a description.
	 * 
	 * @param desc
	 * @return
	 */
	public static List<String> matchDescriptionClasses(String desc) {
		String pattern = "(?<=L).*?(?=[<;(])";
		Pattern pat = Pattern.compile(pattern);
		Matcher m = pat.matcher(desc);
		List<String> matches = new ArrayList<String>();
		while (m.find()) {
			matches.add(m.group());
		}
		return matches;
	}

	/**
	 * Returns a list of all numbers found in a given string.
	 * 
	 * @param text
	 * @return
	 */
	public static List<String> matchNumbers(String text) {
		String pattern = "\\d+[0-9]+";
		Pattern pat = Pattern.compile(pattern);
		Matcher m = pat.matcher(text);
		List<String> matches = new ArrayList<String>();
		while (m.find()) {
			matches.add(m.group());
		}
		return matches;
	}

	/**
	 * Returns true if text matches a given pattern.
	 * 
	 * @param pattern
	 * @param text
	 * @return
	 */
	public static boolean isMatch(String pattern, String text) {
		Pattern pat = Pattern.compile(pattern);
		Matcher m = pat.matcher(text);
		while (m.find()) {
			return true;
		}
		return false;
	}
}
