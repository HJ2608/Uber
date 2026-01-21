# Architecture Documentation

This document describes the system architecture, design patterns, and component structure of the Uber Clone application.

## System Overview

The application follows a layered architecture pattern with clear separation of concerns:

```
┌─────────────────────────────────────────┐
│         Client Applications             │
│   (Web, Mobile, Driver App)             │
└──────────────┬──────────────────────────┘
               │
               │ HTTP/REST + WebSocket
               │
┌──────────────▼──────────────────────────┐
│      Spring Boot Application            │
│  ┌──────────────────────────────────┐   │
│  │   Controllers (REST + WebSocket) │   │
│  └──────────────┬───────────────────┘   │
│                 │                       │
│  ┌──────────────▼───────────────────┐   │
│  │      Service Layer               │   │
│  └──────────────┬───────────────────┘   │
│                 │                       │
│  ┌──────────────▼───────────────────┐   │
│  │      Repository Layer            │   │
│  └──────────────┬───────────────────┘   │
└─────────────────┼───────────────────────┘
                  │
                  │ JPA / JDBC
                  │
┌─────────────────▼───────────────────────┐
│         PostgreSQL Database             │
└─────────────────────────────────────────┘
```

## Component Structure

### Package Organization

```
com.firstapp.uber/
├── auth/                    # Authentication & Authorization
│   ├── AuthController       # Login/Signup endpoints
│   ├── AuthService          # Business logic for auth
│   ├── JwtService           # JWT token generation/validation
│   ├── JwtAuthFilter        # JWT authentication filter
│   └── CustomUserDetails    # User principal implementation
│
├── config/                  # Configuration classes
│   ├── SecurityConfig       # Spring Security configuration
│   ├── WebSocketConfig      # WebSocket/STOMP configuration
│   ├── WebSocketAuthInterceptor  # WebSocket authentication
│   └── RestTemplateConfig  # HTTP client configuration
│
├── controller/              # REST Controllers
│   ├── ride/               # Ride management endpoints
│   ├── driver/             # Driver management endpoints
│   ├── driverlocation/     # Driver location endpoints
│   ├── cab/                # Cab management endpoints
│   ├── otp/                # OTP endpoints
│   └── google/             # Geolocation endpoints
│
├── service/                 # Business Logic Layer
│   ├── ride/               # Ride service implementations
│   ├── driver/             # Driver service implementations
│   ├── driverlocation/     # Location tracking services
│   ├── cab/                # Cab management services
│   ├── otp/                # OTP generation/verification
│   └── google/             # Google Maps API integration
│
├── repository/              # Data Access Layer
│   ├── ride/               # Ride data access
│   ├── driver/             # Driver data access
│   ├── driverlocation/     # Location data access
│   ├── cab/                # Cab data access
│   └── otp/                # OTP data access
│
├── dto/                     # Data Transfer Objects
│   ├── ride/               # Ride DTOs
│   ├── driver/             # Driver DTOs
│   ├── cab/                # Cab DTOs
│   ├── driverledger/       # Driver Ledger DTO
│   ├── driverlocation/     # Driver Location DTO
│   ├── otp/                # Otp DTO
│   └── user/               # User DTO
│
├── websocket/               # WebSocket Components
│   ├── controller/         # WebSocket message handlers
│   ├── listener/           # WebSocket event listeners
│   └── registry/           # Session management
│
└── model/                   # Domain Models
    ├── User                 # User entity
    ├── DriverStatus         # Driver status enum
    ├── PaymentMode          # Payment mode enum
    ├── OtpPurpose           # Otp purpose enum
    ├── PaymentStatus        # Payment Status enum
    └── Status               # Ride Status enum
```

## Core Components

### 1. Authentication & Security

#### JWT Authentication Flow

```
Client Request
    │
    ├─► JwtAuthFilter (checks Authorization header)
    │       │
    │       ├─► Valid Token? ──► Extract User Principal
    │       │                       │
    │       └─► Invalid Token ──► 401 Unauthorized
    │
    └─► Controller (with authenticated principal)
```

**Key Components:**
- `JwtAuthFilter`: Intercepts HTTP requests and validates JWT tokens
- `JwtService`: Handles token generation, validation, and user extraction
- `SecurityConfig`: Configures Spring Security filter chain
- `CustomUserDetails`: Custom user principal implementation

#### WebSocket Authentication

WebSocket connections are authenticated via `WebSocketAuthInterceptor`:
- Intercepts STOMP CONNECT messages
- Extracts JWT token from Authorization header
- Validates token and sets user principal
- Rebuilds message with authenticated principal

### 2. Ride Management System

#### Ride Lifecycle

```
REQUESTED ──► ASSIGNED ──► STARTED ──► COMPLETED
    │             │            │            │
    └─────────────┴────────────┴────────────┘
                    CANCELLED
```

**Key Services:**
- `RideService`: Core ride business logic
- `RideServiceImpl`: Implementation with driver matching
- `RideRepository`: Data access layer

**Features:**
- Automatic driver matching based on proximity
- OTP verification before ride start
- Real-time status updates via WebSocket
- Payment processing integration

### 3. Driver Management

#### Driver Status Management

```
OFFLINE ──► ONLINE ──► BUSY (when ride assigned)
```

**Key Components:**
- `DriverService`: Driver business logic
- `DriverLocationService`: Location tracking and updates
- `DriverNotificationService`: Real-time notifications

**Features:**
- Online/offline status tracking
- Real-time location updates
- Nearby ride matching algorithm
- Earnings calculation and ledger

### 4. Real-time Communication

#### WebSocket Architecture

```
Client                    Server
  │                         │
  │─── CONNECT ────────────►│
  │   (with JWT token)      │
  │                         │──► WebSocketAuthInterceptor
  │                         │──► Validate Token
  │                         │──► Set Principal
  │◄── CONNECTED ───────────│
  │                         │
  │─── SUBSCRIBE ──────────►│
  │   /queue/ride-requests  │
  │                         │
  │◄── MESSAGE ─────────────│ (Ride Request)
  │                         │
  │─── SEND ────────────────►│
  │   /app/driver/ride/     │
  │   response               │
```

**WebSocket Components:**
- `WebSocketConfig`: STOMP broker configuration
- `WebSocketAuthInterceptor`: Authentication interceptor
- `WebSocketController`: Message handlers
- `WebSocketEventsListener`: Connection/disconnection events
- `WebSocketSessionRegistry`: Session management

**Topics:**
- `/queue/ride-requests{driverId}`: Ride requests for drivers
- `/queue/ride-status{custId}`: Status updates for customers
- `/queue/ride-status{driverId}`: Status updates for drivers

**Message Mappings:**
- `/app/driver/ride/response`: Driver accepts/rejects ride

### 5. Geolocation Services

#### Google Maps Integration

**Services:**
- `GoogleMapsService`: Wrapper for Google Maps API
- `GeocodingService`: Address ↔ Coordinates conversion

**Features:**
- Geocoding (address to coordinates)
- Reverse geocoding (coordinates to address)
- Distance matrix calculation
- ETA calculation

**Configuration:**
- API key in `application.properties`
- Base URL: `https://maps.googleapis.com/maps/api`
- Region: `in` (India)
- Language: `en`

### 6. Database Schema

#### Entity Relationships

```
users (1) ──┐
            │
            ├──► (1) drivers (1) ──► (1) cabs
            │
            └──► (1) rides (N) ──► (1) drivers
                    │
                    ├──► (1) otps
                    │
                    └──► (1) driver_ledger
```

**Key Tables:**
- `users`: User accounts and authentication
- `drivers`: Driver profiles and status
- `cabs`: Cab information and types
- `rides`: Ride records and status
- `driver_locations`: Real-time location tracking
- `otps`: OTP codes for verification
- `driver_ledger`: Financial transactions

### 7. Data Access Layer

#### Data Access Technologies

The application uses a **hybrid approach** for data access:

**JPA (Primary - Spring Data JPA)**
- Most repositories extend `JpaRepository<T, ID>`
- Provides automatic CRUD operations
- Supports custom queries with `@Query` annotations
- Used in:
  - `RideRepository` - Ride data access
  - `DriverRepository` - Driver data access
  - `CabRepository` - Cab data access
  - `DriverLocationRepository` - Location data access
  - `OtpRepository` - OTP data access
  - `DriverLedgerRepository` - Financial ledger access

**JDBC (Selective - Spring JdbcClient)**
- Used for specific cases requiring fine-grained control
- Used in:
  - `UserRepo` - User data access with custom SQL queries

**Benefits of Hybrid Approach:**
- JPA provides productivity and type safety for most operations
- JDBC allows custom SQL for complex queries or legacy code
- Both technologies coexist seamlessly in Spring Boot

## Design Patterns

### 1. Layered Architecture
- **Controllers**: Handle HTTP requests/responses
- **Services**: Business logic and orchestration
- **Repositories**: Data access abstraction
- **DTOs**: Data transfer between layers

### 2. Dependency Injection
- Constructor-based injection throughout
- Spring-managed beans
- Loose coupling between components

### 3. Repository Pattern
- Abstract data access layer
- **JPA-based implementations** (primary) - Most repositories extend `JpaRepository`
- **JDBC-based implementations** (selective) - Used in specific cases like `UserRepo` with `JdbcClient`
- Easy to swap data sources

### 4. Service Layer Pattern
- Business logic encapsulation
- Transaction management
- Cross-cutting concerns

### 5. DTO Pattern
- Separate data transfer objects
- Prevents entity exposure
- Versioning support

## Security Architecture

### Authentication Flow

```
1. User Login
   └─► AuthService.login()
       └─► Validate credentials
       └─► Generate JWT token
       └─► Return token to client

2. Authenticated Request
   └─► JwtAuthFilter intercepts
       └─► Extract token from header
       └─► Validate token (JwtService)
       └─► Set Authentication in SecurityContext
       └─► Proceed to controller

3. WebSocket Connection
   └─► WebSocketAuthInterceptor intercepts CONNECT
       └─► Extract token from STOMP headers
       └─► Validate token
       └─► Set user principal
       └─► Allow connection
```

### Authorization

- **Role-based**: USER, DRIVER roles
- **Method-level**: Spring Security annotations
- **Resource-level**: User ID validation in services

## Data Flow

### Ride Creation Flow

```
1. Customer creates ride
   POST /api/rides
   └─► RideController.createRide()
       └─► RideService.createRide()
           ├─► Calculate estimated fare
           ├─► Generate OTP
           ├─► Save ride (REQUESTED)
           └─► Find nearby drivers
               └─► Send WebSocket notification
                   └─► /queue/ride-requests{driverId}
```

### Driver Acceptance Flow

```
1. Driver receives notification
   WebSocket: /queue/ride-requests{driverId}
   
2. Driver accepts ride
   WebSocket: /app/driver/ride/response
   └─► WebSocketController.driverResponse()
       └─► RideService.handleDriverResponse()
           ├─► Assign driver to ride
           ├─► Update ride status (ASSIGNED)
           ├─► Notify customer
           └─► Update driver status (BUSY)
```

### Ride Start Flow

```
1. Customer starts ride
   POST /api/rides/start
   └─► RideController.startRide()
       └─► RideService.startRide()
           ├─► Verify OTP
           ├─► Update ride status (STARTED)
           └─► Record start time
```

## Scalability Considerations

### Current Limitations
- Single instance deployment
- In-memory session registry
- Synchronous processing

### Future Improvements
1. **Horizontal Scaling**
   - Redis for session management
   - Load balancer for multiple instances

2. **Caching**
   - Cache driver locations
   - Cache geocoding results
   - Cache ride calculations

3. **Message Queue**
   - RabbitMQ/Kafka for ride notifications
   - Async processing for heavy operations

## Monitoring & Observability

### Current State
- Basic health check endpoint (`/ping`)
- Console logging

## Error Handling

### Current Approach
- Exception propagation
- HTTP status codes
- Basic error messages

### Recommended Improvements
- Global exception handler
- Structured error responses
- Error logging and monitoring
- Retry mechanisms for external APIs

## Testing Strategy

### Unit Tests
- Service layer logic
- Utility methods
- DTO validation

### Integration Tests
- Repository layer
- Controller endpoints
- WebSocket connections

### End-to-End Tests
- Complete ride flow
- Authentication flow
- Payment processing

## Deployment Architecture

### Development
```
Local Machine
├─► Spring Boot Application (Port 9090)
└─► PostgreSQL (Port 5432)
```

## Configuration Management

### Application Properties
- Database connection
- JWT secret key
- Google Maps API key
- Server port

### Environment-Specific Configs
- Development: `application.properties`
- Production: Environment variables or external config server

## API Versioning

Currently, no versioning is implemented. Consider:
- URL versioning: `/api/v1/rides`
- Header versioning: `Accept: application/vnd.api.v1+json`

## Future Enhancements

1. **Microservices Architecture**
   - Separate services for rides, drivers, payments
   - API Gateway for routing
   - Service discovery

2.**Advanced Features**
   - Ride sharing (pool)
   - Scheduled rides
   - Multiple payment gateways
   - Rating and review system

