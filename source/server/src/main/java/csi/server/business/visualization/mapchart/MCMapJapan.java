package csi.server.business.visualization.mapchart;

import java.util.HashMap;

public class MCMapJapan extends MCMapBase {

    public MCMapJapan() {
        super();
        // Callibration for mercator (lat/long) projection
        tl_lat = "";
        tl_long = "";
        br_lat = "";
        br_long = "";
        zoom = "95%";
        zoom_x = "42.36%";
        zoom_y = "1.8%";

        mapFile = "japan.swf";
        mapAreas = new HashMap<String, String>();

        mapAreas.put("aichi", "Aichi");
        mapAreas.put("akita", "Akita");
        mapAreas.put("aomori", "Aomori");
        mapAreas.put("chiba", "Chiba");
        mapAreas.put("ehime", "Ehime");
        mapAreas.put("fukui", "Fukui");
        mapAreas.put("fukuoka", "Fukuoka");
        mapAreas.put("fukushima", "Fukushima");
        mapAreas.put("gifu", "Gifu");
        mapAreas.put("gunma", "Gunma");
        mapAreas.put("hiroshima", "Hiroshima");
        mapAreas.put("hokkaido", "Hokkaido");
        mapAreas.put("hyogo", "Hyogo");
        mapAreas.put("ibaraki", "Ibaraki");
        mapAreas.put("ishikawa", "Ishikawa");
        mapAreas.put("iwate", "Iwate");
        mapAreas.put("kagawa", "Kagawa");
        mapAreas.put("kagoshima", "Kagoshima");
        mapAreas.put("kanagawa", "Kanagawa");
        mapAreas.put("kochi", "Kochi");
        mapAreas.put("kumamoto", "Kumamoto");
        mapAreas.put("kyoto", "Kyoto");
        mapAreas.put("mie", "Mie");
        mapAreas.put("miyagi", "Miyagi");
        mapAreas.put("miyazaki", "Miyazaki");
        mapAreas.put("nagano", "Nagano");
        mapAreas.put("nagasaki", "Nagasaki");
        mapAreas.put("nara", "Nara");
        mapAreas.put("niigata", "Niigata");
        mapAreas.put("oita", "Oita");
        mapAreas.put("okayama", "Okayama");
        mapAreas.put("okinawa", "Okinawa");
        mapAreas.put("osaka", "Osaka");
        mapAreas.put("saga", "Saga");
        mapAreas.put("saitama", "Saitama");
        mapAreas.put("shiga", "Shiga");
        mapAreas.put("shimane", "Shimane");
        mapAreas.put("shizuoka", "Shizuoka");
        mapAreas.put("tochigi", "Tochigi");
        mapAreas.put("tokushima", "Tokushima");
        mapAreas.put("tokyo", "Tokyo");
        mapAreas.put("tottori", "Tottori");
        mapAreas.put("toyama", "Toyama");
        mapAreas.put("wakayama", "Wakayama");
        mapAreas.put("yamagata", "Yamagata");
        mapAreas.put("yamaguchi", "Yamaguchi");
        mapAreas.put("yamanashi", "Yamanashi");

        areaCenters.put("aichi", new LatLong(35.180188, 136.906565));
        areaCenters.put("akita", new LatLong(39.720001, 140.102559));
        areaCenters.put("aomori", new LatLong(40.822072, 140.747365));
        areaCenters.put("chiba", new LatLong(35.607268, 140.106291));
        areaCenters.put("ehime", new LatLong(33.841624, 132.765681));
        areaCenters.put("fukui", new LatLong(36.064067, 136.219493));
        areaCenters.put("fukuoka", new LatLong(33.590355, 130.401716));
        areaCenters.put("fukushima", new LatLong(37.760722, 140.473355));
        areaCenters.put("gifu", new LatLong(35.423298, 136.760654));
        areaCenters.put("gunma", new LatLong(36.390663, 139.060394));
        areaCenters.put("hiroshima", new LatLong(34.385205, 132.455301));
        areaCenters.put("hokkaido", new LatLong(43.064293, 141.346454));
        areaCenters.put("hyogo", new LatLong(34.691277, 135.183077));
        areaCenters.put("ibaraki", new LatLong(36.340599, 140.442883));
        areaCenters.put("ishikawa", new LatLong(36.594675, 136.625683));
        areaCenters.put("iwate", new LatLong(39.703443, 141.152422));
        areaCenters.put("kagawa", new LatLong(34.340178, 134.043481));
        areaCenters.put("kagoshima", new LatLong(31.596573, 130.55714));
        areaCenters.put("kanagawa", new LatLong(35.447506, 139.642342));
        areaCenters.put("kochi", new LatLong(33.558803, 133.531167));
        areaCenters.put("kumamoto", new LatLong(32.8031, 130.707891));
        areaCenters.put("kyoto", new LatLong(35.011603, 135.767952));
        areaCenters.put("mie", new LatLong(34.730283, 136.508588));
        areaCenters.put("miyagi", new LatLong(38.268698, 140.871924));
        areaCenters.put("miyazaki", new LatLong(31.907695, 131.420271));
        areaCenters.put("nagano", new LatLong(36.648456, 138.194105));
        areaCenters.put("nagasaki", new LatLong(32.7503, 129.877696));
        areaCenters.put("nara", new LatLong(34.685085, 135.804994));
        areaCenters.put("niigata", new LatLong(37.916081, 139.0363));
        areaCenters.put("oita", new LatLong(33.239567, 131.609285));
        areaCenters.put("okayama", new LatLong(34.655106, 133.919436));
        areaCenters.put("okinawa", new LatLong(26.212405, 127.680935));
        areaCenters.put("osaka", new LatLong(34.693737, 135.502164));
        areaCenters.put("saga", new LatLong(34.716035, 135.52368));
        areaCenters.put("saitama", new LatLong(35.861727, 139.645475));
        areaCenters.put("shiga", new LatLong(35.004713, 135.869387));
        areaCenters.put("shimane", new LatLong(35.472295, 133.0505));
        areaCenters.put("shizuoka", new LatLong(34.975579, 138.382575));
        areaCenters.put("tochigi", new LatLong(36.565717, 139.883557));
        areaCenters.put("tokushima", new LatLong(34.07027, 134.554845));
        areaCenters.put("tokyo", new LatLong(35.689484, 139.691695));
        areaCenters.put("tottori", new LatLong(35.501133, 134.235091));
        areaCenters.put("toyama", new LatLong(36.695951, 137.213675));
        areaCenters.put("wakayama", new LatLong(34.230512, 135.170809));
        areaCenters.put("yamagata", new LatLong(38.25222, 140.335602));
        areaCenters.put("yamaguchi", new LatLong(34.176689, 131.479797));
        areaCenters.put("yamanashi", new LatLong(35.664128, 138.568305));

    }
}
