@if "%DEBUG%" == "" @echo off

set PROJ_DIR=%~dp0
if "%PROJ_DIR%" == "" set PROJ_DIR=.

if defined JAVA_HOME goto findJavaFromJavaHome

set JAVA_EXE=java.exe
%JAVA_EXE% -version >NUL 2>&1
if "%ERRORLEVEL%" == "0" goto init
@echo ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.
goto fail

:findJavaFromJavaHome
set JAVA_HOME=%JAVA_HOME:"=%
set JAVA_EXE=%JAVA_HOME%/bin/java.exe

if exist "%JAVA_EXE%" goto init
echo ERROR: JAVA_HOME is set to an invalid directory: %JAVA_HOME%
goto fail

:init
set CMD_LINE_ARGS=%*
%JAVA_EXE% "-jar" "%PROJ_DIR%/.build/libs/schedge-all.jar" %CMD_LINE_ARGS%


