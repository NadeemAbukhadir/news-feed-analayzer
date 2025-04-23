#!/bin/bash

# Service Names
SERVER_SERVICE_NAME="news-analyzer-server"
CLIENT_SERVICE_NAME="mock-news-client"

# Service files
LOG_DIR="logs"
SERVER_PID_FILE=".${SERVER_SERVICE_NAME}.pid"
CLIENT_PIDS_FILE=".${CLIENT_SERVICE_NAME}.pids"

# Terminal colors for better output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo "Stopping News Analyzer Services..."

# Function to check if a process exists
process_exists() {
    ps -p $1 > /dev/null 2>&1
    return $?
}

# Function to kill processes gracefully, then forcefully if needed
kill_process() {
    local pid=$1
    local name=$2

    if process_exists $pid; then
        echo -n "Stopping $name (PID: $pid)... "
        kill $pid 2>/dev/null

        # Give process time to terminate gracefully
        for i in {1..5}; do
            if ! process_exists $pid; then
                echo -e "${GREEN}Stopped${NC}"
                return 0
            fi
            sleep 1
        done

        # If still running, force kill
        echo -n "Still running, force killing... "
        kill -9 $pid 2>/dev/null

        if ! process_exists $pid; then
            echo -e "${GREEN}Terminated${NC}"
        else
            echo -e "${RED}Failed to terminate!${NC}"
            return 1
        fi
    else
        echo -e "${YELLOW}Process $name (PID: $pid) is not running${NC}"
    fi

    return 0
}

# Stop server
if [ -f "$SERVER_PID_FILE" ]; then
    SERVER_PID=$(cat $SERVER_PID_FILE)
    kill_process $SERVER_PID "$SERVER_SERVICE_NAME"
    rm -f $SERVER_PID_FILE
else
    echo -e "${YELLOW}Server PID file not found. The server may not be running or was started manually.${NC}"

    # Try to find the server process anyway
    SERVER_PIDS=$(pgrep -f "server-1.0-SNAPSHOT-jar-with-dependencies.jar")
    if [ -n "$SERVER_PIDS" ]; then
        echo -e "${YELLOW}Found server processes running without PID file.${NC}"
        for pid in $SERVER_PIDS; do
            kill_process $pid "Server (auto-detected)"
        done
    fi
fi

# Stop clients
CLIENT_COUNT=0
if [ -f "$CLIENT_PIDS_FILE" ]; then
    while read -r CLIENT_PID; do
        if [ -n "$CLIENT_PID" ]; then
            ((CLIENT_COUNT++))
            kill_process $CLIENT_PID "$CLIENT_SERVICE_NAME $CLIENT_COUNT"
        fi
    done < "$CLIENT_PIDS_FILE"
    rm -f $CLIENT_PIDS_FILE
else
    echo -e "${YELLOW}Client PIDs file not found. Clients may not be running or were started manually.${NC}"

    # Try to find client processes anyway
    CLIENT_PIDS=$(pgrep -f "mock-news-feed-client-1.0-SNAPSHOT-jar-with-dependencies.jar")
    if [ -n "$CLIENT_PIDS" ]; then
        echo -e "${YELLOW}Found client processes running without PID file.${NC}"
        for pid in $CLIENT_PIDS; do
            kill_process $pid "Client (auto-detected)"
            ((CLIENT_COUNT++))
        done
    fi
fi

# Final verification
echo "Verifying all processes stopped..."
REMAINING_SERVER_PIDS=$(pgrep -f "server-1.0-SNAPSHOT-jar-with-dependencies.jar")
REMAINING_CLIENT_PIDS=$(pgrep -f "mock-news-feed-client-1.0-SNAPSHOT-jar-with-dependencies.jar")

if [ -n "$REMAINING_SERVER_PIDS" ] || [ -n "$REMAINING_CLIENT_PIDS" ]; then
    echo -e "${RED}Some processes are still running:${NC}"
    if [ -n "$REMAINING_SERVER_PIDS" ]; then
        echo -e "${RED}Server PIDs still running: $REMAINING_SERVER_PIDS${NC}"
    fi
    if [ -n "$REMAINING_CLIENT_PIDS" ]; then
        echo -e "${RED}Client PIDs still running: $REMAINING_CLIENT_PIDS${NC}"
    fi
    echo -e "${YELLOW}You may need to kill these processes manually.${NC}"
else
    echo -e "${GREEN}All News Analyzer processes have been stopped successfully.${NC}"
fi

# Check and cleanup zombie processes
ZOMBIES=$(ps aux | grep -v grep | grep -E 'java.*\.jar' | grep 'Z')
if [ -n "$ZOMBIES" ]; then
    echo -e "${YELLOW}Zombie Java processes detected. These will be cleaned up by the system eventually.${NC}"
fi

# Cleanup any leftover pid files
if [ -f "$SERVER_PID_FILE" ]; then
    rm -f $SERVER_PID_FILE
    echo "Removed server PID file"
fi

if [ -f "$CLIENT_PIDS_FILE" ]; then
    rm -f $CLIENT_PIDS_FILE
    echo "Removed client PIDs file"
fi

# Port release verification
PORT_CHECK=$(ss -tuln | grep ":8080 ")
if [ -n "$PORT_CHECK" ]; then
    echo -e "${YELLOW}Warning: Port 8080 is still in use. It may not have been released yet or is used by another application.${NC}"
else
    echo -e "${GREEN}Port 8080 is now available.${NC}"
fi

echo "---------------------------------------------------"
echo -e "${GREEN}Cleanup complete${NC}"
echo "Log files are still available in the $LOG_DIR directory"
echo "---------------------------------------------------"

exit 0