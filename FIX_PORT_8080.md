# Fix for Port 8080 Issue

## Problem
Port 8080 is being used by another process (likely Zookeeper or another Java service) that keeps restarting.

## Solution Options

### Option 1: Change Server Port (RECOMMENDED)
The server has been configured to use **port 8081** instead of 8080.

**Update the client to use port 8081:**
- Edit `client/src/main/java/edu/nyu/cs9053/reservo/client/api/ApiClient.java`
- Change `BASE_URL = "http://localhost:8080"` to `BASE_URL = "http://localhost:8081"`

### Option 2: Kill All Java Processes (AGGRESSIVE)
```bash
killall -9 java
```
⚠️ **Warning**: This will kill ALL Java processes including other applications!

### Option 3: Find and Kill Specific Process
```bash
# Find what's using port 8080
lsof -i:8080

# Kill the specific PID (replace XXXX with the PID)
kill -9 XXXX
```

### Option 4: Use a Different Port
Edit `server/src/main/resources/application.properties`:
```properties
server.port=8082  # or any other free port
```

## Current Configuration
- Server is now set to use **port 8081**
- Update client API URL to match

## Verify Port is Free
```bash
lsof -i:8081  # Check if 8081 is free
```

