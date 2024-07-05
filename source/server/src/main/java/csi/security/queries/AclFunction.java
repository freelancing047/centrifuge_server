package csi.security.queries;

/**
 * Created by centrifuge on 3/29/2018.
 */
public enum AclFunction {
   LIST_NAMES("SELECT d.name", "SELECT d.name", false),
   LIST_UUID("SELECT d.uuid.uuid", "SELECT d.uuid", true),
   LIST_ANY_UUID("SELECT d.uuid.uuid", "SELECT d.uuid", false),
   LIST_RESOURCE_ACL(
         "SELECT d.uuid.uuid, d.name, d.remarks, d.createDate, d.lastOpenDate, d.lastUpdateDate, d.size, d.useCount, d.flags, a",
         "SELECT d.uuid, d.name, d.remarks, d.createdate, d.lastopendate, d.lastupdatedate, d.size, d.usecount, d.flags, a",
         true),
   LIST_RESOURCE(
         "SELECT d.uuid.uuid, d.name, d.remarks, d.createDate, d.lastOpenDate, d.lastUpdateDate, d.size, d.useCount, d.flags, d.owner",
         "SELECT d.uuid, d.name, d.remarks, d.createdate, d.lastopendate, d.lastupdatedate, d.size, d.usecount, d.flags, d.owner",
         true),
   LIST_BASICS("SELECT d.uuid.uuid, d.name, d.remarks, d.lastOpenDate, a.owner, d.size",
         "SELECT d.uuid, d.name, d.remarks, d.lastopendate, a.owner, d.size", true),
   LIST_OBJECT("SELECT d", "SELECT d", false),
   COUNT("SELECT count(d)", "SELECT count(d)", false),
   COUNT_ACL("SELECT count(a)", "SELECT count(a)", false),
   DISTINCT_TABLE_OWNERS("SELECT DISTINCT d.topLevel", "SELECT DISTINCT d.toplevel", false),
   DISTINCT_TABLE_SCHEMAS("SELECT DISTINCT d.midLevel", "SELECT DISTINCT d.midlevel", false),
   DISTINCT_TABLE_TYPES("SELECT DISTINCT d.lowLevel", "SELECT DISTINCT d.lowlevel", false),
   LIST_INSTALLED_COLUMNS("SELECT d.columns", "SELECT d.columns", false),
   LIST_PRIOR_TYPE("SELECT d.priorType", "SELECT d.priortype", true),
   LIST_ACL("SELECT a", "SELECT a", false);

   private String[] text;
   private boolean requiresJoin;

   private AclFunction(String javaIn, String sqlIn, boolean requiresJoinIn) {
      text = ((javaIn == null) || (sqlIn == null)) ? null : new String[] { javaIn, sqlIn };
      requiresJoin = requiresJoinIn;
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

   public boolean requiresJoin() {
      return requiresJoin;
   }
}
