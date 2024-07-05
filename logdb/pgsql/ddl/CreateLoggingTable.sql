--create function to define the plpgsql language. This language is created only if it doesn't exist.
CREATE OR REPLACE FUNCTION make_plpgsql()
RETURNS VOID
LANGUAGE SQL
AS $$
CREATE LANGUAGE plpgsql;
$$;

--executes the make_plpgsql function if it doesn't exist.
SELECT
    CASE
    WHEN EXISTS(
        SELECT 1
        FROM pg_catalog.pg_language
        WHERE lanname='plpgsql'
    )
    THEN NULL
    ELSE make_plpgsql() END;

--deletes function that creates the plpgsql language.
DROP FUNCTION make_plpgsql();

--function which creates the logging table. It checks first if any table with logs name exists and if not it will create it.
create or replace function create_log_table_if_not_exists() returns void as
$$
begin

    if not exists(select * from information_schema.tables
        where
            table_catalog = CURRENT_CATALOG and table_schema = CURRENT_SCHEMA
            and table_name = 'logs') then
        if not exists (SELECT * FROM pg_class c WHERE c.relkind = 'S' and c.relname='logs_sequence') then
	      create sequence logs_sequence;
        end if;
        CREATE TABLE logs (
          id                  int8 CONSTRAINT logs_pk PRIMARY KEY DEFAULT NEXTVAL('logs_sequence'),
          log_date            timestamp with time zone,
          location_info       varchar(1000),
          message             varchar(4000),
          priority            varchar(50),
          thread_name         varchar(200),
          user_name           varchar(200),
          session_id          varchar(100),
          action_uri          varchar(200),
          client_ip_address   varchar(50),
          server_ip_address   varchar(50),
          application_id      varchar(50)
        );

    end if;

end;
$$
language 'plpgsql';

--execute function that creates the logging table
select create_log_table_if_not_exists();
--deletes the function that creates the logging table
drop function create_log_table_if_not_exists();