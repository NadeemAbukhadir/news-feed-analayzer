@echo off
setlocal EnableDelayedExpansion

echo Terminating News Analyzer Server and Clients...

:: Kill server process
taskkill /FI "WINDOWTITLE eq NewsAnalyzerServer*" /F /T > nul 2>&1
if %errorlevel% equ 0 (
    echo Server terminated
) else (
    echo No server process found
)

:: Kill client processes
set CLIENT_COUNT=0
for /F "tokens=2" %%p in ('tasklist /FI "WINDOWTITLE eq MockNewsClient_*" /FO LIST ^| find "PID:"') do (
    set /A CLIENT_COUNT+=1
    taskkill /PID %%p /F > nul 2>&1
)

if %CLIENT_COUNT% gtr 0 (
    echo Terminated %CLIENT_COUNT% client instances
) else (
    echo No client processes found
)

:: Final verification
echo Checking remaining processes...
tasklist /FI "IMAGENAME eq java.exe" /FI "WINDOWTITLE eq NewsAnalyzerServer*" /FO TABLE > nul
if %errorlevel% equ 0 (
    echo [WARNING] Server process still running!
)

tasklist /FI "IMAGENAME eq java.exe" /FI "WINDOWTITLE eq MockNewsClient_*" /FO TABLE > nul
if %errorlevel% equ 0 (
    echo [WARNING] Client processes still running!
)

echo Cleanup complete
pause