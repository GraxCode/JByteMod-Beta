package me.lpk.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {

	/**
	 * Replaces strings with old references with ones with updated references.
	 * 
	 * @param orig
	 * @param oldStr
	 * @param newStr
	 * @return
	 */
	public static String replace(String orig, String oldStr, String newStr) {
		StringBuffer sb = new StringBuffer(orig);
		while (contains(sb.toString(), oldStr)) {
			if (orig.contains("(") && orig.contains(";")) {
				// orig is most likely a method desc
				int start = sb.indexOf("L" + oldStr) + 1;
				int end = sb.indexOf(oldStr + ";") + oldStr.length();
				if (start > -1 && end <= orig.length()) {
					sb.replace(start, end, newStr);
				} else {
					System.err.println("REPLACE FAIL: (" + oldStr + ") - " + orig);
					break;
				}
			} else if (orig.startsWith("L") && orig.endsWith(";")) {
				// orig is most likely a class desc
				if (orig.substring(1, orig.length() - 1).equals(oldStr)) {
					sb.replace(1, orig.length() - 1, newStr);
				}
			} else {
				// Dunno
				if (orig.equals(oldStr)) {
					sb.replace(0, sb.length(), newStr);
				} else {
					// This shouldn't happen.
					System.err.println("FUCK: (" + sb.toString() + ") - " + oldStr + ":" + newStr);
					break;
				}
			}
		}
		return sb.toString();
	}

	public static boolean contains(String orig, String check) {
		if (orig.contains(check)) {
			String regex = "([L]" + check.replace("/", "\\/") + "{1}[;])";
			if (orig.contains("(") && orig.contains(";")) {
				return orig.matches(regex);
			} else if (orig.startsWith("L") && orig.endsWith(";") && orig.substring(1, orig.length() - 1).equals(check)) {
				return true;
			} else if (orig.equals(check)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Checks if a given string is a link.
	 * 
	 * TODO: Make this not shitty.
	 * 
	 * @param input
	 * @return
	 */
	public static boolean isLink(String input) {
		String regex = "[-a-zA-Z0-9@:%._\\+~#=]{2,256}\\.[a-z]{2,6}\\b([-a-zA-Z0-9@:%_\\+.~#?&\\/=]*)";
		if (input.contains("/") && input.contains(".") && input.matches(regex)) {
			return true;
		}
		// TODO: This is old, swap this section out for regex. The above does
		// not catch every URL.
		String[] lookFor = new String[] { "http://", "https://", "www.", ".", "ftp:", ".net", ".gov", ".com", ".org", ".php", ".tk", "www", ".io", ".xyz", ".cf",
				"upload" };
		int i = 0;
		for (String lf : lookFor) {
			if (input.toLowerCase().contains(lf.toLowerCase())) {
				i++;
				if (i > 2) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Checks if a description is just a primitive.
	 * 
	 * @param description
	 * @return
	 */
	public static boolean isPrimitive(String description) {
		String x = asmTrim(description);
		if (x.length() == 0) {
			return true;
		} else if (x.equals("Z") || x.equals("J") || x.equals("I") || x.equals("F") || x.equals("D") || x.equals("C") || x.equals("T") || x.equals("G")) {
			return true;
		}
		return false;
	}

	/**
	 * Checks if a given string is an IP address.
	 * 
	 * @param input
	 * @return
	 */
	public static boolean isIP(String input) {
		String regex = "(?:[0-9]{1,3}\\.){3}[0-9]{1,3}";
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(input);
		String ss = input.replace(".", "");
		if (m.find() && isNumeric(ss) && (input.length() - ss.length() > 2)) {
			return true;
		}
		return false;
	}

	/**
	 * Checks if a string is a number
	 * 
	 * @param s
	 * @return
	 */
	public static boolean isNumeric(String s) {
		try {
			Double.parseDouble(s);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public static String asmTrim(String s) {
		// TODO: should this remove primitives?
		// (?=([L;()\/\[IDFJBZV]))
		return s.replaceAll("(?=([L;()\\/\\[IDFJBZV]))", "");
	}
}