-- Migrate SQL must use $ as separator

drop procedure if exists sp_demo $
create procedure sp_demo()
begin
    declare continue handler for SQLEXECEPTION begin end;
    alter table tablename
      add column 'column_name' INT NULL default 0 comment 'demo to add column when migrate';
end $

call sp_demo $

-- update your_tabe set field = 'value' where id = 1 $
-- other migrate SQL