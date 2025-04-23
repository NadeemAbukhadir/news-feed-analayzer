#!/bin/bash

# Determine project root directory relative to this script
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"

# Service Names
SERVER_SERVICE_NAME="news-analyzer-server"
CLIENT_SERVICE_NAME="mock-news-client"

# Configuration PROPERTIES
# Server properties
SERVER_PORT=8080
SERVER_CONNECTIONS_POOL_SIZE=10
NEWS_SUMMARY_REPORT_PERIOD_IN_SECONDS=10
# Client properties
NEWS_ANALYZE_SERVER_HOST=localhost
NEWS_ANALYZE_SERVER_PORT=8080
SEND_MESSAGE_INTERVAL_IN_MS=200

# Service paths with updated absolute paths
SERVER_JAR="${PROJECT_ROOT}/server/target/server-1.0-SNAPSHOT-jar-with-dependencies.jar"
CLIENT_JAR="${PROJECT_ROOT}/mock-news-feed-client/target/mock-news-feed-client-1.0-SNAPSHOT-jar-with-dependencies.jar"
LOG_DIR="${PROJECT_ROOT}/logs"
SERVER_PID_FILE="${PROJECT_ROOT}/.${SERVER_SERVICE_NAME}.pid"
CLIENT_PIDS_FILE="${PROJECT_ROOT}/.${CLIENT_SERVICE_NAME}.pids"

# Create log directory if it doesn't exist
mkdir -p "$LOG_DIR"

# Check if server is already running
if [ -f "$SERVER_PID_FILE" ]; then
    SERVER_PID=$(cat "$SERVER_PID_FILE")
    if ps -p $SERVER_PID > /dev/null; then
        echo "$SERVER_SERVICE_NAME is already running with PID $SERVER_PID"
        exit 1
    fi
fi

# Preparation section - check if port is available
if ss -tuln | grep -q ":$SERVER_PORT "; then
    echo "[ERROR] Port $SERVER_PORT is in use"
    exit 1
fi

# Build section - run Maven from project root
echo "Building project..."
cd "$PROJECT_ROOT" && mvn clean package
if [ $? -ne 0 ]; then
    echo "[ERROR] Build failed"
    exit 1
fi

# Check if JAR files exist
if [ ! -f "$SERVER_JAR" ]; then
    echo "[ERROR] Server JAR file not found: $SERVER_JAR"
    exit 1
fi

if [ ! -f "$CLIENT_JAR" ]; then
    echo "[ERROR] Client JAR file not found: $CLIENT_JAR"
    exit 1
fi

# Start the server
echo "Starting $SERVER_SERVICE_NAME..."
java -Dserver.port=$SERVER_PORT \
     -Dserver.connectionsPoolSize=$SERVER_CONNECTIONS_POOL_SIZE \
     -Dscheduler.news-summary-report.periodInSeconds=$NEWS_SUMMARY_REPORT_PERIOD_IN_SECONDS \
     -jar "$SERVER_JAR" > "$LOG_DIR/${SERVER_SERVICE_NAME}.log" 2>&1 &

# Save server PID
SERVER_PID=$!
echo $SERVER_PID > "$SERVER_PID_FILE"
echo "$SERVER_SERVICE_NAME started with PID $SERVER_PID"

# Wait for server to be ready
echo "Waiting for $SERVER_SERVICE_NAME to initialize..."
SERVER_READY=false
for i in {1..15}; do
    if ss -tuln | grep -q ":$SERVER_PORT "; then
        SERVER_READY=true
        echo "$SERVER_SERVICE_NAME is running and listening on port $SERVER_PORT"
        break
    fi
    sleep 1
done

if [ "$SERVER_READY" = false ]; then
    echo "[ERROR] $SERVER_SERVICE_NAME failed to start within 15 seconds"
    kill $SERVER_PID 2>/dev/null
    rm -f "$SERVER_PID_FILE"
    exit 1
fi

# Start client instances
echo "Starting $CLIENT_SERVICE_NAME instances..."
CLIENT_PIDS=()

for ((i=1; i<=SERVER_CONNECTIONS_POOL_SIZE; i++)); do
    echo "Starting $CLIENT_SERVICE_NAME instance $i..."
    java -Dserver.host=$NEWS_ANALYZE_SERVER_HOST \
         -Dserver.port=$NEWS_ANALYZE_SERVER_PORT \
         -Dscheduler.message-send.intervalInMs=$SEND_MESSAGE_INTERVAL_IN_MS \
         -jar "$CLIENT_JAR" > "$LOG_DIR/${CLIENT_SERVICE_NAME}_${i}.log" 2>&1 &
    
    CLIENT_PID=$!
    CLIENT_PIDS+=($CLIENT_PID)
    echo "$CLIENT_SERVICE_NAME instance $i started with PID $CLIENT_PID"
done

# Save client PIDs
printf "%s\n" "${CLIENT_PIDS[@]}" > "$CLIENT_PIDS_FILE"

# Summary
echo "---------------------------------------------------"
echo "Deployment successful"
echo "$SERVER_SERVICE_NAME is running with PID $SERVER_PID"
echo "$SERVER_CONNECTIONS_POOL_SIZE $CLIENT_SERVICE_NAME instances are running"
echo ""
echo "Log locations:"
echo "  Server log: $LOG_DIR/${SERVER_SERVICE_NAME}.log"
echo "  Client logs: $LOG_DIR/${CLIENT_SERVICE_NAME}_*.log"
echo ""
echo "To monitor server logs: tail -f $LOG_DIR/${SERVER_SERVICE_NAME}.log"
echo "To monitor a specific client log: tail -f $LOG_DIR/${CLIENT_SERVICE_NAME}_1.log"
echo ""
echo "To stop all services, use: $SCRIPT_DIR/stop.sh"
echo "---------------------------------------------------"

exit 0