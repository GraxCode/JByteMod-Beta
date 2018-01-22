# JByteMod-Beta
JByteMod is a multifunctional bytecode editor with syntax highlighting and live decompiling.
New in 1.4.0: Method graphing features

![Screenshot 1](https://i.imgur.com/s5aQgyU.png)
![Screenshot 2](https://i.imgur.com/s9TRfKZ.png)
![Screenshot 3](https://i.imgur.com/hDaEfq8.png)
![Screenshot 4](https://i.imgur.com/lGsIjKk.png)
![Screenshot 5](https://i.imgur.com/iqStYJJ.png)

JByteMod was originally based on JBytedit (Written in kotlin) by QMatt
I decided to recode after QMatt deleted his account it because it wasn't very stable and had almost no features

## Libraries
- Apache Commons IO
- Objectweb ASM
- RSyntaxTextArea
- SkidSuite
- Procyon Decompiler
- Fernflower Decompiler
- JGraphX
- JFreeGraph

## Plugins

To create a plugin you have to extend me.grax.jbytemod.Plugin and export it as a .jar file.

### Installation

To install a plugin you have to place the .jar file in a folder named "plugins" (that's in the same directory as JByteMod)

### Example Plugin

https://github.com/GraxCode/JTattooPlugin

## How to build

    mvn package
    java -jar target/JByteMod-1.3.0.jar
    
