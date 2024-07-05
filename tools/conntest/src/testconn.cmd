@echo off

SETLOCAL

set SCRIPT_DIR=%~dp0
set SCRIPT_DIR=%SCRIPT_DIR:~0,-1%

"%JAVA_HOME%\bin\java" -jar "%SCRIPT_DIR%\testconn.jar" %*
 
ENDLOCAL
