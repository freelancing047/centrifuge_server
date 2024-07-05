package csi.server.common.codec;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

public interface Codec {

    CodecType getType();

    String getContentType();

    String marshal(Object obj);

    void marshal(Object obj, OutputStream stream);

    void marshal(Object obj, Writer writer);

    Object unmarshal(String data);

    Object unmarshal(InputStream stream);

    Object unmarshal(Reader reader);
}
