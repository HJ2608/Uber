# Uber Clone - Ride Sharing Application

A full-stack ride-sharing application built with Spring Boot, featuring real-time driver tracking, ride management, payment processing, and WebSocket-based notifications.

##  Features

### Core Functionality
- **User Authentication & Authorization**
  - JWT-based authentication
  - Role-based access control (USER, DRIVER)
  - OTP verification for login and ride start
  - Secure password hashing with BCrypt

- **Ride Management**
  - Create ride requests with pickup and drop locations
  - Real-time driver assignment
  - Ride status tracking (REQUESTED, ASSIGNED, STARTED, COMPLETED, CANCELLED)
  - OTP verification before ride start
  - ETA calculation using Google Maps API
  - Payment processing with multiple payment methods

- **Driver Management**
  - Driver registration and profile management
  - Online/offline status tracking
  - Driver location tracking and updates
  - Nearby ride matching based on driver location
  - Driver earnings and ledger management
  - Driver rating system

- **Real-time Communication**
  - WebSocket support for real-time notifications
  - STOMP protocol for messaging
  - Real-time ride request notifications to drivers
  - Ride status updates to customers

- **Geolocation Services**
  - Integration with Google Maps API
  - Geocoding (address to coordinates)
  - Reverse geocoding (coordinates to address)
  - Distance and time calculation
  - Route optimization

- **Cab Management**
  - Multiple cab types (MINI, SEDAN, SUV)
  - Cab registration and assignment to drivers
  - Cab availability management

##  Technology Stack

- **Backend Framework**: Spring Boot 3.5.8
- **Language**: Java 17
- **Database**: PostgreSQL
- **Security**: Spring Security with JWT
- **WebSocket**: Spring WebSocket with STOMP
- **Build Tool**: Maven
- **External APIs**: Google Maps API

##  Prerequisites

- Java 17 or higher
- Maven 3.6+
- PostgreSQL 12+
- Google Maps API Key

##  Quick Start

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd uber
   ```

2. **Set up PostgreSQL database**
   ```sql
   CREATE DATABASE uber_db;
   ```

3. **Configure application properties**
   - Update `src/main/resources/application.properties` with your database credentials
   - Add your Google Maps API key

4. **Run the application**
   ```bash
   mvn spring-boot:run
   ```

5. **Access the application**
   - API Base URL: `http://localhost:9090`
   - WebSocket Endpoint: `ws://localhost:9090/ws`

For detailed setup instructions, see [SETUP.md](SETUP.md)

## Documentation

- [API Documentation](API.md) - Complete API endpoint reference
- [Architecture Guide](ARCHITECTURE.md) - System design and component details
- [Setup Guide](SETUP.md) - Detailed installation and configuration

##  Security

- JWT-based stateless authentication
- Password encryption using BCrypt
- Role-based access control
- WebSocket authentication via JWT token
- CORS configuration for cross-origin requests

##  API Endpoints

### Authentication
- `POST /api/auth/signup` - User registration
- `POST /api/auth/login` - User login
- `POST /api/auth/send-login-otp` - Send OTP for login
- `POST /api/auth/verify-login-otp` - Verify OTP

### Rides
- `POST /api/rides` - Create a new ride
- `GET /api/rides/{rideId}` - Get ride details
- `POST /api/rides/{rideId}/assign-driver` - Assign driver to ride
- `POST /api/rides/start` - Start a ride with OTP
- `POST /api/rides/{rideId}/end` - End a ride
- `POST /api/rides/{rideId}/cancel` - Cancel a ride
- `GET /api/rides/current` - Get current ride for customer
- `GET /api/rides/{rideId}/eta` - Get estimated time of arrival
- `POST /api/rides/{rideId}/payment-success` - Mark payment as successful

### Drivers
- `GET /drivers` - Get all drivers
- `POST /drivers` - Create a new driver
- `GET /drivers/{driverId}/home` - Get driver dashboard
- `POST /drivers/{driverId}/status` - Update driver status
- `POST /drivers/{driverId}/accept/{rideId}` - Accept a ride

### Driver Location
- `PUT /api/driver-location/{driverId}` - Update driver location
- `GET /api/driver-location/{driverId}` - Get driver location

### Cabs
- `GET /api/cabs` - Get all cabs
- `POST /api/cabs` - Create a new cab
- `POST /api/cabs/assign-driver` - Assign driver to cab

### Geolocation
- `GET /api/geo/geocode` - Convert address to coordinates
- `GET /api/geo/reverse-geocode` - Convert coordinates to address
- `GET /api/geo/distance` - Calculate distance and time

For complete API documentation, see [API.md](API.md)

## WebSocket

### Connection
- **Endpoint**: `/ws`
- **Protocol**: STOMP over WebSocket
- **Authentication**: JWT token in Authorization header

### Topics
- `/queue/ride-requests{driverId}` - Ride requests for specific driver
- `/queue/ride-status{custId}` - Ride status updates for customer
- `/queue/ride-status{driverId}` - Ride status updates for driver

### Message Mappings
- `/app/driver/ride/response` - Driver response to ride request

##  Database Schema

The application uses PostgreSQL with the following main tables:
- `users` - User accounts and authentication
- `drivers` - Driver profiles and status
- `cabs` - Cab information
- `rides` - Ride records and status
- `driver_locations` - Real-time driver locations
- `otps` - OTP codes for verification
- `driver_ledger` - Driver earnings and transactions

See `src/main/resources/schema.sql` for complete schema definition.

##  Testing

Run tests with:
```bash
mvn test
```

## 👥 Contributing

This is a demo project. Contributions and suggestions are welcome!


