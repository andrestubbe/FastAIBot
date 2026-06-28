@echo off
echo ========================================================
echo ⚡ Starting FastBot Multi-Modal Demo
echo ========================================================
echo.
cd examples\Demo
mvn compile exec:java -Dexec.mainClass="fastbot.DemoBotCLI"
pause
