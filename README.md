# JByteMod-Beta
JByteMod is a multifunctional bytecode editor with syntax highlighting and live decompiling and method graphing.
The successor of JByteMod: https://github.com/GraxCode/Cafebabe

![Screenshot 1](https://i.imgur.com/9RUqMYC.png)
![Screenshot 2](https://i.imgur.com/Cjj1Dh0.png)
![Screenshot 3](https://i.imgur.com/0x21dMo.png)
![Screenshot 4](https://i.imgur.com/NdWIxqd.png)
![Screenshot 5](https://i.imgur.com/eSUKCHi.png)

JByteMod was originally based on JBytedit (Written in Kotlin) by QMatt.
I decided to recode it after QMatt deleted his account because it wasn't very stable and had almost no features.

## CLI
| Argument | Description |
| --- | --- |
| --help | Displays help |
| --file | File to open (.jar / .class) |
| --dir | Working directory |
| --config | Config file name |


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

    mvn clean
    mvn package
    java -jar target/JByteMod-1.5.1.jar
    
