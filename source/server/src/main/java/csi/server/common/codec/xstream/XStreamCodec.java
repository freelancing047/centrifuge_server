package csi.server.common.codec.xstream;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

import com.thoughtworks.xstream.XStream;

import csi.server.common.codec.Codec;
import csi.server.common.codec.CodecType;

/**
 * Simple wrapper around xstream object
 * to make it conform to the Codec interface
 *
 */
public class XStreamCodec implements Codec {

    private XStream xstream = null;
    private CodecType type;

    public XStreamCodec(CodecType type, XStream xstream) {
        super();
        this.type = type;
        this.xstream = xstream;
    }

    @Override
    public CodecType getType() {
        return this.type;
    }

    @Override
    public String getContentType() {
        return type.getContentType();
    }

    public XStream getXstream() {
        return xstream;
    }

    @Override
    public String marshal(Object obj) {
        return this.xstream.toXML(obj);
    }

    @Override
    public void marshal(Object obj, OutputStream stream) {
        this.xstream.toXML(obj, stream);
    }

    @Override
    public void marshal(Object obj, Writer writer) {
        this.xstream.toXML(obj, writer);
    }

    @Override
    public Object unmarshal(String data) {
        return this.xstream.fromXML(data);
    }

    @Override
    public Object unmarshal(InputStream stream) {
        return this.xstream.fromXML(stream);
    }

    @Override
    public Object unmarshal(Reader reader) {
        return this.xstream.fromXML(reader);
    }

}
