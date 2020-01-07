show databases;
create database tinyurl;
use tinyurl;
create table urls(id int(32) not null auto_increment primary key, longurl varchar(128), tinyurl varchar(8));
