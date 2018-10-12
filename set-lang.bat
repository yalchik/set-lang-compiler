@echo off
set BUILDMODE=%2

java -jar translator/target/translator-1.0-SNAPSHOT-jar-with-dependencies.jar %1
if not exist program.cpp goto end

g++ -o program.exe program.cpp

if not "%BUILDMODE%" == "-keep-cpp" del program.cpp

: end
