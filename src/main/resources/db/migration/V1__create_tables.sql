CREATE TABLE clients(id SERIAL NOT NULL PRIMARY KEY, name VARCHAR(255) NOT NULL, created INT NOT NULL, last_login INT NOT NULL);

CREATE TABLE users(id SERIAL NOT NULL PRIMARY KEY, client_id INT NOT NULL REFERENCES clients(id),
                   external_id VARCHAR(255) NOT NULL, human_readable_id VARCHAR(255) NOT NULL, created INT NOT NULL);

CREATE TABLE events(id SERIAL NOT NULL PRIMARY KEY, client_id INT NOT NULL REFERENCES clients(id),
                    external_user_id VARCHAR(255) NOT NULL, event_name VARCHAR (64) NOT NULL, tstamp INT NOT NULL);
