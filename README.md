# Case Study - Cleaning Booking Microservices

📖 Information

In this case study, cleaning professionals provide home-cleaning services to customers. Each professional belongs to a vehicle group, and the system must enforce booking constraints such as working hours, breaks between appointments, Friday restrictions, and same-vehicle assignment for multi-professional bookings.

The project is built with Java and Spring Boot and exposes REST APIs through a central API Gateway. The requirements include availability checks, booking creation, booking updates, API documentation, and relational database persistence. The initial setup must include 5 vehicles and 5 cleaners per vehicle, for a total of 25 cleaners.

---

## Core Business Rules

The booking domain follows these rules:

| Rule | Description |
|------|-------------|
| Working days | Cleaning professionals do not work on Fridays |
| Working hours | Working window is between `08:00` and `22:00` |
| First appointment | Cannot start before `08:00` |
| Last appointment | Must finish before `22:00` |
| Break rule | At least `30 minutes` must exist between back-to-back appointments |
| Vehicle grouping | A vehicle serves for the whole day |
| Cleaner assignment | Each vehicle has `5` assigned cleaner professionals |
| Duration | A service can be only `2` or `4` hours |
| Professional count | A booking can request `1`, `2`, or `3` professionals |
| Same vehicle rule | If multiple professionals are assigned to one booking, they must belong to the same vehicle |

---

## Booking Process

### 1. Availability Check

Two types of availability lookup are required:

1. **Date-based availability**  
   If only the date is provided, the service should return:
    - available cleaner professional list
    - their available times for the selected date

2. **Slot-based availability**  
   If date, appointment start time, and appointment duration are provided, the service should return:
    - only the professionals available for the given time period

### 2. Booking Creation

Each booking:

- is assigned to `1`, `2`, or `3` cleaner professionals
- must satisfy all business rules
- updates cleaner availability after creation
- requires same-vehicle assignment when multiple professionals are selected

### 3. Booking Update

Booking date and time can be updated.

After update:

- cleaner availability must be recalculated
- conflicts must be revalidated
- business rules must still be satisfied

### 4. Example: Availability Recalculation After Booking Update

The availability response changes immediately after a booking is updated. This behavior proves that the service recalculates cleaner schedules dynamically based on booking time, assigned professionals, and the required `30-minute` break rule.

#### Example runtime observation

Here is the process after updating a booking and calling availability by date again.

**What changed (vehicle `5b2b...dea4`)**

Compare the two availability calls:

- **Before update** (`15:24:05`)
- **After update** (`15:25:46`)

#### Before update

Cleaner `f5a8...` had full-day availability:

- `startTimes2h`: `08:00` → `20:00`
- `startTimes4h`: `08:00` → `18:00`

Cleaner `cb896...` also had full-day times.

#### After update

Cleaner `f5a8...` now has:

- `startTimes2h`: only `08:00..11:30` and `18:30..20:00` *(the middle is gone)*
- `startTimes4h`: only `08:00`, `08:30`, `09:00`, `09:30`

Cleaner `cb896...` shows the same reduced pattern.

#### Why this happens

That pattern is exactly what you would expect if there is a booking from `14:00` → `18:00`.

Anything that would touch the booking without the `30-minute` break is removed.

The earliest start time after the booking becomes `18:30` because:

- booking ends at `18:00`
- mandatory break is `30 minutes`
- earliest next valid slot is `18:30`

#### Why cleaners `2949...` and `fa30...` still look more free

Those two cleaners still start at `11:30` because they are blocked only by the seeded booking from `09:00`–`11:00`.

With the required `30-minute` break, their earliest available slot becomes `11:30`.

They do **not** show the `14:00`–`18:00` gap, which strongly suggests:

- ✅ The updated booking `ad3b...` is **not** assigned to cleaners `2949...` and `fa30...`
- ✅ The updated booking **is** assigned to cleaners `f5a8...` and `cb896...`

#### Conclusion

This example demonstrates that the availability engine:

- recalculates schedules after booking updates
- applies booking overlap restrictions correctly
- enforces the `30-minute` break rule
- updates availability per cleaner rather than per vehicle only
- reflects the actual professionals assigned to the updated booking

---

## Domain Model

### Vehicle

Represents the transportation unit that serves cleaners for the entire day.

Suggested fields:

- `id`
- `code`
- `licensePlate`
- `cleaners`

### Cleaner Professional

Represents a cleaning professional assigned to a vehicle.

Suggested fields:

- `id`
- `fullName`
- `vehicleId`
- `vehicle`

### Booking

Represents a customer cleaning reservation.

Suggested fields:

- `id`
- `startAt`
- `durationHours`
- `professionalCount`
- `vehicleId`
- `assignedProfessionals`
- `status`

---

## Architecture Overview

This project is consumed through the API Gateway.

### Services exposed through the gateway

- **PROFESSIONALS-SERVICE**
- **BOOKING-SERVICE**
- **API-GATEWAY actuator endpoints**

### Base URL

```bash
http://localhost:8080
```

---

## API Documentation

The Postman collection exposes OpenAPI JSON through the gateway.

| Service | Method | Endpoint | Description |
|---------|--------|----------|-------------|
| Professionals OpenAPI | GET | `/professionals/v3/api-docs` | OpenAPI JSON for Professionals Service |
| Booking OpenAPI | GET | `/booking/v3/api-docs` | OpenAPI JSON for Booking Service |

---

## Endpoint Table

### Professionals Service

These endpoints are routed through the API Gateway.

| Module | Method | Endpoint | Description | Request Body / Params |
|--------|--------|----------|-------------|------------------------|
| Vehicles | POST | `/api/professionals/vehicles` | List vehicles with pagination and sorting | `pagination.pageNumber`, `pagination.pageSize`, `sorting.sortBy`, `sorting.sortDirection` |
| Cleaners | POST | `/api/professionals/cleaners` | List all cleaners with pagination and sorting | `pagination.pageNumber`, `pagination.pageSize`, `sorting.sortBy`, `sorting.sortDirection` |
| Cleaners by Vehicle | POST | `/api/professionals/vehicles/{vehicleId}/cleaners` | List cleaners assigned to a specific vehicle | Path variable: `vehicleId` + pagination/sorting body |

#### Example request body for vehicles

```json
{
  "pagination": {
    "pageNumber": 1,
    "pageSize": 20
  },
  "sorting": {
    "sortBy": "code",
    "sortDirection": "ASC"
  }
}
```

#### Example request body for cleaners

```json
{
  "pagination": {
    "pageNumber": 1,
    "pageSize": 20
  },
  "sorting": {
    "sortBy": "fullName",
    "sortDirection": "ASC"
  }
}
```

### Booking Service

| Module | Method | Endpoint | Description | Request Body / Params |
|--------|--------|----------|-------------|------------------------|
| Availability by Date | GET | `/api/availability?date={date}` | Returns available professionals and available times for the given date | Query param: `date` |
| Availability by Slot | GET | `/api/availability/slot?startAt={startAt}&durationHours={durationHours}&professionalCount={professionalCount}` | Returns available professionals for the requested slot | Query params: `startAt`, `durationHours`, `professionalCount` |
| Create Booking | POST | `/api/bookings` | Creates a booking and assigns professionals | JSON body: `startAt`, `durationHours`, `professionalCount` |
| Update Booking | PUT | `/api/bookings/{bookingId}` | Updates an existing booking | Path variable: `bookingId`, JSON body: `newStartAt`, `newDurationHours` |

#### Example request for availability by date

```bash
GET /api/availability?date=2026-02-25
```

#### Example request for slot-based availability

```bash
GET /api/availability/slot?startAt=2026-02-25T10:00:00&durationHours=2&professionalCount=2
```

#### Example request body for booking creation

```json
{
  "startAt": "2026-02-25T10:00:00",
  "durationHours": 2,
  "professionalCount": 2
}
```

#### Example request body for booking update

```json
{
  "newStartAt": "2026-02-25T14:30:00",
  "newDurationHours": 4
}
```

### API Gateway Optional Endpoints

| Module | Method | Endpoint | Description |
|--------|--------|----------|-------------|
| Gateway Routes | GET | `/actuator/gateway/routes` | Shows registered gateway routes |
| Gateway Health | GET | `/actuator/health` | Shows API Gateway health status |

---

## Postman Collection Variables

The uploaded Postman collection defines these variables:

| Variable | Example Value | Description |
|----------|---------------|-------------|
| `baseUrl` | `http://localhost:8080` | API Gateway base URL |
| `date` | `2026-02-25` | Used for date-based availability |
| `startAt` | `2026-02-25T10:00:00` | Booking start date-time |
| `durationHours` | `2` | Booking duration in hours |
| `professionalCount` | `2` | Number of assigned professionals |
| `newStartAt` | `2026-02-25T14:30:00` | Updated booking start date-time |
| `newDurationHours` | `2` | Updated booking duration |
| `vehicleId` | dynamic | Vehicle selected from previous requests |
| `cleanerId` | dynamic | Cleaner selected from previous requests |
| `bookingId` | dynamic | Booking created in previous requests |

---

### Technologies

* Java 25
* Spring Boot 3.0
* Restful API
* Open Api (Swagger)
* Maven
* Junit5
* Mockito
* Integration Tests
* TestContainer
* Mapstruct
* Docker
* Docker Compose
* CI/CD (Github Actions)
* Postman
* Postgres
* JaCoCo (Test Report)
* Prometheus
* Grafana
* SonarQube
* Zipkin
* Kafka
* Kafdrop

---

### Postman

```text
Import postman collection under postman_collection folder
```

## Environment Variables

Example `.env` values:

```env
# Shared environment for docker-compose.yml and docker-local-compose.yml

POSTGRES_USER=justlife
POSTGRES_PASSWORD=justlife

PROFESSIONALS_DB=professionals
BOOKINGS_DB=bookings

REDIS_PORT=6379

# Kafka
KAFKA_HOST_PORT=9092
KAFKA_INTERNAL_PORT=29092
KAFKA_CONTROLLER_PORT=9093
KAFKA_TOPIC_BOOKING_EVENTS=booking.events
KAFKA_TOPIC_BOOKING_EVENTS_DLT=booking.events.dlt

# Observability
ZIPKIN_PORT=9411
ZIPKIN_ENDPOINT=http://zipkin:9411/api/v2/spans
TRACING_SAMPLING_PROBABILITY=1.0

PROMETHEUS_PORT=9090
GRAFANA_PORT=3000
GRAFANA_ADMIN_USER=admin
GRAFANA_ADMIN_PASSWORD=admin

# SonarQube
SONAR_PORT=9000
SONAR_DB=sonarqube
SONAR_DB_USER=sonar
SONAR_DB_PASSWORD=sonar

# Kafdrop
KAFDROP_PORT=9091
```

### JaCoCo (Test Report)

After the command named `mvn clean install` completes, the JaCoCo report will be available at:

```text
target/site/jacoco/index.html
```

Navigate to the `target/site/jacoco/` directory.

Open the `index.html` file in your browser to view the detailed coverage report.

---

## Run the Application

### Maven, Docker and Kubernetes Running Process

### Maven Run

To build and run the application with `Maven`, please follow the directions shown below:

```sh
$ git clone https://github.com/Rapter1990/cleaning-booking-microservices.git
$ cd cleaning-booking-microservices
$ docker compose -f docker-local-compose.yml up -d
$ Run config-server
$ Run discovery-server
$ Run api-gateway
$ Run professional-service
$ Run booking-service
```

---

### Docker Run

The application can be built and run by the `Docker` engine. The `Dockerfile` has multistage build, so you do not need to build and run separately.

Please follow directions shown below in order to build and run the application with Docker Compose file:

```sh
$ cd cleaning-booking-microservices
$ docker compose -f docker-compose.yml up -d
```

If you change anything in the project and run it on Docker, you can also use this command shown below:

```sh
$ cd cleaning-booking-microservices
$ docker compose -f docker-compose.yml up -d --build
```

To monitor the application, you can use the following tools:

- **Prometheus**  
  Open in your browser at [http://localhost:9090](http://localhost:9090)  
  Prometheus collects and stores application metrics.

- **Grafana**  
  Open in your browser at [http://localhost:3000](http://localhost:3000)  
  Grafana provides a dashboard for visualizing the metrics.  
  **Default credentials**:
    - Username: `admin`
    - Password: `admin`

Define prometheus data source url, use this link shown below:

```text
http://prometheus:9090
```

To trace the application, you can use the following tool:

- **Zipkin**  
  Open in your browser at `http://localhost:9411/`  
  Zipkin provides distributed tracing across the microservices and helps you follow request flow between gateway and backend services.

- **Kafdrop**  
  Open in your browser at `http://localhost:9091/`  
  Kafdrop provides a web UI for browsing Kafka brokers, topics, partitions, consumers, and messages such as booking events and DLQ messages (for example, `booking.events.dlt`).

### Docker Image Location

```text
https://hub.docker.com/repository/docker/noyandocker/cleaning-booking-microservices-config-server/general
https://hub.docker.com/repository/docker/noyandocker/cleaning-booking-microservices-discovery-server/general
https://hub.docker.com/repository/docker/noyandocker/cleaning-booking-microservices-api-gateway/general
https://hub.docker.com/repository/docker/noyandocker/cleaning-booking-microservices-professional-service/general
https://hub.docker.com/repository/docker/noyandocker/cleaning-booking-microservices-booking-service/general
```

---

### SonarQube

- Go to `localhost:9000` for Docker
- Enter username and password as `admin`
- Change password
- Click `Create Local Project`
- Choose the baseline for this code for the project as `Use the global setting`
- Click `Locally` in Analyze Method
- Define Token
- Click `Continue`
- Copy `sonar.host.url` and `sonar.token` (`sonar.login`) in the `properties` part in `pom.xml`
- Run `mvn sonar:sonar` to show code analysis

### 📸 Screenshots

<details>
<summary>Click here to show the screenshots of project</summary>
    <p> Figure 1 </p>
    <img src ="screenshots/1.PNG">
    <p> Figure 2 </p>
    <img src ="screenshots/2.PNG">
    <p> Figure 3 </p>
    <img src ="screenshots/3.PNG">
    <p> Figure 4 </p>
    <img src ="screenshots/jacoco_report_booking_service.PNG">
    <p> Figure 5 </p>
    <img src ="screenshots/jacoco_report_professional_service.PNG">
    <p> Figure 6 </p>
    <img src ="screenshots/docker.PNG">
    <p> Figure 7 </p>
    <img src ="screenshots/prometheus.PNG">
    <p> Figure 8 </p>
    <img src ="screenshots/grafana.PNG">
    <p> Figure 9 </p>
    <img src ="screenshots/zipkin_1.PNG">
    <p> Figure 10 </p>
    <img src ="screenshots/sonarqube_1.PNG">
    <p> Figure 11 </p>
    <img src ="screenshots/sonarqube_2.PNG">
    <p> Figure 12 </p>
    <img src ="screenshots/sonarqube_3.PNG">
    <p> Figure 13 </p>
    <img src ="screenshots/sonarqube_4.PNG">
    <p> Figure 14 </p>
    <img src ="screenshots/sonarqube_5.PNG">
    <p> Figure 15 </p>
    <img src ="screenshots/sonarqube_6.PNG">
</details>

---

### Contributors

- [Sercan Noyan Germiyanoğlu](https://github.com/Rapter1990)
