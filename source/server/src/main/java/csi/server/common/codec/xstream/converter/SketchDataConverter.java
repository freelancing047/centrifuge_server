package csi.server.common.codec.xstream.converter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class SketchDataConverter implements Converter {

    public boolean canConvert(Class clazz) {
        return clazz.equals(ArrayList.class);
    }

    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {

        ArrayList<ArrayList<String[]>> fullValues = (ArrayList<ArrayList<String[]>>) source;

        for (ArrayList<String[]> value : fullValues) {
            writer.startNode("row");
            for (String[] keyValuesPair : value) {
                writer.startNode(keyValuesPair[0]);
                writer.setValue(String.valueOf(keyValuesPair[1]));
                writer.endNode();
            }
            writer.endNode();
        }

    }

    // TODO: implement unmarshall

    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        Map map = new HashMap();

        return map;
    }

}
