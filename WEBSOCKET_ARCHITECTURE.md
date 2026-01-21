# WebSocket Architecture - Detailed Explanation

This document explains how WebSocket communication works in the Uber Clone application.

## Overview

**Important:** There is **NO direct peer-to-peer connection** between USER and DRIVER. All communication goes through the server using a **hub-and-spoke** model (server as hub).

## Complete Flow

### Phase 1: Initial Connections

```
┌─────────┐                    ┌─────────┐                    ┌─────────┐
│  USER   │                    │ SERVER  │                    │ DRIVER  │
│ (Cust)  │                    │         │                    │         │
└────┬────┘                    └────┬────┘                    └────┬────┘
     │                              │                              │
     │─── CONNECT ──────────────────►│                              │
     │   ws://localhost:9090/ws     │                              │
     │   Authorization: Bearer <token>│                              │
     │                              │                              │
     │                              │─── CONNECT ──────────────────►│
     │                              │   ws://localhost:9090/ws     │
     │                              │   Authorization: Bearer <token>│
     │                              │                              │
     │◄── CONNECTED ─────────────────│                              │
     │                              │                              │
     │                              │◄── CONNECTED ────────────────│
     │                              │                              │
     │─── SUBSCRIBE ────────────────►│                              │
     │   /queue/ride-status{custId} │                              │
     │                              │                              │
     │                              │─── SUBSCRIBE ────────────────►│
     │                              │   /queue/ride-requests{driverId}│
     │                              │                              │
```

**What happens:**
1. **USER** connects to WebSocket endpoint `/ws` with JWT token
2. **DRIVER** connects to WebSocket endpoint `/ws` with JWT token
3. Server validates tokens and establishes connections
4. **USER** subscribes to `/queue/ride-status{custId}` (for ride updates)
5. **DRIVER** subscribes to `/queue/ride-requests{driverId}` (for ride requests)
6. Server registers driver session in `WebSocketSessionRegistry`

### Phase 2: User Creates Ride Request

```
┌─────────┐                    ┌─────────┐                    ┌─────────┐
│  USER   │                    │ SERVER  │                    │ DRIVER  │
└────┬────┘                    └────┬────┘                    └────┬────┘
     │                              │                              │
     │─── POST /api/rides ──────────►│                              │
     │   {pickupLat, pickupLng,     │                              │
     │    dropLat, dropLng}          │                              │
     │                              │                              │
     │                              │──► Find nearby drivers        │
     │                              │──► Check who's online        │
     │                              │                              │
     │                              │─── SEND TO MULTIPLE DRIVERS ──►│
     │                              │   /queue/ride-requests{id1} │
     │                              │                              │
     │                              │─── SEND TO MULTIPLE DRIVERS ──►│
     │                              │   /queue/ride-requests{id2} │
     │                              │                              │
     │◄── 201 Created ──────────────│                              │
     │   {rideId, estimatedFare}    │                              │
     │                              │                              │
     │                              │◄── MESSAGE ───────────────────│
     │                              │   Ride Request Received      │
     │                              │                              │
```

**What happens:**
1. **USER** creates ride via REST API (`POST /api/rides`)
2. Server:
   - Calculates estimated fare
   - Finds nearby available drivers (within 3km radius)
   - Checks which drivers are online (via `WebSocketSessionRegistry`)
   - Creates ride record with status `REQUESTED`
3. Server sends ride request to **ALL nearby online drivers** via WebSocket:
   - Topic: `/queue/ride-requests{driverId}` (one per driver)
   - Message: `DriverRequest` with pickup/drop locations and fare
4. **Multiple drivers** receive the same ride request simultaneously

### Phase 3: Driver Accepts Ride

```
┌─────────┐                    ┌─────────┐                    ┌─────────┐
│  USER   │                    │ SERVER  │                    │ DRIVER  │
└────┬────┘                    └────┬────┘                    └────┬────┘
     │                              │                              │
     │                              │◄── SEND ──────────────────────│
     │                              │   /app/driver/ride/response │
     │                              │   {rideId, accepted: true}   │
     │                              │                              │
     │                              │──► Validate driver            │
     │                              │──► Assign driver to ride      │
     │                              │──► Update ride status         │
     │                              │──► Notify other drivers       │
     │                              │                              │
     │◄── MESSAGE ──────────────────│                              │
     │   /queue/ride-status{custId}│                              │
     │   {rideId, driverId, status: ASSIGNED}│                      │
     │                              │                              │
     │                              │◄── MESSAGE ───────────────────│
     │                              │   /queue/ride-status{driverId}│
     │                              │   "You got the ride!"         │
     │                              │                              │
```

**What happens:**
1. **DRIVER** accepts ride by sending message to `/app/driver/ride/response`
2. Server:
   - Validates driver is authenticated and has DRIVER role
   - Assigns driver to ride (updates database)
   - Changes ride status from `REQUESTED` to `ASSIGNED`
   - Notifies **USER** via `/queue/ride-status{custId}`
   - Notifies **accepted DRIVER** via `/queue/ride-status{driverId}`
   - Notifies **other drivers** (who received the request) that ride is taken
3. **USER** receives notification that driver accepted
4. **DRIVER** receives confirmation

### Phase 4: Driver Location Updates (During Active Ride)

```
┌─────────┐                    ┌─────────┐                    ┌─────────┐
│  USER   │                    │ SERVER  │                    │ DRIVER  │
└────┬────┘                    └────┬────┘                    └────┬────┘
     │                              │                              │
     │                              │◄── SEND ──────────────────────│
     │                              │   /app/driver/location        │
     │                              │   {lat, lng}                  │
     │                              │                              │
     │                              │──► Update location in DB       │
     │                              │──► Find active ride            │
     │                              │                              │
     │◄── BROADCAST ────────────────│                              │
     │   /topic/ride/{rideId}/location│                            │
     │   {lat, lng}                 │                              │
     │                              │                              │
```

**What happens:**
1. **DRIVER** sends location update via `/app/driver/location` (WebSocket message)
2. Server:
   - Updates driver location in database
   - Finds active ride for this driver
   - If ride exists and is active, broadcasts to topic `/topic/ride/{rideId}/location`
3. **USER** (who subscribed to this topic) receives location updates
4. **Note:** USER must subscribe to `/topic/ride/{rideId}/location` to receive updates

## Key Concepts

### 1. No Direct USER-DRIVER Connection

**Myth:** USER and DRIVER have a direct WebSocket connection.

**Reality:** Both connect to the server separately. The server acts as a message broker.

```
USER ──► Server ◄── DRIVER
         (Hub)
```

### 2. Multiple Drivers Receive Same Request

When a USER creates a ride:
- Server finds **multiple nearby drivers** (up to 10 within 3km)
- **All online drivers** receive the same ride request
- **First driver to accept** gets the ride
- Other drivers are notified the ride is taken

### 3. Topics vs Queues

**Queues** (`/queue/...`):
- Point-to-point messaging
- Each message goes to **one subscriber**
- Used for: ride requests to specific drivers, status updates to specific users

**Topics** (`/topic/...`):
- Publish-subscribe messaging
- Each message goes to **all subscribers**
- Used for: driver location broadcasts (multiple users could track same driver)

### 4. WebSocket Topics/Queues Used

| Topic/Queue | Purpose | Subscriber |
|------------|---------|------------|
| `/queue/ride-requests{driverId}` | Ride requests sent to specific driver | DRIVER |
| `/queue/ride-status{custId}` | Ride status updates for customer | USER |
| `/queue/ride-status{driverId}` | Ride status updates for driver | DRIVER |
| `/queue/ride-cancelled` | Notification that ride was taken by another driver | DRIVER |
| `/topic/ride/{rideId}/location` | Driver location updates during active ride | USER (and potentially others) |

### 5. Message Endpoints (Where clients send messages)

| Endpoint | Purpose | Sender |
|----------|---------|--------|
| `/app/driver/ride/response` | Driver accepts/rejects ride | DRIVER |
| `/app/driver/location` | Driver sends location update | DRIVER |

## Complete Example Flow

### Scenario: User books a ride, driver accepts, driver location tracked

```
1. USER connects to WebSocket
   └─► Subscribes to: /queue/ride-status{userId}

2. DRIVER connects to WebSocket
   └─► Subscribes to: /queue/ride-requests{driverId}
   └─► Server registers driver in WebSocketSessionRegistry

3. USER creates ride (REST API)
   POST /api/rides
   └─► Server finds 3 nearby drivers (Driver1, Driver2, Driver3)
   └─► Server sends ride request to all 3:
       ├─► /queue/ride-requests{driver1Id}
       ├─► /queue/ride-requests{driver2Id}
       └─► /queue/ride-requests{driver3Id}

4. DRIVER1 accepts first
   SEND /app/driver/ride/response {rideId, accepted: true}
   └─► Server assigns Driver1 to ride
   └─► Server sends notifications:
       ├─► /queue/ride-status{userId} → "Ride assigned"
       ├─► /queue/ride-status{driver1Id} → "You got the ride!"
       ├─► /queue/ride-cancelled → Driver2: "Ride already accepted"
       └─► /queue/ride-cancelled → Driver3: "Ride already accepted"

5. USER subscribes to driver location
   └─► Subscribes to: /topic/ride/{rideId}/location

6. DRIVER1 sends location updates
   SEND /app/driver/location {lat, lng}
   └─► Server broadcasts to: /topic/ride/{rideId}/location
   └─► USER receives location updates in real-time

7. Ride starts (with OTP verification)
   └─► Status changes to STARTED/ONGOING

8. Ride ends
   └─► Status changes to COMPLETED
   └─► USER and DRIVER receive final status updates
```

## Important Notes

1. **USER doesn't directly send WebSocket messages to DRIVER**
   - USER creates ride via REST API
   - Server handles WebSocket communication

2. **Multiple drivers compete for the same ride**
   - First-come-first-served basis
   - Other drivers are automatically notified when ride is taken

3. **Driver location is broadcast via topic**
   - Any subscriber to `/topic/ride/{rideId}/location` receives updates
   - Not a direct connection between USER and DRIVER

4. **Session Management**
   - `WebSocketSessionRegistry` tracks which drivers are online
   - Only online drivers receive ride requests
   - When driver disconnects, they're removed from registry

5. **Authentication**
   - All WebSocket connections require JWT token
   - Token validated on CONNECT via `WebSocketAuthInterceptor`
   - Principal (user info) available in message handlers

