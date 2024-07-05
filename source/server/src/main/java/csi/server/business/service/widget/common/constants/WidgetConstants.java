package csi.server.business.service.widget.common.constants;

/**
 * <p>Constants interface.</p>
 * <p>Contains all constants needed by the Centrifuge widgets application </p>
 * <p/>
 * <ul>
 * <li>Servlet constants - request parameters</li>
 * <li>Actions to be processed - known actions by the server</li>
 * <li>Javascript method names - Javascript method to be executed on the client</li>
 * <li>Action arguments - Arguments usually found in the key-value pairs at JSON message unmarshalling</li>
 * <li>Static data related constants - Constants used to define path to documents and document extensions</li>
 * </ul>
 */
public class WidgetConstants {

    /**
     * Servlet constants
     */
    public static final String ACTION = "action";
    public static final String JSON = "json";
    public static final String OBJECT_TYPE = "objectType";
    public static final String REQUEST = "request";

    public static final String LOAD_ACTION = "load";

    /**
     * Javascript method names
     */
    public static final String LOAD_WIDGET_JAVASCRIPT_METHOD = "loadWidget";

    /**
     * Action arguments
     */
    public static final String WIDGET = "widget";
    public static final String POSITION = "position";

    /**
     * Static data related constants
     */
    public static final String CENTRIFUGE_WIDGETS_PAGES = "/Centrifuge/pages/";
    public static final String JSP_EXTENSION = ".jsp";

    /**
     * Known implementation for JSON marshalling/unmarshalling procedure
     */
    public static final String JACKSON_IMPLEMENTATION = "jackson";

    public static final String GOOGLE_MAPS_API_KEY = "GoogleMapsAPIKey";

    public static final String GOOGLE_DEFAULT_KEY = "ABQIAAAAytAfaHcq0lzeqT9s-qNzkBQ96jwkjCOTv4qF2ISfZYJDlbyUFhQOnyAiFwsEi98JWdiVUrMcR7kRYA";
}
