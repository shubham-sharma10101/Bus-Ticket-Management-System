# 🚍 Krisna Travels - Luxury Bus Reservation System

A complete Desktop-based Bus Ticket Booking & Management Application developed in **Java (Swing & AWT)** connected to **MySQL Server 8.0** via JDBC.

## ✨ Key Features
- **Modern User Interface**: Clean split-screen authentication, rounded pill navigation, and flat design aesthetics.
- **20+ Daily Departures**: 4 departures per route covering Morning, Afternoon, Evening, and Night for all major intercity routes.
- **Interactive 2x2 Seat Floorplan**: Real-time visual seat availability (Available in Green, Selected in Amber, Booked in Red) with driver cockpit and aisle walkway.
- **Working Passenger Promo Codes**: Live coupon discounts (`KRISNA20`, `SUPER150`, `WEEKEND15`, `STUDENT30`) with instant fare deduction.
- **MySQL Database Integration**: Full CRUD operations with auto-sync for Users, Buses, and Tickets.
- **Ticket History & Receipt Export**: Cancel bookings in real-time or print formatted `.txt` boarding pass receipts.
- **System Analytics**: Live metrics for total active fleet, confirmed bookings, and total revenue generated.

## 🛠️ Technology Stack
- **Language**: Java (JDK 8 / 11 / 17 / 21)
- **GUI Framework**: Java Swing & AWT
- **Database**: MySQL Community Server 8.0
- **Driver**: MySQL Connector/J (`mysql-connector-j-8.x.jar`)

## 🚀 How to Run
1. Ensure **MySQL Server** is running on `localhost:3306`.
2. Open the project in **IntelliJ IDEA** or Eclipse.
3. Add `mysql-connector-j.jar` to project dependencies.
4. Run `Main.java`. Default admin login: `admin@bus.com` / `admin123`.
