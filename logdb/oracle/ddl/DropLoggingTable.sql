--deletes the logs table and everything that is related to it.
DECLARE c number;
BEGIN
select count(*) into c from user_sequences where sequence_name = 'LOGSEQ';
        if c > 0 then execute immediate
         'drop sequence LOGSEQ';
        end if;
select count(*) into c from user_triggers where trigger_name = 'LOGS_ID_TRIGGER';
        if c > 0 then execute immediate
         'drop trigger LOGS_ID_TRIGGER';
        end if;
select count(*) into c from user_tables where table_name = 'LOGS';
        if c > 0 then execute immediate
         'drop table logs';
        end if;
END;