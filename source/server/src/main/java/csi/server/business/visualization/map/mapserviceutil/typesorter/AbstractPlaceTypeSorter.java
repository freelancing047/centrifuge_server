package csi.server.business.visualization.map.mapserviceutil.typesorter;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import csi.server.business.visualization.map.MapServiceUtil;
import csi.server.business.visualization.map.PlaceDynamicTypeInfo;
import csi.server.common.model.map.PlaceidTypenameDuple;

public abstract class AbstractPlaceTypeSorter {
   protected List<PlaceidTypenameDuple> keys;
   protected PlaceDynamicTypeInfo dynamicTypeInfo;
   protected int id;
   Set<PlaceidTypenameDuple> dynamicTypeInfoTypeNames;

   public AbstractPlaceTypeSorter(List<PlaceidTypenameDuple> keys) {
      this.keys = keys;
   }

   public abstract void sort();

   void applyToDynamicTypenameCache(Iterator<PlaceidTypenameDuple> keys) {
      while (keys.hasNext()) {
         PlaceidTypenameDuple key = keys.next();
         MapServiceUtil.applyTypenameToTypeInfo(dynamicTypeInfo, key, id);
         id++;
      }
   }
}
