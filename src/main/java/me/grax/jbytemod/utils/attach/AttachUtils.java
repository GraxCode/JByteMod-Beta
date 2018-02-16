package me.grax.jbytemod.utils.attach;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Locale;

import javax.swing.JOptionPane;

import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;
import com.sun.tools.attach.spi.AttachProvider;

public class AttachUtils {
  public void loadAgent(String agentJar, int pid, String options) {
    VirtualMachine vm = getVirtualMachine(pid);
    if (vm == null) {
      throw new RuntimeException("Can\'t attach to this jvm. Add -javaagent:" + agentJar + " to the commandline");
    } else {
      try {
        try {
          vm.loadAgent(agentJar, options);
        } finally {
          vm.detach();
        }

      } catch (Exception var9) {
        throw new RuntimeException("Can\'t attach to this jvm. Add -javaagent:" + agentJar + " to the commandline", var9);
      }
    }
  }

  public static VirtualMachine getVirtualMachine(int pid) {
    if (VirtualMachine.list().size() > 0) {
      try {
        return VirtualMachine.attach(String.valueOf(pid));
      } catch (Exception var6) {
        if (var6.getMessage() != null && var6.getMessage().contains("process running under")) {
          JOptionPane.showMessageDialog(null, "Cannot attach to process run with different jvm!");
        }
        throw new RuntimeException(var6);
      }
    } else {
      String jvm = System.getProperty("java.vm.name").toLowerCase(Locale.ENGLISH);
      if (!jvm.contains("hotspot") && !jvm.contains("openjdk") && !jvm.contains("dynamic code evolution")) {
        return null;
      } else {
        Class<?> virtualMachineClass = pickVmImplementation();

        try {
          AttachProviderPlaceHolder e = new AttachProviderPlaceHolder();
          Constructor<?> vmConstructor = virtualMachineClass.getDeclaredConstructor(new Class[] { AttachProvider.class, String.class });
          vmConstructor.setAccessible(true);
          VirtualMachine newVM = (VirtualMachine) vmConstructor.newInstance(new Object[] { e, String.valueOf(pid) });
          return newVM;
        } catch (UnsatisfiedLinkError var7) {
          throw new RuntimeException("This jre doesn\'t support the native library for attaching to the jvm", var7);
        } catch (Exception var8) {
          throw new RuntimeException(var8);
        }
      }
    }
  }

  private static Class<?> pickVmImplementation() {
    String os = System.getProperty("os.name").toLowerCase(Locale.ENGLISH);

    try {
      if (os.contains("win")) {
        return AttachUtils.class.getClassLoader().loadClass("sun.tools.attach.WindowsVirtualMachine");
      } else if (!os.contains("nix") && !os.contains("nux") && os.indexOf("aix") <= 0) {
        if (os.contains("mac")) {
          return AttachUtils.class.getClassLoader().loadClass("sun.tools.attach.BsdVirtualMachine");
        } else if (!os.contains("sunos") && !os.contains("solaris")) {
          throw new RuntimeException("Can\'t find a vm implementation for the operational system: " + System.getProperty("os.name"));
        } else {
          return AttachUtils.class.getClassLoader().loadClass("sun.tools.attach.SolarisVirtualMachine");
        }
      } else {
        return AttachUtils.class.getClassLoader().loadClass("sun.tools.attach.LinuxVirtualMachine");
      }
    } catch (Exception var2) {
      throw new RuntimeException(var2);
    }
  }

  static class AttachProviderPlaceHolder extends AttachProvider {
    AttachProviderPlaceHolder() {
      super();
    }

    public String name() {
      return null;
    }

    public String type() {
      return null;
    }

    public VirtualMachine attachVirtualMachine(String id) throws AttachNotSupportedException, IOException {
      return null;
    }

    public List<VirtualMachineDescriptor> listVirtualMachines() {
      return null;
    }
  }
}
