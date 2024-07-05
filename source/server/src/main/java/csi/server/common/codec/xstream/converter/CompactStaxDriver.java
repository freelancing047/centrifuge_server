package csi.server.common.codec.xstream.converter;

import java.io.Writer;

import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.io.xml.CompactWriter;
import com.thoughtworks.xstream.io.xml.StaxDriver;

public class CompactStaxDriver extends StaxDriver {

    @Override
    public HierarchicalStreamWriter createWriter(Writer out) {
        return new CompactWriter(out);
    }

}
