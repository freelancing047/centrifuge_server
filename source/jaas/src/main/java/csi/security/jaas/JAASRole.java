package csi.security.jaas;

public class JAASRole extends JAASPrincipal {

    private static final long serialVersionUID = 1L;

    public static final String AUTHENTICATED_ROLE = "authenticated";
    public static final String ADMIN_ROLE_NAME = "administrators";
    public static final String ADMIN_GROUP_NAME = "Administrators";
    public static final String SECURITY_ROLE_NAME = "securityofficers";
    public static final String SECURITY_GROUP_NAME = "SecurityOfficers";
    public static final String EVERYONE_ROLE_NAME = "everyone";
    public static final String EVERYONE_GROUP_NAME = "Everyone";
    public static final String ADMIN_USER_NAME = "admin";
    public static final String SECURITY_USER_NAME = "cso";
    public static final String META_VIEWER_ROLE_NAME = "metaviewers";
    public static final String META_VIEWER_GROUP_NAME = "MetaViewers";
    public static final String ORIGINATOR_ROLE_NAME = "originators";
    public static final String ORIGINATOR_GROUP_NAME = "Originators";

    public JAASRole(String name) {
        super(name);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof JAASRole)) {
            return false;
        }

        JAASRole that = (JAASRole) o;

        return getName().equals(that.getName());
    }

}
