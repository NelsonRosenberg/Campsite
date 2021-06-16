-----------------------------------------------
-- Create Initial Booking Database
-----------------------------------------------

CREATE SEQUENCE IF NOT EXISTS booking_seq START 1 INCREMENT 50;

CREATE TABLE booking
  (
     id          INTEGER NOT NULL,
     booking_id  VARCHAR NOT NULL,
     name        VARCHAR NOT NULL,
     email       VARCHAR NOT NULL,
     PRIMARY KEY (id)
  );

CREATE INDEX booking_id_idx
  ON booking (booking_id);

CREATE TABLE booking_date
  (
     booking_id INTEGER NOT NULL,
     date       DATE NOT NULL
  );

ALTER TABLE booking_date
   ADD CONSTRAINT booking_id_fk
   FOREIGN KEY (booking_id)
   REFERENCES booking;

ALTER TABLE booking_date
  ADD CONSTRAINT date_unique UNIQUE (date);