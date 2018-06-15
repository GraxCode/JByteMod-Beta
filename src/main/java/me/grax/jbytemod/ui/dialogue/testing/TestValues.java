package me.grax.jbytemod.ui.dialogue.testing;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import me.grax.jbytemod.JByteMod;
import me.grax.jbytemod.ui.dialogue.InsnEditDialogue;

public class TestValues {
  public int integer = 0;
  public float floatval = 0;
  public double doubleval = 0;
  public long longval = 0;
  public byte byteval = 0;
  public char charval = 'c';
  public short shortval = 0;
  public Integer integer2 = 0;
  public Float floatval2 = 0f;
  public Double doubleval2 = 0d;
  public Long longval2 = 0L;
  public Byte byteval2 = 0;
  public Character charval2 = 0;
  public Short shortval2 = 0;
  public String test = "Test";
  /* public String stringNull = null; */
  public List<String> list = Arrays.asList("1", "2", "3");

  public static void main(String[] args) throws IllegalArgumentException, IllegalAccessException {
    JByteMod.initialize();
    TestValues tv = new TestValues();
    new InsnEditDialogue(null, tv).open();
    for(Field f : tv.getClass().getDeclaredFields()) {
      System.out.println(f.getName() + " " + f.get(tv));
    }
  }
}
