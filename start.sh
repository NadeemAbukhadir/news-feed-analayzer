#!/bin/bash

# Configuration PROPERTIES
# PROPERTIES PROPERTIES - port property is used in both apps
set SERVER_PORT=8080
set SERVER_CONNECTIONS_POOL_SIZE=10
set NEWS_SUMMARY_REPORT_PERIOD_IN_SECONDS=10
# CLIENT PROPERTIES
set NEWS_ANALYZE_SERVER_HOST=localhost
set NEWS_ANALYZE_SERVER_PORT=8080
set SEND_MESSAGE_INTERVAL_IN_MS=200

# Preparation section
if netstat -tuln | grep -q ":$SERVER_PORT "; then
    echo "[ERROR] Port $SERVER_PORT is in use"
    exit 1
fi

# Build section
echo "Building project..."
mvn clean package
if [ $? -ne 0 ]; then
    echo "[ERROR] Build failed"
    exit 1
fi

# Check system JARs
if [ ! -f "server/target/server-1.0-SNAPSHOT-jar-with-dependencies.jar" ]; then
    echo "[ERROR] Server JAR file not found"
    exit 1
fi

if [ ! -f "mock-news-feed-client/target/mock-news-feed-client-1.0-SNAPSHOT-jar-with-dependencies.jar" ]; then
    echo "[ERROR] Client JAR file not found"
    exit 1
fi

# Server startup
echo "Starting News Analyzer Server..."
gnome-terminal --title="NewsAnalyzerServer" -- bash -c \
    "java -Dserver.port=$SERVER_PORT -Dserver.connectionsPoolSize=$SERVER_CONNECTIONS_POOL_SIZE -dscheduler.news-summary-report.periodInSeconds=$NEWS_SUMMARY_REPORT_PERIOD_IN_SECONDS -jar server/target/server-1.0-SNAPSHOT-jar-with-dependencies.jar; exec bash"

# Wait for server initialization
echo "Waiting for server startup..."
SERVER_READY=false
for i in {1..15}; do
    if netstat -tuln | grep -q ":$SERVER_PORT "; then
        SERVER_READY=true
        break
    fi
    sleep 1
done

if [ "$SERVER_READY" = false ]; then
    echo "[ERROR] Server failed to start within 15 seconds"
    exit 1
fi

# Client deployment
for ((i=1; i<=SERVER_CONNECTIONS_POOL_SIZE; i++)); do
    echo "Starting Client instance $i..."
    gnome-terminal --title="MockNewsClient_$i" -- bash -c \
        "java -Dserver.host=$NEWS_ANALYZE_SERVER_HOST -Dserver.port=$NEWS_ANALYZE_SERVER_PORT -Dscheduler.message-send.intervalInMs=$SEND_MESSAGE_INTERVAL_IN_MS -jar mock-news-feed-client/target/mock-news-feed-client-1.0-SNAPSHOT-jar-with-dependencies.jar; exec bash"
done

# Final output
echo "---------------------------------------------------"
echo "Deployment successful"
echo "Server and clients are running in separate windows"
read -p "Press any key to exit this script..."
exit 0