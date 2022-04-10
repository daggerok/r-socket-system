CREATE SCHEMA IF NOT EXISTS public;
drop table if exists users;
create table users(
  id       BIGSERIAL NOT NULL,
  name     VARCHAR NOT NULL,
  created_at       TIMESTAMP WITH TIME ZONE default now() at time zone 'UTC',
  CONSTRAINT users_pk PRIMARY KEY (id)
);
