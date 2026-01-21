# API Documentation

Complete reference for all REST API endpoints in the Uber Clone application.

## Base URL

```
http://localhost:9090
```

## Authentication

Most endpoints require JWT authentication. Include the token in the Authorization header:

```
Authorization: Bearer <your-jwt-token>
```

---

## Authentication Endpoints

### Sign Up

Create a new user account.

**Endpoint:** `POST /api/auth/signup`

**Request Body:**
```json
{
  "firstName": "John",
  "lastName": "Doe",
  "email": "john.doe@example.com",
  "mobileNum": "+1234567890",
  "password": "securePassword123",
  "role": "USER"
}
```

**Response:** `201 Created`
```json
{
  "userId": 1,
  "email": "john.doe@example.com",
  "message": "User registered successfully"
}
```

---

### Login

Authenticate user and receive JWT token.

**Endpoint:** `POST /api/auth/login`

**Request Body:**
```json
{
  "email": "john.doe@example.com",
  "password": "securePassword123"
}
```

**Response:** `200 OK`
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "userId": 1,
  "email": "john.doe@example.com",
  "role": "USER"
}
```

---

### Send Login OTP

Generate and send OTP for login verification.

**Endpoint:** `POST /api/auth/send-login-otp`

**Request Body:**
```json
{
  "user_id": 1
}
```

**Response:** `200 OK`
```json
{
  "otp_id": 123,
  "otp_code": "123456",
  "purpose": "LOGIN"
}
```

---

### Verify Login OTP

Verify OTP code for login.

**Endpoint:** `POST /api/auth/verify-login-otp`

**Request Body:**
```json
{
  "user_id": 1,
  "otp_code": "123456"
}
```

**Response:** `200 OK`
```
"OTP verified, login success"
```

---

## Ride Endpoints

### Create Ride

Create a new ride request.

**Endpoint:** `POST /api/rides`

**Headers:**
```
Authorization: Bearer <token>
```

**Request Body:**
```json
{
  "pickupLat": 28.7041,
  "pickupLng": 77.1025,
  "dropLat": 28.6139,
  "dropLng": 77.2090
}
```

**Response:** `201 Created`
```json
{
  "rideId": 1,
  "estimatedFare": 150.50,
  "status": "REQUESTED"
}
```

---

### Get All Rides

Retrieve all rides (admin/development endpoint).

**Endpoint:** `GET /api/rides`

**Response:** `200 OK`
```json
[
  {
    "id": 1,
    "custId": 1,
    "driverId": null,
    "pickupLat": 28.7041,
    "pickupLng": 77.1025,
    "dropLat": 28.6139,
    "dropLng": 77.2090,
    "status": "REQUESTED",
    "estimatedFare": 150.50,
    "finalFare": null
  }
]
```

---

### Get Current Ride

Get the current active ride for a customer.

**Endpoint:** `GET /api/rides/current?custId={customerId}`

**Query Parameters:**
- `custId` (required): Customer user ID

**Response:** `200 OK`
```json
{
  "id": 1,
  "custId": 1,
  "driverId": 5,
  "status": "STARTED",
  "pickupLat": 28.7041,
  "pickupLng": 77.1025,
  "dropLat": 28.6139,
  "dropLng": 77.2090,
  "estimatedFare": 150.50,
  "finalFare": null
}
```

---

### Assign Driver to Ride

Manually assign a driver to a ride.

**Endpoint:** `POST /api/rides/{rideId}/assign-driver`

**Path Parameters:**
- `rideId` (required): Ride ID

**Request Body:**
```json
{
  "driverId": 5
}
```

**Response:** `200 OK`
```json
{
  "id": 1,
  "driverId": 5,
  "status": "ASSIGNED",
  ...
}
```

---

### Start Ride

Start a ride with OTP verification.

**Endpoint:** `POST /api/rides/start`

**Request Body:**
```json
{
  "user_id": 1,
  "otp_code": "123456"
}
```

**Response:** `200 OK`
```
"Ride started successfully"
```

**Error Response:** `400 Bad Request`
```
"Invalid or expired OTP"
```

---

### End Ride

End an ongoing ride.

**Endpoint:** `POST /api/rides/{rideId}/end`

**Path Parameters:**
- `rideId` (required): Ride ID

**Response:** `200 OK`
```json
{
  "id": 1,
  "status": "COMPLETED",
  "finalFare": 145.00,
  "endedOn": "2024-01-15T10:30:00"
}
```

---

### Cancel Ride

Cancel a ride request.

**Endpoint:** `POST /api/rides/{rideId}/cancel`

**Path Parameters:**
- `rideId` (required): Ride ID

**Response:** `200 OK`
```json
{
  "id": 1,
  "status": "CANCELLED",
  ...
}
```

---

### Get Ride ETA

Get estimated time of arrival for a ride.

**Endpoint:** `GET /api/rides/{rideId}/eta`

**Path Parameters:**
- `rideId` (required): Ride ID

**Response:** `200 OK`
```json
{
  "etaMinutes": 15,
  "distanceKm": 5.2,
  "durationText": "15 mins"
}
```

---

### Get Ride Card

Get ride card information for display.

**Endpoint:** `GET /api/rides/card?rideId={rideId}`

**Query Parameters:**
- `rideId` (required): Ride ID

**Response:** `200 OK`
```json
{
  "rideId": 1,
  "driverName": "John Driver",
  "cabModel": "Toyota Camry",
  "cabType": "SEDAN",
  "pickupAddress": "123 Main St",
  "dropAddress": "456 Oak Ave",
  "estimatedFare": 150.50,
  "status": "ASSIGNED"
}
```

---

### Mark Payment Success

Mark payment as successful for a completed ride.

**Endpoint:** `POST /api/rides/{rideId}/payment-success`

**Path Parameters:**
- `rideId` (required): Ride ID

**Request Body:**
```json
{
  "method": "CASH"
}
```

**Response:** `200 OK`
```json
{
  "id": 1,
  "paymentStatus": "PAID",
  "paymentMethod": "CASH",
  ...
}
```

---

## Driver Endpoints

### Get All Drivers

Retrieve all registered drivers.

**Endpoint:** `GET /drivers`

**Response:** `200 OK`
```json
[
  {
    "id": 1,
    "name": "John Driver",
    "licenseNo": "DL123456",
    "avgRating": 4.5,
    "ratingCount": 120,
    "cabId": 1,
    "isOnline": "ONLINE"
  }
]
```

---

### Get Driver by ID

Get driver details by ID.

**Endpoint:** `GET /drivers/id/{id}`

**Path Parameters:**
- `id` (required): Driver ID

**Response:** `200 OK`
```json
{
  "id": 1,
  "name": "John Driver",
  "licenseNo": "DL123456",
  "avgRating": 4.5,
  "ratingCount": 120,
  "cabId": 1,
  "isOnline": "ONLINE"
}
```

---

### Create Driver

Register a new driver.

**Endpoint:** `POST /drivers`

**Request Body:**
```json
{
  "name": "John Driver",
  "licenseNo": "DL123456",
  "cabId": 1,
  "userId": 2
}
```

**Response:** `200 OK`

---

### Update Driver

Update driver information.

**Endpoint:** `PUT /drivers/id/{id}`

**Path Parameters:**
- `id` (required): Driver ID

**Request Body:**
```json
{
  "name": "John Driver Updated",
  "licenseNo": "DL123456",
  "avgRating": 4.6,
  "ratingCount": 125
}
```

**Response:** `200 OK`
```json
true
```

---

### Delete Driver

Delete a driver record.

**Endpoint:** `DELETE /drivers/id/{id}`

**Path Parameters:**
- `id` (required): Driver ID

**Response:** `204 No Content`

---

### Get Driver Home/Dashboard

Get driver dashboard with current ride and nearby rides.

**Endpoint:** `GET /drivers/{driverId}/home`

**Path Parameters:**
- `driverId` (required): Driver ID

**Response:** `200 OK`
```json
{
  "currentRide": null,
  "nearbyRides": [
    {
      "id": 1,
      "pickupLat": 28.7041,
      "pickupLng": 77.1025,
      "estimatedFare": 150.50,
      "status": "REQUESTED"
    }
  ]
}
```

**Response (Driver Offline):** `200 OK`
```json
{
  "status": "OFFLINE",
  "message": "Driver is offline"
}
```

---

### Update Driver Status

Update driver online/offline status.

**Endpoint:** `POST /drivers/{driverId}/status?status={status}`

**Path Parameters:**
- `driverId` (required): Driver ID

**Query Parameters:**
- `status` (required): Status value (`ONLINE`, `OFFLINE`)

**Response:** `200 OK`
```json
{
  "message": "Status updated",
  "status": "ONLINE"
}
```

---

### Accept Ride

Driver accepts a ride request.

**Endpoint:** `POST /drivers/{driverId}/accept/{rideId}`

**Path Parameters:**
- `driverId` (required): Driver ID
- `rideId` (required): Ride ID

**Response:** `200 OK`
```
"Ride accepted"
```

---

## Driver Location Endpoints

### Update Driver Location

Update driver's current location.

**Endpoint:** `PUT /api/driver-location/{driverId}`

**Path Parameters:**
- `driverId` (required): Driver ID

**Request Body:**
```json
{
  "lat": 28.7041,
  "lng": 77.1025
}
```

**Response:** `200 OK`
```json
{
  "driverId": 1,
  "lat": 28.7041,
  "lng": 77.1025,
  "updatedAt": "2024-01-15T10:00:00Z"
}
```

---

### Get Driver Location

Get current location of a driver.

**Endpoint:** `GET /api/driver-location/{driverId}`

**Path Parameters:**
- `driverId` (required): Driver ID

**Response:** `200 OK`
```json
{
  "driverId": 1,
  "lat": 28.7041,
  "lng": 77.1025,
  "updatedAt": "2024-01-15T10:00:00Z"
}
```

---

## Cab Endpoints

### Get All Cabs

Retrieve all registered cabs.

**Endpoint:** `GET /api/cabs`

**Response:** `200 OK`
```json
[
  {
    "id": 1,
    "registrationNo": "DL01AB1234",
    "model": "Toyota Camry",
    "color": "White",
    "cabType": "SEDAN",
    "isActive": true
  }
]
```

---

### Get Cab by ID

Get cab details by ID.

**Endpoint:** `GET /api/cabs/{id}`

**Path Parameters:**
- `id` (required): Cab ID

**Response:** `200 OK`
```json
{
  "id": 1,
  "registrationNo": "DL01AB1234",
  "model": "Toyota Camry",
  "color": "White",
  "cabType": "SEDAN",
  "isActive": true
}
```

---

### Create Cab

Register a new cab.

**Endpoint:** `POST /api/cabs`

**Request Body:**
```json
{
  "registrationNo": "DL01AB1234",
  "model": "Toyota Camry",
  "color": "White",
  "cabType": "SEDAN"
}
```

**Response:** `200 OK`
```json
{
  "id": 1,
  "registrationNo": "DL01AB1234",
  "model": "Toyota Camry",
  "color": "White",
  "cabType": "SEDAN",
  "isActive": true
}
```

---

### Update Cab

Update cab information.

**Endpoint:** `PUT /api/cabs/{id}`

**Path Parameters:**
- `id` (required): Cab ID

**Request Body:**
```json
{
  "registrationNo": "DL01AB1234",
  "model": "Toyota Camry 2024",
  "color": "Black",
  "cabType": "SEDAN"
}
```

**Response:** `200 OK`
```json
{
  "id": 1,
  "registrationNo": "DL01AB1234",
  "model": "Toyota Camry 2024",
  "color": "Black",
  "cabType": "SEDAN",
  "isActive": true
}
```

---

### Delete Cab

Delete a cab record.

**Endpoint:** `DELETE /api/cabs/{id}`

**Path Parameters:**
- `id` (required): Cab ID

**Response:** `204 No Content`

---

### Assign Driver to Cab

Assign a driver to a cab.

**Endpoint:** `POST /api/cabs/assign-driver`

**Request Body:**
```json
{
  "cabId": 1,
  "driverId": 5
}
```

**Response:** `200 OK`
```
"Driver assigned successfully"
```

---

## Geolocation Endpoints

### Geocode

Convert address to coordinates.

**Endpoint:** `GET /api/geo/geocode?address={address}`

**Query Parameters:**
- `address` (required): Address string

**Response:** `200 OK`
```json
{
  "lat": 28.7041,
  "lng": 77.1025
}
```

---

### Reverse Geocode

Convert coordinates to address.

**Endpoint:** `GET /api/geo/reverse-geocode?lat={lat}&lng={lng}`

**Query Parameters:**
- `lat` (required): Latitude
- `lng` (required): Longitude

**Response:** `200 OK`
```json
{
  "address": "123 Main Street, New Delhi, Delhi 110001, India"
}
```

---

### Distance Calculation

Calculate distance and time between two points.

**Endpoint:** `GET /api/geo/distance?originLat={lat}&originLng={lng}&destLat={lat}&destLng={lng}`

**Query Parameters:**
- `originLat` (required): Origin latitude
- `originLng` (required): Origin longitude
- `destLat` (required): Destination latitude
- `destLng` (required): Destination longitude

**Response:** `200 OK`
```json
{
  "distanceText": "5.2 km",
  "distanceMeters": 5200,
  "durationText": "15 mins",
  "durationSeconds": 900
}
```

---

## Health Check

### Ping

Check if the service is running.

**Endpoint:** `GET /ping`

**Response:** `200 OK`
```
"pong"
```

---

## Error Responses

### 400 Bad Request
```json
{
  "error": "Invalid request",
  "message": "Detailed error message"
}
```

### 401 Unauthorized
```json
{
  "error": "Unauthorized",
  "message": "Invalid or missing authentication token"
}
```

### 404 Not Found
```json
{
  "error": "Not Found",
  "message": "Resource not found"
}
```

### 500 Internal Server Error
```json
{
  "error": "Internal Server Error",
  "message": "An unexpected error occurred"
}
```

---

## Rate Limiting

Currently, there are no rate limits implemented. Consider implementing rate limiting for production use.

## WebSocket API

See [ARCHITECTURE.md](ARCHITECTURE.md) for WebSocket endpoint documentation.

