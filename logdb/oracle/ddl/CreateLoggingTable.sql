--create sequence which will increment the primary key value. Sequence is created only if no other sequence with LOGSEQ name exists
DECLARE c number;
BEGIN
select count(*) into c from user_sequences where sequence_name = 'LOGSEQ';
        if c = 0 then execute immediate
            'CREATE SEQUENCE LOGSEQ START WITH 1 INCREMENT BY 1';
        end if;
--verify if any table with LOGS name exists and if not create it. This table will contain the centrifuge logs
select count(*) into c from user_tables where table_name = 'LOGS';
if c = 0 then execute immediate 'create table logs(
          id                 integer NOT NULL PRIMARY KEY,
          log_date            date,
          location_info       varchar2(1000),
          message             varchar2(4000),
          priority            varchar2(50),
          thread_name         varchar2(200),
          user_name           varchar2(200),
          session_id          varchar2(100),
          action_uri          varchar2(200),
          client_ip_address   varchar2(50),
          server_ip_address   varchar2(50),
          application_id      varchar2(50))';
end if;
--verify if a trigger with LOGS_ID_TRIGGER name exists and if not create it.
--This trigger will execute before any insert into logs table and will generate the value for the primary key of this table.
select count(*) into c from user_triggers where trigger_name = 'LOGS_ID_TRIGGER';
if c = 0 then execute immediate
     'create trigger LOGS_ID_TRIGGER 
      before insert 
      on logs 
      for each row 
      begin 
         select LOGSEQ.nextval into :new.id from dual; 
      end;';      
end if;
END;