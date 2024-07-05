package csi.server.common.enumerations;

import java.io.Serializable;

/**
 * Created by centrifuge on 7/31/2015.
 */
public enum CsiEncoding implements Serializable {
    
    UTF_8("UTF-8", "UTF8"),
    UTF_16LE("UTF-16 (Little Endian)", "UnicodeLittle"),
    UTF_16BE("UTF-16 (Big Endian)", "UnicodeBig"),
    BIG5("BIG5", "Big5"),
    EUC_CN("EUC_CN", null),
    EUC_JP("EUC_JP", "EUCJIS"),
    EUC_JIS_2004("EUC_JIS_2004", null),
    EUC_KR("EUC_KR", null),
    EUC_TW("EUC_TW", null),
    GB18030("GB18030", null),
    GBK("GBK", "GBK"),
    ISO_8859_5("ISO_8859_5", "8859_5"),
    ISO_8859_6("ISO_8859_6", "8859_6"),
    ISO_8859_7("ISO_8859_7", "8859_7"),
    ISO_8859_8("ISO_8859_8", "8859_8"),
    JOHAB("JOHAB", null),
    KOI8R("KOI8R", null),
    KOI8U("KOI8U", null),
    LATIN1("LATIN1", "8859_1"),
    LATIN2("LATIN2", "8859_2"),
    LATIN3("LATIN3", "8859_3"),
    LATIN4("LATIN4", "8859_4"),
    LATIN5("LATIN5", "8859_9"),
    LATIN6("LATIN6", null),
    LATIN7("LATIN7", "ISO8859_13"),
    LATIN8("LATIN8", null),
    LATIN9("LATIN9", "ISO8859_15_FDIS"),
    LATIN10("LATIN10", null),
    MULE_INTERNAL("MULE_INTERNAL", null),
    SJIS("SJIS", null),
    SHIFT_JIS_2004("SHIFT_JIS_2004", null),
    SQL_ASCII("SQL_ASCII", "ASCII"),
    UHC("UHC", null),
    WIN866("WIN866", null),
    WIN874("WIN874", null),
    WIN1250("WIN1250", null),
    WIN1251("WIN1251", null),
    WIN1252("WIN1252", null),
    WIN1253("WIN1253", null),
    WIN1254("WIN1254", null),
    WIN1255("WIN1255", null),
    WIN1256("WIN1256", null),
    WIN1257("WIN1257", null),
    WIN1258("WIN1258", null);

    private String _label;
    private String _javaName;

    public String getLabel() {

        return _label;
    }

    public String getJavaName() {

        return _javaName;
    }

    private CsiEncoding(String labelIn, String javaNameIn) {

        _label = labelIn;
        _javaName = javaNameIn;
    }
}
