@echo off
REM Build order_core and run unit tests. ASCII only: cmd.exe misreads UTF-8 without BOM.
setlocal
set "PATH=%LOCALAPPDATA%\Microsoft\WinGet\Links;%PATH%"

call "D:\0.vs-c++game\VC\Auxiliary\Build\vcvars64.bat" >nul || goto :fail

set "BUILD_DIR=C:\build\order-core"
cmake -S "%~dp0..\core" -B "%BUILD_DIR%" -G Ninja -DCMAKE_BUILD_TYPE=Release || goto :fail
cmake --build "%BUILD_DIR%" || goto :fail

ctest --test-dir "%BUILD_DIR%" --output-on-failure || goto :fail

echo.
echo BUILD OK -^> %BUILD_DIR%
exit /b 0

:fail
echo BUILD FAILED
exit /b 1
