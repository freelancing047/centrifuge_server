package csi.server.common.codec.xstream;

import java.util.UUID;

import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.ConverterLookup;
import com.thoughtworks.xstream.core.TreeMarshaller;
import com.thoughtworks.xstream.core.util.ObjectIdDictionary;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.io.path.Path;
import com.thoughtworks.xstream.io.path.PathTracker;
import com.thoughtworks.xstream.io.path.PathTrackingWriter;
import com.thoughtworks.xstream.mapper.Mapper;

import csi.server.common.model.ModelObject;

public class ReferenceByUuidMarshaller extends TreeMarshaller {

    private ObjectIdDictionary references;
    private ObjectIdDictionary implicitElements;
    private PathTracker pathTracker;
    private Path lastPath;
    private boolean isXml;

    public static class ReferencedImplicitElementException extends ConversionException {

        /**
         * @deprecated Method ReferencedImplicitElementException is deprecated
         */

        public ReferencedImplicitElementException(String msg) {
            super(msg);
        }

        public ReferencedImplicitElementException(Object item, Path path) {
            super("Cannot reference implicit element");
            add("implicit-element", item.toString());
            add("referencing-element", path.toString());
        }
    }

    public ReferenceByUuidMarshaller(HierarchicalStreamWriter writer, ConverterLookup converterLookup, Mapper mapper, boolean isXml) {
        super(writer, converterLookup, mapper);
        references = new ObjectIdDictionary();
        implicitElements = new ObjectIdDictionary();
        pathTracker = new PathTracker();
        this.writer = new PathTrackingWriter(writer, pathTracker);
        this.isXml = isXml;
    }

    public void convert(Object item, Converter converter) {
        if (getMapper().isImmutableValueType(item.getClass()) || !(item instanceof ModelObject)) {
            converter.marshal(item, writer, this);
        } else {
            Path currentPath = pathTracker.getPath();
            Object existingReferenceKey = references.lookupId(item);
            if (existingReferenceKey != null) {
                String attributeName = getMapper().aliasForSystemAttribute("reference");
                if (attributeName != null) {
                    writer.addAttribute(attributeName, createReference(currentPath, existingReferenceKey));
                }
            } else {
                if (implicitElements.lookupId(item) != null)
                    throw new ReferencedImplicitElementException(item, currentPath);
                Object newReferenceKey = createReferenceKey(currentPath, item);
                if (lastPath == null || !currentPath.isAncestor(lastPath)) {
                    fireValidReference(newReferenceKey);
                    lastPath = currentPath;
                    references.associateId(item, newReferenceKey);
                } else {
                    implicitElements.associateId(item, newReferenceKey);
                }
                converter.marshal(item, writer, this);
            }
        }
    }

    protected String createReference(Path currentPath, Object existingReferenceKey) {
        return existingReferenceKey.toString();
    }

    protected Object createReferenceKey(Path currentPath, Object item) {
        if (item instanceof ModelObject) {
            return ((ModelObject) item).getUuid();
        } else {
            return UUID.randomUUID();
        }

        // return "o_" + UUID.randomUUID().toString();
    }

    protected void fireValidReference(Object referenceKey) {
        if (referenceKey != null) {
            String attributeName = getMapper().aliasForSystemAttribute("id");
            if (attributeName != null) {
                writer.addAttribute(attributeName, referenceKey.toString());
            }
        }
    }

}
