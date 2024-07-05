package csi.server.common.codec.xstream.converter;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

import csi.server.dao.jpa.xml.XMLSerializedEntityXStreamFactory;
import csi.shared.core.color.ColorModel;
import csi.shared.core.color.SingleColorModel;

/**
 * @author Centrifuge Systems, Inc.
 */
public class MatrixColorConverter implements Converter {



    @Override
    public void marshal(Object o, HierarchicalStreamWriter hierarchicalStreamWriter, MarshallingContext marshallingContext) {
        try {
            new XMLSerializedEntityXStreamFactory().afterPropertiesSet();
        } catch (Exception e) {
            e.printStackTrace();
        }

        XMLSerializedEntityXStreamFactory.get().marshal(o, hierarchicalStreamWriter);
    }

    @Override
    public Object unmarshal(HierarchicalStreamReader hierarchicalStreamReader, UnmarshallingContext unmarshallingContext) {
        SingleColorModel singleColorModel = new SingleColorModel();
        hierarchicalStreamReader.moveDown();
        singleColorModel.setColor(hierarchicalStreamReader.getValue());
        hierarchicalStreamReader.moveUp();
        return singleColorModel;
    }

    @Override
    public boolean canConvert(Class aClass) {
        return ColorModel.class.isAssignableFrom(aClass);
    }
}
