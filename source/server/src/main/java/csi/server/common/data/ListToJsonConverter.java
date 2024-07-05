package csi.server.common.data;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

@Converter(autoApply = false)
public class ListToJsonConverter implements AttributeConverter<List<String>,String> {
   private static final Logger LOG = LogManager.getLogger(ListToJsonConverter.class);

   @SuppressWarnings("serial")
   private TypeToken<ArrayList<String>> typeToken = new TypeToken<ArrayList<String>>() {
   };

   @Override
   public String convertToDatabaseColumn(List<String> attributes) {
      return ((attributes == null) || attributes.isEmpty()) ? "" : new Gson().toJson(attributes);
   }

   @Override
   public List<String> convertToEntityAttribute(String serialString) {
      List<String> result = new ArrayList<String>();

      if ((serialString != null) && !serialString.isEmpty()) {
         try {
            result = new Gson().fromJson(serialString, typeToken.getType());
         } catch (Exception exception) {
            LOG.error(exception);
         }
      }
      return result;
   }
}
