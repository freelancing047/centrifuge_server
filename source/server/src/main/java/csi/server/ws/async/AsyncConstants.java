package csi.server.ws.async;

/**
 * Holds all the <code>async</code> package useful constants.
 * 
 * @author <a href="mailto:iulian.boanca@lpro.leverpointinc.com">Iulian Boanca</a>
 * 
 */
public class AsyncConstants {

    public static final String KEY_USER = "user";
    public static final String KEY_AGENT = "agent";
    public static final String KEY_PASSWORD = "password";

    // request parameter specifying preferred message format (see CodecType).
    public static final String MESSAGE_FORMAT = "_f";
    /******************************************************************************/

    /* TaskContext keys */
    public static final String KEY_IS_MULTI_PART = "KeyIsMultiPart";

    /* MultiPartInfo keys */
    public static final String KEY_FILEITEMS = "KeyFileitems";

    /******************************************************************************/
    /* BusinessContext keys used for ServletConfig initialization configurations */
    public static final String KEY_INIT_CONFIG_AUTHORIZED_ROLES = "KeyInitConfigAuthorizedRoles";
    public static final String KEY_INIT_CONFIG_MAX_FILE_SIZE = "KeyInitConfigMaxFileSize";
    public static final String KEY_INIT_CONFIG_AUTO_REGISTER_UNKNOWN_USER = "KeyInitConfigAutoRegisterUnknownUser";
    public static final String KEY_INIT_CONFIG_TEMP_THRESHOLD = "KeyInitConfigTempThreshold";
    public static final String KEY_INIT_CONFIG_TEMP_DIRECTORY = "KeyInitConfigTempDirectory";
    public static final String HTTP_HEADER_CLIENT_ID = "X-Client-Id";
    public static final String HTTP_HEADER_TASK_ID = "X-Task-Id";
    public static final String HTTP_HEADER_TASK_STATUS = "X-Task-Status";
    public static final String HTTP_HEADER_CHARACTER_ENCODING_UTF8 = "UTF-8";
    public static final String HTTP_HEADER_PRAGMA = "Pragma";
    public static final String HTTP_HEADER_EXPIRES = "Expires";
    public static final String HTTP_HEADER_CACHE_CONTROL = "Cache-Control";
    public static final String XML_PROLOG = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
}
