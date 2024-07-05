package csi.server.common.codec.xstream.converter;

import java.util.HashMap;
import java.util.Map;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * Provides a converter for generic Maps. By default XStream only converts
 * specific concrete types. This causes issues for JPA where we deal with vendor
 * specific collection classes that enable lazy loading of data. We'll handle
 * the generic java.util.Map interface here to avoid XStream dropping into
 * reflection based serialization!
 * <p/>
 */
public class SimpleMapConverter implements Converter {
   public boolean canConvert(Class clazz) {
      return clazz.equals(HashMap.class);
   }

   public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
      Map<String,Object> map = (Map<String,Object>) source;

      for (Map.Entry<String,Object> entry : map.entrySet()) {
         writer.startNode(entry.getKey());
         writer.setValue(String.valueOf(entry.getValue()));
         writer.endNode();
      }
   }

   // TODO: implement unmarshall

   public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
      return new HashMap();
   }
}
