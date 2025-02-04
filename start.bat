@echo off
setlocal EnableDelayedExpansion

:: Configuration PROPERTIES
set SERVER_PORT=5001
set SERVER_HOST=localhost
set SERVER_CONNECTIONS_POOL_SIZE=5
set CLIENT_MESSAGE_SCHEDULER_INTERVAL_MS=2000

:: PREPARATION SECTION
netstat -ano | findstr ":%SERVER_PORT%" > nul && (
    echo [ERROR] Port %SERVER_PORT% is in use
    exit /b 1
)

:: BUILD SECTION
echo Building project...
call mvn clean package
if %errorlevel% neq 0 (
    echo [ERROR] Build failed
    exit /b 1
)

:: CHECK SYSTEM JARs
if not exist "server\target\server-1.0-SNAPSHOT-jar-with-dependencies.jar" (
    echo [ERROR] Server JAR file not found
    exit /b 1
)

if not exist "mock-news-feed-client\target\mock-news-feed-client-1.0-SNAPSHOT-jar-with-dependencies.jar" (
    echo [ERROR] Client JAR file not found
    exit /b 1
)

:: SERVER STARTUP (Visible Console Window)
echo Starting News Analyzer Server...
start "NewsAnalyzerServer" cmd /k "java -Dserver.port=%SERVER_PORT% -Dserver.connectionsPoolSize=%SERVER_CONNECTIONS_POOL_SIZE% -jar server\target\server-1.0-SNAPSHOT-jar-with-dependencies.jar"

:: Wait for server initialization
echo Waiting for server startup...
set SERVER_READY=
for /L %%i in (1,1,15) do (
    timeout /t 1 > nul
    netstat -ano | findstr ":%SERVER_PORT%" > nul && (
        set SERVER_READY=1
        goto server_ready
    )
)
if not defined SERVER_READY (
    echo [ERROR] Server failed to start within 15 seconds
    exit /b 1
)

:server_ready

:: CLIENT DEPLOYMENT (Visible Console Windows)
for /L %%i in (1,1,%SERVER_CONNECTIONS_POOL_SIZE%) do (
    echo Starting Client instance %%i...
    start "MockNewsClient_%%i" cmd /k "java -Dserver.host=%SERVER_HOST% -Dserver.port=%SERVER_PORT% -Dscheduler.message-send.intervalInMs=%CLIENT_MESSAGE_SCHEDULER_INTERVAL_MS% -jar mock-news-feed-client\target\mock-news-feed-client-1.0-SNAPSHOT-jar-with-dependencies.jar"
)

:: OUTPUT
echo ---------------------------------------------------
echo Deployment successful
echo Server and clients are running in separate windows
echo Press any key to exit this script...
pause > nul
exit /b 0