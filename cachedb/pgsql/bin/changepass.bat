@echo off

setlocal

set DBPATH=..\..\metadb
set DBNAME=metadb
set HOST=127.0.0.1
set PORT=9191

set USER=%1
set PASS=%2
set NEWPASS=%3

if "%USER%"=="" GOTO syntax
if "%PASS%"=="" GOTO syntax
if "%NEWPASS%"=="" GOTO syntax

goto BEGIN

:startserver
setlocal
set path=%1
pg_ctl start -s -w -D %path%
endlocal
exit /b

:stopserver
setlocal
set path=%1
pg_ctl stop -s -m fast -D %path%
endlocal
exit /b

:changepass
setlocal
set host=%1
set port=%2
set dbname=%3
set user=%4
set pass=%5
set newpass=%6

set PGPASSWORD=%pass%
psql -h %host% -p %port% -d %dbname% -U %user% -q -w -c "ALTER ROLE %user% PASSWORD '%newpass%';"
endlocal
exit /b


:syntax
echo SYNTAX changepass username password newpassword
echo   username    - Cache database system user name
echo   password    - Current password
echo   newpassword - New password
echo.
goto end


:starterror
echo ERROR: Failed to start cache database
goto :END


:sqlerror
call :stopserver
echo ERROR: Failed to change password

goto :END


:BEGIN

call :startserver %DBPATH%
if %ERRORLEVEL% NEQ 0 goto starterror

call :changepass %HOST% %PORT% %DBNAME% %USER% %PASS% %NEWPASS%
if %ERRORLEVEL% NEQ 0 goto sqlerror

call :stopserver %DBPATH%

echo SUCCESS: Password sucessfully changed


:END

endlocal