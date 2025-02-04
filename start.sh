#!/bin/bash

# Configuration properties
export SERVER_PORT=5001
export SERVER_HOST="localhost"
# Also, determines the number of deployed client instances.
export SERVER_CONNECTIONS_POOL_SIZE=5
export CLIENT_MESSAGE_SCHEDULER_INTERVAL_MS=2000

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
    "java -Dserver.port=$SERVER_PORT -Dserver.connectionsPoolSize=$SERVER_CONNECTIONS_POOL_SIZE -jar server/target/server-1.0-SNAPSHOT-jar-with-dependencies.jar; exec bash"

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
        "java -Dserver.host=$SERVER_HOST -Dserver.port=$SERVER_PORT -Dscheduler.message-send.intervalInMs=$CLIENT_MESSAGE_SCHEDULER_INTERVAL_MS -jar mock-news-feed-client/target/mock-news-feed-client-1.0-SNAPSHOT-jar-with-dependencies.jar; exec bash"
done

# Final output
echo "---------------------------------------------------"
echo "Deployment successful"
echo "Server and clients are running in separate windows"
read -p "Press any key to exit this script..."
exit 0