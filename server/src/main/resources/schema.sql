-- Reservo Database Schema (H2 Compatible)

-- Users table
CREATE TABLE IF NOT EXISTS users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL,
    username_hash VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    email VARCHAR(100),
    is_admin BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Resources table
CREATE TABLE IF NOT EXISTS resources (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    capacity INT NOT NULL,
    slot_duration_minutes INT NOT NULL DEFAULT 60,
    booking_horizon_days INT NOT NULL DEFAULT 30,
    max_hours_per_day INT,
    rules_json TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_capacity CHECK (capacity > 0)
);

-- Time slots table
CREATE TABLE IF NOT EXISTS time_slot (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    resource_id BIGINT NOT NULL,
    start_ts TIMESTAMP NOT NULL,
    end_ts TIMESTAMP NOT NULL,
    capacity_remaining INT NOT NULL DEFAULT 0,
    version INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_time_slot_resource FOREIGN KEY (resource_id) REFERENCES resources(id) ON DELETE CASCADE,
    CONSTRAINT unique_slot UNIQUE (resource_id, start_ts, end_ts),
    CONSTRAINT chk_capacity_remaining CHECK (capacity_remaining >= 0),
    CONSTRAINT chk_time_order CHECK (start_ts < end_ts)
);

-- Holds table
CREATE TABLE IF NOT EXISTS hold (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    time_slot_id BIGINT NOT NULL,
    qty INT NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    request_id VARCHAR(100) UNIQUE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_hold_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_hold_time_slot FOREIGN KEY (time_slot_id) REFERENCES time_slot(id) ON DELETE CASCADE,
    CONSTRAINT unique_user_slot_hold UNIQUE (user_id, time_slot_id),
    CONSTRAINT chk_hold_qty CHECK (qty > 0)
);

-- Reservations table
CREATE TABLE IF NOT EXISTS reservation (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    time_slot_id BIGINT NOT NULL,
    qty INT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'CONFIRMED',
    request_id VARCHAR(100) UNIQUE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_reservation_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_reservation_time_slot FOREIGN KEY (time_slot_id) REFERENCES time_slot(id) ON DELETE CASCADE,
    CONSTRAINT unique_confirmed_reservation UNIQUE (time_slot_id, user_id, status),
    CONSTRAINT chk_reservation_qty CHECK (qty > 0)
);

-- Waitlist table (FIFO)
CREATE TABLE IF NOT EXISTS waitlist (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    time_slot_id BIGINT NOT NULL,
    queued_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_waitlist_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_waitlist_time_slot FOREIGN KEY (time_slot_id) REFERENCES time_slot(id) ON DELETE CASCADE,
    CONSTRAINT unique_user_slot_waitlist UNIQUE (user_id, time_slot_id)
);

-- Notifications table
CREATE TABLE IF NOT EXISTS notification (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    type VARCHAR(50) NOT NULL,
    message TEXT NOT NULL,
    read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_notification_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Audit events table (append-only)
CREATE TABLE IF NOT EXISTS audit_event (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    ts TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    type VARCHAR(50) NOT NULL,
    payload_json TEXT NOT NULL
);

-- Indexes for performance
CREATE INDEX IF NOT EXISTS idx_time_slot_resource_start ON time_slot(resource_id, start_ts);
CREATE INDEX IF NOT EXISTS idx_hold_expires ON hold(expires_at);
CREATE INDEX IF NOT EXISTS idx_reservation_user ON reservation(user_id);
CREATE INDEX IF NOT EXISTS idx_reservation_status ON reservation(status);
CREATE INDEX IF NOT EXISTS idx_waitlist_slot ON waitlist(time_slot_id, queued_at);
CREATE INDEX IF NOT EXISTS idx_notification_user ON notification(user_id, read);
