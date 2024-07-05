package csi.server.util;

import org.apache.empire.db.postgresql.DBDatabaseDriverPostgreSQL;

@SuppressWarnings("serial")
public class PostgreSQLDriverExtensions extends DBDatabaseDriverPostgreSQL {

    /*
     * Start our extension codes @ 9000... make sure to update our local
     * counters...
     */

    // String functions
    public static final int FUNC_REGEXP_SPLIT_TO_ARRAY = 9000;
    public static final int FUNC_REGEXP_SPLIT_TO_TABLE = 9001;
    public static final int FUNC_REGEXP_SPLIT_PART = 9002;
    public static final int FUNC_TRANSLATE = 9003;

    public static final int SQL_FUNC_HOUR = 9004;
    public static final int SQL_FUNC_MINUTE = 9005;
    public static final int SQL_FUNC_YEARMONTH = 9006;
    public static final int SQL_FUNC_DATE = 9007;

    static final int MIN_ID = FUNC_REGEXP_SPLIT_TO_ARRAY;
    static final int MAX_ID = 10000;

    @Override
    protected boolean detectQuoteName(String name) {
        boolean quote = super.detectQuoteName(name);
        if (quote) {
            // make sure we're not dealing with a name that is already quoted!@!
            char s = name.charAt(0);
            char e = name.charAt(name.length() - 1);
            if (s == e && s == '"') {
                quote = false;
            }
        }

        return quote;
    }

    @Override
    public String getSQLPhrase(int phrase) {
        switch (phrase) {
            case FUNC_REGEXP_SPLIT_TO_ARRAY:
                return "regexp_split_to_array(?, {0}, {1})";
            case FUNC_REGEXP_SPLIT_TO_TABLE:
                return "regexp_split_to_table(?, {0}, {1})";
            case FUNC_REGEXP_SPLIT_PART:
                return "split_part(?, {0}, {1})";
            case FUNC_TRANSLATE:
                return "translate(?, {0}, {1})";
            case SQL_FUNC_DATE:
                return "date( ? )";
            case SQL_FUNC_DAY:
                return "extract( day from ?)::integer";
            case SQL_FUNC_HOUR:
                return "extract( hour from ?)::integer";
            case SQL_FUNC_MINUTE:
                return "extract( minute from ?)::integer";
            case SQL_FUNC_YEAR:
                return "extract( year from ? )::integer";
            case SQL_FUNC_MONTH:
                return "extract( month from ? )::integer";
            case SQL_FUNC_CEILING:
                return "ceiling(?)";
            case SQL_FUNC_FLOOR:
                return "floor(?)";
            case SQL_FUNC_YEARMONTH:
                return "to_char(?, 'YYYY-MM')";
        }

        return super.getSQLPhrase(phrase);

    }

    protected boolean inRange(int i) {
        return MIN_ID <= i && i <= MAX_ID || SQL_FUNC_DAY == i;
    }

}
