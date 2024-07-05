package csi.server.common.codec;

public enum CodecType {

    FLEX_XML("application/xml", CodecRefType.NO_REF, true),

    // stream xml codecs
    XML("application/xml", CodecRefType.NO_REF, true), XML_UUID_REF("application/xml", CodecRefType.UUID_REF, true), XML_ID_REF("application/xml", CodecRefType.ID_REF, true), XML_XPATH_REL_REF(
            "application/xml", CodecRefType.XPATH_REL_REF, true), XML_XPATH_ABS_REF("application/xml", CodecRefType.XPATH_ABS_REF, true),

    JETTISON("application/json", CodecRefType.NO_REF, true), // JSON using XStream's Jettison Driver
    // flexjson json codecs
    JSON("application/json", CodecRefType.NO_REF, false), // same json_deep
    JSON_DEEP("application/json", CodecRefType.NO_REF, false), // flexjson deep
    JSON_SHALLOW("application/json", CodecRefType.NO_REF, false); // flexjson shallow

    private String contentType;
    private CodecRefType refMode;
    private boolean isXml;

    CodecType(String contentType) {
        this.contentType = contentType;
        this.refMode = CodecRefType.NO_REF;
    }

    CodecType(String contentType, CodecRefType refMode, boolean isXml) {
        this.contentType = contentType;
        this.refMode = refMode;
        this.isXml = isXml;
    }

    public String getContentType() {
        return this.contentType;
    }

    public CodecRefType getRefMode() {
        return this.refMode;
    }

    public boolean isXml() {
        return this.isXml;
    }

    public static CodecType resolveValue(String sval) {
        CodecType t = null;
        if (sval != null && !sval.isEmpty()) {
            try {
                t = CodecType.valueOf(sval.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                // ignore
            }
        }

        return t;
    }

}
