# NYU Resource Reservation System

A Java-based reservation system for NYU students to book campus resources (study rooms, sports courts, lab benches, etc.).

## Prerequisites

- Java 21 or higher
- Maven 3.6 or higher

## How to Run

### Step 1: Start the Server

**On macOS/Linux:**
```bash
./start-server.sh
```

**On Windows:**
```cmd
start-server.bat
```

**Manual start:**
```bash
cd server
mvn spring-boot:run
```

The server will start on `http://localhost:8081`

### Step 2: Start the Client

**On macOS/Linux:**
```bash
./start-client.sh
```

**On Windows:**
```cmd
start-client.bat
```

**Manual start:**
```bash
cd client
mvn javafx:run
```

## Default Credentials

- **Admin**: username: `admin`, password: `admin123`
- **Test Users**: username: `user1`, `user2`, `user3`, password: `user123`

## Features

- User registration with NYU email verification (@nyu.edu)
- Resource booking with hold system
- Reservation management
- Waitlist functionality
- Real-time notifications
- Admin console for resource management
- Account settings (email, password, logout)

## Troubleshooting

**Port already in use:**
```bash
# Stop the server
./stop-server.sh

# Or kill processes on port 8081
lsof -ti:8081 | xargs kill -9
```

**Server not starting:**
- Ensure Java 21 is installed: `java -version`
- Check Maven is installed: `mvn -version`

**Client won't connect:**
- Make sure the server is running first
- Verify server is on port 8081

Utkarsh Mittal (um2100), Snigdha Shrivastava (ss19776)
