package csi.security.queries;

import csi.server.common.enumerations.AclControlType;

/**
 * Created by centrifuge on 3/29/2018.
 */
public enum AclScope {
   ANY(null, null, false, false, false),
   OWNED("(a.owner IN (:roles)) AND ", "(a.owner IN (:roles)) AND ", false, true, false),
   OWNED_BY_OTHER("(a.owner IN (:other1)) AND ", "(a.owner IN (:other1)) AND ", false, false, true),
   AUTHORIZED_OWNED_BY_OTHER(
         "(a.owner IN (:other1)) AND (EXISTS (SELECT e FROM a.entries e"
               + " WHERE (e.accessType IN (:permissions)) AND (e.roleName IN (:roles)))) AND ",
         "(a.owner IN (:other1)) AND (EXISTS (SELECT e FROM a.entries e"
               + " WHERE (e.accesstype IN (:permissions)) AND (e.rolename IN (:roles)))) AND ",
         true, true, true),
   AUTHORIZED(
         "(EXISTS (SELECT e FROM a.entries e WHERE (e.accessType IN (:permissions)) AND"
               + " (e.roleName IN (:roles)))) AND ",
         "(EXISTS (SELECT e FROM a.entries e WHERE (e.accesstype IN (:permissions)) AND"
               + " (e.rolename IN (:roles)))) AND ",
         true, true, false),
   AUTHORIZED_TO_OTHER(
         "(EXISTS (SELECT e FROM a.entries e WHERE (e.accessType IN (:permissions)) AND"
               + " (e.roleName IN (:other1)))) AND ",
         "(EXISTS (SELECT e FROM a.entries e WHERE (e.accesstype IN (:permissions)) AND"
               + " (e.rolename IN (:other1)))) AND ",
         true, false, true),
   OWNED_AUTHORIZED_TO_OTHER(
         "(a.owner IN (:roles)) AND (EXISTS (SELECT e FROM a.entries e"
               + " WHERE (e.accessType IN (:permissions)) AND (e.roleName IN (:other1)))) AND ",
         "(a.owner IN (:roles)) AND (EXISTS (SELECT e FROM a.entries e"
               + " WHERE (e.accesstype IN (:permissions)) AND (e.rolename IN (:other1)))) AND ",
         true, true, true),
   OWNED_AUTHORIZED(
         "(a.owner IN (:roles)) AND (EXISTS (SELECT e FROM a.entries e WHERE"
               + " (e.accessType IN (:permissions)) AND (e.roleName IN (:roles)))) AND ",
         "(a.owner IN (:roles)) AND (EXISTS (SELECT e FROM a.entries e WHERE"
               + " (e.accesstype IN (:permissions)) AND (e.rolename IN (:roles)))) AND ",
         true, true, false),
   OWNED_UNAUTHORIZED(
         "(a.owner IN (:roles)) AND (NOT EXISTS (SELECT e FROM a.entries e"
               + " WHERE (e.accessType IN (:permissions)) AND (e.roleName IN (:roles)))) AND ",
         "(a.owner IN (:roles)) AND (NOT EXISTS (SELECT e FROM a.entries e"
               + " WHERE (e.accesstype IN (:permissions)) AND (e.rolename IN (:roles)))) AND ",
         true, true, false),
   NOT_DEMO_AUTHORIZED(
         "(a.owner != 'administrators') AND (EXISTS (SELECT e FROM a.entries e"
               + " WHERE (e.accessType IN (:permissions)) AND (e.roleName IN (:roles)))) AND ",
         "(a.owner != 'administrators') AND (EXISTS (SELECT e FROM a.entries e"
               + " WHERE (e.accesstype IN (:permissions)) AND (e.rolename IN (:roles)))) AND ",
         true, true, false),
   OWNED_BY_OTHER_AUTHORIZED_TO_OTHER(
         "(a.owner IN (:other1)) AND (EXISTS (SELECT e FROM a.entries e" + " WHERE (e.roleName IN (:other2)))) AND ",
         "(a.owner IN (:other1)) AND (EXISTS (SELECT e FROM a.entries e" + " WHERE (e.rolename IN (:other2)))) AND ",
         false, false, true),
   AUTHORIZED_OWNED_BY_OTHER_AUTHORIZED_TO_OTHER(
         "(a.owner IN (:other1)) AND (EXISTS (SELECT e FROM a.entries e" + " WHERE (e.accessType IN (:permissions)) AND"
               + " (e.roleName IN (:roles)))) AND (EXISTS (SELECT e FROM"
               + " a.entries e WHERE (e.roleName IN (:other2)))) AND ",
         "(a.owner IN (:other1)) AND (EXISTS (SELECT e FROM a.entries e" + " WHERE (e.accesstype IN (:permissions)) AND"
               + " (e.rolename IN (:roles)))) AND (EXISTS (SELECT e FROM"
               + " a.entries e WHERE (e.rolename IN (:other2)))) AND ",
         true, true, true),
   AUTHORIZED_AUTHORIZED_TO_OTHER(
         "(EXISTS (SELECT e FROM a.entries e WHERE (e.accessType IN (:permissions))"
               + " AND (e.roleName IN (:roles)))) AND (EXISTS (SELECT e FROM a.entries e"
               + " WHERE (e.roleName IN (:other1)))) AND ",
         "(EXISTS (SELECT e FROM a.entries e WHERE (e.accesstype IN (:permissions))"
               + " AND (e.rolename IN (:roles)))) AND (EXISTS (SELECT e FROM a.entries e"
               + " WHERE (e.rolename IN (:other1)))) AND ",
         true, true, true),
   OWNED_OR_AUTHORIZED_AUTHORIZED_TO_OTHER(
         "((a.owner IN (:roles)) OR (EXISTS (SELECT e FROM a.entries e"
               + " WHERE (e.accessType IN (:permissions)) AND (e.roleName IN"
               + " (:roles))))) AND (EXISTS (SELECT e FROM a.entries e" + " WHERE (e.roleName IN (:other1)))) AND ",
         "((a.owner IN (:roles)) OR (EXISTS (SELECT e FROM a.entries e"
               + " WHERE (e.accesstype IN (:permissions)) AND (e.rolename IN"
               + " (:roles))))) AND (EXISTS (SELECT e FROM a.entries e" + " WHERE (e.rolename IN (:other1)))) AND ",
         true, true, true),
   OWNED_OR_AUTHORIZED(
         "((a.owner IN (:roles)) OR (EXISTS (SELECT e FROM a.entries e WHERE (e.accessType IN"
               + " (:permissions)) AND (e.roleName IN (:roles))))) AND ",
         "((a.owner IN (:roles)) OR (EXISTS (SELECT e FROM a.entries e WHERE (e.accesstype IN"
               + " (:permissions)) AND (e.rolename IN (:roles))))) AND ",
         true, true, false),
   OWNER_FILTER("(a.owner IN (:owners)) AND ", "(a.owner IN (:owners)) AND ", false, false, false),
   NOT_OWNER_FILTER("(a.owner NOT IN (:notOwners)) AND ", "(a.owner NOT IN (:notOwners)) AND ", false, false, false),
   USER_FILTER("(EXISTS (SELECT e FROM a.entries e WHERE (e.roleName IN (:users)))) AND ",
         "(EXISTS (SELECT e FROM a.entries e WHERE (e.rolename IN (:users)))) AND ", false, false, false),
   NOT_USER_FILTER("(NOT EXISTS (SELECT e FROM a.entries e WHERE (e.roleName IN (:notUsers)))) AND ",
         "(NOT EXISTS (SELECT e FROM a.entries e WHERE (e.rolename IN (:notUsers)))) AND ", false, false, false),
   READER_FILTER(
         "(EXISTS (SELECT e FROM a.entries e WHERE" + " (e.roleName IN (:readers)) AND (e.accessType = 1))) AND ",
         "(EXISTS (SELECT e FROM a.entries e WHERE" + " (e.rolename IN (:readers)) AND (e.accesstype = 1))) AND ",
         false, false, false),
   NOT_READER_FILTER(
         "(NOT EXISTS (SELECT e FROM a.entries e WHERE"
               + " (e.roleName IN (:notReaders)) AND (e.accessType = 1))) AND ",
         "(NOT EXISTS (SELECT e FROM a.entries e WHERE"
               + " (e.rolename IN (:notReaders)) AND (e.accesstype = 1))) AND ",
         false, false, false),
   EDITER_FILTER(
         "(EXISTS (SELECT e FROM a.entries e WHERE" + " (e.roleName IN (:editors)) AND (e.accessType = 2))) AND ",
         "(EXISTS (SELECT e FROM a.entries e WHERE" + " (e.rolename IN (:editors)) AND (e.accesstype = 2))) AND ",
         false, false, false),
   NOT_EDITER_FILTER(
         "(NOT EXISTS (SELECT e FROM a.entries e WHERE"
               + " (e.roleName IN (:notEditors)) AND (e.accessType = 2))) AND ",
         "(NOT EXISTS (SELECT e FROM a.entries e WHERE"
               + " (e.rolename IN (:notEditors)) AND (e.accesstype = 2))) AND ",
         false, false, false),
   DELETER_FILTER(
         "(EXISTS (SELECT e FROM a.entries e WHERE" + " (e.roleName IN (:deleters)) AND (e.accessType = 3))) AND ",
         "(EXISTS (SELECT e FROM a.entries e WHERE" + " (e.rolename IN (:deleters)) AND (e.accesstype = 3))) AND ",
         false, false, false),
   NOT_DELETER_FILTER(
         "(NOT EXISTS (SELECT e FROM a.entries e WHERE"
               + " (e.roleName IN (:notDeleters)) AND (e.accessType = 3))) AND ",
         "(NOT EXISTS (SELECT e FROM a.entries e WHERE"
               + " (e.rolename IN (:notDeleters)) AND (e.accesstype = 3))) AND ",
         false, false, false),
   NO_ACL("d.uuid.uuid NOT IN (SELECT uuid FROM ACL)", "d.uuid NOT IN (SELECT uuid FROM acl)", false, false, false),
   RESOURCE_OWNER_FILTER("(d.owner IN (:owners))", "(d.owner IN (:owners))", false, false, false),
   RESOURCE_NOT_OWNER_FILTER("(d.owner NOT IN (:notOwners))", "(d.owner NOT IN (:notOwners))", false, false, false),
   RESOURCE_OWNED("(a.owner IN (:roles))", "(a.owner IN (:roles))", false, true, false),;

   private String[] text;
   private boolean requiresPermissions;
   private boolean requiresRoles;
   private boolean requiresOther;

   private AclScope(String javaIn, String sqlIn, boolean requiresPermissionsIn, boolean requiresRolesIn,
                    boolean requiresOtherIn) {
      text = ((javaIn == null) || (sqlIn == null)) ? null : new String[] { javaIn, sqlIn };
      requiresPermissions = requiresPermissionsIn;
      requiresRoles = requiresRolesIn;
      requiresOther = requiresOtherIn;
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

   public boolean requiresPermissions() {
      return requiresPermissions;
   }

   public boolean requiresRoles() {
      return requiresRoles;
   }

   public boolean requiresOther() {
      return requiresOther;
   }

   public static AclScope checkRequirement(AclControlType permissionIn) {
      return (AclControlType.EMBEDDED == permissionIn) ? AclScope.ANY : AclScope.AUTHORIZED;
   }

   public static AclScope checkOwnerRequirement(AclControlType permissionIn) {
      return (AclControlType.EMBEDDED == permissionIn)
                ? AclScope.OWNED_BY_OTHER
                : AclScope.AUTHORIZED_OWNED_BY_OTHER;
   }
}
