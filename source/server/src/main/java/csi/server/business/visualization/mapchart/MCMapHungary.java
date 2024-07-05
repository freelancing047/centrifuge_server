package csi.server.business.visualization.mapchart;

import java.util.HashMap;

public class MCMapHungary extends MCMapBase {

    public MCMapHungary() {
        super();
        // Callibration for mercator (lat/long) projection
        tl_lat = "48.58597";
        tl_long = "16.114278";
        br_lat = "45.737024";
        br_long = "22.897";
        zoom = "70%";
        zoom_x = "6.72%";
        zoom_y = "3.95%";

        mapFile = "hungary.swf";
        mapAreas = new HashMap<String, String>();

        mapAreas.put("HU_BK", "Bács-Kiskun");
        mapAreas.put("HU_BA", "Baranya");
        mapAreas.put("HU_BE", "Békés");
        mapAreas.put("HU_BC", "Békéscsaba");
        mapAreas.put("HU_BZ", "Borsod-Abaúj-Zemplén");
        mapAreas.put("HU_BU", "Budapest");
        mapAreas.put("HU_CS", "Csongrád");
        mapAreas.put("HU_DE", "Debrecen");
        mapAreas.put("HU_DU", "Dunaújváros");
        mapAreas.put("HU_EG", "Eger");
        mapAreas.put("HU_FE", "Fejér");
        mapAreas.put("HU_GY", "Gyor");
        mapAreas.put("HU_GS", "Gyor-Moson-Sopron");
        mapAreas.put("HU_HB", "Hajdú-Bihar");
        mapAreas.put("HU_HE", "Heves");
        mapAreas.put("HU_HV", "Hódmezovásárhely");
        mapAreas.put("HU_JN", "Jász-Nagykun-Szolnok");
        mapAreas.put("HU_KV", "Kaposvár");
        mapAreas.put("HU_KM", "Kecskemét");
        mapAreas.put("HU_KE", "Komárom-Esztergom");
        mapAreas.put("HU_MI", "Miskolc");
        mapAreas.put("HU_NK", "Nagykanizsa");
        mapAreas.put("HU_NO", "Nógrád");
        mapAreas.put("HU_NY", "Nyíregyháza");
        mapAreas.put("HU_PS", "Pécs");
        mapAreas.put("HU_PE", "Pest");
        mapAreas.put("HU_SO", "Somogy");
        mapAreas.put("HU_SN", "Sopron");
        mapAreas.put("HU_SZ", "Szabolcs-Szatmár-Bereg");
        mapAreas.put("HU_SD", "Szeged");
        mapAreas.put("HU_SF", "Székesfehérvár");
        mapAreas.put("HU_SK", "Szolnok");
        mapAreas.put("HU_SH", "Szombathely");
        mapAreas.put("HU_TB", "Tatabánya");
        mapAreas.put("HU_TO", "Tolna");
        mapAreas.put("HU_VA", "Vas");
        mapAreas.put("HU_VE", "Veszprém");
        mapAreas.put("HU_VM", "Veszprém");
        mapAreas.put("HU_ZA", "Zala");
        mapAreas.put("HU_ZE", "Zalaegerszeg");

        areaCenters.put("HU_BK", new LatLong(46.566144, 19.427246));
        areaCenters.put("HU_BA", new LatLong(45.948458, 18.271917));
        areaCenters.put("HU_BE", new LatLong(46.871739, 21.129379));
        areaCenters.put("HU_BC", new LatLong(46.684216, 21.086754));
        areaCenters.put("HU_BZ", new LatLong(48.293938, 20.993411));
        areaCenters.put("HU_BU", new LatLong(47.498406, 19.040758));
        areaCenters.put("HU_CS", new LatLong(46.710833, 20.144575));
        areaCenters.put("HU_DE", new LatLong(47.529974, 21.639357));
        areaCenters.put("HU_DU", new LatLong(46.980668, 18.912708));
        areaCenters.put("HU_EG", new LatLong(47.903199, 20.373108));
        areaCenters.put("HU_FE", new LatLong(47.121793, 18.529482));
        areaCenters.put("HU_GY", new LatLong(47.68405, 17.635103));
        areaCenters.put("HU_GS", new LatLong(47.650932, 17.250634));
        areaCenters.put("HU_HB", new LatLong(47.468836, 21.245322));
        areaCenters.put("HU_HE", new LatLong(47.805762, 20.203856));
        areaCenters.put("HU_HV", new LatLong(46.430411, 20.31883));
        areaCenters.put("HU_JN", new LatLong(47.255497, 20.523231));
        areaCenters.put("HU_KV", new LatLong(46.366531, 17.782486));
        areaCenters.put("HU_KM", new LatLong(46.906209, 19.689977));
        areaCenters.put("HU_KE", new LatLong(47.64331, 18.119096));
        areaCenters.put("HU_MI", new LatLong(48.104322, 20.691375));
        areaCenters.put("HU_NK", new LatLong(46.455237, 16.995343));
        areaCenters.put("HU_NO", new LatLong(48.009814, 19.545752));
        areaCenters.put("HU_NY", new LatLong(47.953161, 21.726778));
        areaCenters.put("HU_PS", new LatLong(46.111323, 18.233148));
        areaCenters.put("HU_PE", new LatLong(47.320379, 19.441199));
        areaCenters.put("HU_SO", new LatLong(46.554859, 17.586673));
        areaCenters.put("HU_SN", new LatLong(47.684896, 16.583054));
        areaCenters.put("HU_SZ", new LatLong(48.039495, 22.00333));
        areaCenters.put("HU_SD", new LatLong(46.253744, 20.145949));
        areaCenters.put("HU_SF", new LatLong(47.188716, 18.413809));
        areaCenters.put("HU_SK", new LatLong(47.174662, 20.176264));
        areaCenters.put("HU_SH", new LatLong(47.23514, 16.621923));
        areaCenters.put("HU_TB", new LatLong(47.586187, 18.394764));
        areaCenters.put("HU_TO", new LatLong(46.476279, 18.557062));
        areaCenters.put("HU_VA", new LatLong(47.092911, 16.681218));
        areaCenters.put("HU_VE", new LatLong(47.092967, 17.583781));
        areaCenters.put("HU_VM", new LatLong(47.142967, 17.873785));
        areaCenters.put("HU_ZA", new LatLong(46.73844, 16.915225));
        areaCenters.put("HU_ZE", new LatLong(46.845389, 16.847235));

    }
}
