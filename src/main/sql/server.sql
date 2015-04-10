drop table raw;
create table raw (
id INT AUTO_INCREMENT PRIMARY KEY,
timestamp INT(11),
device VARCHAR(128),
user VARCHAR(128),
topic VARCHAR(128),
latitude VARCHAR(20),
longitude VARCHAR(20),
speed VARCHAR(10),
altitude VARCHAR(10),
comment VARCHAR(1024),
INDEX(timestamp,user)
);
