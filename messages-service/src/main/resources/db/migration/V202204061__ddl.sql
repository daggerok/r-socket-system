CREATE SCHEMA IF NOT EXISTS public;
----CREATE EXTENSION IF NOT EXISTS "uuid-ossp"; -- required by uuid_generate_v1 in real pg, not h2 with node=pg...
drop table if exists messages;
create table messages(
--id       UUID NOT NULL DEFAULT RANDOM_UUID(),
  id       BIGSERIAL NOT NULL,
  body     VARCHAR NOT NULL,
  at       TIMESTAMP WITH TIME ZONE default now() at time zone 'UTC',
  CONSTRAINT messages_pk PRIMARY KEY (id)
);
