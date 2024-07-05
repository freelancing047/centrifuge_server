package csi.server.common.codec.xstream.converter;

import com.thoughtworks.xstream.converters.basic.DateConverter;

public class SQLDateConverter extends DateConverter {

    private static final String DEFAULT_FORMAT = "yyyy/MM/dd HH:mm:ss 'GMT'Z";

    public SQLDateConverter() {
        super(DEFAULT_FORMAT, new String[] { "yyyy-MM-dd HH:mm:ss.S a", "yyyy-MM-dd HH:mm:ssz", "yyyy-MM-dd HH:mm:ss z", "yyyy-MM-dd HH:mm:ssa" });
    }

    @Override
    public boolean canConvert(Class type) {
        return type.equals(java.sql.Date.class) || type.equals(java.sql.Timestamp.class);
    }

}
