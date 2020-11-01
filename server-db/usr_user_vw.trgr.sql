create trigger usr_user_instof_trg instead of insert on usr_user_vw
begin
    insert into usr_user (login, hash)
    values (lower(new.login), new.hash);

    insert into usr_log_vw (action_rowid, user_rowid, login, hash)
    values (1, last_insert_rowid(), new.login, new.hash);
end;

create trigger usr_user_instof_upd_trg instead of update on usr_user_vw
begin
    update usr_user
       set hash = ifnull(new.hash, hash)
     where ROWID = ifnull(new.ROWID, (select ROWID from usr_user where login = lower(new.login)));

    insert into usr_log_vw (action_rowid, user_rowid, login, hash)
    values (3, last_insert_rowid(), new.login, new.hash);
end;
