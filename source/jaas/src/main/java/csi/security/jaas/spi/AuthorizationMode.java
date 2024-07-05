package csi.security.jaas.spi;

/**
 * Created by centrifuge on 4/27/2016.
 */
public enum AuthorizationMode
{
    JDBC, LDAP, CERT, NONE, KERBEROS, SAML, FORM, SERVICE, DEFAULT, INFO;

    public boolean isSet(int valueIn) {

        return (0 != ((1 << ordinal()) & valueIn));
    }
}
