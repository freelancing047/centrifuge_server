
rem ---------------------------------------------------------------------------
rem Script to set CATALINA_HOME.  Unlike standard Tomcat,
rem Centrifuge does not accept inherited environment settings
rem for this value.
rem
rem $Id$
rem ---------------------------------------------------------------------------

set CURRENT_DIR=%~dp0


: If errorlevel is set, the likely problem is that extensions are not enabled.
if not errorlevel 1 goto stripTrailing 
CURRENT_DIR=
exit /b 1

:stripTrailing
set CURRENT_DIR=%CURRENT_DIR:~0,-1%

if not exist "%CURRENT_DIR%\catalina.bat" goto tryCD
:
: Now set CATALINA_HOME to be the parent directory of CURRENT_DIR
FOR /F "delims=" %%i IN ("%CURRENT_DIR%") DO set CATALINA_HOME=%%~dpi
set CATALINA_HOME=%CATALINA_HOME:~0,-1%
if exist "%CATALINA_HOME%\bin\catalina.bat" goto okHome

:tryCD
set CURRENT_DIR=%cd%
set CATALINA_HOME=%CURRENT_DIR%
if exist "%CATALINA_HOME%\bin\catalina.bat" goto okHome
cd ..
set CATALINA_HOME=%cd%
cd %CURRENT_DIR%
if exist "%CATALINA_HOME%\bin\catalina.bat" goto okHome
:
echo Failure setting CATALINA_HOME environment variable.
echo This environment variable is needed to run this program
echo Please contact support
exit /B 1
:
:okHome
set JRE_HOME=jre

set CATALINA_BASE=foo
set CATALINA_BASE=
set CATALINA_OPTS=foo
set CATALINA_OPTS=
set CATALINA_TMPDIR=foo
set CATALINA_TMPDIR=
set JAVA_HOME=foo
set JAVA_HOME=
set JAVA_OPTS=foo
set JAVA_OPTS=
set JSSE_HOME=foo
set JSSE_HOME=

echo CATALINA_HOME set to %CATALINA_HOME%