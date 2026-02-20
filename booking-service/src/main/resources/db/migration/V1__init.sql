CREATE TABLE booking (
                         id VARCHAR(36) PRIMARY KEY,
                         start_at TIMESTAMP NOT NULL,
                         end_at TIMESTAMP NOT NULL,
                         duration_hours INT NOT NULL,
                         vehicle_id VARCHAR(36) NOT NULL,
                         status VARCHAR(20) NOT NULL
);

CREATE TABLE booking_cleaner (
                                 id VARCHAR(36) PRIMARY KEY,
                                 booking_id VARCHAR(36) NOT NULL,
                                 cleaner_id VARCHAR(36) NOT NULL,
                                 CONSTRAINT fk_booking_cleaner_booking
                                     FOREIGN KEY (booking_id) REFERENCES booking(id) ON DELETE CASCADE,
                                 CONSTRAINT uk_booking_cleaner
                                     UNIQUE (booking_id, cleaner_id)
);

CREATE INDEX idx_booking_start_at ON booking(start_at);
CREATE INDEX idx_booking_vehicle_id ON booking(vehicle_id);
CREATE INDEX idx_booking_cleaner_booking_id ON booking_cleaner(booking_id);
CREATE INDEX idx_booking_cleaner_cleaner_id ON booking_cleaner(cleaner_id);
