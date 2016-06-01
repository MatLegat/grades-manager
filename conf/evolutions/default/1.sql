
# --- !Ups

CREATE TABLE "User" (
    login varchar(50) NOT NULL,
    name varchar(255) NOT NULL,
    email varchar(255) NOT NULL,
    password varchar(100) NOT NULL,
    PRIMARY KEY (login)
);

# --- !Downs

DROP TABLE "User";
