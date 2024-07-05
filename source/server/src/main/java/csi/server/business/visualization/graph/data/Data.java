package csi.server.business.visualization.graph.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

import org.bson.types.ObjectId;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBObject;

import prefuse.data.Node;
import prefuse.data.Tuple;

import csi.graph.GraphStorage;
import csi.graph.mongo.Helper;
import csi.server.business.visualization.graph.GraphContext;
import csi.server.business.visualization.graph.base.AbstractGraphObjectStore;
import csi.server.business.visualization.graph.base.NodeStore;
import csi.server.business.visualization.graph.base.ObjectAttributes;
import csi.server.business.visualization.graph.base.property.AggregateProperty;
import csi.server.business.visualization.graph.base.property.ComputedProperty;
import csi.server.business.visualization.graph.base.property.Property;
import csi.server.common.model.attribute.AttributeAggregateType;
import csi.server.common.model.visualization.graph.GraphConstants;
import csi.shared.gwt.viz.graph.LinkDirection;

public class Data {
   private static final Pattern DOT_PATTERN = Pattern.compile("\\.");

    public static final String       Label         = "label";
    public static final String       Type          = "type";
    public static final String       Name          = "name";
    public static final String       Count         = "count";
    public static final String       Defs          = "defs";
    public static final String       Hidden        = "hidden";
    public static final String       Visualized    = "vized";
    public static final String       Icon          = "icon";
    public static final String       Shape         = "shape";
    public static final String       HideLabels    = "hideLabels";
    public static final String       Bundle        = "bundle";
    public static final String       Key           = "key";
    public static final String       Anchored      = "anchored";
    public static final String       Scale         = "scale";
    public static final String       RelativeSize  = "relSize";
    public static final String       Color         = "color";
    public static final String       SpecId        = "specId";
    public static final String       Position      = "pos";
    public static final String       X             = "x";
    public static final String       Y             = "y";
    public static final String       Plunked       = "plunked";
    public static final String       Annotation    = "annotation";

    static final String              Document      = "Document";
    static final String              URL           = "URL";
    private static final String      Rows          = "rows";
    static final String              Style         = "style";
    static final String              Width         = "width";
    static final String              CountForward  = "f";
    static final String              CountReverse  = "r";
    static final String              CountNone     = "n";
    static final String              PrimaryType   = "pType";

    public static final String       Parent        = "parent";
    public static final String       Children      = "children";
    public static final String       Size          = "size";
    public static final String       Transparency  = "transparency";
    public static final String       SizeMode      = "sizeMode";

    static final String              EdgeDirection = "edir";

    public static final List<String> Internals;
    private static final String      ToolTipFlags  = "ttf";
    public static final String       Value         = "v";
    public static final String       Spanned       = "_spanned";
    public static final String       ValueType     = "vt";
    private static final String      Aggregate     = "aggregate";

    public static enum valueTypes { TIME, DATE }

    static {
        Internals = new ArrayList<String>();
        Internals.add(Label);
        Internals.add(Type);
        Internals.add(Helper.INTERNAL);
        Internals.add(Helper.APP_ID);
        Internals.add(Helper.DOC_ID);
    }

    public static boolean isInternalKey(String s) {
        if (s == null) {
            return false;
        }

        s = s.toLowerCase();

        boolean flag = Internals.contains(s);
        if (!flag) {
            // need to check our persistence key and configuration keys
            // TODO: need to coalesce to just one internal key.
            flag = s.startsWith(Helper.INTERNAL) || s.startsWith(GraphConstants.CSI_INTERNAL_NAMESPACE);
        }

        return flag;
    }

    public static String getSimpleKey(String value) {
        String s = value;

        if( isInternalKey(s)) {
            int index = value.lastIndexOf('.');
            if (index != -1) {
                s = value.substring(index + 1);
            }
        } else {
            s = DOT_PATTERN.matcher(s).replaceAll("_");
        }
        return s.trim();
    }

   static boolean useSingleValue(String key) {
      return ObjectAttributes.CSI_INTERNAL_SIZE.equals(key);
   }

    @SuppressWarnings("rawtypes")
    static void addAttributes(BasicDBObjectBuilder builder, AbstractGraphObjectStore details, boolean onlyInternal, GraphStorage storage) {
        Map<String, Property> attrs = details.getAttributes();
        Set<String> keys = attrs.keySet();
        for (String key : keys) {
            if (key == null) {
                continue;
            }

            boolean isInternal = isInternalKey(key);
            // include only if we have the type of key we want.
            if (isInternal == onlyInternal) {

                String updatedKey = getSimpleKey(key);
                if (key.length() == 0) {
                    continue;
                }

                Property prop = attrs.get(key);
                boolean tooltip = prop.isIncludeInTooltip();
                boolean excludeEmpty = prop.isHideEmptyInTooltip();

                List<Object> values = prop.getValues();

                boolean singleValueOnly = useSingleValue(key);

                BasicDBObjectBuilder propBuilder = BasicDBObjectBuilder.start();

                // only write out the flags if one of them isn't the default...
                if (!tooltip || excludeEmpty) {
                    ArrayList tipFlags = new ArrayList(2);
                    tipFlags.add(excludeEmpty);
                    tipFlags.add(tooltip);
                    propBuilder.add(Data.ToolTipFlags, tipFlags);
                }

                if (values.isEmpty()) {
                    // no-op.
                } else if ((values.size() == 1) || singleValueOnly) {
                    propBuilder.add(Data.Value, values.get(0));
                } else {
                    BasicDBList list = new BasicDBList();
                    if (values.size() > Helper.SPAN_MAX) {
                    	propBuilder.add(Data.Value+Data.Spanned, "true");
                    	createSpannedArray(list, values, storage);
                    }
                    else {
                        list.addAll(values);
                    }
                    propBuilder.add(Data.Value, list);
                }

                if (prop instanceof AggregateProperty) {
                    AggregateProperty aggregateProperty = (AggregateProperty)prop;
                    Property nestedProperty = aggregateProperty.getProperty();
                    if (nestedProperty instanceof ComputedProperty) {
                        ComputedProperty cp = (ComputedProperty) nestedProperty;
                        propBuilder.add(Data.Aggregate, cp.getType().name());
                    }
                }

                builder.add(updatedKey, propBuilder.get());
            }
        }

    }

    private static void createSpannedArray(BasicDBList list, List<?> values, GraphStorage storage) {
    	int cnt = values.size();
    	int j;
    	for (int i = 0;i < cnt;) {
    		j = (i + Helper.SPAN_MAX)-1;
    		if (j >= cnt) {
    			j = cnt -1;
    		}
    		List<?> slist = values.subList(i, j);
    		String uuid = UUID.randomUUID().toString();
    		BasicDBObjectBuilder ref = BasicDBObjectBuilder.start();
    		ref.add(Helper.DOC_ID, uuid);
    		ref.add(Count, Integer.valueOf(cnt));
    		list.add(ref.get());
    		BasicDBObjectBuilder doc = BasicDBObjectBuilder.start();
    		doc.add(Helper.DOC_ID, uuid);
    		doc.add(Spanned, slist);
    		storage.addSpanned(doc.get());
    		i += Helper.SPAN_MAX-1;
    	}
    }

    private static List<?> getSpannedArray(BasicDBList list, GraphStorage storage) {
    	Iterator itr = list.iterator();
    	Object[] wrk = null;
    	boolean first = true;
    	int i = 0;
    	while (itr.hasNext()) {
    		DBObject obj = (DBObject)itr.next();
    		if (first) {
    			Integer cnt = (Integer) obj.get(Count);
    			wrk = new Object[cnt];
    			first = false;
    		}
    		DBObject span = storage.findSpanned(obj);
    		List<Object> sarray = (List<Object>) span.get(Spanned);
    		int howMany = sarray.size();

    		for (int j = 0; j < howMany; j++) {
    			wrk[i++] = sarray.get(j);
    		}
    	}
    	return Arrays.asList(wrk);
    }

    private static void createSpannedMap(BasicDBList list, Map<String, Integer> map, GraphStorage storage) {
        Set<String> keys = map.keySet();
        int span = 0;
        BasicDBList typeList = null;
        String uuid = null;
        int cnt = keys.size();
        for (String key : keys) {
        	if (span == 0) {
        		typeList = new BasicDBList();
        		uuid = UUID.randomUUID().toString();
                BasicDBObjectBuilder ref = BasicDBObjectBuilder.start();
                ref.add(Helper.DOC_ID, uuid);
                ref.add(Count, Integer.valueOf(cnt));
        		list.add(ref.get());
        	}
        	createMapEntries(typeList, map, key);
        	span++;
        	if (span > Helper.SPAN_MAX) {
        		span = 0;
                BasicDBObjectBuilder doc = BasicDBObjectBuilder.start();
        		doc.add(Helper.DOC_ID, uuid);
        		doc.add(Spanned, typeList);
        		storage.addSpanned(doc.get());
        		typeList = new BasicDBList();
        	}
        }
    }

    private static void getSpannedMap(BasicDBList list, BasicDBList map, GraphStorage storage) {
    	Iterator itr = list.iterator();
    	while (itr.hasNext()) {
    		DBObject obj = (DBObject) itr.next();
    		DBObject span = storage.findSpanned(obj);
    		BasicDBList slist = (BasicDBList) span.get(Spanned);
    		map.addAll(slist);
    	}

    }

    private static void createMapEntries(BasicDBList typeList, Map<String, Integer> map, String key) {
        BasicDBObjectBuilder typeBuilder = BasicDBObjectBuilder.start();
        typeBuilder.add(Name, key);
        typeBuilder.add(Count, map.get(key));
        typeList.add(typeBuilder.get());
    }

    static void removeRedundantAttributes(BasicDBObjectBuilder builder) {
        DBObject dbObject = builder.get();
        DBObject data = Helper.getInternalPayload(dbObject);

        Set<String> toRemove = new HashSet<String>();
        Set<String> keySet = data.keySet();
        for (String key : keySet) {
            if (Type.equalsIgnoreCase(key) || Label.equalsIgnoreCase(key) || Width.equalsIgnoreCase(key)) {
                toRemove.add(key);
            }
        }

        for (String key : toRemove) {
            data.removeField(key);
        }

    }

   static ObjectId getNodeDocId(Node node) {
      return (ObjectId) node.get(GraphConstants.DOC_ID);
   }

    static void buildTypes(BasicDBObjectBuilder builder, AbstractGraphObjectStore details, GraphStorage storage) {
        Map<String, Integer> map = details.getTypes();
        BasicDBList typeList = new BasicDBList();
        if (map.size() > Helper.SPAN_MAX) {
        	builder.add(Type+Spanned, "true");
        	createSpannedMap(typeList, map, storage);
        }
        else {
            Set<String> keys = map.keySet();
            for (String key : keys) {
            	createMapEntries(typeList, map, key);
            }
        }

        builder.add(Type, typeList);
    }

    static void addDefCounts(BasicDBObjectBuilder builder, AbstractGraphObjectStore details, GraphStorage storage) {
        Map<String, List<Integer>> rows = details.getRows();
        Set<String> keys = rows.keySet();
        BasicDBList defList = new BasicDBList();
        BasicDBObjectBuilder defBuilder;
        for (String key : keys) {
            defBuilder = BasicDBObjectBuilder.start();
            List<Integer> specRows = rows.get(key);
            defBuilder.add(Name, key);
            defBuilder.add(Count, specRows.size());
            if (specRows.size() > Helper.SPAN_MAX) {
            	defBuilder.add(Rows+Spanned, "true");
            	BasicDBList list = new BasicDBList();
            	createSpannedArray(list, specRows, storage);
            	defBuilder.add(Rows, list);
            }
            else {
            	defBuilder.add(Rows, specRows);
            }
            defList.add(defBuilder.get());
        }

        builder.add(Defs, defList);
    }

    static void readDefCounts(DBObject data, AbstractGraphObjectStore details, GraphStorage storage) {
        if (data.containsField(Defs)) {

            Map<String, List<Integer>> internalRows = new HashMap<String, List<Integer>>();
            BasicDBList defs = (BasicDBList) data.get(Defs);
            Iterator iterator = defs.iterator();
            while (iterator.hasNext()) {
                DBObject def = (DBObject) iterator.next();
                String key = (String) def.get(Name);
                BasicDBList dbRows = null;
                Object dbObject = def.get(Rows);
                if (dbObject instanceof ArrayList<?>) {
                	dbRows = new BasicDBList();
                	dbRows.addAll((ArrayList)dbObject);
                } else if (dbObject instanceof BasicDBList) {
                	dbRows = (BasicDBList) dbObject;
                }
                if (dbRows == null) {
                    continue;
                }
                List<Integer> rows;
                if (def.containsField(Rows+Spanned)) {
                	rows = (List<Integer>)getSpannedArray(dbRows, storage);
                }
                else {
	                rows = new ArrayList<Integer>(dbRows.size());
	                Iterator rowIt = dbRows.iterator();
	                while (rowIt.hasNext()) {
	                    Number row = (Number) rowIt.next();
	                    rows.add(row.intValue());
	                }
                }

                internalRows.put(key, rows);
            }

            details.setRows(internalRows);
        }
    }

    static void readNonInternalAttributes(DBObject data, AbstractGraphObjectStore store, GraphStorage storage) {
        Map<String, Property> attributes = store.getAttributes();
        Set<String> keys = data.keySet();
        for (String key : keys) {
            if (Internals.contains(key)) {
                continue;
            }

            String originalKey = getOriginalKey(key);
            String aggregateType = null;
            DBObject wrapper = (DBObject) data.get(key);
            aggregateType = (String)wrapper.get(Data.Aggregate);
            //Create the appropriate type of property object
            Property prop = (aggregateType == null) ? new Property(originalKey) : new ComputedProperty(originalKey);

            if (wrapper.containsField(Data.ToolTipFlags)) {
                List flags = (List) wrapper.get(Data.ToolTipFlags);
                boolean flag = (Boolean) flags.get(0);
                prop.setHideEmptyInTooltip(flag);

                flag = (Boolean) flags.get(1);
                prop.setIncludeInTooltip(flag);
            }

            if( wrapper.containsField(Data.Value)) {
                Object vals = wrapper.get(Data.Value);
            	if (wrapper.containsField(Data.Value+Data.Spanned)) {
            		List array = getSpannedArray((BasicDBList) vals, storage);
            		prop.getValues().addAll(array);
            	}
            	else {
	                if( vals instanceof List) {
	                    prop.getValues().addAll((List)vals);
	                } else {
	                    prop.getValues().add(vals);
	                }
            	}
            }
            if (aggregateType != null) {
                ComputedProperty cp = (ComputedProperty)prop;
                cp.setType(AttributeAggregateType.valueOf(aggregateType));
                prop = buildAggregateProperty(cp, cp.getType());
            }
            attributes.put(originalKey, prop);
        }
    }

    static AggregateProperty buildAggregateProperty(ComputedProperty computed, AttributeAggregateType computedKind) {
        computed.setType(computedKind);

        AggregateProperty aggregate = new AggregateProperty(computed.getName());
        aggregate.setValue((Double) computed.getValues().get(0));
        aggregate.setProperty(computed);

        return aggregate;

    }

    public static String[] linkDirections = new String[] {
        LinkDirection.REVERSE.name(),
        LinkDirection.FORWARD.name(),
        LinkDirection.BOTH.name(),
        LinkDirection.NONE.name(),
        LinkDirection.DYNAMIC.name(),
    };

   private static String getOriginalKey(String key) {
      StringBuilder originalKey = new StringBuilder(key);

      for (String linkDirection : linkDirections) {
         int indx = key.indexOf("_"+linkDirection);

         if (indx > -1) {
            originalKey.setCharAt(indx, '.');
            break;
         }
      }
      return originalKey.toString();
   }

    static void readDocId(DBObject data, AbstractGraphObjectStore store) {
        if (data.containsField(Helper.DOC_ID)) {
            store.setDocId(data.get(Helper.DOC_ID));
        }
    }

    static void readId(DBObject data, AbstractGraphObjectStore store) {
        String key = (String) data.get(Helper.APP_ID);
        if ((key != null) && (key.length() != 0)) {
            store.setKey(key);
        }
    }

    static void readLabel(DBObject data, AbstractGraphObjectStore store) {
        String label = (String) data.get(Label);
        String[] labels = label.split(";");
        for (String oneLabel : labels ) {
         store.addLabel(oneLabel.trim());
      }
    }

    static void readTypes(DBObject data, AbstractGraphObjectStore store, GraphStorage storage) {
        Map<String, Integer> typeMap = store.getTypes();

        String primaryType = null;
        int maxCount = 0;

        BasicDBList types = (BasicDBList)data.get(Type);
        if (data.containsField(Type+Spanned)) {
        	BasicDBList list = new BasicDBList();
        	getSpannedMap(types, list, storage);
        	types = list;
        }
        else {
        	types = (BasicDBList) data.get(Type);
        }
        if (types == null) {
            return;
        }
        Iterator itr = types.iterator();
        while (itr.hasNext()) {
        	DBObject info = (DBObject) itr.next();
            String typeName = (String) info.get(Name);
            Number count = (Number) info.get(Count);
            if (count.intValue() > maxCount) {
                primaryType = typeName;
            }
            typeMap.put(typeName, count.intValue());
        }

        if (primaryType != null) {
            store.setType(primaryType);
        }

    }

    static String resolveAppId(NodeStore details) {
        String key = details.getKey();
        if (key == null) {
            key = details.getLabel();
        }

        return key;
    }

    public static void readPropertyValue(BasicDBObject data, AbstractGraphObjectStore details, String name, String alias, GraphStorage storage) {
        Map<String, Property> attrs = details.getAttributes();
        Property prop = attrs.get(name);
        if (prop == null) {
            prop = new Property(name);
            attrs.put(name, prop);
        }

        DBObject wrapper = (DBObject) data.get(alias);
        if (wrapper.containsField(Data.ToolTipFlags)) {
            boolean flag;

            List flags = (List) wrapper.get(Data.ToolTipFlags);
            flag = (Boolean) flags.get(0);
            prop.setHideEmptyInTooltip(flag);
            flag = (Boolean) flags.get(1);
            prop.setIncludeInTooltip(flag);
        }

        if (wrapper.containsField(Data.Value)) {
            Object o = wrapper.get(Data.Value);
            if (o instanceof Collection) {
                prop.getValues().addAll((Collection) o);
            } else {
                prop.getValues().add(o);
            }
        }
    }

    public static void setVisualizationFlag(Tuple tuple, DBObject val) {
        DBObject payload = Helper.getInternalPayload(val);
        if( payload.containsField(Data.Visualized)) {
            tuple.set(GraphContext.IS_VISUALIZED, payload.get(Data.Visualized));
        }
    }

}
