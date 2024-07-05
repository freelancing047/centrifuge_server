SET statement_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;

CREATE ROLE csiserver LOGIN
  ENCRYPTED PASSWORD 'changeme'
  SUPERUSER INHERIT CREATEDB CREATEROLE;

CREATE DATABASE metadb WITH TEMPLATE = template0 ENCODING = 'UTF8';
