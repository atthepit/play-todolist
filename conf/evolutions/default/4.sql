# --- !Ups

CREATE SEQUENCE category_id_seq;
CREATE TABLE category (
	id INTEGER NOT NULL DEFAULT NEXTVAL('category_id_seq'),
	name VARCHAR(255) NOT NULL,
	user_login VARCHAR(255) NOT NULL
);

INSERT INTO category (name, user_login) VALUES('shopping', 'pedro');

# --- !Downs

DROP TABLE category;