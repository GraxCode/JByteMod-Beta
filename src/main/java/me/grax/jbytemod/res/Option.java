package me.grax.jbytemod.res;

public class Option {

  private String name;
  private String group;

  private Object value;
  private Type type;

  public Option(String name, Object value, Type type) {
    this(name, value, type, "general");
  }

  public Option(String name, Object value, Type type, String group) {
    this.name = name;
    switch (type) {
    case INT:
      this.value = Integer.parseInt(value.toString());
      break;
    case BOOLEAN:
      this.value = Boolean.parseBoolean(value.toString());
      break;
    default:
      this.value = value;
      break;
    }
    this.type = type;
    this.group = group;
  }

  public boolean getBoolean() {
    return (boolean) value;
  }

  public String getString() {
    return (String) value;
  }

  public int getInteger() {
    return (int) value;
  }

  public void setValue(Object value) {
    this.value = value;
  }

  public String getGroup() {
    return group;
  }

  public void setGroup(String group) {
    this.group = group;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Object getValue() {
    return value;
  }

  public Type getType() {
    return type;
  }

  public void setType(Type type) {
    this.type = type;
  }

  public enum Type {
    BOOLEAN, STRING, INT
  }
}
