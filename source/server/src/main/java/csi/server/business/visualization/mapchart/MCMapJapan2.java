package csi.server.business.visualization.mapchart;

import java.util.HashMap;

public class MCMapJapan2 extends MCMapJapan {

    public MCMapJapan2() {
        super();
        // Callibration for mercator (lat/long) projection
        tl_lat = "45.521656";
        tl_long = "123.661020";
        br_lat = "24.219790";
        br_long = "145.818365";
        zoom = "95%";
        zoom_x = "42.36%";
        zoom_y = "1.8%";

        mapFile = "japan2.swf";
        mapAreas = new HashMap<String, String>();

        mapAreas.put("JP_AI", "Aichi");
        mapAreas.put("JP_AK", "Akita");
        mapAreas.put("JP_AO", "Aomori");
        mapAreas.put("JP_CH", "Chiba");
        mapAreas.put("JP_EH", "Ehime");
        mapAreas.put("JP_FI", "Fukui");
        mapAreas.put("JP_FO", "Fukuoka");
        mapAreas.put("JP_FS", "Fukushima");
        mapAreas.put("JP_GF", "Gifu");
        mapAreas.put("JP_GM", "Gunma");
        mapAreas.put("JP_HS", "Hiroshima");
        mapAreas.put("JP_HK", "Hokkaido");
        mapAreas.put("JP_HG", "Hyogo");
        mapAreas.put("JP_IB", "Ibaraki");
        mapAreas.put("JP_IS", "Ishikawa");
        mapAreas.put("JP_IW", "Iwate");
        mapAreas.put("JP_KG", "Kagawa");
        mapAreas.put("JP_KS", "Kagoshima");
        mapAreas.put("JP_KN", "Kanagawa");
        mapAreas.put("JP_KC", "Kochi");
        mapAreas.put("JP_KM", "Kumamoto");
        mapAreas.put("JP_KY", "Kyoto");
        mapAreas.put("JP_ME", "Mie");
        mapAreas.put("JP_MG", "Miyagi");
        mapAreas.put("JP_MZ", "Miyazaki");
        mapAreas.put("JP_NN", "Nagano");
        mapAreas.put("JP_NS", "Nagasaki");
        mapAreas.put("JP_NR", "Nara");
        mapAreas.put("JP_NI", "Niigata");
        mapAreas.put("JP_OT", "Oita");
        mapAreas.put("JP_OY", "Okayama");
        mapAreas.put("JP_ON", "Okinawa");
        mapAreas.put("JP_OS", "Osaka");
        mapAreas.put("JP_SG", "Saga");
        mapAreas.put("JP_ST", "Saitama");
        mapAreas.put("JP_SH", "Shiga");
        mapAreas.put("JP_SM", "Shimane");
        mapAreas.put("JP_SZ", "Shizuoka");
        mapAreas.put("JP_TC", "Tochigi");
        mapAreas.put("JP_TS", "Tokushima");
        mapAreas.put("JP_TK", "Tokyo");
        mapAreas.put("JP_TT", "Tottori");
        mapAreas.put("JP_TY", "Toyama");
        mapAreas.put("JP_WK", "Wakayama");
        mapAreas.put("JP_YT", "Yamagata");
        mapAreas.put("JP_YC", "Yamaguchi");
        mapAreas.put("JP_YN", "Yamanashi");

        areaCenters.put("JP_AI", new LatLong(35.180188, 136.906565));
        areaCenters.put("JP_AK", new LatLong(39.720001, 140.102559));
        areaCenters.put("JP_AO", new LatLong(40.822072, 140.747365));
        areaCenters.put("JP_CH", new LatLong(35.607268, 140.106291));
        areaCenters.put("JP_EH", new LatLong(33.841624, 132.765681));
        areaCenters.put("JP_FI", new LatLong(36.064067, 136.219493));
        areaCenters.put("JP_FO", new LatLong(33.590355, 130.401716));
        areaCenters.put("JP_FS", new LatLong(37.760722, 140.473355));
        areaCenters.put("JP_GF", new LatLong(35.423298, 136.760654));
        areaCenters.put("JP_GM", new LatLong(36.390663, 139.060394));
        areaCenters.put("JP_HS", new LatLong(34.385205, 132.455301));
        areaCenters.put("JP_HK", new LatLong(43.064293, 141.346454));
        areaCenters.put("JP_HG", new LatLong(34.691277, 135.183077));
        areaCenters.put("JP_IB", new LatLong(36.340599, 140.442883));
        areaCenters.put("JP_IS", new LatLong(36.594675, 136.625683));
        areaCenters.put("JP_IW", new LatLong(39.703443, 141.152422));
        areaCenters.put("JP_KG", new LatLong(34.340178, 134.043481));
        areaCenters.put("JP_KS", new LatLong(31.596573, 130.55714));
        areaCenters.put("JP_KN", new LatLong(35.447506, 139.642342));
        areaCenters.put("JP_KC", new LatLong(33.558803, 133.531167));
        areaCenters.put("JP_KM", new LatLong(32.8031, 130.707891));
        areaCenters.put("JP_KY", new LatLong(35.011603, 135.767952));
        areaCenters.put("JP_ME", new LatLong(34.730283, 136.508588));
        areaCenters.put("JP_MG", new LatLong(38.268698, 140.871924));
        areaCenters.put("JP_MZ", new LatLong(31.907695, 131.420271));
        areaCenters.put("JP_NN", new LatLong(36.648456, 138.194105));
        areaCenters.put("JP_NS", new LatLong(32.7503, 129.877696));
        areaCenters.put("JP_NR", new LatLong(34.685085, 135.804994));
        areaCenters.put("JP_NI", new LatLong(37.916081, 139.0363));
        areaCenters.put("JP_OT", new LatLong(33.239567, 131.609285));
        areaCenters.put("JP_OY", new LatLong(34.655106, 133.919436));
        areaCenters.put("JP_ON", new LatLong(26.212405, 127.680935));
        areaCenters.put("JP_OS", new LatLong(34.693737, 135.502164));
        areaCenters.put("JP_SG", new LatLong(34.716035, 135.52368));
        areaCenters.put("JP_ST", new LatLong(35.861727, 139.645475));
        areaCenters.put("JP_SH", new LatLong(35.004713, 135.869387));
        areaCenters.put("JP_SM", new LatLong(35.472295, 133.0505));
        areaCenters.put("JP_SZ", new LatLong(34.975579, 138.382575));
        areaCenters.put("JP_TC", new LatLong(36.565717, 139.883557));
        areaCenters.put("JP_TS", new LatLong(34.07027, 134.554845));
        areaCenters.put("JP_TK", new LatLong(35.689484, 139.691695));
        areaCenters.put("JP_TT", new LatLong(35.501133, 134.235091));
        areaCenters.put("JP_TY", new LatLong(36.695951, 137.213675));
        areaCenters.put("JP_WK", new LatLong(34.230512, 135.170809));
        areaCenters.put("JP_YT", new LatLong(38.25222, 140.335602));
        areaCenters.put("JP_YC", new LatLong(34.176689, 131.479797));
        areaCenters.put("JP_YN", new LatLong(35.664128, 138.568305));

    }
}
