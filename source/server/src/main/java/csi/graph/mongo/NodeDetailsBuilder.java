package csi.graph.mongo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBObject;

import csi.server.business.visualization.graph.base.NodeStore;
import csi.server.business.visualization.graph.base.property.Property;
import csi.server.common.model.visualization.graph.GraphConstants;

public class NodeDetailsBuilder {
   private static final String               LABEL       = "label";
   private static final String               TYPE        = "type";
   private static final String               NAME        = "name";
   private static final String               COUNT       = "count";
   private static final String               DEFS        = "defs";
   private static final String               HIDDEN      = "hidden";
   private static final String               ICON        = "icon";
   private static final String               SHAPE       = "shape";
   private static final String               HIDE_LABELS = "hideLabels";
   private static final String               ANCHORED    = "anchored";
   private static final String               SCALE       = "scale";
   private static final String               REL_SIZE    = "relSize";
   private static final String               COLOR       = "color";

   private static NodeDetailsBuilder singleton = new NodeDetailsBuilder();

   static NodeDetailsBuilder instance() {
      return singleton;
   }

   public NodeStore from(DBObject data) {
      NodeStore details = new NodeStore();

      if (data != null) {
         for (String key : details.getAttributes().keySet()) {
            if (!isInternalKey(key)) {
               Object o = data.get(key);
               Property prop = new Property(key);

               if (o instanceof Collection) {
                  prop.getValues().addAll((Collection<?>) o);
               } else {
                  prop.getValues().add(o);
               }
            }
         }
         if (data.containsField(TYPE)) {
            BasicDBList types = (BasicDBList) data.get(TYPE);
            String primaryType = null;
            int highestCount = 0;

            for (Object o : types) {
               DBObject type = (DBObject) o;
               String name = (String) type.get(NAME);
               Integer count = (Integer) type.get(COUNT);

               if (count.intValue() > highestCount) {
                  primaryType = name;
                  highestCount = count.intValue();
               }
               details.getTypes().put(name, count);
            }
            // set the type to be the one w/w the highest count.
            if (primaryType != null) {
               details.setType(primaryType);
            }
         }
         DBObject container = (DBObject) data.get(Helper.INTERNAL);

         if (container != null) {
            if (container.containsField(DEFS)) {
               BasicDBList list = (BasicDBList) container.get(DEFS);
               Map<String, List<Integer>> rows = details.getRows();

               for (Object o : list) {
                  DBObject def = (DBObject) o;
                  rows.put((String) def.get(NAME), new ArrayList<Integer>());
               }
            }
            buildInternalsFrom(details, container);
         }
         details.setKey((String) data.get(Helper.APP_ID));

         if (data.containsField(LABEL)) {
            details.addLabel((String) data.get(LABEL));
         }
      }
      return details;
   }

   public DBObject to(NodeStore details) {
      BasicDBObjectBuilder builder = BasicDBObjectBuilder.start();

      if (details.getKey() != null) {
         builder.append(Helper.APP_ID, details.getKey());
      }
      if (details.getLabel() != null) {
         builder.append(LABEL, details.getLabel());
      }
      BasicDBObjectBuilder typeBuilder;
      BasicDBList typeList = new BasicDBList();

      for (Map.Entry<String,Integer> entry : details.getTypes().entrySet()) {
         typeBuilder = BasicDBObjectBuilder.start();

         typeBuilder.add(NAME, entry.getKey());
         typeBuilder.add(COUNT, entry.getValue());
         typeList.add(typeBuilder.get());
      }
      builder.add(TYPE, typeList);

      Map<String, List<Integer>> rows = details.getRows();

      builder.push(Helper.INTERNAL);
      {
         buildDefs(builder, rows.keySet(), rows);
         buildInternalsTo(builder, details);
      }
      builder.pop();

      for (Map.Entry<String, Property> entry : details.getAttributes().entrySet()) {
         String key = entry.getKey();

         if (!isInternalKey(key)) {
            List<Object> values = entry.getValue().getValues();

            switch (values.size()) {
               case 0:
                  break;
               case 1:
                  builder.add(key, values.get(0));
                  break;
               default:
                  BasicDBList list = new BasicDBList();

                  list.addAll(values);
                  builder.add(key, list);
                  break;
            }
         }
      }
      return builder.get();
   }

   private static void buildInternalsTo(BasicDBObjectBuilder builder, NodeStore details) {
      if (details.isHidden()) {
         builder.add(HIDDEN, Boolean.TRUE);
      }
      if (details.isHideLabels()) {
         builder.add(HIDE_LABELS, Boolean.TRUE);
      }
      if (details.isAnchored()) {
         builder.add(ANCHORED, Boolean.TRUE);
      }
      if (details.getScale() != 1) {
         builder.add(SCALE, Integer.valueOf(details.getScale()));
      }
      if (BigDecimal.valueOf(details.getRelativeSize()).compareTo(BigDecimal.ONE) != 0) {
         builder.add(REL_SIZE, Double.valueOf(details.getRelativeSize()));
      }
      for (Map.Entry<String,Property> entry : details.getAttributes().entrySet()) {
         String key = entry.getKey();

         if (key.startsWith(GraphConstants.CSI_INTERNAL_NAMESPACE)) {
            String stripped = key.substring(GraphConstants.CSI_INTERNAL_NAMESPACE.length() + 1);
            Object value = entry.getValue().getValues().get(0);

            builder.add(stripped.toLowerCase(), value);
         }
      }
      if (details.getIcon() != null) {
         builder.add(ICON, details.getIcon());
      }
      if (details.getShape() != null) {
         builder.add(SHAPE, details.getShape());
      }
      if (details.getColor() != null) {
         builder.add(COLOR, details.getColor());
      }
      // TODO: capture bundling data here.
   }

   private static void buildInternalsFrom(NodeStore details, DBObject data) {
      BasicDBObject wrapper = new BasicDBObject();

      wrapper.putAll(data);

      if (wrapper.containsField(HIDDEN)) {
         details.setHidden(wrapper.getBoolean(HIDDEN));
      }
      if (wrapper.containsField(HIDE_LABELS)) {
         details.setHideLabels(wrapper.getBoolean(HIDE_LABELS));
      }
      if (wrapper.containsField(ANCHORED)) {
         details.setAnchored(wrapper.getBoolean(ANCHORED));
      }
      if (wrapper.containsField(SCALE)) {
         details.setScale(wrapper.getInt(SCALE));
      }
      if (wrapper.containsField(REL_SIZE)) {
         details.setRelativeSize(wrapper.getDouble(REL_SIZE));
      }
      if (wrapper.containsField(ICON)) {
         details.setIcon(wrapper.getString(ICON));
      }
      if (wrapper.containsField(SHAPE)) {
         details.setShape(wrapper.getString(SHAPE));
      }
      if (wrapper.containsField(COLOR)) {
         details.setColor(Integer.valueOf(wrapper.getInt(COLOR)));
      }
   }

   protected static void buildDefs(BasicDBObjectBuilder builder, Set<String> keys, Map<String,List<Integer>> rows) {
      BasicDBList defList = new BasicDBList();
      BasicDBObjectBuilder defBuilder;

      for (String key : keys) {
         defBuilder = BasicDBObjectBuilder.start();

         List<Integer> specRows = rows.get(key);

         defBuilder.add(NAME, key);
         defBuilder.add(COUNT, Integer.valueOf(specRows.size()));
         defList.add(defBuilder.get());
      }
      builder.add(DEFS, defList);
   }

   private static boolean isInternalKey(String key) {
      return (key == null) || Helper.isRestrictedKey(key) || LABEL.equals(key);
   }
}
