# --- !Ups

alter table task add column due_to DATE;

# --- !Downs

alter table task drop column "due_to";