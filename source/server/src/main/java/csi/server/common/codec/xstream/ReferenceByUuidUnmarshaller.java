package csi.server.common.codec.xstream;

import java.util.HashMap;
import java.util.Map;

import com.sun.tools.ws.processor.model.ModelObject;
import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.ConverterLookup;
import com.thoughtworks.xstream.core.AbstractReferenceUnmarshaller;
import com.thoughtworks.xstream.core.util.FastStack;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.mapper.Mapper;

public class ReferenceByUuidUnmarshaller extends AbstractReferenceUnmarshaller {

    private Map values;
    private FastStack parentStack;
    private boolean isXml;

    public ReferenceByUuidUnmarshaller(Object root, HierarchicalStreamReader reader, ConverterLookup converterLookup, Mapper mapper, boolean isXml) {
        super(root, reader, converterLookup, mapper);
        values = new HashMap();
        parentStack = new FastStack(16);
        this.isXml = isXml;
    }

    protected Object convert(Object parent, Class type, Converter converter) {
        if (parentStack.size() > 0) {
            Object parentReferenceKey = parentStack.peek();
            if (parentReferenceKey != null && !values.containsKey(parentReferenceKey))
                values.put(parentReferenceKey, parent);
        }
        String attributeName = getMapper().aliasForSystemAttribute("reference");

        String reference = null;
        if (type.isAssignableFrom(ModelObject.class) && attributeName != null) {
            reference = attributeName != null ? reader.getAttribute(attributeName) : null;
        }

        Object result;
        if (reference != null) {
            result = values.get(getReferenceKey(reference));
            if (result == null) {
                ConversionException ex = new ConversionException("Invalid reference");
                ex.add("reference", reference);
                throw ex;
            }
        } else {
            Object currentReferenceKey = getCurrentReferenceKey();
            parentStack.push(currentReferenceKey);
            result = super.convert(parent, type, converter);
            if (currentReferenceKey != null && result != null) {
                values.put(currentReferenceKey, result);
            }
            parentStack.popSilently();
        }
        return result;
    }

    protected Object getReferenceKey(String reference) {
        return reference;
    }

    protected Object getCurrentReferenceKey() {
        String attributeName = getMapper().aliasForSystemAttribute("id");
        return attributeName != null ? reader.getAttribute(attributeName) : null;
    }
}
