#!/bin/bash

echo "Terminating News Analyzer Server and Clients..."

# Kill server processes
server_pids=$(pgrep -f "server/target/server-1.0-SNAPSHOT-jar-with-dependencies.jar")
if [ -n "$server_pids" ]; then
    kill -9 $server_pids
    echo "Server terminated"
else
    echo "No server process found"
fi

# Kill client processes
client_pids=$(pgrep -f "mock-news-feed-client/target/mock-news-feed-client-1.0-SNAPSHOT-jar-with-dependencies.jar")
client_count=$(echo "$client_pids" | wc -w)
if [ $client_count -gt 0 ]; then
    kill -9 $client_pids
    echo "Terminated $client_count client instances"
else
    echo "No client processes found"
fi

# Final verification
echo "Checking remaining processes..."
if pgrep -f "server/target/server-1.0-SNAPSHOT-jar-with-dependencies.jar" >/dev/null; then
    echo "[WARNING] Server process still running!"
fi

if pgrep -f "mock-news-feed-client/target/mock-news-feed-client-1.0-SNAPSHOT-jar-with-dependencies.jar" >/dev/null; then
    echo "[WARNING] Client processes still running!"
fi

echo "Cleanup complete"
read -p "Press any key to exit..."