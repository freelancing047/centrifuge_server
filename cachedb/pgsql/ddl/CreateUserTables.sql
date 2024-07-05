
CREATE VIEW UsersView AS
    SELECT roles.name, users.password, users.lastlogin, users.expirationdate, users.disabled, users.perpetual, users.suspended
    FROM roles, users
    WHERE roles.id = users.id;

CREATE VIEW GroupMembershipView AS
    SELECT r1.name AS group_name, r2.name AS role_name 
    FROM roles r1, Group_Members, roles r2
    WHERE r1.id = Group_Members.group_id AND r2.id = Group_Members.role_id;
   
