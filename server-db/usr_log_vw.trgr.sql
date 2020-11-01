create trigger usr_log_instof_trg instead of insert on usr_log_vw
begin
    insert into usr_log (action_time, action_rowid, user_rowid, login, hash, txt)
    values ((select strftime('%Y-%m-%d %H:%M:%S.%f', 'now'))
          , new.action_rowid
          , ifnull(new.user_rowid, (select ROWID from usr_user us where us.login = lower(new.login)))
          , new.login
          , new.hash
          , new.txt);
end;
