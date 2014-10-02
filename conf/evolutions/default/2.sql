# Task-user schema

# --- !Ups

Create table task_user (
  login varchar(255) NOT NULL,
  constraint pk_task_user primary key (login)
);

alter table task add user_login varchar(255) NOT NULL;
alter table task add constraint fk_task_user foreign key (user_login) references task_user (login) on delete cascade on update cascade;
create index ix_task_user_1 on task (user_login);

insert into task_user values ('anonymous');
insert into task_user values ('pedro');
insert into task_user values ('pablo');
insert into task_user values ('sergio');

# --- !Downs

SET REFERENTIAL_INTEGRITY FALSE;

drop table if exists task_user;

alter table task drop constraint "fk_task_user";
alter table task drop index "ix_task_user_1";
ALTER TABLE task drop column "user_login"

SET REFERENTIAL_INTEGRITY TRUE;