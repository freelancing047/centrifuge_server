package csi.server.business.visualization.mapchart;

import java.util.HashMap;

public class MCMapUSA extends MCMapBase {

    public MCMapUSA() {
        super();

        // Callibration for mercator (lat/long) projection
        tl_lat = "71.455555";
        tl_long = "-178.244751";
        br_lat = "18.96519";
        br_long = "-66.936676";
        zoom = "214%";
        zoom_x = "-89.58%";
        zoom_y = "-107.59%";

        mapFile = "usa_mercator.swf";
        mapAreas = new HashMap<String, String>();

        mapAreas.put("AL", "Alabama");
        mapAreas.put("AK", "Alaska");
        mapAreas.put("AZ", "Arizona");
        mapAreas.put("AR", "Arkansas");
        mapAreas.put("CA", "California");
        mapAreas.put("CO", "Colorado");
        mapAreas.put("CT", "Connecticut");
        mapAreas.put("DE", "Delaware");
        mapAreas.put("DC", "District of Columbia");
        mapAreas.put("FL", "Florida");
        mapAreas.put("GA", "Georgia");
        mapAreas.put("HI", "Hawaii");
        mapAreas.put("ID", "Idaho");
        mapAreas.put("IL", "Illinois");
        mapAreas.put("IN", "Indiana");
        mapAreas.put("IA", "Iowa");
        mapAreas.put("KS", "Kansas");
        mapAreas.put("KY", "Kentucky");
        mapAreas.put("LA", "Louisiana");
        mapAreas.put("ME", "Maine");
        mapAreas.put("MD", "Maryland");
        mapAreas.put("MA", "Massachusetts");
        mapAreas.put("MI", "Michigan");
        mapAreas.put("MN", "Minnesota");
        mapAreas.put("MS", "Mississippi");
        mapAreas.put("MO", "Missouri");
        mapAreas.put("MT", "Montana");
        mapAreas.put("NE", "Nebraska");
        mapAreas.put("NV", "Nevada");
        mapAreas.put("NH", "New Hampshire");
        mapAreas.put("NJ", "New Jersey");
        mapAreas.put("NM", "New Mexico");
        mapAreas.put("NY", "New York");
        mapAreas.put("NC", "North Carolina");
        mapAreas.put("ND", "North Dakota");
        mapAreas.put("OH", "Ohio");
        mapAreas.put("OK", "Oklahoma");
        mapAreas.put("OR", "Oregon");
        mapAreas.put("PA", "Pennsylvania");
        mapAreas.put("RI", "Rhode Island");
        mapAreas.put("SC", "South Carolina");
        mapAreas.put("SD", "South Dakota");
        mapAreas.put("TN", "Tennessee");
        mapAreas.put("TX", "Texas");
        mapAreas.put("UT", "Utah");
        mapAreas.put("VT", "Vermont");
        mapAreas.put("VA", "Virginia");
        mapAreas.put("WA", "Washington");
        mapAreas.put("WV", "West Virginia");
        mapAreas.put("WI", "Wisconsin");
        mapAreas.put("WY", "Wyoming");

        areaCenters.put("AL", new LatLong(32.840570, -86.631863));
        areaCenters.put("AK", new LatLong(63.833332, -152.000000));
        areaCenters.put("AZ", new LatLong(34.539005, -112.470199));
        areaCenters.put("AR", new LatLong(34.748655, -92.274494));
        areaCenters.put("CA", new LatLong(36.962055, -120.063994));
        areaCenters.put("CO", new LatLong(38.840000, -105.040001));
        areaCenters.put("CT", new LatLong(41.618790, -72.713509));
        areaCenters.put("DE", new LatLong(39.158035, -75.524734));
        areaCenters.put("DC", new LatLong(38.890369, -77.031990));
        areaCenters.put("FL", new LatLong(28.554515, -82.387824));
        areaCenters.put("GA", new LatLong(32.839685, -83.627579));
        areaCenters.put("HI", new LatLong(21.135160, -157.010330));
        areaCenters.put("ID", new LatLong(44.504845, -114.236819));
        areaCenters.put("IL", new LatLong(39.801055, -89.643604));
        areaCenters.put("IN", new LatLong(39.766910, -86.149964));
        areaCenters.put("IA", new LatLong(42.025345, -93.620199));
        areaCenters.put("KS", new LatLong(38.366015, -98.765148));
        areaCenters.put("KY", new LatLong(37.568789, -85.255134));
        areaCenters.put("LA", new LatLong(31.124930, -92.062434));
        areaCenters.put("ME", new LatLong(43.923279, -69.644913));
        areaCenters.put("MD", new LatLong(38.924976, -76.628841));
        areaCenters.put("MA", new LatLong(42.262990, -71.802354));
        areaCenters.put("MI", new LatLong(44.250409, -85.402265));
        areaCenters.put("MN", new LatLong(46.353165, -94.200689));
        areaCenters.put("MS", new LatLong(32.745510, -89.538019));
        areaCenters.put("MO", new LatLong(38.577515, -92.177839));
        areaCenters.put("MT", new LatLong(47.066535, -109.424419));
        areaCenters.put("NE", new LatLong(41.401770, -99.639974));
        areaCenters.put("NV", new LatLong(39.493242, -117.071848));
        areaCenters.put("NH", new LatLong(43.693366, -71.629290));
        areaCenters.put("NJ", new LatLong(40.217875, -74.759404));
        areaCenters.put("NM", new LatLong(34.594965, -106.029649));
        areaCenters.put("NY", new LatLong(43.098745, -75.654929));
        areaCenters.put("NC", new LatLong(35.466257, -79.159328));
        areaCenters.put("ND", new LatLong(47.485445, -100.438404));
        areaCenters.put("OH", new LatLong(39.962000, -83.003014));
        areaCenters.put("OK", new LatLong(35.472005, -97.520334));
        areaCenters.put("OR", new LatLong(44.302920, -120.844044));
        areaCenters.put("PA", new LatLong(40.911660, -77.777709));
        areaCenters.put("RI", new LatLong(41.684681, -71.515091));
        areaCenters.put("SC", new LatLong(33.998550, -81.045249));
        areaCenters.put("SD", new LatLong(44.372, -100.322000));
        areaCenters.put("TN", new LatLong(35.844400, -86.394339));
        areaCenters.put("TX", new LatLong(31.134805, -99.336184));
        areaCenters.put("UT", new LatLong(39.264700, -111.637019));
        areaCenters.put("VT", new LatLong(44.093145, -72.733224));
        areaCenters.put("VA", new LatLong(37.552246, -78.551898));
        areaCenters.put("WA", new LatLong(47.414187, -120.304898));
        areaCenters.put("WV", new LatLong(38.665505, -80.712649));
        areaCenters.put("WI", new LatLong(44.666681, -90.173835));
        areaCenters.put("WY", new LatLong(42.832915, -108.730179));

    }
}
