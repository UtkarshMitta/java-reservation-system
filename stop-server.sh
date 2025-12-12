#!/bin/bash

echo "Stopping Reservo Server..."

# Find and kill server processes
SERVER_PIDS=$(lsof -ti:8081)
if [ -n "$SERVER_PIDS" ]; then
    echo "Found server processes: $SERVER_PIDS"
    kill $SERVER_PIDS 2>/dev/null
    sleep 2
    # Force kill if still running
    SERVER_PIDS=$(lsof -ti:8081)
    if [ -n "$SERVER_PIDS" ]; then
        echo "Force killing remaining processes..."
        kill -9 $SERVER_PIDS 2>/dev/null
    fi
    echo "✓ Server stopped"
else
    echo "No server process found on port 8081"
fi

# Also kill any Maven processes running spring-boot:run
MAVEN_PIDS=$(ps aux | grep "[s]pring-boot:run" | awk '{print $2}')
if [ -n "$MAVEN_PIDS" ]; then
    echo "Stopping Maven processes..."
    kill $MAVEN_PIDS 2>/dev/null
    sleep 1
    kill -9 $MAVEN_PIDS 2>/dev/null
fi

# Clean up PID file
rm -f /tmp/reservo-server.pid

echo "Done."

