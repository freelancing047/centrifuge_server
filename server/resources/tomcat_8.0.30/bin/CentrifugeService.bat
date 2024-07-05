@echo off
if "%OS%" == "Windows_NT" setlocal
rem ---------------------------------------------------------------------------
rem NT Service Install/Uninstall script
rem
rem Options
rem install                Install the service using Tomcat6 as service name.
rem                        Service is installed using default settings.
rem remove                 Remove the service from the System.
rem
rem name        (optional) If the second argument is present it is considered
rem                        to be new service name                                           
rem
rem $Id: service.bat 414655 2006-06-15 18:56:42Z yoavs $
rem ---------------------------------------------------------------------------

: I hate repeating the code below across multiple scripts,
: but we cannot call out to a shared batch file because
: this script might be invoked from a different directory
: than the one in which the scripts reside. 
:

SET CURRENT_DIR=%~dp0

: If errorlevel is set, the likely problem is that extensions are not enabled.
if not errorlevel 1 goto callVarSetter
Unable to determine home directory of %0
goto end

:callVarSetter
call "%CURRENT_DIR%setcatalinahome"

if exist "%CATALINA_HOME%\bin\catalina.bat" goto okHome
echo The CATALINA_HOME environment variable could not be correctly assigned.
echo %0 fails
goto end

:okHome

cd %CATALINA_HOME%

if exist "%CATALINA_HOME%\bin\tomcat6.exe" goto okHome
echo The tomcat6.exe was not found in directory %CATALINA_HOME%\bin...
echo Service installation fails
goto end

:okHome
if not "%CATALINA_BASE%" == "" goto gotBase
set CATALINA_BASE=%CATALINA_HOME%
:gotBase
 
set EXECUTABLE=%CATALINA_HOME%\bin\tomcat6.exe

rem Set default Service name
set SERVICE_NAME=CentrifugeServer
set PR_DISPLAYNAME=Centrifuge Server

if "%1" == "" goto displayUsage
if "%2" == "" goto setServiceName
set SERVICE_NAME=%2

:setServiceName
if %1 == install goto doInstall
if %1 == remove goto doRemove
if %1 == uninstall goto doRemove
echo Unknown parameter "%1"
:displayUsage
echo.
echo Usage: CentrifugeService install/remove [service_name]
goto end

:doRemove
rem Remove the service
"%EXECUTABLE%" //DS//%SERVICE_NAME%
echo The service '%SERVICE_NAME%' has been removed
goto end

:doInstall
rem Install the service
echo Installing the service '%SERVICE_NAME%' ...
echo Using CATALINA_HOME:    %CATALINA_HOME%
echo Using CATALINA_BASE:    %CATALINA_BASE%

rem Use the environment variables as an example
rem Each command line option is prefixed with PR_

set PR_DESCRIPTION=Centrifuge Server
set PR_INSTALL=%EXECUTABLE%
set PR_LOGPATH=%CATALINA_BASE%\logs
set PR_CLASSPATH=%CATALINA_HOME%\bin\bootstrap.jar

set PR_JVM=%CATALINA_HOME%\jre\bin\client\jvm.dll
if exist "%PR_JVM%" goto foundJvm

echo Script error.  Could not find jvm. Installation fails.
goto end

:foundJvm
echo Using JVM:              %PR_JVM%
"%EXECUTABLE%" //IS//%SERVICE_NAME% --StartClass org.apache.catalina.startup.Bootstrap --StopClass org.apache.catalina.startup.Bootstrap --StartParams start --StopParams stop --Startup auto
if not errorlevel 1 goto installed
echo Failed installing '%SERVICE_NAME%' service
goto end

:installed
: Clear the environment variables. They are not needed any more.
set PR_DISPLAYNAME=
set PR_DESCRIPTION=
set PR_INSTALL=
set PR_LOGPATH=
set PR_CLASSPATH=
set PR_JVM=

"%EXECUTABLE%" //US//%SERVICE_NAME% --JvmOptions "-Dcatalina.base=%CATALINA_BASE%;-Dcatalina.home=%CATALINA_HOME%;-Djava.endorsed.dirs=%CATALINA_HOME%\common\endorsed;-Djava.security.auth.login.config=%CATALINA_HOME%\conf\jaas.config" --StartMode jvm --StopMode jvm 

: More extra parameters
set PR_LOGPATH=%CATALINA_BASE%\logs
set PR_STDOUTPUT=auto
set PR_STDERROR=auto
"%EXECUTABLE%" //US//%SERVICE_NAME% ++JvmOptions "-Djava.io.tmpdir=%CATALINA_BASE%\temp" --JvmMs 128 --JvmMx 256 --StartPath "%CATALINA_HOME%" --StopPath "%CATALINA_HOME%"

echo The service '%SERVICE_NAME%' has been installed.

sc start %SERVICE_NAME%

:end

if "%OS%" == "Windows_NT" endlocal