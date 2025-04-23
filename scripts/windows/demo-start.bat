@echo off
setlocal enabledelayedexpansion

REM Determine project root directory relative to this script
set "SCRIPT_DIR=%~dp0"
set "PROJECT_ROOT=%SCRIPT_DIR%..\..\"
set "PROJECT_ROOT=%PROJECT_ROOT:\=/%"

REM Service Names
set SERVER_SERVICE_NAME=news-analyzer-server
set CLIENT_SERVICE_NAME=mock-news-client

REM Configuration Properties
REM Server properties
set SERVER_PORT=8080
set SERVER_CONNECTIONS_POOL_SIZE=10
set NEWS_SUMMARY_REPORT_PERIOD_IN_SECONDS=10
REM Client properties
set NEWS_ANALYZE_SERVER_HOST=localhost
set NEWS_ANALYZE_SERVER_PORT=8080
set SEND_MESSAGE_INTERVAL_IN_MS=200

REM Service paths with updated absolute paths
set "SERVER_JAR=%PROJECT_ROOT%/server/target/server-1.0-SNAPSHOT-jar-with-dependencies.jar"
set "CLIENT_JAR=%PROJECT_ROOT%/mock-news-feed-client/target/mock-news-feed-client-1.0-SNAPSHOT-jar-with-dependencies.jar"
set "LOG_DIR=%PROJECT_ROOT%/logs"
set "SERVER_PID_FILE=%PROJECT_ROOT%/.%SERVER_SERVICE_NAME%.pid"
set "CLIENT_PIDS_FILE=%PROJECT_ROOT%/.%CLIENT_SERVICE_NAME%.pids"

REM Create log directory if it doesn't exist
if not exist "%LOG_DIR%" mkdir "%LOG_DIR%"

REM Check if server is already running by looking for the PID file and checking if process exists
if exist "%SERVER_PID_FILE%" (
    set /p SERVER_PID=<"%SERVER_PID_FILE%"
    tasklist /FI "PID eq %SERVER_PID%" | find "%SERVER_PID%" > nul
    if not errorlevel 1 (
        echo %SERVER_SERVICE_NAME% is already running with PID %SERVER_PID%
        exit /b 1
    )
)

REM Preparation section - check if port is available
netstat -ano | findstr ":%SERVER_PORT% " > nul
if %errorlevel% equ 0 (
    echo [ERROR] Port %SERVER_PORT% is in use
    exit /b 1
)

REM Build section - run Maven from project root
echo Building project...
pushd "%PROJECT_ROOT%"
call mvn clean package
if %errorlevel% neq 0 (
    echo [ERROR] Build failed
    popd
    exit /b 1
)
popd

REM Check if JAR files exist
if not exist "%SERVER_JAR%" (
    echo [ERROR] Server JAR file not found: %SERVER_JAR%
    exit /b 1
)

if not exist "%CLIENT_JAR%" (
    echo [ERROR] Client JAR file not found: %CLIENT_JAR%
    exit /b 1
)

REM Start the server
echo Starting %SERVER_SERVICE_NAME%...
start /B "News Analyzer Server" cmd /c "java -Dserver.port=%SERVER_PORT% ^
     -Dserver.connectionsPoolSize=%SERVER_CONNECTIONS_POOL_SIZE% ^
     -Dscheduler.news-summary-report.periodInSeconds=%NEWS_SUMMARY_REPORT_PERIOD_IN_SECONDS% ^
     -jar "%SERVER_JAR%" > "%LOG_DIR%\%SERVER_SERVICE_NAME%.log" 2>&1"

REM Get the server PID using wmic (Note: this is a Windows-specific approach)
REM Give process time to start
timeout /t 1 > nul
for /f "tokens=2 delims=," %%a in ('wmic process where "CommandLine like '%%server-1.0-SNAPSHOT-jar-with-dependencies.jar%%'" get ProcessId /format:csv ^| findstr /r [0-9]') do (
    set SERVER_PID=%%a
)

if "%SERVER_PID%"=="" (
    echo [ERROR] Failed to get server PID
    exit /b 1
)

REM Save server PID
echo %SERVER_PID% > "%SERVER_PID_FILE%"
echo %SERVER_SERVICE_NAME% started with PID %SERVER_PID%

REM Wait for server to be ready
echo Waiting for %SERVER_SERVICE_NAME% to initialize...
set SERVER_READY=false
for /l %%i in (1, 1, 15) do (
    netstat -ano | findstr ":%SERVER_PORT% " > nul
    if %errorlevel% equ 0 (
        set SERVER_READY=true
        echo %SERVER_SERVICE_NAME% is running and listening on port %SERVER_PORT%
        goto :server_ready_check
    )
    timeout /t 1 > nul
)

:server_ready_check
if "%SERVER_READY%"=="false" (
    echo [ERROR] %SERVER_SERVICE_NAME% failed to start within 15 seconds
    taskkill /F /PID %SERVER_PID% 2>nul
    del "%SERVER_PID_FILE%" 2>nul
    exit /b 1
)

REM Start client instances
echo Starting %CLIENT_SERVICE_NAME% instances...
set "CLIENT_PIDS="

for /l %%i in (1, 1, %SERVER_CONNECTIONS_POOL_SIZE%) do (
    echo Starting %CLIENT_SERVICE_NAME% instance %%i...
    start /B "News Client %%i" cmd /c "java -Dserver.host=%NEWS_ANALYZE_SERVER_HOST% ^
         -Dserver.port=%NEWS_ANALYZE_SERVER_PORT% ^
         -Dscheduler.message-send.intervalInMs=%SEND_MESSAGE_INTERVAL_IN_MS% ^
         -jar "%CLIENT_JAR%" > "%LOG_DIR%\%CLIENT_SERVICE_NAME%_%%i.log" 2>&1"

    REM Get client PID - note the slight delay to allow process to start
    timeout /t 1 > nul
    for /f "tokens=2 delims=," %%a in ('wmic process where "CommandLine like '%%mock-news-feed-client-1.0-SNAPSHOT-jar-with-dependencies.jar%%'" get ProcessId /format:csv ^| findstr /r [0-9]') do (
        set "CLIENT_PID=%%a"
        if not defined CLIENT_PIDS (
            set "CLIENT_PIDS=!CLIENT_PID!"
        ) else (
            set "CLIENT_PIDS=!CLIENT_PIDS!^
!CLIENT_PID!"
        )
        echo %CLIENT_SERVICE_NAME% instance %%i started with PID !CLIENT_PID!
    )
)

REM Save client PIDs
echo %CLIENT_PIDS% > "%CLIENT_PIDS_FILE%"

REM Summary
echo ---------------------------------------------------
echo Deployment successful
echo %SERVER_SERVICE_NAME% is running with PID %SERVER_PID%
echo %SERVER_CONNECTIONS_POOL_SIZE% %CLIENT_SERVICE_NAME% instances are running
echo.
echo Log locations:
echo   Server log: %LOG_DIR%\%SERVER_SERVICE_NAME%.log
echo   Client logs: %LOG_DIR%\%CLIENT_SERVICE_NAME%_*.log
echo.
echo To monitor server logs: type "%LOG_DIR%\%SERVER_SERVICE_NAME%.log"
echo To monitor a specific client log: type "%LOG_DIR%\%CLIENT_SERVICE_NAME%_1.log"
echo.
echo To stop all services, use: %SCRIPT_DIR%stop.bat
echo ---------------------------------------------------

exit /b 0