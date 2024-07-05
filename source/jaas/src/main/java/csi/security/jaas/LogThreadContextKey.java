package csi.security.jaas;

/**
 * Keep some ThreadContext keys to be used when reading/writing to ThreadContext
 */
public class LogThreadContextKey {
    /** Ip address of the server */
    public static final String SERVER_IP = "server_ip_address";

    /** User provided string */
    public static final String APPLICATION_ID = "application_id";

    /** Current HTTP session */
    public static final String SESSION_ID = "session_id";

    /** Current HTTP session */
    public static final String METHOD_NAME = "method_name";

    /** Current, authenticated user of the request */
    public static final String REMOTE_USER = "remote_user";

    /** Current, authenticated user of the request */
    public static final String USER = "user_name";

    /** Current, DN for authenticated user of the request */
    public static final String DN = "distinguished_name";

    /** Relative path of the operation executed */
    public static final String ACTION_URI = "action_uri";

    /** IP address of client */
    public static final String REMOTE_ADDR = "client_ip_address";
}
