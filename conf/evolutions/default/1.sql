
# --- !Ups

CREATE TABLE "User" (
    login varchar(50) NOT NULL PRIMARY KEY,
    name varchar(255) NOT NULL,
    email varchar(255) NOT NULL UNIQUE,
    password varchar(100) NOT NULL
);

# --- !Downs

DROP TABLE "User";
