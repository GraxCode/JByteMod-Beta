package me.grax.jbytemod.logging;

import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;

import org.apache.commons.io.output.ByteArrayOutputStream;

public class Logging extends PrintStream {


  public Logging() {
    super(new ByteArrayOutputStream(), true);
  }

  private static final SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");

  public void log(String text) {
    logConsole(getPrefix(Level.INFO), text);
  }

  public void warn(String text) {
    logConsole(getPrefix(Level.WARN), text);
  }

  public void err(String text) {
    logConsole(getPrefix(Level.ERROR), text);
  }

  public String getPrefix(Level l) {
    return "[" + format.format(new Date()) + "] [" + l.name() + "]";
  }

  public enum Level {
    WARN, INFO, ERROR
  }

  private void logConsole(String prefix, String text) {
    System.out.print(prefix);
    System.out.print(" ");
    System.out.println(text);
  }
  @Override
  public void println(String x) {
    log(x);
  }
}
