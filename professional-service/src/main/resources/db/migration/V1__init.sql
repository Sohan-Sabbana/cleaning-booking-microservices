CREATE TABLE vehicle (
                         id VARCHAR(36) PRIMARY KEY,
                         code VARCHAR(50) NOT NULL UNIQUE,
                         license_plate VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE cleaner (
                         id VARCHAR(36) PRIMARY KEY,
                         full_name VARCHAR(255) NOT NULL,
                         vehicle_id VARCHAR(36) NOT NULL,
                         CONSTRAINT fk_cleaner_vehicle
                             FOREIGN KEY (vehicle_id) REFERENCES vehicle(id) ON DELETE CASCADE
);

CREATE INDEX idx_cleaner_vehicle_id ON cleaner(vehicle_id);