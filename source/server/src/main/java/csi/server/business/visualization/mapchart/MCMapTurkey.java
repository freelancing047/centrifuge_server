package csi.server.business.visualization.mapchart;

import java.util.HashMap;

public class MCMapTurkey extends MCMapBase {

    public MCMapTurkey() {
        super();
        // Callibration for mercator (lat/long) projection
        tl_lat = "42.094016";
        tl_long = "25.6665542";
        br_lat = "35.807049";
        br_long = "44.818478";
        zoom = "90%";
        zoom_x = "6.05%";
        zoom_y = "38.87%";

        mapFile = "turkey.swf";
        mapAreas = new HashMap<String, String>();

        mapAreas.put("TR_AA", "Adana");
        mapAreas.put("TR_AD", "Adiyaman");
        mapAreas.put("TR_AF", "Afyonkarahisar (Afyon)");
        mapAreas.put("TR_AG", "Agri");
        mapAreas.put("TR_AK", "Aksaray");
        mapAreas.put("TR_AM", "Amasya");
        mapAreas.put("TR_AN", "Ankara");
        mapAreas.put("TR_AL", "Antalya");
        mapAreas.put("TR_AR", "Ardahan");
        mapAreas.put("TR_AV", "Artvin");
        mapAreas.put("TR_AY", "Aydin");
        mapAreas.put("TR_BK", "Balikesir");
        mapAreas.put("TR_BR", "Bartin");
        mapAreas.put("TR_BM", "Batman");
        mapAreas.put("TR_BB", "Bayburt");
        mapAreas.put("TR_BC", "Bilecik");
        mapAreas.put("TR_BG", "Bingöl");
        mapAreas.put("TR_BT", "Bitlis");
        mapAreas.put("TR_BL", "Bolu");
        mapAreas.put("TR_BD", "Burdur");
        mapAreas.put("TR_BU", "Bursa");
        mapAreas.put("TR_CK", "Çanakkale");
        mapAreas.put("TR_CI", "Çankiri");
        mapAreas.put("TR_CM", "Çorum");
        mapAreas.put("TR_DN", "Denizli");
        mapAreas.put("TR_DY", "Diyarbakir");
        mapAreas.put("TR_DU", "Düzce");
        mapAreas.put("TR_ED", "Edirne");
        mapAreas.put("TR_EG", "Elazig");
        mapAreas.put("TR_EN", "Erzincan");
        mapAreas.put("TR_EM", "Erzurum");
        mapAreas.put("TR_ES", "Eskisehir");
        mapAreas.put("TR_GA", "Gaziantep");
        mapAreas.put("TR_GI", "Giresun");
        mapAreas.put("TR_GU", "Gümüshane");
        mapAreas.put("TR_HK", "Hakkari");
        mapAreas.put("TR_HT", "Hatay");
        mapAreas.put("TR_IG", "Igdir");
        mapAreas.put("TR_IP", "Isparta");
        mapAreas.put("TR_IB", "Istanbul");
        mapAreas.put("TR_IZ", "Izmir");
        mapAreas.put("TR_KM", "Kahramanmaras");
        mapAreas.put("TR_KB", "Karabük");
        mapAreas.put("TR_KR", "Karaman");
        mapAreas.put("TR_KA", "Kars");
        mapAreas.put("TR_KS", "Kastamonu");
        mapAreas.put("TR_KY", "Kayseri");
        mapAreas.put("TR_KI", "Kilis");
        mapAreas.put("TR_KK", "Kirikkale");
        mapAreas.put("TR_KL", "Kirklareli");
        mapAreas.put("TR_KH", "Kirsehir");
        mapAreas.put("TR_KC", "Kocaeli");
        mapAreas.put("TR_KO", "Konya");
        mapAreas.put("TR_KU", "Kütahya");
        mapAreas.put("TR_ML", "Malatya");
        mapAreas.put("TR_MN", "Manisa");
        mapAreas.put("TR_MR", "Mardin");
        mapAreas.put("TR_IC", "Mersin");
        mapAreas.put("TR_MG", "Mugla");
        mapAreas.put("TR_MS", "Mus");
        mapAreas.put("TR_NV", "Nevsehir");
        mapAreas.put("TR_NG", "Nigde");
        mapAreas.put("TR_OR", "Ordu");
        mapAreas.put("TR_OS", "Osmaniye");
        mapAreas.put("TR_RI", "Rize");
        mapAreas.put("TR_SK", "Sakarya");
        mapAreas.put("TR_SS", "Samsun");
        mapAreas.put("TR_SU", "Sanliurfa");
        mapAreas.put("TR_SI", "Siirt");
        mapAreas.put("TR_SP", "Sinop");
        mapAreas.put("TR_SR", "Sirnak");
        mapAreas.put("TR_SV", "Sivas");
        mapAreas.put("TR_TG", "Tekirdag");
        mapAreas.put("TR_TT", "Tokat");
        mapAreas.put("TR_TB", "Trabzon");
        mapAreas.put("TR_TC", "Tunceli");
        mapAreas.put("TR_US", "Usak");
        mapAreas.put("TR_VA", "Van");
        mapAreas.put("TR_YL", "Yalova");
        mapAreas.put("TR_YZ", "Yozgat");
        mapAreas.put("TR_ZO", "Zonguldak");

    }
}
