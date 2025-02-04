package csi.server.business.visualization.mapchart;

import java.util.HashMap;

public class MCMapWorld extends MCMapBase {

    public MCMapWorld() {

        // Callibration for mercator (lat/long) projection
        tl_lat = "83.63";
        tl_long = "-168.49";
        br_lat = "-55.58";
        br_long = "190.3";
        zoom = "100%";
        zoom_x = "0%";
        zoom_y = "0%";

        mapFile = "world.swf";

        mapAreas = new HashMap<String, String>();

        mapAreas.put("AC", "Ascension Island");
        mapAreas.put("AD", "Andorra");
        mapAreas.put("AE", "United Arab Emirates");
        mapAreas.put("AF", "Afghanistan");
        mapAreas.put("AG", "Antigua and Barbuda");
        mapAreas.put("AI", "Anguilla");
        mapAreas.put("AL", "Albania");
        mapAreas.put("AM", "Armenia");
        mapAreas.put("AN", "Netherlands Antilles");
        mapAreas.put("AO", "Angola");
        mapAreas.put("AQ", "Antarctica");
        mapAreas.put("AR", "Argentina");
        mapAreas.put("AS", "American Samoa");
        mapAreas.put("AT", "Austria");
        mapAreas.put("AU", "Australia");
        mapAreas.put("AW", "Aruba");
        mapAreas.put("AX", "Aland Islands");
        mapAreas.put("AZ", "Azerbaijan");
        mapAreas.put("BA", "Bosnia and Herzegovina");
        mapAreas.put("BB", "Barbados");
        mapAreas.put("BD", "Bangladesh");
        mapAreas.put("BE", "Belgium");
        mapAreas.put("BF", "Burkina Faso");
        mapAreas.put("BG", "Bulgaria");
        mapAreas.put("BH", "Bahrain");
        mapAreas.put("BI", "Burundi");
        mapAreas.put("BJ", "Benin");
        mapAreas.put("BM", "Bermuda");
        mapAreas.put("BN", "Brunei Darussalam");
        mapAreas.put("BO", "Bolivia");
        mapAreas.put("BR", "Brazil");
        mapAreas.put("BS", "Bahamas");
        mapAreas.put("BT", "Bhutan");
        mapAreas.put("BV", "Bouvet Island");
        mapAreas.put("BW", "Botswana");
        mapAreas.put("BY", "Belarus");
        mapAreas.put("BZ", "Belize");
        mapAreas.put("CA", "Canada");
        mapAreas.put("CC", "Cocos");
        mapAreas.put("CD", "Congo");
        mapAreas.put("CF", "Central African Republic");
        mapAreas.put("CG", "Congo");
        mapAreas.put("CH", "Switzerland");
        mapAreas.put("CI", "Cote D'Ivoire");
        mapAreas.put("CK", "Cook Islands");
        mapAreas.put("CL", "Chile");
        mapAreas.put("CM", "Cameroon");
        mapAreas.put("CN", "China");
        mapAreas.put("CO", "Colombia");
        mapAreas.put("CR", "Costa Rica");
        mapAreas.put("CS", "Czechoslovakia");
        mapAreas.put("CU", "Cuba");
        mapAreas.put("CV", "Cape Verde");
        mapAreas.put("CX", "Christmas Island");
        mapAreas.put("CY", "Cyprus");
        mapAreas.put("CZ", "Czech Republic");
        mapAreas.put("DE", "Germany");
        mapAreas.put("DJ", "Djibouti");
        mapAreas.put("DK", "Denmark");
        mapAreas.put("DM", "Dominica");
        mapAreas.put("DO", "Dominican Republic");
        mapAreas.put("DZ", "Algeria");
        mapAreas.put("EC", "Ecuador");
        mapAreas.put("EE", "Estonia");
        mapAreas.put("EG", "Egypt");
        mapAreas.put("EH", "Western Sahara");
        mapAreas.put("ER", "Eritrea");
        mapAreas.put("ES", "Spain");
        mapAreas.put("ET", "Ethiopia");
        mapAreas.put("FI", "Finland");
        mapAreas.put("FJ", "Fiji");
        mapAreas.put("FK", "Falkland Islands");
        mapAreas.put("FM", "Micronesia");
        mapAreas.put("FO", "Faroe Islands");
        mapAreas.put("FR", "France");
        mapAreas.put("FX", "France");
        mapAreas.put("GA", "Gabon");
        mapAreas.put("GB", "Great Britain");
        mapAreas.put("GD", "Grenada");
        mapAreas.put("GE", "Georgia");
        mapAreas.put("GF", "French Guiana");
        mapAreas.put("GH", "Ghana");
        mapAreas.put("GI", "Gibraltar");
        mapAreas.put("GL", "Greenland");
        mapAreas.put("GM", "Gambia");
        mapAreas.put("GN", "Guinea");
        mapAreas.put("GP", "Guadeloupe");
        mapAreas.put("GQ", "Equatorial Guinea");
        mapAreas.put("GR", "Greece");
        mapAreas.put("GS", "S. Georgia and S. Sandwich Isls.");
        mapAreas.put("GT", "Guatemala");
        mapAreas.put("GU", "Guam");
        mapAreas.put("GW", "Guinea-Bissau");
        mapAreas.put("GY", "Guyana");
        mapAreas.put("HK", "Hong Kong");
        mapAreas.put("HM", "Heard and McDonald Islands");
        mapAreas.put("HN", "Honduras");
        mapAreas.put("HR", "Croatia");
        mapAreas.put("HT", "Haiti");
        mapAreas.put("HU", "Hungary");
        mapAreas.put("ID", "Indonesia");
        mapAreas.put("IE", "Ireland");
        mapAreas.put("IL", "Israel");
        mapAreas.put("IM", "Isle of Man");
        mapAreas.put("IN", "India");
        mapAreas.put("IO", "British Indian Ocean Territory");
        mapAreas.put("IQ", "Iraq");
        mapAreas.put("IR", "Iran");
        mapAreas.put("IS", "Iceland");
        mapAreas.put("IT", "Italy");
        mapAreas.put("JE", "Jersey");
        mapAreas.put("JM", "Jamaica");
        mapAreas.put("JO", "Jordan");
        mapAreas.put("JP", "Japan");
        mapAreas.put("KE", "Kenya");
        mapAreas.put("KG", "Kyrgyzstan");
        mapAreas.put("KH", "Cambodia");
        mapAreas.put("KI", "Kiribati");
        mapAreas.put("KM", "Comoros");
        mapAreas.put("KN", "Saint Kitts and Nevis");
        mapAreas.put("KP", "Korea");
        mapAreas.put("KR", "Korea");
        mapAreas.put("KW", "Kuwait");
        mapAreas.put("KY", "Cayman Islands");
        mapAreas.put("KZ", "Kazakhstan");
        mapAreas.put("LA", "Laos");
        mapAreas.put("LB", "Lebanon");
        mapAreas.put("LC", "Saint Lucia");
        mapAreas.put("LI", "Liechtenstein");
        mapAreas.put("LK", "Sri Lanka");
        mapAreas.put("LR", "Liberia");
        mapAreas.put("LS", "Lesotho");
        mapAreas.put("LT", "Lithuania");
        mapAreas.put("LU", "Luxembourg");
        mapAreas.put("LV", "Latvia");
        mapAreas.put("LY", "Libya");
        mapAreas.put("MA", "Morocco");
        mapAreas.put("MC", "Monaco");
        mapAreas.put("MD", "Moldova");
        mapAreas.put("ME", "Montenegro");
        mapAreas.put("MG", "Madagascar");
        mapAreas.put("MH", "Marshall Islands");
        mapAreas.put("MK", "F.Y.R.O.M.");
        mapAreas.put("ML", "Mali");
        mapAreas.put("MM", "Myanmar");
        mapAreas.put("MN", "Mongolia");
        mapAreas.put("MO", "Macau");
        mapAreas.put("MP", "Northern Mariana Islands");
        mapAreas.put("MQ", "Martinique");
        mapAreas.put("MR", "Mauritania");
        mapAreas.put("MS", "Montserrat");
        mapAreas.put("MT", "Malta");
        mapAreas.put("MU", "Mauritius");
        mapAreas.put("MV", "Maldives");
        mapAreas.put("MW", "Malawi");
        mapAreas.put("MX", "Mexico");
        mapAreas.put("MY", "Malaysia");
        mapAreas.put("MZ", "Mozambique");
        mapAreas.put("NA", "Namibia");
        mapAreas.put("NC", "New Caledonia");
        mapAreas.put("NE", "Niger");
        mapAreas.put("NF", "Norfolk Island");
        mapAreas.put("NG", "Nigeria");
        mapAreas.put("NI", "Nicaragua");
        mapAreas.put("NL", "Netherlands");
        mapAreas.put("NO", "Norway");
        mapAreas.put("NP", "Nepal");
        mapAreas.put("NR", "Nauru");
        mapAreas.put("NT", "Neutral Zone");
        mapAreas.put("NU", "Niue");
        mapAreas.put("NZ", "New Zealand");
        mapAreas.put("OM", "Oman");
        mapAreas.put("PA", "Panama");
        mapAreas.put("PE", "Peru");
        mapAreas.put("PF", "French Polynesia");
        mapAreas.put("PG", "Papua New Guinea");
        mapAreas.put("PH", "Philippines");
        mapAreas.put("PK", "Pakistan");
        mapAreas.put("PL", "Poland");
        mapAreas.put("PM", "St. Pierre and Miquelon");
        mapAreas.put("PN", "Pitcairn");
        mapAreas.put("PR", "Puerto Rico");
        mapAreas.put("PS", "Palestinian Territory");
        mapAreas.put("PT", "Portugal");
        mapAreas.put("PW", "Palau");
        mapAreas.put("PY", "Paraguay");
        mapAreas.put("QA", "Qatar");
        mapAreas.put("RE", "Reunion");
        mapAreas.put("RO", "Romania");
        mapAreas.put("RS", "Serbia");
        mapAreas.put("RU", "Russian Federation");
        mapAreas.put("RW", "Rwanda");
        mapAreas.put("SA", "Saudi Arabia");
        mapAreas.put("SB", "Solomon Islands");
        mapAreas.put("SC", "Seychelles");
        mapAreas.put("SD", "Sudan");
        mapAreas.put("SE", "Sweden");
        mapAreas.put("SG", "Singapore");
        mapAreas.put("SH", "St. Helena");
        mapAreas.put("SI", "Slovenia");
        mapAreas.put("SJ", "Svalbard & Jan Mayen Islands");
        mapAreas.put("SK", "Slovak Republic");
        mapAreas.put("SL", "Sierra Leone");
        mapAreas.put("SM", "San Marino");
        mapAreas.put("SN", "Senegal");
        mapAreas.put("SO", "Somalia");
        mapAreas.put("SR", "Suriname");
        mapAreas.put("ST", "Sao Tome and Principe");
        mapAreas.put("SU", "USSR");
        mapAreas.put("SV", "El Salvador");
        mapAreas.put("SY", "Syria");
        mapAreas.put("SZ", "Swaziland");
        mapAreas.put("TC", "Turks and Caicos Islands");
        mapAreas.put("TD", "Chad");
        mapAreas.put("TF", "French Southern Territories");
        mapAreas.put("TG", "Togo");
        mapAreas.put("TH", "Thailand");
        mapAreas.put("TJ", "Tajikistan");
        mapAreas.put("TK", "Tokelau");
        mapAreas.put("TM", "Turkmenistan");
        mapAreas.put("TN", "Tunisia");
        mapAreas.put("TO", "Tonga");
        mapAreas.put("TP", "East Timor");
        mapAreas.put("TR", "Turkey");
        mapAreas.put("TT", "Trinidad and Tobago");
        mapAreas.put("TV", "Tuvalu");
        mapAreas.put("TW", "Taiwan");
        mapAreas.put("TZ", "Tanzania");
        mapAreas.put("UA", "Ukraine");
        mapAreas.put("UG", "Uganda");
        mapAreas.put("UK", "United Kingdom");
        mapAreas.put("UM", "US Minor Outlying Islands");
        mapAreas.put("US", "United States");
        mapAreas.put("UY", "Uruguay");
        mapAreas.put("UZ", "Uzbekistan");
        mapAreas.put("VA", "Vatican City State");
        mapAreas.put("VC", "Saint Vincent & the Grenadines");
        mapAreas.put("VE", "Venezuela");
        mapAreas.put("VG", "British Virgin Islands");
        mapAreas.put("VI", "US Virgin Islands");
        mapAreas.put("VN", "Viet Nam");
        mapAreas.put("VU", "Vanuatu");
        mapAreas.put("WF", "Wallis and Futuna Islands");
        mapAreas.put("WS", "Samoa");
        mapAreas.put("YE", "Yemen");
        mapAreas.put("YT", "Mayotte");
        mapAreas.put("YU", "Yugoslavia");
        mapAreas.put("ZA", "South Africa");
        mapAreas.put("ZM", "Zambia");
        mapAreas.put("ZR", "Zaire");
        mapAreas.put("ZW", "Zimbabwe");

        // somewhat 'center' of each country. derived from
        // http://www.maxmind.com/app/country_latlon
        // vim:
        // :%s/,\(.*$\)/, new LatLong( \1 )/gc
        // :%s/\(^\w)*,/"\1"/gc
        // :%s/\(.*$\)/areaCenters.put( \1 );/gc
        areaCenters.put("AD", new LatLong(42.5000, 1.5000));
        areaCenters.put("AE", new LatLong(24.0000, 54.0000));
        areaCenters.put("AF", new LatLong(33.0000, 65.0000));
        areaCenters.put("AG", new LatLong(17.0500, -61.8000));
        areaCenters.put("AI", new LatLong(18.2500, -63.1667));
        areaCenters.put("AL", new LatLong(41.0000, 20.0000));
        areaCenters.put("AM", new LatLong(40.0000, 45.0000));
        areaCenters.put("AN", new LatLong(12.2500, -68.7500));
        areaCenters.put("AO", new LatLong(-12.5000, 18.5000));
        areaCenters.put("AP", new LatLong(35.0000, 105.0000));
        areaCenters.put("AQ", new LatLong(-90.0000, 0.0000));
        areaCenters.put("AR", new LatLong(-34.0000, -64.0000));
        areaCenters.put("AS", new LatLong(-14.3333, -170.0000));
        areaCenters.put("AT", new LatLong(47.3333, 13.3333));
        areaCenters.put("AU", new LatLong(-27.0000, 133.0000));
        areaCenters.put("AW", new LatLong(12.5000, -69.9667));
        areaCenters.put("AZ", new LatLong(40.5000, 47.5000));
        areaCenters.put("BA", new LatLong(44.0000, 18.0000));
        areaCenters.put("BB", new LatLong(13.1667, -59.5333));
        areaCenters.put("BD", new LatLong(24.0000, 90.0000));
        areaCenters.put("BE", new LatLong(50.8333, 4.0000));
        areaCenters.put("BF", new LatLong(13.0000, -2.0000));
        areaCenters.put("BG", new LatLong(43.0000, 25.0000));
        areaCenters.put("BH", new LatLong(26.0000, 50.5500));
        areaCenters.put("BI", new LatLong(-3.5000, 30.0000));
        areaCenters.put("BJ", new LatLong(9.5000, 2.2500));
        areaCenters.put("BM", new LatLong(32.3333, -64.7500));
        areaCenters.put("BN", new LatLong(4.5000, 114.6667));
        areaCenters.put("BO", new LatLong(-17.0000, -65.0000));
        areaCenters.put("BR", new LatLong(-10.0000, -55.0000));
        areaCenters.put("BS", new LatLong(24.2500, -76.0000));
        areaCenters.put("BT", new LatLong(27.5000, 90.5000));
        areaCenters.put("BV", new LatLong(-54.4333, 3.4000));
        areaCenters.put("BW", new LatLong(-22.0000, 24.0000));
        areaCenters.put("BY", new LatLong(53.0000, 28.0000));
        areaCenters.put("BZ", new LatLong(17.2500, -88.7500));
        areaCenters.put("CA", new LatLong(60.0000, -95.0000));
        areaCenters.put("CC", new LatLong(-12.5000, 96.8333));
        areaCenters.put("CD", new LatLong(0.0000, 25.0000));
        areaCenters.put("CF", new LatLong(7.0000, 21.0000));
        areaCenters.put("CG", new LatLong(-1.0000, 15.0000));
        areaCenters.put("CH", new LatLong(47.0000, 8.0000));
        areaCenters.put("CI", new LatLong(8.0000, -5.0000));
        areaCenters.put("CK", new LatLong(-21.2333, -159.7667));
        areaCenters.put("CL", new LatLong(-30.0000, -71.0000));
        areaCenters.put("CM", new LatLong(6.0000, 12.0000));
        areaCenters.put("CN", new LatLong(35.0000, 105.0000));
        areaCenters.put("CO", new LatLong(4.0000, -72.0000));
        areaCenters.put("CR", new LatLong(10.0000, -84.0000));
        areaCenters.put("CU", new LatLong(21.5000, -80.0000));
        areaCenters.put("CV", new LatLong(16.0000, -24.0000));
        areaCenters.put("CX", new LatLong(-10.5000, 105.6667));
        areaCenters.put("CY", new LatLong(35.0000, 33.0000));
        areaCenters.put("CZ", new LatLong(49.7500, 15.5000));
        areaCenters.put("DE", new LatLong(51.0000, 9.0000));
        areaCenters.put("DJ", new LatLong(11.5000, 43.0000));
        areaCenters.put("DK", new LatLong(56.0000, 10.0000));
        areaCenters.put("DM", new LatLong(15.4167, -61.3333));
        areaCenters.put("DO", new LatLong(19.0000, -70.6667));
        areaCenters.put("DZ", new LatLong(28.0000, 3.0000));
        areaCenters.put("EC", new LatLong(-2.0000, -77.5000));
        areaCenters.put("EE", new LatLong(59.0000, 26.0000));
        areaCenters.put("EG", new LatLong(27.0000, 30.0000));
        areaCenters.put("EH", new LatLong(24.5000, -13.0000));
        areaCenters.put("ER", new LatLong(15.0000, 39.0000));
        areaCenters.put("ES", new LatLong(40.0000, -4.0000));
        areaCenters.put("ET", new LatLong(8.0000, 38.0000));
        areaCenters.put("EU", new LatLong(47.0000, 8.0000));
        areaCenters.put("FI", new LatLong(64.0000, 26.0000));
        areaCenters.put("FJ", new LatLong(-18.0000, 175.0000));
        areaCenters.put("FK", new LatLong(-51.7500, -59.0000));
        areaCenters.put("FM", new LatLong(6.9167, 158.2500));
        areaCenters.put("FO", new LatLong(62.0000, -7.0000));
        areaCenters.put("FR", new LatLong(46.0000, 2.0000));
        areaCenters.put("GA", new LatLong(-1.0000, 11.7500));
        areaCenters.put("GB", new LatLong(54.0000, -2.0000));
        areaCenters.put("GD", new LatLong(12.1167, -61.6667));
        areaCenters.put("GE", new LatLong(42.0000, 43.5000));
        areaCenters.put("GF", new LatLong(4.0000, -53.0000));
        areaCenters.put("GH", new LatLong(8.0000, -2.0000));
        areaCenters.put("GI", new LatLong(36.1833, -5.3667));
        areaCenters.put("GL", new LatLong(72.0000, -40.0000));
        areaCenters.put("GM", new LatLong(13.4667, -16.5667));
        areaCenters.put("GN", new LatLong(11.0000, -10.0000));
        areaCenters.put("GP", new LatLong(16.2500, -61.5833));
        areaCenters.put("GQ", new LatLong(2.0000, 10.0000));
        areaCenters.put("GR", new LatLong(39.0000, 22.0000));
        areaCenters.put("GS", new LatLong(-54.5000, -37.0000));
        areaCenters.put("GT", new LatLong(15.5000, -90.2500));
        areaCenters.put("GU", new LatLong(13.4667, 144.7833));
        areaCenters.put("GW", new LatLong(12.0000, -15.0000));
        areaCenters.put("GY", new LatLong(5.0000, -59.0000));
        areaCenters.put("HK", new LatLong(22.2500, 114.1667));
        areaCenters.put("HM", new LatLong(-53.1000, 72.5167));
        areaCenters.put("HN", new LatLong(15.0000, -86.5000));
        areaCenters.put("HR", new LatLong(45.1667, 15.5000));
        areaCenters.put("HT", new LatLong(19.0000, -72.4167));
        areaCenters.put("HU", new LatLong(47.0000, 20.0000));
        areaCenters.put("ID", new LatLong(-5.0000, 120.0000));
        areaCenters.put("IE", new LatLong(53.0000, -8.0000));
        areaCenters.put("IL", new LatLong(31.5000, 34.7500));
        areaCenters.put("IN", new LatLong(20.0000, 77.0000));
        areaCenters.put("IO", new LatLong(-6.0000, 71.5000));
        areaCenters.put("IQ", new LatLong(33.0000, 44.0000));
        areaCenters.put("IR", new LatLong(32.0000, 53.0000));
        areaCenters.put("IS", new LatLong(65.0000, -18.0000));
        areaCenters.put("IT", new LatLong(42.8333, 12.8333));
        areaCenters.put("JM", new LatLong(18.2500, -77.5000));
        areaCenters.put("JO", new LatLong(31.0000, 36.0000));
        areaCenters.put("JP", new LatLong(36.0000, 138.0000));
        areaCenters.put("KE", new LatLong(1.0000, 38.0000));
        areaCenters.put("KG", new LatLong(41.0000, 75.0000));
        areaCenters.put("KH", new LatLong(13.0000, 105.0000));
        areaCenters.put("KI", new LatLong(1.4167, 173.0000));
        areaCenters.put("KM", new LatLong(-12.1667, 44.2500));
        areaCenters.put("KN", new LatLong(17.3333, -62.7500));
        areaCenters.put("KP", new LatLong(40.0000, 127.0000));
        areaCenters.put("KR", new LatLong(37.0000, 127.5000));
        areaCenters.put("KW", new LatLong(29.3375, 47.6581));
        areaCenters.put("KY", new LatLong(19.5000, -80.5000));
        areaCenters.put("KZ", new LatLong(48.0000, 68.0000));
        areaCenters.put("LA", new LatLong(18.0000, 105.0000));
        areaCenters.put("LB", new LatLong(33.8333, 35.8333));
        areaCenters.put("LC", new LatLong(13.8833, -61.1333));
        areaCenters.put("LI", new LatLong(47.1667, 9.5333));
        areaCenters.put("LK", new LatLong(7.0000, 81.0000));
        areaCenters.put("LR", new LatLong(6.5000, -9.5000));
        areaCenters.put("LS", new LatLong(-29.5000, 28.5000));
        areaCenters.put("LT", new LatLong(56.0000, 24.0000));
        areaCenters.put("LU", new LatLong(49.7500, 6.1667));
        areaCenters.put("LV", new LatLong(57.0000, 25.0000));
        areaCenters.put("LY", new LatLong(25.0000, 17.0000));
        areaCenters.put("MA", new LatLong(32.0000, -5.0000));
        areaCenters.put("MC", new LatLong(43.7333, 7.4000));
        areaCenters.put("MD", new LatLong(47.0000, 29.0000));
        areaCenters.put("ME", new LatLong(42.0000, 19.0000));
        areaCenters.put("MG", new LatLong(-20.0000, 47.0000));
        areaCenters.put("MH", new LatLong(9.0000, 168.0000));
        areaCenters.put("MK", new LatLong(41.8333, 22.0000));
        areaCenters.put("ML", new LatLong(17.0000, -4.0000));
        areaCenters.put("MM", new LatLong(22.0000, 98.0000));
        areaCenters.put("MN", new LatLong(46.0000, 105.0000));
        areaCenters.put("MO", new LatLong(22.1667, 113.5500));
        areaCenters.put("MP", new LatLong(15.2000, 145.7500));
        areaCenters.put("MQ", new LatLong(14.6667, -61.0000));
        areaCenters.put("MR", new LatLong(20.0000, -12.0000));
        areaCenters.put("MS", new LatLong(16.7500, -62.2000));
        areaCenters.put("MT", new LatLong(35.8333, 14.5833));
        areaCenters.put("MU", new LatLong(-20.2833, 57.5500));
        areaCenters.put("MV", new LatLong(3.2500, 73.0000));
        areaCenters.put("MW", new LatLong(-13.5000, 34.0000));
        areaCenters.put("MX", new LatLong(23.0000, -102.0000));
        areaCenters.put("MY", new LatLong(2.5000, 112.5000));
        areaCenters.put("MZ", new LatLong(-18.2500, 35.0000));
        areaCenters.put("NA", new LatLong(-22.0000, 17.0000));
        areaCenters.put("NC", new LatLong(-21.5000, 165.5000));
        areaCenters.put("NE", new LatLong(16.0000, 8.0000));
        areaCenters.put("NF", new LatLong(-29.0333, 167.9500));
        areaCenters.put("NG", new LatLong(10.0000, 8.0000));
        areaCenters.put("NI", new LatLong(13.0000, -85.0000));
        areaCenters.put("NL", new LatLong(52.5000, 5.7500));
        areaCenters.put("NO", new LatLong(62.0000, 10.0000));
        areaCenters.put("NP", new LatLong(28.0000, 84.0000));
        areaCenters.put("NR", new LatLong(-0.5333, 166.9167));
        areaCenters.put("NU", new LatLong(-19.0333, -169.8667));
        areaCenters.put("NZ", new LatLong(-41.0000, 174.0000));
        areaCenters.put("OM", new LatLong(21.0000, 57.0000));
        areaCenters.put("PA", new LatLong(9.0000, -80.0000));
        areaCenters.put("PE", new LatLong(-10.0000, -76.0000));
        areaCenters.put("PF", new LatLong(-15.0000, -140.0000));
        areaCenters.put("PG", new LatLong(-6.0000, 147.0000));
        areaCenters.put("PH", new LatLong(13.0000, 122.0000));
        areaCenters.put("PK", new LatLong(30.0000, 70.0000));
        areaCenters.put("PL", new LatLong(52.0000, 20.0000));
        areaCenters.put("PM", new LatLong(46.8333, -56.3333));
        areaCenters.put("PR", new LatLong(18.2500, -66.5000));
        areaCenters.put("PS", new LatLong(32.0000, 35.2500));
        areaCenters.put("PT", new LatLong(39.5000, -8.0000));
        areaCenters.put("PW", new LatLong(7.5000, 134.5000));
        areaCenters.put("PY", new LatLong(-23.0000, -58.0000));
        areaCenters.put("QA", new LatLong(25.5000, 51.2500));
        areaCenters.put("RE", new LatLong(-21.1000, 55.6000));
        areaCenters.put("RO", new LatLong(46.0000, 25.0000));
        areaCenters.put("RS", new LatLong(44.0000, 21.0000));
        areaCenters.put("RU", new LatLong(60.0000, 100.0000));
        areaCenters.put("RW", new LatLong(-2.0000, 30.0000));
        areaCenters.put("SA", new LatLong(25.0000, 45.0000));
        areaCenters.put("SB", new LatLong(-8.0000, 159.0000));
        areaCenters.put("SC", new LatLong(-4.5833, 55.6667));
        areaCenters.put("SD", new LatLong(15.0000, 30.0000));
        areaCenters.put("SE", new LatLong(62.0000, 15.0000));
        areaCenters.put("SG", new LatLong(1.3667, 103.8000));
        areaCenters.put("SH", new LatLong(-15.9333, -5.7000));
        areaCenters.put("SI", new LatLong(46.0000, 15.0000));
        areaCenters.put("SJ", new LatLong(78.0000, 20.0000));
        areaCenters.put("SK", new LatLong(48.6667, 19.5000));
        areaCenters.put("SL", new LatLong(8.5000, -11.5000));
        areaCenters.put("SM", new LatLong(43.7667, 12.4167));
        areaCenters.put("SN", new LatLong(14.0000, -14.0000));
        areaCenters.put("SO", new LatLong(10.0000, 49.0000));
        areaCenters.put("SR", new LatLong(4.0000, -56.0000));
        areaCenters.put("ST", new LatLong(1.0000, 7.0000));
        areaCenters.put("SV", new LatLong(13.8333, -88.9167));
        areaCenters.put("SY", new LatLong(35.0000, 38.0000));
        areaCenters.put("SZ", new LatLong(-26.5000, 31.5000));
        areaCenters.put("TC", new LatLong(21.7500, -71.5833));
        areaCenters.put("TD", new LatLong(15.0000, 19.0000));
        areaCenters.put("TF", new LatLong(-43.0000, 67.0000));
        areaCenters.put("TG", new LatLong(8.0000, 1.1667));
        areaCenters.put("TH", new LatLong(15.0000, 100.0000));
        areaCenters.put("TJ", new LatLong(39.0000, 71.0000));
        areaCenters.put("TK", new LatLong(-9.0000, -172.0000));
        areaCenters.put("TM", new LatLong(40.0000, 60.0000));
        areaCenters.put("TN", new LatLong(34.0000, 9.0000));
        areaCenters.put("TO", new LatLong(-20.0000, -175.0000));
        areaCenters.put("TR", new LatLong(39.0000, 35.0000));
        areaCenters.put("TT", new LatLong(11.0000, -61.0000));
        areaCenters.put("TV", new LatLong(-8.0000, 178.0000));
        areaCenters.put("TW", new LatLong(23.5000, 121.0000));
        areaCenters.put("TZ", new LatLong(-6.0000, 35.0000));
        areaCenters.put("UA", new LatLong(49.0000, 32.0000));
        areaCenters.put("UG", new LatLong(1.0000, 32.0000));
        areaCenters.put("UM", new LatLong(19.2833, 166.6000));
        areaCenters.put("US", new LatLong(38.0000, -97.0000));
        areaCenters.put("UY", new LatLong(-33.0000, -56.0000));
        areaCenters.put("UZ", new LatLong(41.0000, 64.0000));
        areaCenters.put("VA", new LatLong(41.9000, 12.4500));
        areaCenters.put("VC", new LatLong(13.2500, -61.2000));
        areaCenters.put("VE", new LatLong(8.0000, -66.0000));
        areaCenters.put("VG", new LatLong(18.5000, -64.5000));
        areaCenters.put("VI", new LatLong(18.3333, -64.8333));
        areaCenters.put("VN", new LatLong(16.0000, 106.0000));
        areaCenters.put("VU", new LatLong(-16.0000, 167.0000));
        areaCenters.put("WF", new LatLong(-13.3000, -176.2000));
        areaCenters.put("WS", new LatLong(-13.5833, -172.3333));
        areaCenters.put("YE", new LatLong(15.0000, 48.0000));
        areaCenters.put("YT", new LatLong(-12.8333, 45.1667));
        areaCenters.put("ZA", new LatLong(-29.0000, 24.0000));
        areaCenters.put("ZM", new LatLong(-15.0000, 30.0000));
        areaCenters.put("ZW", new LatLong(-20.0000, 30.0000));

    }
}
