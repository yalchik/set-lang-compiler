@echo off
set BUILDMODE=%2

java -jar translator/target/translator-1.0-SNAPSHOT-jar-with-dependencies.jar %1
if not exist Program.java goto end

javac Program.java
java Program

del *.class
if not "%BUILDMODE%" == "-keep-src" del Program.java

: end
