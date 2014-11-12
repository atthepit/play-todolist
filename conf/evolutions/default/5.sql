# --- !Ups

ALTER TABLE task ADD COLUMN category BIGINT;
ALTER TABLE task ADD CONSTRAINT fk_task_category FOREIGN KEY (category) REFERENCES category(id);

# --- !Downs

ALTER TABLE task DROP FOREIGN KEY fk_task_category;
ALTER TABLE task DROP COLUMN "category";