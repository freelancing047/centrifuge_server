package csi.server.business.service.chart.storage.postgres;

import java.util.List;
import java.util.function.Function;

import com.mongodb.DBObject;

public class DataToFullSelectionTransformer implements Function<DBObject,List<String>> {
//   private List<String> drills = null;

   public DataToFullSelectionTransformer(List<String> drills) {
//      this.drills = drills;
   }

   @Override
   public List<String> apply(DBObject table) {
      return (List<String>) table.get(ChartKeyConstants.CATEGORIES_KEY);
   }
}
