package csi.security.queries;

/**
 * Created by centrifuge on 3/29/2018.
 */
public enum AclCommands {
   GET_CAPCO_TAGS("SELECT t FROM CapcoSecurityTag t", "SELECT t FROM capcosecuritytag t"),
   GET_GENERIC_TAGS("SELECT t FROM GenericSecurityTag t", "SELECT t FROM genericsecuritytag t"),
   GET_SOURCE_CONTROLS("SELECT e FROM SourceAclEntry e", "SELECT e FROM sourceaclentry e"),
   GET_RESOURCE("SELECT d FROM ModelResource d WHERE (d.uuid.uuid = :uuid)",
         "SELECT d FROM modelresource d WHERE (d.uuid = :uuid)"),
   GET_RESOURCE_NAME("SELECT d.name FROM ModelResource d WHERE (d.uuid.uuid = :uuid)",
         "SELECT d.name FROM modelresource d WHERE (d.uuid = :uuid)"),
   GET_OWNER("SELECT a.owner FROM ACL a WHERE (a.uuid = :uuid)", "SELECT a.owner FROM acl a WHERE (a.uuid = :uuid)"),
   GET_ACL_ENTRIES("SELECT a.entries FROM ACL a WHERE (a.uuid IN (:uuids))",
         "SELECT a.entries FROM acl a WHERE (a.uuid IN (:uuids))"),
   CHANGE_OWNER_ACL("UPDATE ACL SET owner = :owner WHERE uuid IN (:resources)",
         "UPDATE acl SET owner = :owner WHERE uuid IN (:resources)"),
   CHANGE_OWNER_RESOURCE("UPDATE ModelResource SET owner = :owner WHERE uuid.uuid IN (:resources)",
         "UPDATE modelresource SET owner = :owner WHERE uuid IN (:resources)"),
   CHANGE_NAME("UPDATE ModelResource SET name = :name WHERE uuid.uuid = :uuid",
         "UPDATE modelresource SET name = :name WHERE uuid = :uuid"),
   CHANGE_ALL_OWNERSHIP_ACL("UPDATE ACL SET owner = :newowner WHERE owner = :oldowner",
         "UPDATE acl SET owner = :newowner WHERE owner = :oldowner"),
   CHANGE_ALL_OWNERSHIP_RESOURCE("UPDATE ModelResource SET owner = :newowner WHERE owner = :oldowner",
         "UPDATE modelresource SET owner = :newowner WHERE owner = :oldowner"),
   RENAME_RESOURCE("UPDATE ModelResource SET name = :name, remarks = :remarks WHERE uuid.uuid = :uuid",
         "UPDATE modelresource SET name = :name, remarks = :remarks WHERE uuid = :uuid"),
   REQUIRE_OWNERSHIP(" AND (a.owner IN (:roles))", " AND (a.owner IN (:roles))"),
   GET_MULTIPLE_RESOURCE_ACL_LIST("SELECT a1 FROM ACL a1 WHERE uuid IN (:resources)",
         "SELECT a1 FROM acl a1 WHERE uuid IN (:resources)"),
   GET_SINGLE_RESOURCE_ACL_LIST("SELECT a1 FROM ACL a1 WHERE uuid = :uuid", "SELECT a1 FROM acl a1 WHERE uuid = :uuid"),
   GET_USER_RESOURCE_ACL_LIST("SELECT a FROM ACL a WHERE :role IN (SELECT e.roleName FROM a.entries e)",
         "SELECT a FROM acl a WHERE :role IN (SELECT e.rolename FROM a.entries e)"),
   HAS_PARAMETERS("EXISTS (SELECT 1 FROM QueryParameterDef p WHERE p.parent = d.uuid.uuid)",
         "EXISTS (SELECT 1 FROM queryparameterdef p WHERE p.parent = d.uuid)");

   private String[] text;

   private AclCommands(String javaIn, String sqlIn) {
      text = ((javaIn == null) || (sqlIn == null)) ? null : new String[] { javaIn, sqlIn };
   }

   public boolean hasText() {
      return (text != null);
   }

   public String getText(Constants.QueryMode modeIn) {
      return (text == null) ? null : text[modeIn.ordinal()];
   }

   public String getJava() {
      return (text == null) ? null : text[0];
   }

   public String getSql() {
      return (text == null) ? null : text[1];
   }
}
