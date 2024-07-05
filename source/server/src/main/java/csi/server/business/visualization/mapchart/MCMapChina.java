package csi.server.business.visualization.mapchart;

import java.util.HashMap;

public class MCMapChina extends MCMapBase {

    public MCMapChina() {
        super();
        // Callibration for mercator (lat/long) projection
        tl_lat = "53.553745";
        tl_long = "73.620045";
        br_lat = "18.168882";
        br_long = "134.768463";
        zoom = "95%";
        zoom_x = "12.72%";
        zoom_y = "0.32%";

        mapFile = "china.swf";
        mapAreas = new HashMap<String, String>();

        mapAreas.put("CN_34", "Anhui");
        mapAreas.put("CN_11", "Beijing");
        mapAreas.put("CN_50", "Chongqing");
        mapAreas.put("CN_35", "Fujian");
        mapAreas.put("CN_62", "Gansu");
        mapAreas.put("CN_44", "Guangdong");
        mapAreas.put("CN_45", "Guangxi");
        mapAreas.put("CN_52", "Guizhou");
        mapAreas.put("CN_46", "Hainan");
        mapAreas.put("CN_13", "Hebei");
        mapAreas.put("CN_23", "Heilongjiang");
        mapAreas.put("CN_41", "Henan");
        mapAreas.put("CN_91", "Hong Kong");
        mapAreas.put("CN_42", "Hubei");
        mapAreas.put("CN_43", "Hunan");
        mapAreas.put("CN_15", "Inner Mongolia (Nei Mongolia)");
        mapAreas.put("CN_32", "Jiangsu");
        mapAreas.put("CN_36", "Jiangxi");
        mapAreas.put("CN_22", "Jilin");
        mapAreas.put("CN_21", "Liaoning");
        mapAreas.put("CN_92", "Macau (Macao)");
        mapAreas.put("CN_64", "Ningxia");
        mapAreas.put("CN_63", "Qinghai");
        mapAreas.put("CN_61", "Shaanxi");
        mapAreas.put("CN_37", "Shandong");
        mapAreas.put("CN_31", "Shanghai");
        mapAreas.put("CN_14", "Shanxi");
        mapAreas.put("CN_51", "Sichuan");
        mapAreas.put("CN_12", "Tianjin");
        mapAreas.put("CN_54", "Tibet (Xizang)");
        mapAreas.put("CN_65", "Xinjiang");
        mapAreas.put("CN_53", "Yunnan");
        mapAreas.put("CN_33", "Zhejiang");
        mapAreas.put("TW", "Taiwan");

        areaCenters.put("CN_34", new LatLong(31.860611, 117.2847));
        areaCenters.put("CN_11", new LatLong(40.278071, 116.067188));
        areaCenters.put("CN_50", new LatLong(29.962686, 107.351234));
        areaCenters.put("CN_35", new LatLong(26.294877, 118.003027));
        areaCenters.put("CN_62", new LatLong(36.059299, 103.826363));
        areaCenters.put("CN_44", new LatLong(23.884774, 113.24707));
        areaCenters.put("CN_45", new LatLong(23.845649, 108.764648));
        areaCenters.put("CN_52", new LatLong(26.898435, 106.706927));
        areaCenters.put("CN_46", new LatLong(19.529341, 109.556808));
        areaCenters.put("CN_13", new LatLong(38.537252, 114.869259));
        areaCenters.put("CN_23", new LatLong(47.100045, 127.836914));
        areaCenters.put("CN_41", new LatLong(34.262501, 113.353023));
        areaCenters.put("CN_91", new LatLong(22.396428, 114.109497));
        areaCenters.put("CN_42", new LatLong(31.090574, 111.928711));
        areaCenters.put("CN_43", new LatLong(28.052029, 111.676649));
        areaCenters.put("CN_15", new LatLong(43.357138, 114.213867));
        areaCenters.put("CN_32", new LatLong(33.137551, 119.814648));
        areaCenters.put("CN_36", new LatLong(28.274628, 115.60893));
        areaCenters.put("CN_22", new LatLong(43.896289, 125.325766));
        areaCenters.put("CN_21", new LatLong(41.836521, 123.237162));
        areaCenters.put("CN_92", new LatLong(22.198745, 113.543873));
        areaCenters.put("CN_64", new LatLong(37.295331, 105.852148));
        areaCenters.put("CN_63", new LatLong(35.660669, 95.756834));
        areaCenters.put("CN_61", new LatLong(34.365173, 108.654132));
        areaCenters.put("CN_37", new LatLong(36.669227, 117.519896));
        areaCenters.put("CN_31", new LatLong(31.330708, 121.072916));
        areaCenters.put("CN_14", new LatLong(37.873464, 112.262537));
        areaCenters.put("CN_51", new LatLong(30.483122, 102.632031));
        areaCenters.put("CN_12", new LatLong(39.383294, 117.17793));
        areaCenters.put("CN_54", new LatLong(31.503629, 88.417969));
        areaCenters.put("CN_65", new LatLong(41.442726, 85.869141));
        areaCenters.put("CN_53", new LatLong(24.44715, 101.293945));
        areaCenters.put("CN_33", new LatLong(29.366214, 120.15383));
        areaCenters.put("TW", new LatLong(23.85781, 120.660515));

    }
}
