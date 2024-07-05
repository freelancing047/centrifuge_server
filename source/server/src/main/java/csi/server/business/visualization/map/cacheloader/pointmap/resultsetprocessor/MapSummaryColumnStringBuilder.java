package csi.server.business.visualization.map.cacheloader.pointmap.resultsetprocessor;

import csi.config.Configuration;
import csi.shared.core.visualization.map.MapSettingsDTO;

public class MapSummaryColumnStringBuilder {
    private MapSettingsDTO mapSettings;
    private int placeId;
    private int precision;

    public MapSummaryColumnStringBuilder(MapSettingsDTO mapSettings, int placeId, int precision) {
        this.mapSettings = mapSettings;
        this.placeId = placeId;
        this.precision = precision;
    }

    private static String getColumnTrimmedToPrecision(String columnString, int precision) {
        String s = getColumnDecimalMovedRightBy(columnString, precision);
        s = getColumnFloored(s);
        return getColumnDecimalMovedRightBy(s, -precision);
    }

    private static String getColumnDecimalMovedRightBy(String columnString, int precision) {
        return columnString + " * POWER(10, " + precision + ")";
    }

    private static String getColumnFloored(String columnString) {
        return "FLOOR(" + columnString + ")";
    }

    public String getLatColumnString() {
        return getColumnString("\"" + mapSettings.getPlaceSettings().get(placeId).getLatColumn() + "\"");
    }

    private String getColumnString(String quotedColumnString) {
        if (precision == Configuration.getInstance().getMapConfig().getDetailLevel())
            return quotedColumnString;
        else
            return getFlooredColumnString(quotedColumnString);
    }

    private String getFlooredColumnString(String columnString) {
        return getColumnTrimmedToPrecision(columnString, precision);
    }

    public String getLongColumnString() {
        return getColumnString("\"" + mapSettings.getPlaceSettings().get(placeId).getLongColumn() + "\"");
    }
}
