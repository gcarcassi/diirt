CREATE TABLE data (id INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), name VARCHAR(24) NOT NULL, value DECIMAL(3,2) NOT NULL, timestamp timestamp default CURRENT_TIMESTAMP, CONSTRAINT primary_key PRIMARY KEY (id))
INSERT INTO data(name, value) values('A' , 3.15)
INSERT INTO data(name, value) values('B' , 4.51)
