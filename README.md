# JByteMod-Beta
JByteMod is a multifunctional bytecode editor with syntax highlighting and live decompiling.
New in 1.4.0: Method graphing features

![Screenshot 1](https://i.imgur.com/Wwoe7mN.png)
![Screenshot 2](https://i.imgur.com/Cjj1Dh0.png)
![Screenshot 3](https://i.imgur.com/0x21dMo.png)
![Screenshot 4](https://i.imgur.com/NdWIxqd.png)
![Screenshot 5](https://i.imgur.com/eSUKCHi.png)

JByteMod was originally based on JBytedit (Written in kotlin) by QMatt.
I decided to recode it after QMatt deleted his account because it wasn't very stable and had almost no features.

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

    mvn clean package
    java -jar target/JByteMod-1.5.3.jar
    
