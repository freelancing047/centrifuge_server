package csi.server.business.visualization.mapchart;

import java.util.HashMap;

public class MCMapNewZealand extends MCMapBase {

    public MCMapNewZealand() {
        super();
        // Callibration for mercator (lat/long) projection
        tl_lat = "-34.1394966";
        tl_long = "165.882843";
        br_lat = "-52.599593";
        br_long = "184.161346";
        zoom = "135%";
        zoom_x = "44.04%";
        zoom_y = "1.71%";

        mapFile = "new_zealand.swf";
        mapAreas = new HashMap<String, String>();

        mapAreas.put("NZ_NTL", "Northland");
        mapAreas.put("NZ_AUK", "Auckland");
        mapAreas.put("NZ_WKO", "Waikato");
        mapAreas.put("NZ_BOP", "Bay of Plenty");
        mapAreas.put("NZ_GIS", "East Cape");
        mapAreas.put("NZ_HKB", "Hawke's Bay");
        mapAreas.put("NZ_TKI", "Taranaki");
        mapAreas.put("NZ_MWT", "Manawatu-Wanganui");
        mapAreas.put("NZ_WGN", "Wellington");
        mapAreas.put("NZ_TAS", "Tasman");
        mapAreas.put("NZ_MBH", "Marlborough");
        mapAreas.put("NZ_NSN", "Nelson");
        mapAreas.put("NZ_WTC", "West Coast");
        mapAreas.put("NZ_CAN", "Canterbury");
        mapAreas.put("NZ_OTA", "Otago");
        mapAreas.put("NZ_STL", "Southland");
        mapAreas.put("NZ_CI", "Chatham Islands");

        areaCenters.put("NZ_NTL", new LatLong(-35.3951, 173.630005));
        areaCenters.put("NZ_AUK", new LatLong(-36.827385, 174.765735));
        areaCenters.put("NZ_WKO", new LatLong(-37.621377, 175.058619));
        areaCenters.put("NZ_BOP", new LatLong(-37.923277, 176.557619));
        areaCenters.put("NZ_GIS", new LatLong(-37.983333, 177.85));
        areaCenters.put("NZ_HKB", new LatLong(-39.108987, 176.541637));
        areaCenters.put("NZ_TKI", new LatLong(-39.300201, 174.371994));
        areaCenters.put("NZ_MWT", new LatLong(-39.504299, 175.645004));
        areaCenters.put("NZ_WGN", new LatLong(-41.05648, 175.276217));
        areaCenters.put("NZ_TAS", new LatLong(-41.601538, 172.478275));
        areaCenters.put("NZ_NSN", new LatLong(-41.060786, 173.184));
        areaCenters.put("NZ_MBH", new LatLong(-41.518201, 173.432133));
        areaCenters.put("NZ_WTC", new LatLong(-41.892186, 171.741971));
        areaCenters.put("NZ_CAN", new LatLong(-43.486035, 171.516641));
        areaCenters.put("NZ_OTA", new LatLong(-45.27844, 169.419628));
        areaCenters.put("NZ_STL", new LatLong(-45.537433, 168.015204));
        areaCenters.put("NZ_CI", new LatLong(-43.715752, 183.340067));

    }

}
