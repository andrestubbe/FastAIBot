@echo off
cd /d "%~dp0"
chcp 65001 >nul
cls

set MAVEN_OPTS=--enable-native-access=ALL-UNNAMED

echo Building Project...
call mvn -f examples/Demo/pom.xml clean compile dependency:build-classpath -Dmdep.outputFile=cp.txt -DskipTests -q
if %ERRORLEVEL% NEQ 0 ( echo Build failed. & pause & exit /b %ERRORLEVEL% )

echo Running Demo3DTerminal...
:: Force Windows Console to support 24-bit True Color
reg add HKCU\Console /v VirtualTerminalLevel /t REG_DWORD /d 1 /f >nul 2>&1

for /f "usebackq delims=" %%i in ("examples\Demo\cp.txt") do set CP=%%i

:: Copy native DLLs so local llama can find its backends during run
copy /Y ..\FastAIModel\build\*.dll . >nul

java --enable-native-access=ALL-UNNAMED --add-modules jdk.incubator.vector -cp "examples\Demo\target\classes;%CP%" fastbot.Demo

:: Clean up DLLs after execution
del /Q *.dll


