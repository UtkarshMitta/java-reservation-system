# Quick Start Guide - Reservo

## ✅ Server Status
The server is now running successfully on **http://localhost:8080**

## Starting the Application

### Option 1: Use the Startup Scripts

**Terminal 1 - Server:**
```bash
cd "/Users/rajendraprasadmittal/Downloads/Java Project"
./start-server.sh
```

**Terminal 2 - Client:**
```bash
cd "/Users/rajendraprasadmittal/Downloads/Java Project"
./start-client.sh
```

### Option 2: Manual Start

**Server:**
```bash
cd "/Users/rajendraprasadmittal/Downloads/Java Project/server"
export PATH="/opt/homebrew/opt/openjdk@21/bin:/opt/homebrew/bin:$PATH"
mvn spring-boot:run
```

**Client:**
```bash
cd "/Users/rajendraprasadmittal/Downloads/Java Project/client"
export PATH="/opt/homebrew/opt/openjdk@21/bin:/opt/homebrew/bin:$PATH"
mvn javafx:run
```

## If Port 8080 is Already in Use

```bash
# Kill any process using port 8080
lsof -ti:8080 | xargs kill -9

# Or kill all Java processes (more aggressive)
killall -9 java
```

## Default Credentials

- **Admin**: username: `admin`, password: `admin123`
- **Test Users**: username: `user1`, `user2`, `user3`, password: `user123`

## Test the Server

```bash
# Get all resources
curl http://localhost:8080/resources

# Login
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

## H2 Database Console

Access at: http://localhost:8080/h2-console

- JDBC URL: `jdbc:h2:file:./data/reservo`
- Username: `sa`
- Password: (leave empty)

## Troubleshooting

1. **Port 8080 in use**: See "If Port 8080 is Already in Use" above
2. **Java version**: Ensure Java 21 is in PATH: `export PATH="/opt/homebrew/opt/openjdk@21/bin:/opt/homebrew/bin:$PATH"`
3. **Maven not found**: Install via Homebrew: `brew install maven`
4. **Database errors**: Delete and recreate: `rm -rf server/data`

## Project Structure

- `server/` - Spring Boot REST API server
- `client/` - JavaFX desktop client
- `README.md` - Full project documentation

