package csi.server.business.visualization;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.annotations.GwtIncompatible;

import csi.server.common.enumerations.CsiDataType;

/*
 * Broke this out to a separate class. As we support spinning off from one view to another, there will be a need for
 * getting the underlying rows from many different classes.
 */

public class SupportingRows {
 @GwtIncompatible(value = "true" )
    public static final Logger LOG = LogManager.getLogger(SupportingRows.class);

    public static final String LEFT = "left";

    public static final String RIGHT = "right";

    public static final String LENGTH = "length";

    public static final String SUBSTRING = "substr";

    public static final String YEAR = "year";

    public static final String YEAR_MONTH = "year and month";

    public static final String MONTH = "month";

    public static final String DAY = "day";

    public static final String HOUR = "hour";

    public static final String MINUTE = "minute";

    public static final String DATE = "date";

    public static final String CEILING = "ceiling";

    public static final String FLOOR = "floor";
    
    // For quarter:
    // (cast(date_part('month', columnName) as integer) - 1) / 3 + 1

    static Map<String, CsiDataType> functionValueTypes = new HashMap<String, CsiDataType>();

    static {
        functionValueTypes.put(LEFT, CsiDataType.String);
        functionValueTypes.put(RIGHT, CsiDataType.String);
        functionValueTypes.put(LENGTH, CsiDataType.Integer);
        functionValueTypes.put(SUBSTRING, CsiDataType.String);
        functionValueTypes.put(YEAR, CsiDataType.Number);
        functionValueTypes.put(YEAR_MONTH, CsiDataType.String);
        functionValueTypes.put(MONTH, CsiDataType.Number);
        functionValueTypes.put(DAY, CsiDataType.Number);
        functionValueTypes.put(HOUR, CsiDataType.Number);
        functionValueTypes.put(MINUTE, CsiDataType.Number);
        functionValueTypes.put(DATE, CsiDataType.Date);
        functionValueTypes.put(CEILING, CsiDataType.Number);
        functionValueTypes.put(FLOOR, CsiDataType.Number);
    }

    public static CsiDataType getFunctionType(String bundleFunction) {
        return functionValueTypes.get(bundleFunction);
    }

}
