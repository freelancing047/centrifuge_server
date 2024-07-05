package csi.server.common.codec.xstream;

import com.thoughtworks.xstream.converters.ConverterLookup;
import com.thoughtworks.xstream.core.AbstractTreeMarshallingStrategy;
import com.thoughtworks.xstream.core.TreeMarshaller;
import com.thoughtworks.xstream.core.TreeUnmarshaller;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.mapper.Mapper;

public class ReferenceByUuidMarshallingStrategy extends AbstractTreeMarshallingStrategy {

    boolean isXml = true;

    public ReferenceByUuidMarshallingStrategy(boolean isXml) {
        this.isXml = isXml;
    }

    protected TreeUnmarshaller createUnmarshallingContext(Object root, HierarchicalStreamReader reader, ConverterLookup converterLookup, Mapper mapper) {
        return new ReferenceByUuidUnmarshaller(root, reader, converterLookup, mapper, isXml);
    }

    protected TreeMarshaller createMarshallingContext(HierarchicalStreamWriter writer, ConverterLookup converterLookup, Mapper mapper) {
        return new ReferenceByUuidMarshaller(writer, converterLookup, mapper, isXml);
    }
}