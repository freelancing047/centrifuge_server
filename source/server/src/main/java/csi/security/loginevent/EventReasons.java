package csi.security.loginevent;

public enum EventReasons {
   LOGIN_SUCCESS,
   LOGIN_UNKNOWN_USER,
   LOGIN_BAD_PASSWORD,
   LOGIN_DISABLED,
   LOGIN_CONCURRENT_LIMIT,
   LOGOUT_SUCCESS,
   LOGOUT_SERVER_SHUTDOWN,
   LOGOUT_EVICTED,
   LOGOUT_INACTIVITY_TIMEOUT;
}
