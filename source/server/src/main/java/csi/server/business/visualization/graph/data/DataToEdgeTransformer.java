package csi.server.business.visualization.graph.data;

import java.util.Map;
import java.util.function.Function;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import csi.graph.GraphStorage;
import csi.graph.mongo.Helper;
import csi.server.business.visualization.graph.base.LinkStore;
import csi.server.business.visualization.graph.base.ObjectAttributes;

public class DataToEdgeTransformer implements Function<DBObject,LinkStore> {
   private GraphStorage graphStorage;

   @Override
   public LinkStore apply(DBObject data) {
      LinkStore store = new LinkStore();

      readInternalAttributes(data, store);
      Data.readNonInternalAttributes(data, store, graphStorage);
      return store;
   }

   private void readInternalAttributes(DBObject data, LinkStore store) {
      Data.readDocId(data, store);
      Data.readId(data, store);
      Data.readLabel(data, store);
      Data.readTypes(data, store, graphStorage);

      if (data.containsField(Helper.INTERNAL)) {
         BasicDBObject wrapper = new BasicDBObject();
         wrapper.putAll((Map) data.get(Helper.INTERNAL));

         Data.readDefCounts(wrapper, store, graphStorage);

         if (wrapper.containsField(Data.Hidden)) {
            store.setHidden(wrapper.getBoolean(Data.Hidden));
         }

         if (wrapper.containsField(Data.Style)) {
            store.setStyle(wrapper.getString(Data.Style));
         }

         if (wrapper.containsField(Data.Width)) {
            store.setWidth(wrapper.getDouble(Data.Width));
         }

         if (wrapper.containsField(Data.Scale)) {
            store.setScale(wrapper.getInt(Data.Scale));
         }

         if (wrapper.containsField(Data.CountForward)) {
            store.setCountForward(wrapper.getInt(Data.CountForward));
         }

         if (wrapper.containsField(Data.CountReverse)) {
            store.setCountReverse(wrapper.getInt(Data.CountReverse));
         }

         if (wrapper.containsField(Data.CountNone)) {
            store.setCountNone(wrapper.getInt(Data.CountNone));
         }

         if (wrapper.containsField(Data.Hidden)) {
            store.setHidden(wrapper.getBoolean(Data.Hidden));
         }

         if (wrapper.containsField(Data.Color)) {
            store.setColor(wrapper.getInt(Data.Color));
         }

         if (wrapper.containsField(Data.SpecId)) {
            store.setSpecID(wrapper.getString(Data.SpecId));
         }

         if (wrapper.containsField(Data.Document)) {
            Data.readPropertyValue(wrapper, store, ObjectAttributes.CSI_INTERNAL_DOCUMENT, Data.Document, graphStorage);
         }

         if (wrapper.containsField(Data.URL)) {
            Data.readPropertyValue(wrapper, store, ObjectAttributes.CSI_INTERNAL_URL, Data.Document, graphStorage);
         }

         if (wrapper.containsField(Data.PrimaryType)) {
            store.setType(wrapper.getString(Data.PrimaryType));
         }

         if (wrapper.containsField(Data.Visualized)) {
            store.setVisualized(wrapper.getBoolean(Data.Visualized));
         }

         if (wrapper.containsField(Data.SizeMode)) {
            store.setSizeMode(wrapper.getInt(Data.SizeMode));
         }
         if (wrapper.containsField(Data.Transparency)) {
            double size = wrapper.getDouble(Data.Transparency);
            store.setTransparency((int) size);
         }
         if (wrapper.containsField(Data.Plunked)) {
            store.setPlunked(wrapper.getBoolean(Data.Plunked));
         }
         if (wrapper.containsField(Data.HideLabels)) {
            store.setHideLabels(wrapper.getBoolean(Data.HideLabels));
         }
      }
   }

   public void setGraphStorage(GraphStorage graphStorage) {
      this.graphStorage = graphStorage;
   }
}
