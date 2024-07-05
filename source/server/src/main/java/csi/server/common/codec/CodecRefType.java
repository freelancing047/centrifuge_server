package csi.server.common.codec;

public enum CodecRefType {

    NO_REF, ID_REF, UUID_REF, XPATH_REL_REF, XPATH_ABS_REF;

    public static CodecRefType resolveValue(String sval) {
        CodecRefType t = null;
        if (sval != null && !sval.isEmpty()) {
            try {
                t = CodecRefType.valueOf(sval.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                // ignore
            }
        }

        // default to no references
        if (t == null) {
            t = CodecRefType.NO_REF;
        }

        return t;
    }
}
