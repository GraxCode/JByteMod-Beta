package me.grax.jbytemod.logging;

import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.io.output.ByteArrayOutputStream;

import com.alee.managers.notification.NotificationManager;

import me.grax.jbytemod.JByteMod;

public class Logging extends PrintStream {

  public Logging() {
    super(new ByteArrayOutputStream(), true);
  }

  private GuiLogging gl;
  private static final SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");

  public void log(String text) {
    logConsole(getPrefix(Level.INFO), text);
  }

  public void logNotification(String text) {
    logConsole(getPrefix(Level.INFO), text);
    NotificationManager.showNotification(text);
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

    if (gl != null) {
      gl.interrupt();
    }
    gl = new GuiLogging(text);
    gl.start();
  }

  @Override
  public void println(String x) {
    log(x);
  }

  private static class GuiLogging extends Thread {

    private String text;

    public GuiLogging(String text) {
      this.text = text;
    }

    @Override
    public void run() {
      JByteMod inst = JByteMod.instance;
      if (inst != null) {
        inst.getPP().setTip(text);
        try {
          Thread.sleep(2000);
        } catch (InterruptedException e) {
        }
        inst.getPP().setTip(null);
      }
    }
  }
}
