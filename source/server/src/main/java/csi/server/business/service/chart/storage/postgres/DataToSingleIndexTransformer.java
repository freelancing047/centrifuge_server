package csi.server.business.service.chart.storage.postgres;

import java.util.List;
import java.util.function.Function;

import com.mongodb.DBObject;

public class DataToSingleIndexTransformer implements Function<DBObject,Integer> {
   private String query = null;

   public DataToSingleIndexTransformer(String query) {
      this.query = query;
   }

   @Override
   public Integer apply(DBObject table) {
      List<String> categories = (List<String>) table.get(ChartKeyConstants.CATEGORIES_KEY);
      int count = 0;

      for (String category : categories) {
         if (category.equals(query)) {
            break;
         }
         count++;
      }
      return Integer.valueOf(count);
   }
}
