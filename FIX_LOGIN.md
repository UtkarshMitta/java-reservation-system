# Fixed Login Credentials Issue

## Problem
The password hashes in the database didn't match the actual passwords.

## Solution Applied
✅ Updated `data.sql` with correct BCrypt password hashes:
- `admin123` → Correct hash generated
- `user123` → Correct hash generated

## Next Steps

### 1. Restart the Server
The database has been cleared. You need to restart the server to recreate it with the correct passwords:

```bash
# Stop the current server (Ctrl+C in the terminal running it)
# Then restart:
cd "/Users/rajendraprasadmittal/Downloads/Java Project"
./start-server.sh
```

### 2. Test Login
Once the server restarts, try logging in with:

**Admin:**
- Username: `admin`
- Password: `admin123`

**Users:**
- Username: `user1`, `user2`, or `user3`
- Password: `user123`

### 3. Or Test via API
```bash
curl -X POST http://localhost:8081/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

You should now get a token back instead of "Invalid credentials"!

