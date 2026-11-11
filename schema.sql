-- ========================================================
-- KRISNA TRAVELS - BUS RESERVATION SYSTEM DATABASE SCHEMA
-- ========================================================

CREATE DATABASE IF NOT EXISTS krisna_travels;
USE krisna_travels;

-- 1. Users Table (Authentication & Customer Accounts)
CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    phone VARCHAR(20) NOT NULL,
    password VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. Buses Table (Fleet details, routes, schedules, fares)
CREATE TABLE IF NOT EXISTS buses (
    bus_id VARCHAR(50) PRIMARY KEY,
    operator VARCHAR(100) NOT NULL,
    source VARCHAR(100) NOT NULL,
    destination VARCHAR(100) NOT NULL,
    departure_time VARCHAR(50) NOT NULL,
    bus_type VARCHAR(50) NOT NULL,
    total_seats INT NOT NULL,
    fare DOUBLE NOT NULL,
    amenities VARCHAR(255)
);

-- 3. Tickets Table (Bookings, seats, discounts, payments)
CREATE TABLE IF NOT EXISTS tickets (
    ticket_id VARCHAR(50) PRIMARY KEY,
    passenger_name VARCHAR(100) NOT NULL,
    bus_id VARCHAR(50) NOT NULL,
    seat_no VARCHAR(10) NOT NULL,
    pickup_point VARCHAR(100),
    total_fare DOUBLE NOT NULL,
    category VARCHAR(50),
    booking_date_time VARCHAR(50),
    status VARCHAR(20) DEFAULT 'CONFIRMED',
    FOREIGN KEY (bus_id) REFERENCES buses(bus_id) ON DELETE CASCADE
);

-- Default Admin Account (admin@bus.com / admin123)
INSERT IGNORE INTO users (full_name, email, phone, password) 
VALUES ('Administrator', 'admin@bus.com', '9876543210', 'admin123');

-- Default Seed Fleet (4 departures per route)
INSERT IGNORE INTO buses VALUES 
('KT-101', 'Krisna Royal Volvo A/C', 'Delhi', 'Jaipur', '06:00 AM', 'AC Sleeper (2+1)', 20, 750.00, 'WiFi, Charging, Snacks, AC'),
('KT-102', 'Krisna Intercity Express', 'Delhi', 'Jaipur', '11:30 AM', 'Volvo Multi-Axle', 20, 650.00, 'AC, Water Bottle, Recliner'),
('KT-103', 'Krisna Platinum Cruiser', 'Delhi', 'Jaipur', '05:00 PM', 'Scania Executive', 20, 800.00, 'Live Tracking, AC, USB Port'),
('KT-104', 'Krisna Night Star Sleeper', 'Delhi', 'Jaipur', '10:45 PM', 'BharatBenz AC Sleeper', 20, 950.00, 'Blanket, Pillow, WiFi, AC'),
('KT-201', 'Krisna Superfast Express', 'Mumbai', 'Pune', '06:30 AM', 'Volvo 9600 Intercity', 20, 450.00, 'AC, Water Bottle, Recliner'),
('KT-202', 'Krisna Deccan Queen AC', 'Mumbai', 'Pune', '11:00 AM', 'Volvo Multi-Axle', 20, 500.00, 'Charging Port, Reading Lamp, AC'),
('KT-301', 'Krisna Southern Star', 'Bangalore', 'Chennai', '06:00 AM', 'Luxury AC Semi-Sleeper', 20, 600.00, 'AC, Water, USB Charging'),
('KT-302', 'Krisna Coastal Cruiser', 'Bangalore', 'Chennai', '12:30 PM', 'Volvo B11R Multi-Axle', 20, 750.00, 'WiFi, Entertainment TV, AC');
