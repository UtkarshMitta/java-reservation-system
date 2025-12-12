# Reservo: A Transaction-Safe Multi-Resource Reservation System

A comprehensive Java application demonstrating transaction safety, concurrency control, and real-time updates for campus resource reservations.

## Features

- **Multi-Resource Booking**: Support for various resources (study rooms, sports courts, lab benches, etc.)
- **Hold System**: Short-lived holds with TTL to prevent double bookings
- **Waitlist Management**: FIFO waitlist with automatic promotion
- **Real-Time Updates**: WebSocket-based live updates for all connected clients
- **Transaction Safety**: Optimistic and pessimistic locking to prevent race conditions
- **Audit Trail**: Complete event logging for accountability
- **Admin Console**: Resource management and contention simulation

## Prerequisites

- Java 21 or higher
- Maven 3.6 or higher
- Internet connection (for downloading dependencies)

## Quick Start

### Option 1: Using Startup Scripts (Easiest)

**On macOS/Linux:**
```bash
# Terminal 1 - Start Server
./start-server.sh

# Terminal 2 - Start Client
./start-client.sh
```

**On Windows:**
```cmd
REM Command Prompt 1 - Start Server
start-server.bat

REM Command Prompt 2 - Start Client
start-client.bat
```

### Option 2: Manual Start

**1. Build the Project**

```bash
cd "Java Project"
mvn clean install
```

**2. Start the Server**

```bash
cd server
mvn spring-boot:run
```

The server will start on `http://localhost:8080`

**3. Start the Client**

In a new terminal:

```bash
cd client
mvn javafx:run
```

Or run the JAR directly (after building):

```bash
java -jar target/reservo-client-1.0.0.jar
```

## Default Credentials

- **Admin**: username: `admin`, password: `admin123`
- **Test Users**: username: `user1`, `user2`, `user3`, password: `user123`

## Project Structure

```
Java Project/
├── server/              # Spring Boot server application
│   ├── src/main/java/   # Server source code
│   └── src/main/resources/  # Configuration and SQL
├── client/              # JavaFX client application
│   └── src/main/java/   # Client source code
└── README.md           # This file
```

## API Endpoints

### Authentication
- `POST /auth/login` - Login
- `POST /auth/register` - Register new user

### Resources
- `GET /resources` - List all resources
- `GET /resources/{id}/availability?from={date}&to={date}` - Get availability

### Booking
- `POST /holds` - Place a hold on a time slot
- `POST /holds/{id}/confirm` - Confirm a hold (creates reservation)
- `DELETE /holds/{id}` - Cancel a hold
- `POST /reservations/{id}/cancel` - Cancel a reservation
- `POST /waitlist` - Join waitlist for a full slot

### User Data
- `GET /my-reservations` - Get user's reservations
- `GET /my-waitlist` - Get user's waitlist entries
- `GET /notifications` - Get user's notifications

### Admin
- `POST /admin/resources` - Create a new resource
- `POST /admin/simulate-contention` - Simulate concurrent booking attempts
- `POST /admin/isolation-mode` - Change transaction isolation level

### WebSocket
- `WS /ws` - Real-time event stream (AvailabilityChanged, HoldExpired, Promoted, etc.)

## Database

The application uses H2 embedded database by default. The database file is created at `./data/reservo.mv.db` in the server directory.

### Access H2 Console

1. Start the server
2. Navigate to `http://localhost:8080/h2-console`
3. JDBC URL: `jdbc:h2:file:./data/reservo`
4. Username: `sa`
5. Password: (leave empty)

## Key Concepts Demonstrated

### 1. Transaction Safety
- **Optimistic Locking**: Version fields prevent lost updates
- **Pessimistic Locking**: `SELECT FOR UPDATE` ensures single-winner guarantees
- **Isolation Levels**: Configurable isolation (READ_COMMITTED, SERIALIZABLE)

### 2. Concurrency Control
- Hold system with TTL prevents long-lived locks
- Atomic capacity decrements with version checks
- Background scheduler for hold expiry and waitlist promotion

### 3. Real-Time Updates
- WebSocket events broadcast to all connected clients
- No polling required - instant UI updates

### 4. Audit Trail
- Append-only audit log records all state transitions
- Traceable history for debugging and accountability

## Demo Scenarios

### Scenario 1: Race for Last Seat
1. Open two client instances
2. Login as different users
3. Both try to book the same slot with capacity 1
4. One succeeds, the other gets a conflict message
5. Both UIs update instantly via WebSocket

### Scenario 2: Waitlist Promotion
1. Book all slots for a resource
2. Join waitlist as another user
3. Cancel a reservation
4. Waitlist user is automatically promoted and notified

### Scenario 3: Hold Expiry
1. Place a hold on a slot
2. Wait 60 seconds without confirming
3. Hold expires automatically
4. Capacity becomes available again
5. Waitlist users can be promoted

## Configuration

### Server Configuration (`server/src/main/resources/application.properties`)

- `server.port`: Server port (default: 8080)
- `reservo.hold.ttl-seconds`: Hold expiration time (default: 60)
- `reservo.scheduler.interval-seconds`: Scheduler run interval (default: 5)

### Database Configuration

To use PostgreSQL instead of H2:

1. Update `server/pom.xml` to include PostgreSQL driver
2. Update `application.properties` with PostgreSQL connection details
3. Run migrations manually or use Flyway/Liquibase

## Building JARs

### Server JAR
```bash
cd server
mvn clean package
# JAR will be at: target/reservo-server-1.0.0.jar
java -jar target/reservo-server-1.0.0.jar
```

### Client JAR
```bash
cd client
mvn clean package
# JAR will be at: target/reservo-client-1.0.0.jar
java -jar target/reservo-client-1.0.0.jar
```

## Troubleshooting

### Server won't start
- Check if port 8080 is available
- Ensure Java 21 is installed: `java -version`
- Check Maven is installed: `mvn -version`

### Client won't connect
- Ensure server is running first
- Check server URL in `ApiClient.java` (default: `http://localhost:8080`)

### Database errors
- Delete `./data/reservo.mv.db` to reset database
- Check H2 console for database state

## Development Notes

- The project uses Spring Boot for the server (minimal configuration)
- JavaFX is used for the desktop client
- WebSocket uses Spring's STOMP over SockJS
- Authentication uses simple token-based system (in-memory)
- For production, implement JWT tokens and proper security

## Team

- Snigdha Srivastva (ss19776)
- Utkarsh Mittal (um2100)

## License

This project is created for educational purposes as part of CS9053 - Introduction to Java, Fall 2025.

