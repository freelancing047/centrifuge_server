-- If/when we support optimistic locking of the user tab, init with the commented-out line.
-- INSERT INTO Users (username, password, version) VALUES( 'admin', 'changeme', 1 ), ('centrifuge', 'changeme', 1 );

-- Note this is highly geared to the results of the DDL that come out of the Role, User, and Group 
-- Class hierarchy.  Any changes to the Structure need to be reflected in the statements below!

INSERT INTO Roles ( name ) VALUES ( 'admin' );
INSERT INTO Users ( id, display, password, remark, disabled, perpetual, suspended, creationdate, activatedate ) VALUES ( CURRVAL('roles_id_seq'), 'admin', 'db82de0a7958437da8f486bcaad5ed2ceee75341', 'All-powerful administrator login name', false, true, false, now(), now() );

INSERT INTO Roles ( name ) VALUES ( 'cso' );
INSERT INTO Users ( id, display, password, remark, disabled, perpetual, suspended, creationdate, activatedate ) VALUES ( CURRVAL('roles_id_seq'), 'cso', 'db82de0a7958437da8f486bcaad5ed2ceee75341', 'Centrifuge Security Officer login name', false, true, false, now(), now() );

INSERT INTO Roles ( name ) VALUES ( 'centrifuge' );
INSERT INTO Users ( id, display, password, remark, disabled, perpetual, suspended, creationdate, activatedate ) VALUES (  CURRVAL('roles_id_seq'), 'centrifuge', 'db82de0a7958437da8f486bcaad5ed2ceee75341', 'Normal user login name', false, false, false, now(), now() );

INSERT INTO Roles ( name ) VALUES ( 'administrators' );
INSERT INTO Groups ( id, display, type, remark )  VALUES ( CURRVAL('roles_id_seq'), 'Administrators', 0, 'All-powerful administrator group' );

INSERT INTO Roles ( name ) VALUES ( 'securityofficers' );
INSERT INTO Groups ( id, display, type, remark )  VALUES ( CURRVAL('roles_id_seq'), 'SecurityOfficers', 0, 'All-powerful security administrator group' );

INSERT INTO Roles ( name ) VALUES ( 'everyone' );
INSERT INTO Groups ( id, display, type, remark )  VALUES ( CURRVAL('roles_id_seq'), 'Everyone', 0, 'All users (default group)' );

-- Identify US classifications

INSERT INTO Roles ( name ) VALUES ( 'confidential' );
INSERT INTO Groups ( id, display, type, remark )  VALUES ( CURRVAL('roles_id_seq'), 'CONFIDENTIAL', 1, 'Authorized to handle CONFIDENTIAL intelligence' );
INSERT INTO CapcoGroups ( id, section, header, paragraph, mask, enforce )  VALUES ( CURRVAL('roles_id_seq'), 0, 'CONFIDENTIAL', 'C', 0, true );

INSERT INTO Roles ( name ) VALUES ( 'secret' );
INSERT INTO Groups ( id, display, type, remark )  VALUES ( CURRVAL('roles_id_seq'), 'SECRET', 1, 'Authorized to handle SECRET intelligence' );
INSERT INTO CapcoGroups ( id, section, header, paragraph, mask, enforce )  VALUES ( CURRVAL('roles_id_seq'), 0, 'SECRET', 'S', 0, true );

INSERT INTO Roles ( name ) VALUES ( 'top secret' );
INSERT INTO Groups ( id, display, type, remark )  VALUES ( CURRVAL('roles_id_seq'), 'TOP SECRET', 1, 'Authorized to handle TOP SECRET intelligence' );
INSERT INTO CapcoGroups ( id, section, header, paragraph, mask, enforce )  VALUES ( CURRVAL('roles_id_seq'), 0, 'TOP SECRET', 'TS', 0, true );

-- Identify US compartments

INSERT INTO Roles ( name ) VALUES ( 'sci' );
INSERT INTO Groups ( id, display, type, remark )  VALUES ( CURRVAL('roles_id_seq'), 'SCI', 1, 'Authorized to handle HCS, KDK, SI, and TK' );
INSERT INTO CapcoGroups ( id, section, header, paragraph, mask, enforce )  VALUES ( CURRVAL('roles_id_seq'), 8, null, null, 0, false );

INSERT INTO Roles ( name ) VALUES ( 'sci-hcs' );
INSERT INTO Groups ( id, display, type, remark )  VALUES ( CURRVAL('roles_id_seq'), 'SCI-HCS', 1, 'Authorized to handle sensitive human intelligence' );
INSERT INTO CapcoGroups ( id, section, header, paragraph, mask, enforce )  VALUES ( CURRVAL('roles_id_seq'), 1, 'HCS', 'HCS', 0, true );

INSERT INTO Roles ( name ) VALUES ( 'sci-kdk' );
INSERT INTO Groups ( id, display, type, remark )  VALUES ( CURRVAL('roles_id_seq'), 'SCI-KDK', 1, 'Authorized to handle sensitive geospatial intelligence' );
INSERT INTO CapcoGroups ( id, section, header, paragraph, mask, enforce )  VALUES ( CURRVAL('roles_id_seq'), 1, 'KDK', 'KDK', 0, true );

INSERT INTO Roles ( name ) VALUES ( 'sci-si' );
INSERT INTO Groups ( id, display, type, remark )  VALUES ( CURRVAL('roles_id_seq'), 'SCI-SI', 1, 'Authorized to handle communications intelligence' );
INSERT INTO CapcoGroups ( id, section, header, paragraph, mask, enforce )  VALUES ( CURRVAL('roles_id_seq'), 1, 'SI', 'SI', 0, true );

INSERT INTO Roles ( name ) VALUES ( 'sci-tk' );
INSERT INTO Groups ( id, display, type, remark )  VALUES ( CURRVAL('roles_id_seq'), 'SCI-TK', 1, 'Authorized to handle satellite intelligence' );
INSERT INTO CapcoGroups ( id, section, header, paragraph, mask, enforce )  VALUES ( CURRVAL('roles_id_seq'), 1, 'TK', 'TK', 0, true );

-- Identify US dissemination

INSERT INTO Roles ( name ) VALUES ( 'us-only' );
INSERT INTO Groups ( id, display, type, remark )  VALUES ( CURRVAL('roles_id_seq'), 'US-ONLY', 1, 'Not a Foreign National' );
INSERT INTO CapcoGroups ( id, section, header, paragraph, mask, enforce )  VALUES ( CURRVAL('roles_id_seq'), 5, 'NOT RELEASABLE TO FOREIGN NATIONALS', 'NOFORN', 0, true );

INSERT INTO Roles ( name ) VALUES ( 'fisa-ok' );
INSERT INTO Groups ( id, display, type, remark )  VALUES ( CURRVAL('roles_id_seq'), 'FISA-OK', 1, 'Not a Law Enforcement Official' );
INSERT INTO CapcoGroups ( id, section, header, paragraph, mask, enforce )  VALUES ( CURRVAL('roles_id_seq'), 5, 'FOREIGN INTELLIGENCE SURVEILLANCE ACT', 'FISA', 0, true );

INSERT INTO Roles ( name ) VALUES ( 'fouo' );
INSERT INTO Groups ( id, display, type, remark )  VALUES ( CURRVAL('roles_id_seq'), 'FOUO', 1, 'Authorized to handle official documents' );
INSERT INTO CapcoGroups ( id, section, header, paragraph, mask, enforce )  VALUES ( CURRVAL('roles_id_seq'), 5, 'FOR OFFICIAL USE ONLY', 'FOUO', 0, false );

-- Identify NATO classifications

INSERT INTO Roles ( name ) VALUES ( 'nato restricted' );
INSERT INTO Groups ( id, display, type, remark )  VALUES ( CURRVAL('roles_id_seq'), 'NATO RESTRICTED', 1, 'Authorized to handle RESTRICTED NATO information' );
INSERT INTO CapcoGroups ( id, section, header, paragraph, mask, enforce )  VALUES ( CURRVAL('roles_id_seq'), 0, '//NATO RESTRICTED', '//NR', 0, true );

INSERT INTO Roles ( name ) VALUES ( 'nato confidential' );
INSERT INTO Groups ( id, display, type, remark )  VALUES ( CURRVAL('roles_id_seq'), 'NATO CONFIDENTIAL', 1, 'Authorized to handle CONFIDENTIAL NATO information' );
INSERT INTO CapcoGroups ( id, section, header, paragraph, mask, enforce )  VALUES ( CURRVAL('roles_id_seq'), 0, '//NATO CONFIDENTIAL', '//NC', 0, true );

INSERT INTO Roles ( name ) VALUES ( 'nato secret' );
INSERT INTO Groups ( id, display, type, remark )  VALUES ( CURRVAL('roles_id_seq'), 'NATO SECRET', 1, 'Authorized to handle SECRET NATO information' );
INSERT INTO CapcoGroups ( id, section, header, paragraph, mask, enforce )  VALUES ( CURRVAL('roles_id_seq'), 0, '//NATO SECRET', '//NS', 0, true );

INSERT INTO Roles ( name ) VALUES ( 'cosmic top secret' );
INSERT INTO Groups ( id, display, type, remark )  VALUES ( CURRVAL('roles_id_seq'), 'COSMIC TOP SECRET', 1, 'Authorized to handle TOP SECRET NATO information' );
INSERT INTO CapcoGroups ( id, section, header, paragraph, mask, enforce )  VALUES ( CURRVAL('roles_id_seq'), 0, '//COSMIC TOP SECRET', '//CTS', 0, true );


INSERT INTO Group_Members (GROUP_ID, ROLE_ID ) 
    SELECT groups.id, roles.id 
    FROM roles as groups, roles 
    WHERE groups.name = 'administrators' and roles.name = 'admin';

INSERT INTO Group_Members (GROUP_ID, ROLE_ID ) 
    SELECT groups.id, roles.id 
    FROM roles as groups, roles 
    WHERE groups.name = 'securityofficers' and roles.name = 'cso';

INSERT INTO Group_Members (GROUP_ID, ROLE_ID ) 
    SELECT groups.id, roles.id 
    FROM roles as groups, roles 
    WHERE groups.name = 'everyone' and roles.name = 'centrifuge';

-- Setup classification inheritance for US classifications

INSERT INTO Group_Members (GROUP_ID, ROLE_ID ) 
    SELECT groups.id, roles.id 
    FROM roles as groups, roles 
    WHERE groups.name = 'confidential' and roles.name = 'secret';

INSERT INTO Group_Members (GROUP_ID, ROLE_ID ) 
    SELECT groups.id, roles.id 
    FROM roles as groups, roles 
    WHERE groups.name = 'secret' and roles.name = 'top secret';

-- Setup classification inheritance for US compartments

INSERT INTO Group_Members (GROUP_ID, ROLE_ID ) 
    SELECT groups.id, roles.id 
    FROM roles as groups, roles 
    WHERE groups.name = 'sci-hcs' and roles.name = 'sci';

INSERT INTO Group_Members (GROUP_ID, ROLE_ID ) 
    SELECT groups.id, roles.id 
    FROM roles as groups, roles 
    WHERE groups.name = 'sci-si' and roles.name = 'sci';

INSERT INTO Group_Members (GROUP_ID, ROLE_ID ) 
    SELECT groups.id, roles.id 
    FROM roles as groups, roles 
    WHERE groups.name = 'sci-tk' and roles.name = 'sci';

INSERT INTO Group_Members (GROUP_ID, ROLE_ID ) 
    SELECT groups.id, roles.id 
    FROM roles as groups, roles 
    WHERE groups.name = 'sci-kdk' and roles.name = 'sci';

-- Setup classification inheritance for NATO classifications

INSERT INTO Group_Members (GROUP_ID, ROLE_ID ) 
    SELECT groups.id, roles.id 
    FROM roles as groups, roles 
    WHERE groups.name = 'nato restricted' and roles.name = 'nato confidential';

INSERT INTO Group_Members (GROUP_ID, ROLE_ID ) 
    SELECT groups.id, roles.id 
    FROM roles as groups, roles 
    WHERE groups.name = 'nato confidential' and roles.name = 'nato secret';

INSERT INTO Group_Members (GROUP_ID, ROLE_ID ) 
    SELECT groups.id, roles.id 
    FROM roles as groups, roles 
    WHERE groups.name = 'nato secret' and roles.name = 'cosmic top secret';
