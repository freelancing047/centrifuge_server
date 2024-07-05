@echo off

SETLOCAL

set SCRIPT_DIR=%~dp0
set SCRIPT_DIR=%SCRIPT_DIR:~0,-1%
set CSI_HOME=%SCRIPT_DIR%\..\..
set JRE_HOME=%CSI_HOME%\jre

call "%JRE_HOME%\bin\java" -jar "%SCRIPT_DIR%\migrate.jar" export "%CSI_HOME%" %*

ENDLOCAL
