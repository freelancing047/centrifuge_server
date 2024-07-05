package csi.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FileNameConfig {
   private Map<String,String> orderMap;
   private List<String> orderKeys = null;

   public Map<String,String> getOrderMap() {
      return orderMap;
   }

   public List<String> getOrder() {
      return orderKeys;
   }

   public void setOrderMap(Map<String,String> orderMap) {
      this.orderMap = orderMap;

      if (orderKeys == null) {
         orderKeys = new ArrayList<String>();
      } else {
         orderKeys.clear();
      }
      if (orderMap != null) {
         orderKeys.addAll(orderMap.keySet());
      }
   }
}
