package csi.server.common.codec.xstream.converter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.mapper.Mapper;

import csi.server.common.data.Result;

/**
 * Convert our wrapper response to XML.  This suppresses the default behavior of 
 * writing the field name as the element.  This assumes that the target type has been
 * registered as an alias, otherwise the fully qualified classname is reported.
 * <p>
 * Null values are reported via XStream's default handling of Null.
 * @author Tildenwoods
 *
 */
public class ResultConverter implements Converter {
   private static final Logger LOG = LogManager.getLogger(ResultConverter.class);

    protected Mapper _mapper;

    public ResultConverter(Mapper mapper) {
        _mapper = mapper;
    }

    /**
     * Marshal out the holder in our result.  If the class type is aliased, the short name is
     * retrieved when calling the mapper's serializeClass(); otherwise the FQCN is used.
     */
    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
        Result result = (Result) source;
        Class<? extends Object> type = Mapper.Null.class;
        if (result.holder != null)
            type = result.holder.getClass();

        String tag = _mapper.serializedClass(type);
        writer.startNode(tag);
        context.convertAnother(result.holder);
        writer.endNode();

        if (result.operationStatus == null) {
           LOG.debug(String.format("Null operationStatus for '%s'", tag));
        } else {
            writer.startNode("operationStatus");
            writer.setValue(result.operationStatus);
            writer.endNode();
        }

        if (result.errorMessage != null) {
            writer.startNode("errorMessage");
            writer.setValue(result.errorMessage);
            writer.endNode();
        }
    }

    /** 
     * Not Implemented at this time!
     * @see com.thoughtworks.xstream.converters.Converter#unmarshal(com.thoughtworks.xstream.io.HierarchicalStreamReader, com.thoughtworks.xstream.converters.UnmarshallingContext)
     */
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        return null;
    }

    /**
     * Standard method to determine which classes we can handle in conversion.
     */
    @SuppressWarnings("unchecked")
    public boolean canConvert(Class type) {
        return type.equals(Result.class);
    }

}