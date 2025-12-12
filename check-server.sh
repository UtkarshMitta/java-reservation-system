#!/bin/bash

echo "Checking Reservo Server Status..."
echo ""

# Check if port is in use
if lsof -ti:8081 > /dev/null 2>&1; then
    PID=$(lsof -ti:8081 | head -1)
    echo "✓ Server is RUNNING"
    echo "  Process ID: $PID"
    echo "  Port: 8081"
    echo ""
    
    # Test if server responds
    if curl -s http://localhost:8081/resources > /dev/null 2>&1; then
        echo "✓ Server is responding to requests"
        echo ""
        echo "Server URL: http://localhost:8081"
        echo "H2 Console: http://localhost:8081/h2-console"
    else
        echo "⚠ Server is running but not responding"
    fi
else
    echo "✗ Server is NOT running"
    echo ""
    echo "To start the server, run: ./start-server.sh"
fi

