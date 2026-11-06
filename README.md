# ParkDeck

Hourly parking marketplace. React (Vite) and Spring Boot are separate applications.

```
ParkDeck-Fresh/
├── frontend/    React + Vite. Open this folder for the UI.
└── backend/     Spring Boot. Open this folder in IntelliJ IDEA.
```

The frontend runs at http://localhost:5173. The API runs at http://localhost:8080.

## Billing

Partial hours are charged proportionally. Minutes are divided by 60 and rounded half-up to 4 decimal places, multiplied by the hourly rate, then rounded half-up to 2 decimal places. A 5% service fee is added the same way. Booking confirmation stores a reservation. It does not collect payment.

A confirmed booking can be cancelled until its end time. Cancelled rows stay in history. Host earnings sum `totalPrice` of confirmed bookings only.

Times are interpreted in Asia/Kolkata. A listing's available hours are a same-day window. Overnight bookings are rejected unless the space is open from 00:00 through 23:59. Bookings cannot start in the past or run longer than 7 days. Touching windows (one ends when the next starts) are allowed. Overlaps of confirmed bookings are rejected inside a transaction that locks the parking space row.

## MySQL

1. Install MySQL 8 and start the service. On this machine the service name is `MySQL80`.
2. Create the database (the JDBC URL can also create it when the account is allowed to):

```sql
CREATE DATABASE parkdeck CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

3. Set environment variables. Spring Boot does not load `backend/.env.example` by itself. In IntelliJ: Run > Edit Configurations > Environment variables:

```
DB_URL=jdbc:mysql://localhost:3306/parkdeck?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Kolkata
DB_USERNAME=root
DB_PASSWORD=your-mysql-password
JWT_SECRET=replace-with-a-random-string-at-least-32-bytes
```

PowerShell:

```powershell
$env:DB_URL="jdbc:mysql://localhost:3306/parkdeck?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Kolkata"
$env:DB_USERNAME="root"
$env:DB_PASSWORD="your-mysql-password"
$env:JWT_SECRET="replace-with-a-random-string-at-least-32-bytes"
```

`application.properties` uses those variables. If they are missing, it falls back to user `root`, an empty password, and a local-only JWT secret. Replace that secret before any shared deployment. `spring.jpa.hibernate.ddl-auto=update` is for local development only. It is not a production migration strategy.

## Run the backend

Open `ParkDeck-Fresh/backend` in IntelliJ as a Maven project. Use JDK 21 or newer (this machine has JDK 25; the project compiles to Java 21 bytecode). Main class: `com.parkdeck.ParkdeckApplication`. Port 8080.

From the backend folder, if `mvn` is not on PATH:

```powershell
.\mvnw.cmd test
.\mvnw.cmd spring-boot:run
```

On first startup with an empty `parking_spaces` table, demo listings are inserted once. Demo accounts, local only:

- Driver: `meera.driver@parkdeck.local` / `Parkdeck1`
- Host: `aarav.host@parkdeck.local` / `Parkdeck1`

## Run the frontend

Open `ParkDeck-Fresh/frontend` separately.

```powershell
npm install
npm run dev
npm run build
```

Copy `.env.example` to `.env` if needed. The variable is `VITE_API_BASE_URL=http://localhost:8080`.

CORS allows `http://localhost:5173` and `http://127.0.0.1:5173`.

## Tests

`mvnw test` uses an in-memory H2 database so authentication, listings, pricing, invalid times, overlaps, ownership, and wishlists can run without MySQL. Those tests do not prove the MySQL connection. Start the app with the variables above to verify MySQL.
