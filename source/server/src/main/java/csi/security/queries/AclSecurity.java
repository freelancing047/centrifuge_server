package csi.security.queries;

/**
 * Created by centrifuge on 3/29/2018.
 */
public enum AclSecurity {
   CAPCO_MODIFIER_PASS_DEFAULT(
         "((a.locked = false) AND ((a.useCapcoDefault = true) OR (NOT EXISTS (SELECT e FROM a.capcoTags e"
               + " WHERE (e.roleName IS NOT NULL) AND (e.roleName NOT IN (:roles)))))) AND ",
         "((a.locked = false) AND ((a.usecapcodefault = true) OR (NOT EXISTS (SELECT e FROM a.capcotags e"
               + " WHERE (e.rolename IS NOT NULL) AND (e.rolename NOT IN (:roles)))))) AND "),
   CAPCO_MODIFIER_FAIL_DEFAULT(
         "((a.locked = false) AND (a.useCapcoDefault = false) AND (NOT EXISTS (SELECT e FROM a.capcoTags e"
               + " WHERE (e.roleName IS NOT NULL) AND (e.roleName NOT IN (:roles))))) AND ",
         "((a.locked = false) AND (a.usecapcodefault = false) AND (NOT EXISTS (SELECT e FROM a.capcotags e"
               + " WHERE (e.rolename IS NOT NULL) AND (e.rolename NOT IN (:roles))))) AND "),
   GENERIC_MODIFIER(
         "((a.locked = false) AND (NOT EXISTS (SELECT gt FROM a.genericTags gt"
               + " WHERE (gt.enforce = true) AND (gt.roleName IS NOT NULL)"
               + " AND (gt.roleName NOT IN (:roles))))) AND ",
         "((a.locked = false) AND (NOT EXISTS (SELECT gt FROM a.generictags gt"
               + " WHERE (gt.enforce = true) AND (gt.rolename IS NOT NULL)"
               + " AND (gt.rolename NOT IN (:roles))))) AND "),
   DISTRIBUTION_MODIFIER(
         "((a.locked = false) AND (NOT EXISTS (SELECT dt FROM a.distributionTags dt ) OR (NOT EXISTS"
               + " (SELECT dt FROM a.distributionTags dt WHERE (EXISTS(SELECT dr FROM dt.roleList dr ))"
               + " AND (NOT EXISTS (SELECT dr FROM dt.roleList dr WHERE (dr.roleName IN (:roles)))))))) AND ",
         "((a.locked = false) AND (NOT EXISTS (SELECT dt FROM a.distributiontags dt ) OR (NOT EXISTS"
               + " (SELECT dt FROM a.distributiontags dt WHERE (EXISTS(SELECT dr FROM dt.rolelist dr ))"
               + " AND (NOT EXISTS (SELECT dr FROM dt.rolelist dr WHERE (dr.rolename IN (:roles)))))))) AND "),
   CONFIGURED_MODIFIER(
         "((a.locked = false) AND (NOT EXISTS (SELECT s1 FROM a.sourceEntries s1"
               + " WHERE ((s1.driverAccessRole IS NOT NULL)" + " AND (s1.driverAccessRole NOT IN (:roles)))))"
               + " AND (NOT EXISTS (SELECT s4 FROM a.linkupEntries s4" + " WHERE ((s4.driverAccessRole IS NOT NULL)"
               + " AND (s4.driverAccessRole NOT IN (:roles)))))) AND ",
         "((a.locked = false) AND (NOT EXISTS (SELECT s1 FROM a.sourceentries s1"
               + " WHERE ((s1.driveraccessrole IS NOT NULL)" + " AND (s1.driveraccessrole NOT IN (:roles)))))"
               + " AND (NOT EXISTS (SELECT s4 FROM a.linkupentries s4" + " WHERE ((s4.driveraccessrole IS NOT NULL)"
               + " AND (s4.driveraccessrole NOT IN (:roles)))))) AND "),
   SOURCE_EDIT_MODIFIER(
         "((a.locked = false) AND (NOT EXISTS (SELECT s2 FROM a.sourceEntries s2"
               + " WHERE ((s2.driverAccessRole IS NOT NULL)" + " AND (s2.driverAccessRole NOT IN (:roles)))"
               + " OR ((s2.sourceEditRole IS NOT NULL)" + " AND (s2.sourceEditRole NOT IN (:roles)))))) AND ",
         "((a.locked = false) AND (NOT EXISTS (SELECT s2 FROM a.sourceentries s2"
               + " WHERE ((s2.driveraccessrole IS NOT NULL)" + " AND (s2.driveraccessrole NOT IN (:roles)))"
               + " OR ((s2.sourceeditrole IS NOT NULL)" + " AND (s2.sourceeditrole NOT IN (:roles)))))) AND "),
   EXPORT_MODIFIER(
         "((a.locked = false) AND (NOT EXISTS (SELECT s3 FROM a.sourceEntries s3"
               + " WHERE ((s3.driverAccessRole IS NOT NULL)" + " AND (s3.driverAccessRole NOT IN (:roles)))"
               + " OR ((s3.sourceEditRole IS NOT NULL)" + " AND (s3.sourceEditRole NOT IN (:roles)))"
               + " OR ((s3.connectionEditRole IS NOT NULL)" + " AND (s3.connectionEditRole NOT IN (:roles)))))) AND ",
         "((a.locked = false) AND (NOT EXISTS (SELECT s3 FROM a.sourceentries s3"
               + " WHERE ((s3.driveraccessrole IS NOT NULL)" + " AND (s3.driveraccessrole NOT IN (:roles)))"
               + " OR ((s3.sourceeditrole IS NOT NULL)" + " AND (s3.sourceeditrole NOT IN (:roles)))"
               + " OR ((s3.connectioneditrole IS NOT NULL)" + " AND (s3.connectioneditrole NOT IN (:roles)))))) AND ");

   private String[] text;

   private AclSecurity(String javaIn, String sqlIn) {
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
