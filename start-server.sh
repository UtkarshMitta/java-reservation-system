#!/bin/bash

export PATH="/opt/homebrew/opt/openjdk@21/bin:/opt/homebrew/bin:$PATH"

# Check if server is already running
if lsof -ti:8081 > /dev/null 2>&1; then
    echo "⚠ Server is already running on port 8081"
    echo "To stop it, run: ./stop-server.sh"
    exit 1
fi

echo "Starting Reservo Server..."
cd server

# Run in background and save PID
nohup mvn spring-boot:run > ../server.log 2>&1 &
SERVER_PID=$!
echo $SERVER_PID > /tmp/reservo-server.pid

echo "Server starting in background (PID: $SERVER_PID)"
echo "Logs are being written to: server.log"
echo ""
echo "To check status: ./check-server.sh"
echo "To stop server: ./stop-server.sh"
echo "To view logs: tail -f server.log"
echo ""
sleep 3

# Check if it started successfully
if ps -p $SERVER_PID > /dev/null; then
    echo "✓ Server process started"
    echo "Waiting for server to be ready..."
    for i in {1..30}; do
        if curl -s http://localhost:8081/resources > /dev/null 2>&1; then
            echo "✓ Server is ready and responding!"
            echo "Server URL: http://localhost:8081"
            break
        fi
        sleep 1
    done
else
    echo "✗ Server failed to start. Check server.log for details."
    exit 1
fi

