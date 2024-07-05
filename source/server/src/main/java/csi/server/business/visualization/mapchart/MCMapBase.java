package csi.server.business.visualization.mapchart;

import java.util.HashMap;
import java.util.Map;

/*
 * Base class for maps (ammaps) that ship with the product.  Since the server
 * generates the xml data files that will feed into the ammap flash control, the
 * server needs to know about the areas contained in each map file.  We accomplish
 * this with a server class for each map that contains a hashtable of all of the areas
 * and the default name that we will use for mouse-overs.  Each of the map classes
 * derive from this base class which also includes a hashtable of each of the maps
 * and the lookup string that the client is expected to provide.
 * 
 * TODO: Eventually, would probably be better if the hashmaps for each map were
 * stored in a database table and populated at runtime.
 *
 */
public class MCMapBase {

    public static final String MAP_BASE_DIRECTORY = "../resources/mapchart/";

    // we'll center the unknown location on the origin....
    public static final LatLong UNKNOWN_LOCATION = new LatLong(44.372, -100.322);

    protected String mapFile;

    /*
     * Use a hashmap to identify the areas of the map (ammap).  Eventually,
     * we could change this to a <String, Object> if we want to start 
     * tracking more attributes for each area.  
     * 
     * For now, AMMap has name and title attributes that we will store here.
     */
    protected HashMap<String, String> mapAreas;

    protected Map<String, LatLong> areaCenters;

    /*
     * For lat/long, AMMap uses mercator projections.  For the maps that support
     * this projection, they need to be callibrated.  This is done by feeding in 
     * lat/long for the top left (tl) and bottom right (br) corners of the map.  
     * Use strings rather than floats as these will be inserted as attributes on 
     * the element when we serialize the map.
     */
    protected String tl_long;
    protected String tl_lat;
    protected String br_long;
    protected String br_lat;
    protected String zoom;
    protected String zoom_x;
    protected String zoom_y;

    protected MCMapBase() {
        areaCenters = new HashMap<String, LatLong>();
    }

    public String getMapFile() {
        return mapFile;
    }

    public String getTl_long() {
        return tl_long;
    }

    public String getTl_lat() {
        return tl_lat;
    }

    public String getBr_long() {
        return br_long;
    }

    public String getBr_lat() {
        return br_lat;
    }

    public String getZoom() {
        return zoom;
    }

    public String getZoom_x() {
        return zoom_x;
    }

    public String getZoom_y() {
        return zoom_y;
    }

    public static final HashMap<String, Class> MAP_CLASSES = new HashMap<String, Class>() {

        {
            put("USA", MCMapUSA.class);
            put("World", MCMapWorld.class);
            put("India", MCMapIndia.class);
            put("Germany", MCMapGermany.class);
            put("Hungary", MCMapHungary.class);
            put("Turkey", MCMapTurkey.class);
            put("Japan", MCMapJapan2.class);
            put("China", MCMapChina.class);
            put("New Zealand", MCMapNewZealand.class);
        }
    };

    public static Class getMap(String name) {
        Class returnType = MAP_CLASSES.get(name);
        return returnType;
    }

    public String getAreaTitle(String name) {
        return mapAreas.get(name);
    }

    protected boolean isValidArea(String text) {
        // TODO: Check to make sure that the area is valid
        return true;
    }

    public LatLong getAreaCenter(String areaName) {
        LatLong latLong = areaCenters.get(areaName);

        if (null == latLong) {
            latLong = UNKNOWN_LOCATION;
        }

        return latLong;
    }
}
