DROP VIEW IF EXISTS "public"."UserStatsJoin";
DROP VIEW IF EXISTS "public"."UserStatsUnion";
DROP VIEW IF EXISTS "public"."FullUserAdmin";
DROP VIEW IF EXISTS "public"."FullSecurity";
DROP VIEW IF EXISTS "public"."FullSharing";
DROP VIEW IF EXISTS "public"."SecurityTree";
DROP VIEW IF EXISTS "public"."SharingTree";
DROP VIEW IF EXISTS "public"."SecurityAuthorizations";
DROP VIEW IF EXISTS "public"."SharingMembership";
DROP VIEW IF EXISTS "public"."UserInfo";

  
CREATE OR REPLACE VIEW "public"."UserInfo" AS
	SELECT DISTINCT
		CAST(0 AS INTEGER) AS "TableId", 
		CAST('UserInfo' AS VARCHAR) AS "TableName", 
		"r"."id" AS "UserId",
		"r"."name" AS "User"
	FROM
		"roles" r,
		"users" u
	WHERE
		"r"."id" = "u"."id";
   
CREATE OR REPLACE VIEW "public"."SharingMembership" AS
	SELECT DISTINCT
		CAST(1 AS INTEGER) AS "TableId", 
		CAST('SharingMembership' AS VARCHAR) AS "TableName", 
		"ur"."id" AS "UserId",
		"ur"."name" AS "User",
		"gr"."id" AS "MembershipId",
		"gr"."name" AS "Membership"
	FROM
		"roles" ur,
		"roles" gr,
		"users" u,
		"groups" g,
		"group_members" m
	WHERE
		"gr"."id" = "m"."group_id"
		AND 
		"gr"."id" = "g"."id"
		AND 
		"ur"."id" = "m"."role_id"
		AND 
		"ur"."id" = "u"."id"
		AND 
		"g"."type" = 0;
   
CREATE OR REPLACE VIEW "public"."SecurityAuthorizations" AS
	SELECT DISTINCT
		CAST(2 AS INTEGER) AS "TableId", 
		CAST('SecurityAuthorizations' AS VARCHAR) AS "TableName", 
		"ur"."id" AS "UserId",
		"ur"."name" AS "User",
		"gr"."id" AS "AuthorizationId",
		"gr"."name" AS "Authorization"
	FROM
		"roles" ur,
		"roles" gr,
		"users" u,
		"groups" g,
		"group_members" m
	WHERE
		"gr"."id" = "m"."group_id"
		AND 
		"gr"."id" = "g"."id"
		AND 
		"ur"."id" = "m"."role_id"
		AND 
		"ur"."id" = "u"."id"
		AND 
		"g"."type" = 1;
   
CREATE OR REPLACE VIEW "public"."SharingTree" AS
	SELECT DISTINCT
		CAST(3 AS INTEGER) AS "TableId", 
		CAST('SharingTree' AS VARCHAR) AS "TableName", 
		"r1"."id" AS "ImpliedMembershipId",
		"r1"."name" AS "ImpliedMembership",
		"r2"."id" AS "AppliedMembershipId",
		"r2"."name" AS "AppliedMembership"
	FROM
		"roles" r1,
		"roles" r2,
		"groups" g1,
		"groups" g2,
		"group_members" m
	WHERE
		"r1"."id" = "m"."role_id"
		AND
		"r1"."id" = "g1"."id"
		AND
		"r2"."id" = "m"."group_id"
		AND
		"r2"."id" = "g2"."id"
		AND
		"g1"."type" = 0
		AND
		"g2"."type" = 0;
   
CREATE OR REPLACE VIEW "public"."SecurityTree" AS
	SELECT DISTINCT
		CAST(4 AS INTEGER) AS "TableId", 
		CAST('SecurityTree' AS VARCHAR) AS "TableName", 
		"r1"."id" AS "AppliedAuthorizationId",
		"r1"."name" AS "AppliedAuthorization",
		"r2"."id" AS "ImpliedAuthorizationId",
		"r2"."name" AS "ImpliedAuthorization"
	FROM
		"roles" r1,
		"roles" r2,
		"groups" g1,
		"groups" g2,
		"group_members" m
	WHERE
		"r1"."id" = "m"."role_id"
		AND
		"r1"."id" = "g1"."id"
		AND
		"r2"."id" = "m"."group_id"
		AND
		"r2"."id" = "g2"."id"
		AND
		"g1"."type" = 1
		AND
		"g2"."type" = 1;
	
CREATE OR REPLACE VIEW "public"."FullSharing" AS
	SELECT DISTINCT
		"v1"."TableId",
		"v1"."TableName",
		"v1"."UserId",
		"v1"."User",
		"v1"."MembershipId",
		"v1"."Membership",
		"v1"."AppliedMembershipId",
		"v1"."AppliedMembership",
		"v1"."ImpliedMembershipId",
		"v1"."ImpliedMembership"
	FROM
	(
		SELECT
			"TableId" AS "TableId",
			"TableName" AS "TableName",
			"UserId" AS "UserId",
			"User" AS "User",
			"MembershipId" AS "MembershipId",
			"Membership" AS "Membership",
			null::BIGINT AS "AppliedMembershipId",
			null::VARCHAR AS "AppliedMembership",
			null::BIGINT AS "ImpliedMembershipId",
			null::VARCHAR AS "ImpliedMembership"
		FROM
			"public"."SharingMembership"
		UNION ALL
		SELECT
			"TableId" AS "TableId",
			"TableName" AS "TableName",
			null::BIGINT AS "UserId",
			null::VARCHAR AS "User",
			null::BIGINT AS "MembershipId",
			null::VARCHAR AS "Membership",
			"AppliedMembershipId" AS "AppliedMembershipId",
			"AppliedMembership" AS "AppliedMembership",
			"ImpliedMembershipId" AS "ImpliedMembershipId",
			"ImpliedMembership" AS "ImpliedMembership"
		FROM
			"public"."SharingTree"
	) v1;
	
CREATE OR REPLACE VIEW "public"."FullSecurity" AS
	SELECT DISTINCT
		"v1"."TableId",
		"v1"."TableName",
		"v1"."UserId",
		"v1"."User",
		"v1"."AuthorizationId",
		"v1"."Authorization",
		"v1"."AppliedAuthorizationId",
		"v1"."AppliedAuthorization",
		"v1"."ImpliedAuthorizationId",
		"v1"."ImpliedAuthorization"
	FROM
	(
		SELECT
			"TableId" AS "TableId",
			"TableName" AS "TableName",
			"UserId" AS "UserId",
			"User" AS "User",
			"AuthorizationId" AS "AuthorizationId",
			"Authorization" AS "Authorization",
			null::BIGINT AS "AppliedAuthorizationId",
			null::VARCHAR AS "AppliedAuthorization",
			null::BIGINT AS "ImpliedAuthorizationId",
			null::VARCHAR AS "ImpliedAuthorization"
		FROM
			"public"."SecurityAuthorizations"
		UNION ALL
		SELECT
			"TableId" AS "TableId",
			"TableName" AS "TableName",
			null::BIGINT AS "UserId",
			null::VARCHAR AS "User",
			null::BIGINT AS "AuthorizationId",
			null::VARCHAR AS "Authorization",
			"AppliedAuthorizationId" AS "AppliedAuthorizationId",
			"AppliedAuthorization" AS "AppliedAuthorization",
			"ImpliedAuthorizationId" AS "ImpliedAuthorizationId",
			"ImpliedAuthorization" AS "ImpliedAuthorization"
		FROM
			"public"."SecurityTree"
	) v1;
	
CREATE OR REPLACE VIEW "public"."FullUserAdmin" AS
	SELECT DISTINCT
		"v1"."TableId",
		"v1"."TableName",
		"v1"."UserId",
		"v1"."User",
		"v1"."MembershipId",
		"v1"."Membership",
		"v1"."AppliedMembershipId",
		"v1"."AppliedMembership",
		"v1"."ImpliedMembershipId",
		"v1"."ImpliedMembership",
		"v1"."AuthorizationId",
		"v1"."Authorization",
		"v1"."AppliedAuthorizationId",
		"v1"."AppliedAuthorization",
		"v1"."ImpliedAuthorizationId",
		"v1"."ImpliedAuthorization"
	FROM
	(
		SELECT
			"TableId" AS "TableId",
			"TableName" AS "TableName",
			"UserId" AS "UserId",
			"User" AS "User",
			"MembershipId" AS "MembershipId",
			"Membership" AS "Membership",
			"AppliedMembershipId" AS "AppliedMembershipId",
			"AppliedMembership" AS "AppliedMembership",
			"ImpliedMembershipId" AS "ImpliedMembershipId",
			"ImpliedMembership" AS "ImpliedMembership",
			null::BIGINT AS "AuthorizationId",
			null::VARCHAR AS "Authorization",
			null::BIGINT AS "AppliedAuthorizationId",
			null::VARCHAR AS "AppliedAuthorization",
			null::BIGINT AS "ImpliedAuthorizationId",
			null::VARCHAR AS "ImpliedAuthorization"
		FROM
			"public"."FullSharing"
		UNION ALL
		SELECT
			"TableId" AS "TableId",
			"TableName" AS "TableName",
			"UserId" AS "UserId",
			"User" AS "User",
			null::BIGINT AS "MembershipId",
			null::VARCHAR AS "Membership",
			null::BIGINT AS "AppliedMembershipId",
			null::VARCHAR AS "AppliedMembership",
			null::BIGINT AS "ImpliedMembershipId",
			null::VARCHAR AS "ImpliedMembership",
			"AuthorizationId" AS "AuthorizationId",
			"Authorization" AS "Authorization",
			"AppliedAuthorizationId" AS "AppliedAuthorizationId",
			"AppliedAuthorization" AS "AppliedAuthorization",
			"ImpliedAuthorizationId" AS "ImpliedAuthorizationId",
			"ImpliedAuthorization" AS "ImpliedAuthorization"
		FROM
			"public"."FullSecurity"
	) v1;
	
CREATE OR REPLACE VIEW "public"."UserStatsUnion" AS
		SELECT 'Total Users' AS "Category", COUNT(1) AS "UserCount" FROM "usersview"
		UNION
		SELECT 'Perpetual Users' AS "Category", COUNT(1) AS "UserCount" FROM "usersview" WHERE "perpetual" = true
		UNION
		SELECT 'Active Users' AS "Category", COUNT(1) AS "UserCount" FROM "usersview" WHERE "suspended" = false AND "disabled" = false
		UNION
		SELECT 'Inactive Users' AS "Category", COUNT(1) AS "UserCount" FROM "usersview" WHERE "suspended" = true OR "disabled" = true
		UNION
		SELECT 'Suspended Users' AS "Category", COUNT(1) AS "UserCount" FROM "usersview" WHERE "suspended" = true
		UNION
		SELECT 'Disabled Users' AS "Category", COUNT(1) AS "UserCount" FROM "usersview" WHERE "disabled" = true;
	
CREATE OR REPLACE VIEW "public"."UserStatsJoin" AS
	SELECT "TotalUsers", "PerpetualUsers", "ActiveUsers", "InactiveUsers", "SuspendedUsers", "DisabledUsers" FROM
		(SELECT 1 AS "key1", COUNT(1) AS "TotalUsers" FROM "usersview") t1
		JOIN
		(SELECT 1 AS "key2", COUNT(1) AS "PerpetualUsers" FROM "usersview" WHERE "perpetual" = true) t2
		ON "key1" = "key2"
		JOIN
		(SELECT 1 AS "key3", COUNT(1) AS "ActiveUsers" FROM "usersview" WHERE "suspended" = false AND "disabled" = false) t3
		ON "key1" = "key3"
		JOIN
		(SELECT 1 AS "key4", COUNT(1) AS "InactiveUsers" FROM "usersview" WHERE "suspended" = true OR "disabled" = true) t4
		ON "key1" = "key4"
		JOIN
		(SELECT 1 AS "key5", COUNT(1) AS "SuspendedUsers" FROM "usersview" WHERE "suspended" = true) t5
		ON "key1" = "key5"
		JOIN
		(SELECT 1 AS "key6", COUNT(1) AS "DisabledUsers" FROM "usersview" WHERE "disabled" = true) t6
		ON "key1" = "key6";
