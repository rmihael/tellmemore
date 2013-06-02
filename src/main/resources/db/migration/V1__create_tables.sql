CREATE TABLE clients(id SERIAL NOT NULL PRIMARY KEY, email VARCHAR(255) NOT NULL, name VARCHAR(255) NOT NULL,
                     created BIGINT NOT NULL);

CREATE TABLE users(id SERIAL NOT NULL PRIMARY KEY, client_id INT NOT NULL REFERENCES clients(id),
                   external_id VARCHAR(255) NOT NULL, created BIGINT NOT NULL);

CREATE TABLE events(id SERIAL NOT NULL PRIMARY KEY, client_id INT NOT NULL REFERENCES clients(id),
                    external_user_id VARCHAR(255) NOT NULL, event_name VARCHAR (64) NOT NULL, tstamp BIGINT NOT NULL);

CREATE TABLE facts(id SERIAL NOT NULL PRIMARY KEY, client_id INT NOT NULL REFERENCES clients(id),
                   fact_type SMALLINT NOT NULL, fact_name VARCHAR(255) NOT NULL, created BIGINT NOT NULL);

CREATE TABLE fact_values(id SERIAL NOT NULL PRIMARY KEY, fact_id INT NOT NULL REFERENCES facts(id),
                         user_id INT NOT NULL REFERENCES users(id), numeric_value FLOAT, string_value VARCHAR(255),
                         tstamp BIGINT NOT NULL);
