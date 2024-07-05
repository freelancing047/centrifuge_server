package csi.server.business.service.chart.storage.postgres;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import com.mongodb.DBObject;

public class DataToCategoryListResultQueryTransformer implements Function<DBObject,List<String>> {
   private int maxCategories = 0;
   private String query = null;

   public DataToCategoryListResultQueryTransformer(int maxCategories, String query) {
      this.maxCategories = maxCategories;
      this.query = query;
   }

   @Override
   public List<String> apply(DBObject table) {
      List<String> queriedCategories = new ArrayList<String>();
      List<String> categories = (List<String>) table.get(ChartKeyConstants.CATEGORIES_KEY);
      int count = 0;

      for (String category : categories) {
         if (category.toLowerCase().startsWith(query.toLowerCase())) {
            queriedCategories.add(category);
            count++;

            if (count >= maxCategories) {
               break;
            }
         }
      }
      return queriedCategories;
   }
}
