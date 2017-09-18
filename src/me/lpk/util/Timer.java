package me.lpk.util;

import me.lpk.log.Logger;

public class Timer {
	final long init;
	long then, now;

	public Timer() {
		init = System.currentTimeMillis();
		then = System.currentTimeMillis();
	}

	public void log(String s) {
		now = System.currentTimeMillis();
		Logger.logLow(s + (now - then));
		then = now;
	}

	public void logTotal(String s) {
		now = System.currentTimeMillis();
		Logger.logLow(s + (now - init));
		then = now;
	}
}