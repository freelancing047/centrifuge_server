package csi.server.business.visualization.mapchart;

import java.util.HashMap;

public class MCMapIndia extends MCMapBase {

    public MCMapIndia() {
        super();
        // Callibration for mercator (lat/long) projection
        tl_lat = "36.930858";
        tl_long = "68.144974";
        br_lat = "6.743626";
        br_long = "97.384362";
        zoom = "90%";
        zoom_x = "15.02%";
        zoom_y = "4.5%";

        mapFile = "india.swf";
        mapAreas = new HashMap<String, String>();

        mapAreas.put("IN_AN", "Andaman and Nicobar Islands");
        mapAreas.put("IN_AP", "Andhra Pradesh");
        mapAreas.put("IN_AR", "Arunachal Pradesh");
        mapAreas.put("IN_AS", "Assam");
        mapAreas.put("IN_BR", "Bihar");
        mapAreas.put("IN_CH", "Chandigarh");
        mapAreas.put("IN_CT", "Chhattisgarh");
        mapAreas.put("IN_DN", "Dadra and Nagar Haveli");
        mapAreas.put("IN_DD", "Daman and Diu");
        mapAreas.put("IN_DL", "Delhi");
        mapAreas.put("IN_GA", "Goa");
        mapAreas.put("IN_GJ", "Gujarat");
        mapAreas.put("IN_HR", "Haryana");
        mapAreas.put("IN_HP", "Himachal Pradesh");
        mapAreas.put("IN_JK", "Jammu and Kashmir");
        mapAreas.put("IN_JH", "Jharkhand");
        mapAreas.put("IN_KA", "Karnataka");
        mapAreas.put("IN_KL", "Kerala");
        mapAreas.put("IN_LD", "Lakshadweep");
        mapAreas.put("IN_MP", "Madhya Pradesh");
        mapAreas.put("IN_MH", "Maharashtr");
        mapAreas.put("IN_MN", "Manipur");
        mapAreas.put("IN_ML", "Meghalaya");
        mapAreas.put("IN_MZ", "Mizoram");
        mapAreas.put("IN_NL", "Nagaland");
        mapAreas.put("IN_OR", "Orissa");
        mapAreas.put("IN_PY", "Puducherry");
        mapAreas.put("IN_PB", "Punjab");
        mapAreas.put("IN_RJ", "Rajasthan");
        mapAreas.put("IN_SK", "Sikkim");
        mapAreas.put("IN_TN", "Tamil Nadu");
        mapAreas.put("IN_TR", "Tripura");
        mapAreas.put("IN_UT", "Uttarakhand");
        mapAreas.put("IN_UP", "Uttar Pradesh");
        mapAreas.put("IN_WB", "West Bengal");

        areaCenters.put("IN_AN", new LatLong(11.96756, 92.698387));
        areaCenters.put("IN_AP", new LatLong(17.047752, 80.098212));
        areaCenters.put("IN_AR", new LatLong(28.239099, 94.069545));
        areaCenters.put("IN_AS", new LatLong(26.20088, 92.937726));
        areaCenters.put("IN_BR", new LatLong(25.198009, 85.521896));
        areaCenters.put("IN_CH", new LatLong(30.73778, 76.784439));
        areaCenters.put("IN_CT", new LatLong(21.278657, 81.866144));
        areaCenters.put("IN_DN", new LatLong(20.273106, 73.016914));
        areaCenters.put("IN_DD", new LatLong(20.348472, 72.92613));
        areaCenters.put("IN_DL", new LatLong(28.635308, 77.22496));
        areaCenters.put("IN_GA", new LatLong(15.425379, 73.983003));
        areaCenters.put("IN_GJ", new LatLong(22.258652, 71.19238));
        areaCenters.put("IN_HR", new LatLong(29.058776, 76.085601));
        areaCenters.put("IN_HP", new LatLong(32.102408, 77.561942));
        areaCenters.put("IN_JK", new LatLong(34.149088, 76.825965));
        areaCenters.put("IN_JH", new LatLong(23.691349, 85.272247));
        areaCenters.put("IN_KA", new LatLong(15.317278, 75.713888));
        areaCenters.put("IN_KL", new LatLong(10.514388, 76.641271));
        areaCenters.put("IN_LD", new LatLong(10.332618, 72.74059));
        areaCenters.put("IN_MP", new LatLong(22.973423, 78.656894));
        areaCenters.put("IN_MH", new LatLong(19.75148, 75.713888));
        areaCenters.put("IN_MN", new LatLong(24.663717, 93.906269));
        areaCenters.put("IN_ML", new LatLong(25.467031, 91.366216));
        areaCenters.put("IN_MZ", new LatLong(23.164575, 92.937553));
        areaCenters.put("IN_NL", new LatLong(26.158435, 94.562443));
        areaCenters.put("IN_OR", new LatLong(20.237556, 84.270018));
        areaCenters.put("IN_PY", new LatLong(11.932888, 79.837071));
        areaCenters.put("IN_PB", new LatLong(31.14713, 75.341218));
        areaCenters.put("IN_RJ", new LatLong(27.023804, 74.217933));
        areaCenters.put("IN_SK", new LatLong(27.730627, 88.633784));
        areaCenters.put("IN_TN", new LatLong(11.127122, 78.656894));
        areaCenters.put("IN_TR", new LatLong(23.940848, 91.988153));
        areaCenters.put("IN_UT", new LatLong(30.066753, 79.0193));
        areaCenters.put("IN_UP", new LatLong(27.570589, 80.098187));
        areaCenters.put("IN_WB", new LatLong(22.986757, 87.854976));

    }
}
