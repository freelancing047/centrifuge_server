package csi.security.queries;

/**
 * Created by centrifuge on 3/29/2018.
 */
public enum AclCondition {
   NONE(null, null, 0),
   NAME_MATCH("(d.name = :parm1)", "(d.name = :parm1)", 1),
   UUID_MATCH("(d.uuid.uuid = :parm1)", "(d.uuid = :parm1)", 1),
   ACL_MATCH("(a.uuid = :parm1)", "(a.uuid = :parm1)", 1),
   TYPE_MATCH("(d.uploadType.name = :parm1)", "(d.uploadtype = :parm1)", 1),
   TOP_MID_MATCH("(d.topLevel = :parm1) AND (d.midLevel = :parm2)", "(d.toplevel = :parm1) AND (d.midlevel = :parm2)", 2),
   ACL_LIST("(a.uuid IN (:parm1))", "(a.uuid IN (:parm1))", 1);

   private String[] text;
   private int parameterCount;

   private AclCondition(String javaIn, String sqlIn, int parameterCountIn) {
      text = ((javaIn == null) || (sqlIn == null)) ? null : new String[] { javaIn, sqlIn };
      parameterCount = parameterCountIn;
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

   public int getParameterCount() {
      return parameterCount;
   }
}
