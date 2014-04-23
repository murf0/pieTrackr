create table raw (
id INT AUTO_INCREMENT PRIMARY KEY,
timestamp TIMESTAMP, INDEX(timestamp),
latitude VARCHAR(20),
longitude VARCHAR(20),
speed VARCHAR(10),
altitude VARCHAR(10),
comment VARCHAR(300)
);