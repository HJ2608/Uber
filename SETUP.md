# Setup Guide

Complete guide for setting up and running the Uber Clone application.

## Table of Contents

1. [Prerequisites](#prerequisites)
2. [Database Setup](#database-setup)
3. [Application Configuration](#application-configuration)
4. [Google Maps API Setup](#google-maps-api-setup)
5. [Building the Application](#building-the-application)
6. [Running the Application](#running-the-application)
7. [Verifying Installation](#verifying-installation)
8. [Troubleshooting](#troubleshooting)

## Prerequisites

### Required Software

1. **Java Development Kit (JDK) 17 or higher**
   - Download from: https://adoptium.net/
   - Verify installation:
     ```bash
     java -version
     ```
   - Should show version 17 or higher

2. **Maven 3.6+**
   - Download from: https://maven.apache.org/download.cgi
   - Verify installation:
     ```bash
     mvn -version
     ```

3. **PostgreSQL 12+**
   - Download from: https://www.postgresql.org/download/
   - Verify installation:
     ```bash
     psql --version
     ```

4. **Git** (optional, for cloning)
   - Download from: https://git-scm.com/downloads

### Required Accounts

- **Google Cloud Platform Account** (for Maps API key)
  - Sign up at: https://cloud.google.com/

## Database Setup

### Step 1: Create PostgreSQL Database

1. **Start PostgreSQL service**
   ```bash
   # Windows (as Administrator)
   net start postgresql-x64-XX
   
   # Linux/Mac
   sudo systemctl start postgresql
   # or
   brew services start postgresql
   ```

2. **Connect to PostgreSQL**
   ```bash
   psql -U postgres
   ```

3. **Create database**
   ```sql
   CREATE DATABASE uber_db;
   ```

4. **Verify database creation**
   ```sql
   \l
   ```
   You should see `uber_db` in the list.

5. **Exit PostgreSQL**
   ```sql
   \q
   ```

### Step 2: Database Schema

The application automatically creates the schema on startup using `schema.sql`. However, you can manually verify:

1. **Connect to the database**
   ```bash
   psql -U postgres -d uber_db
   ```

2. **Check tables** (after first run)
   ```sql
   \dt
   ```

   You should see tables:
   - `users`
   - `drivers`
   - `cabs`
   - `rides`
   - `driver_locations`
   - `otps`
   - `driver_ledger`

## Application Configuration

### Step 1: Locate Configuration File

Navigate to:
```
uber/src/main/resources/application.properties
```

### Step 2: Configure Database Connection

Update the following properties with your PostgreSQL credentials:

```properties
# Database Configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/uber_db
spring.datasource.username=postgres
spring.datasource.password=YOUR_PASSWORD_HERE
spring.datasource.driver-class-name=org.postgresql.Driver

# Schema initialization
spring.sql.init.mode=always
spring.sql.init.schema-locations=classpath:schema.sql
```

**Important:** Replace `YOUR_PASSWORD_HERE` with your actual PostgreSQL password.

### Step 3: Configure JWT Secret Key

Generate a secure secret key for JWT tokens:

```properties
jwt.secret_key=YOUR_SECRET_KEY_HERE
```

**Generate a secure key:**
```bash
# Using OpenSSL
openssl rand -hex 32

# Or use any random 32+ character string
```

### Step 4: Configure Server Port (Optional)

Default port is 9090. To change:

```properties
server.port=9090
```

## Google Maps API Setup

### Step 1: Create Google Cloud Project

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project or select existing one
3. Note your project ID

### Step 2: Enable APIs

1. Navigate to **APIs & Services** > **Library**
2. Enable the following APIs:
   - **Maps JavaScript API**
   - **Geocoding API**
   - **Distance Matrix API**

### Step 3: Create API Key

1. Go to **APIs & Services** > **Credentials**
2. Click **Create Credentials** > **API Key**
3. Copy the generated API key
4. (Recommended) Restrict the API key:
   - Click on the key name
   - Under **API restrictions**, select **Restrict key**
   - Choose the APIs you enabled
   - Under **Application restrictions**, add your domain/IP

### Step 4: Add API Key to Application

Update `application.properties`:

```properties
google.maps.api.key=YOUR_GOOGLE_MAPS_API_KEY_HERE
google.maps.region=in
google.maps.language=en
google.maps.base-url=https://maps.googleapis.com/maps/api
```

**Important:** Replace `YOUR_GOOGLE_MAPS_API_KEY_HERE` with your actual API key.

## Building the Application

### Step 1: Navigate to Project Directory

```bash
cd uber
```

### Step 2: Clean and Build

```bash
# Clean previous builds
mvn clean

# Compile and package
mvn package

# Or skip tests
mvn package -DskipTests
```

### Step 3: Verify Build

After successful build, you should see:
```
BUILD SUCCESS
```

The JAR file will be created at:
```
uber/target/uber-0.0.1-SNAPSHOT.jar
```

## Running the Application

### Option 1: Using Maven (Recommended for Development)

```bash
mvn spring-boot:run
```

### Option 2: Using JAR File

```bash
java -jar target/uber-0.0.1-SNAPSHOT.jar
```

### Option 3: Using IDE

1. **IntelliJ IDEA:**
   - Open project
   - Right-click `UberApplication.java`
   - Select **Run 'UberApplication'**

2. **Eclipse:**
   - Import as Maven project
   - Right-click `UberApplication.java`
   - Run As > Java Application

3. **VS Code:**
   - Install Java Extension Pack
   - Open project
   - Run from Run and Debug panel

### Expected Output

You should see logs similar to:

```
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::                (v3.5.8)

2024-01-15 10:00:00.000  INFO --- [main] c.f.u.UberApplication : Starting UberApplication
2024-01-15 10:00:01.000  INFO --- [main] c.f.u.UberApplication : Started UberApplication in 2.5 seconds
```

## Verifying Installation

### Step 1: Health Check

Open your browser or use curl:

```bash
curl http://localhost:9090/ping
```

Expected response:
```
pong
```

### Step 2: Check Database Connection

The application should automatically:
- Connect to PostgreSQL
- Create tables from `schema.sql`
- Be ready to accept requests

Check application logs for any database connection errors.

### Step 3: Test API Endpoint

Test a simple endpoint:

```bash
# Get all users (should return empty array initially)
curl http://localhost:9090/CRUD

# Expected: []
```

### Step 4: Test Authentication

```bash
# Sign up a new user
curl -X POST http://localhost:9090/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Test",
    "lastName": "User",
    "email": "test@example.com",
    "mobileNum": "+1234567890",
    "password": "password123",
    "role": "USER"
  }'
```

Expected response:
```json
{
  "userId": 1,
  "email": "test@example.com",
  "message": "User registered successfully"
}
```

## Environment Variables (Alternative Configuration)

Instead of modifying `application.properties`, you can use environment variables:

### Windows (PowerShell)

```powershell
$env:SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:5432/uber_db"
$env:SPRING_DATASOURCE_USERNAME="postgres"
$env:SPRING_DATASOURCE_PASSWORD="your_password"
$env:JWT_SECRET_KEY="your_secret_key"
$env:GOOGLE_MAPS_API_KEY="your_api_key"
```

### Linux/Mac

```bash
export SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:5432/uber_db"
export SPRING_DATASOURCE_USERNAME="postgres"
export SPRING_DATASOURCE_PASSWORD="your_password"
export JWT_SECRET_KEY="your_secret_key"
export GOOGLE_MAPS_API_KEY="your_api_key"
```

Then run:
```bash
mvn spring-boot:run
```

## Troubleshooting

### Issue: Database Connection Failed

**Error:**
```
org.postgresql.util.PSQLException: Connection refused
```

**Solutions:**
1. Verify PostgreSQL is running:
   ```bash
   # Windows
   net start postgresql-x64-XX
   
   # Linux/Mac
   sudo systemctl status postgresql
   ```

2. Check connection details in `application.properties`
3. Verify database exists:
   ```sql
   \l
   ```

4. Check PostgreSQL port (default: 5432):
   ```bash
   # Windows
   netstat -an | findstr 5432
   
   # Linux/Mac
   netstat -an | grep 5432
   ```

### Issue: Port Already in Use

**Error:**
```
Port 9090 is already in use
```

**Solutions:**
1. Change port in `application.properties`:
   ```properties
   server.port=9091
   ```

2. Or kill the process using port 9090:
   ```bash
   # Windows
   netstat -ano | findstr :9090
   taskkill /PID <PID> /F
   
   # Linux/Mac
   lsof -ti:9090 | xargs kill -9
   ```

### Issue: Schema Creation Failed

**Error:**
```
Error creating schema
```

**Solutions:**
1. Check database user permissions:
   ```sql
   GRANT ALL PRIVILEGES ON DATABASE uber_db TO postgres;
   ```

2. Verify `schema.sql` file exists in `src/main/resources/`

3. Check application logs for specific SQL errors

### Issue: Google Maps API Errors

**Error:**
```
Google Maps API error: REQUEST_DENIED
```

**Solutions:**
1. Verify API key is correct
2. Check API key restrictions
3. Ensure required APIs are enabled:
   - Geocoding API
   - Distance Matrix API
4. Check billing is enabled for your Google Cloud project

### Issue: JWT Token Errors

**Error:**
```
JWT signature does not match
```

**Solutions:**
1. Ensure `jwt.secret_key` is set in `application.properties`
2. Use the same secret key for token generation and validation
3. Secret key should be at least 32 characters

### Issue: Maven Build Fails

**Error:**
```
Could not resolve dependencies
```

**Solutions:**
1. Check internet connection
2. Update Maven:
   ```bash
   mvn clean install -U
   ```

3. Clear Maven cache:
   ```bash
   # Windows
   rmdir /s %USERPROFILE%\.m2\repository
   
   # Linux/Mac
   rm -rf ~/.m2/repository
   ```

### Issue: WebSocket Connection Failed

**Error:**
```
WebSocket connection failed
```

**Solutions:**
1. Verify WebSocket endpoint: `ws://localhost:9090/ws`
2. Check CORS configuration in `WebSocketConfig`
3. Ensure JWT token is included in CONNECT headers:
   ```
   Authorization: Bearer <token>
   ```

## Development Tips

### Hot Reload

Spring Boot DevTools is included for automatic restart:

1. Make code changes
2. Save file
3. Application automatically restarts

### Database Reset

To reset the database:

1. Drop and recreate database:
   ```sql
   DROP DATABASE uber_db;
   CREATE DATABASE uber_db;
   ```

2. Restart application (schema will be recreated)

### Logging

Adjust log levels in `application.properties`:

```properties
logging.level.com.firstapp.uber=DEBUG
logging.level.org.springframework.web=DEBUG
```

## Next Steps

After successful setup:

1. Read [API.md](API.md) for API documentation
2. Read [ARCHITECTURE.md](ARCHITECTURE.md) for system design
3. Test endpoints using Postman or curl
4. Set up WebSocket client for real-time features
5. Configure production environment variables

## Production Deployment

For production deployment:

1. Use environment variables instead of `application.properties`
2. Set up proper database credentials
3. Use HTTPS for API endpoints
4. Configure CORS properly
5. Set up monitoring and logging
6. Use connection pooling
7. Enable database backups
8. Set up load balancing (if scaling)

## Support

If you encounter issues not covered here:

1. Check application logs
2. Review [ARCHITECTURE.md](ARCHITECTURE.md)
3. Check Spring Boot documentation
4. Review PostgreSQL logs
5. Check Google Cloud Console for API issues

