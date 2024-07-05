package csi.server.business.visualization.mapchart;

import java.util.HashMap;

public class MCMapGermany extends MCMapBase {

    public MCMapGermany() {
        super();
        // Callibration for mercator (lat/long) projection
        tl_lat = "55.05723";
        tl_long = "5.8655";
        br_lat = "47.269633";
        br_long = "15.043089";
        zoom = "80%";
        zoom_x = "48.78%";
        zoom_y = "8.78%";

        mapFile = "germany.swf";
        mapAreas = new HashMap<String, String>();

        mapAreas.put("DE_BW", "Baden-Wurttemberg");
        mapAreas.put("DE_BY", "Bavaria");
        mapAreas.put("DE_BE", "Berlin");
        mapAreas.put("DE_BB", "Brandenburg");
        mapAreas.put("DE_HB", "Bremen");
        mapAreas.put("DE_HH", "Hamburg");
        mapAreas.put("DE_HE", "Hesse");
        mapAreas.put("DE_MV", "Mecklenburg-Vorpommern");
        mapAreas.put("DE_NI", "Lower Saxony");
        mapAreas.put("DE_NW", "North Rhine-Westphalia");
        mapAreas.put("DE_RP", "Rhineland-Palatinate");
        mapAreas.put("DE_SL", "Saarland");
        mapAreas.put("DE_SN", "Saxony");
        mapAreas.put("DE_ST", "Saxony-Anhalt");
        mapAreas.put("DE_SH", "Schleswig-Holstein");
        mapAreas.put("DE_TH", "Thuringia");

        areaCenters.put("DE_BW", new LatLong(48.661604, 9.350134));
        areaCenters.put("DE_BY", new LatLong(48.790447, 11.49789));
        areaCenters.put("DE_BE", new LatLong(52.523405, 13.4114));
        areaCenters.put("DE_BB", new LatLong(52.131392, 13.216249));
        areaCenters.put("DE_HB", new LatLong(53.074981, 8.807081));
        areaCenters.put("DE_HH", new LatLong(53.553407, 9.992196));
        areaCenters.put("DE_HE", new LatLong(50.652052, 9.162438));
        areaCenters.put("DE_MV", new LatLong(53.61265, 12.429595));
        areaCenters.put("DE_NI", new LatLong(52.636704, 9.845076));
        areaCenters.put("DE_NW", new LatLong(51.433237, 7.661594));
        areaCenters.put("DE_RP", new LatLong(50.118346, 7.308953));
        areaCenters.put("DE_SL", new LatLong(49.396423, 7.022961));
        areaCenters.put("DE_SN", new LatLong(51.104541, 13.201738));
        areaCenters.put("DE_ST", new LatLong(51.950265, 11.692273));
        areaCenters.put("DE_SH", new LatLong(54.219367, 9.696117));
        areaCenters.put("DE_TH", new LatLong(51.010989, 10.845346));

    }

}
