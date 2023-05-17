CREATE TABLE IF NOT EXISTS subscription (
     id IDENTITY PRIMARY KEY,
     creation_date TIMESTAMP(6) NOT NULL,
     addressable VARCHAR(255) NOT NULL,
     filters JSON
);