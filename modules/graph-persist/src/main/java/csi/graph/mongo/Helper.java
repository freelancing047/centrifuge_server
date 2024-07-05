package csi.graph.mongo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.bson.types.ObjectId;

import com.google.common.collect.Lists;
import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.CommandResult;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.QueryBuilder;
import com.mongodb.WriteResult;

import csi.graph.GraphStorage;
import edu.uci.ics.jung.graph.util.EdgeType;

public class Helper
{
    public static final String  DOC_ID            = "_id";

    // Application specific identifier. Usning naming scheme similar
    // to web usage
    public static final String  SLUG              = "slug";

    public static final String  INTERNAL          = "_csi";
    public static final String  APP_ID            = "id";
    public static final String  SOURCE            = "s";
    public static final String  TARGET            = "t";
    public static final String  EDGE_TYPE         = "et";

    static final String         ANCESTORS         = "ancestors";
    static final String         META              = "meta";
    public static final String  GROUP_ID          = "gId";
    public static final int     SPAN_MAX          = Integer.MAX_VALUE;  // chunk arrays larger than this value

    public static final String  EDGE_SOURCE_QUERY = INTERNAL + "." + SOURCE;
    public static final String  EDGE_TARGET_QUERY = INTERNAL + "." + TARGET;
    public static final String  EDGE_TYPE_QUERY   = INTERNAL + "." + EDGE_TYPE;
    public static final String  PARENT_QUERY      = INTERNAL + "." + GROUP_ID;
    public static final String  ANCESTOR_QUERY    = INTERNAL + "." + ANCESTORS;
    public static final String  META_QUERY        = INTERNAL + "." + META;
    public static final String  AND               = "$and";

    private static final String POS               = "pos";

    private static final String X                 = "x";

    private static final String Y                 = "y";

    public static DBObject      OnlyMongoId       = BasicDBObjectBuilder.start().add(Helper.DOC_ID, 1).get();
    static DBObject             Empty             = new BasicDBObject();

    public static ObjectId getMongoId(DBObject obj) {
        return (ObjectId) obj.get(Helper.DOC_ID);
    }

    public static boolean isInternal(String key) {
        return INTERNAL.equals(key);
    }

    public static DBObject generateQueryFor(DBObject obj) {
        return generateQueryFor(obj, false);
    }

    public static DBObject generateQueryFor(DBObject obj, boolean includeData) {
        QueryBuilder builder = QueryBuilder.start();
        if (obj.containsField(DOC_ID)) {
            builder.put(DOC_ID).is(obj.get(DOC_ID));
        } else if (obj.containsField(APP_ID)) {
            builder.put(APP_ID).is(obj.get(APP_ID));
        } else if (includeData) {
            Set<String> keys = obj.keySet();
            for (String key : keys) {
                Object value = obj.get(key);
                if (value instanceof DBObject) {
                    // fail -- cannot allow nested objects for
                    // query at this point.
                    // TODO: construct helper to flatten the
                    // nested object into dot notation.
                    throw new IllegalArgumentException("query.not-supported.nested-objects");
                }
                builder.put(key).is(value);
            }
        }
        return builder.get();
    }

    /**
     * Constructs a MongoDB query.
     * 
     * @param edge
     * @param includeData
     * @return
     */
    public static DBObject generateEdgeQueryFor(DBObject edge, boolean includeData) {
        QueryBuilder builder = QueryBuilder.start();
        if (edge.containsField(DOC_ID)) {
            builder.put(DOC_ID).is(edge.get(DOC_ID));
        } else if (edge.containsField(APP_ID)) {
            builder.put(APP_ID).is(edge.get(APP_ID));
        } else if (edge.containsField(INTERNAL)) {
            // TODO: do we need searches for source, target, and type?
        } else if (includeData) {
            // TODO:
        }

        return builder.get();
    }

    static DBObject getQueryFor(DBObject obj) {
        QueryBuilder builder = QueryBuilder.start();
        if (obj.containsField(Helper.DOC_ID)) {
            builder.put(Helper.DOC_ID).is(obj.get(Helper.DOC_ID));
        } else {
            builder.put(Helper.APP_ID).is(obj.get(Helper.APP_ID));
        }

        return builder.get();
    }

    public static DBObject getEdgeExistsQuery(DBObject edge, EdgeType edgeType) {
        QueryBuilder builder = QueryBuilder.start();

        if (edge.containsField(Helper.DOC_ID)) {
            builder.put(Helper.DOC_ID).is(edge.get(Helper.DOC_ID));
        } else if (edge.containsField(APP_ID)) {
            builder.put(Helper.APP_ID).is(edge.get(APP_ID));
        } else if (edge.containsField(EDGE_SOURCE_QUERY) && edge.containsField(EDGE_TARGET_QUERY)) {
            builder.put(EDGE_SOURCE_QUERY).is(edge.get(EDGE_SOURCE_QUERY));
            builder.put(EDGE_TARGET_QUERY).is(edge.get(EDGE_TARGET_QUERY));
        } else {
            DBObject data = (DBObject) edge.get(Helper.INTERNAL);
            Object sourceId = data.get(Helper.SOURCE);
            Object targetId = data.get(Helper.TARGET);

            builder.put(Helper.EDGE_SOURCE_QUERY).is(sourceId);
            builder.put(Helper.EDGE_TARGET_QUERY).is(targetId);
        }

        if (edgeType != null) {
            builder.put(Helper.EDGE_TYPE_QUERY).is(edgeType.toString());
        }

        return builder.get();
    }

    public static <T extends DBObject> Collection<DBObject> asCollection(DBCursor cursor) {
        Collection<DBObject> set = new HashSet<DBObject>();
        while (cursor.hasNext()) {
            set.add(cursor.next());
        }
        return set;
    }

    static DBObject copy(DBObject object) {
        return copy(object, false);
    }

    public static DBObject copy(DBObject vertex, boolean includeRestricted) {
        BasicDBObject copy = new BasicDBObject();
        for (String key : vertex.keySet()) {
            if (!includeRestricted && isRestrictedKey(key)) {
                continue;
            }
            copy.put(key, vertex.get(key));
        }

        return copy;
    }

    static boolean isRestrictedKey(String key) {
        boolean restricted = key == null || key.contains(".") || key.equals(DOC_ID) || key.equals(INTERNAL)
                || key.equals(APP_ID);
        return restricted;
    }

    public static boolean hasParent(DBObject vertex) {
        DBObject data = getInternalPayload(vertex);
        boolean hasParent = data.containsField(GROUP_ID);
        return hasParent;
    }

    public static boolean hasError(WriteResult result) {
        CommandResult lastError = result.getLastError();
        return lastError.getException() != null;
    }

    public static void ensureMongoId(DBObject vertex, DBObject payload) {
        if (!vertex.containsField(Helper.DOC_ID)) {
            vertex.put(Helper.DOC_ID, payload.get(Helper.DOC_ID));
        }
    }

    public static boolean markAsContainer(DBObject container) {
        DBObject payload = getInternalPayload(container);

        boolean added = false;
        if (!payload.containsField(META)) {
            payload.put(META, Boolean.TRUE);
            added = true;
        } else {
            boolean isContainer = (Boolean) payload.get(META);
            if (!isContainer) {
                payload.put(META, Boolean.TRUE);
                added = true;
            }
        }

        return added;

    }

    public static DBObject getInternalPayload(DBObject object) {
        if (!object.containsField(INTERNAL)) {
            object.put(INTERNAL, new BasicDBObject());
        }

        DBObject payload = (DBObject) object.get(INTERNAL);
        return payload;
    }

    public static List<Object> getAncestors(DBObject object) {
        DBObject data = getInternalPayload(object);
        List<Object> ancestors;
        if (data.containsField(ANCESTORS)) {
            ancestors = (List<Object>) data.get(ANCESTORS);
        } else {
            ancestors = new ArrayList<Object>();
        }

        return Collections.unmodifiableList(ancestors);
    }

    public static EdgeType getEdgeType(DBObject edge) {
        BasicDBObject data = (BasicDBObject) getInternalPayload(edge);
        String val = data.getString(EDGE_TYPE);
        return EdgeType.valueOf(val);
    }

    public static QueryBuilder getEdgeQueryBuilder(ObjectId id) {
        QueryBuilder builder = QueryBuilder.start();
        builder.put(Helper.EDGE_SOURCE_QUERY).is(id);
        DBObject asSource = builder.get();

        builder = QueryBuilder.start();
        builder.put(Helper.EDGE_TARGET_QUERY).is(id);
        DBObject asTarget = builder.get();

        builder = QueryBuilder.start();
        builder.or(asSource, asTarget);
        return builder;
    }

    public static QueryBuilder getIncidentEdgeQuery(List<ObjectId> ids) {
        QueryBuilder builder = QueryBuilder.start();
        builder.put(EDGE_SOURCE_QUERY).in(ids);
        DBObject asSource = builder.get();

        builder = QueryBuilder.start();
        builder.put(EDGE_TARGET_QUERY).in(ids);
        DBObject asTarget = builder.get();

        builder = QueryBuilder.start();
        List<DBObject> parts = Lists.newArrayList(asSource, asTarget);
        builder.put(AND).is(parts);
        return builder;
    }

    public static QueryBuilder getNonIncidentEdgeQuery(List<ObjectId> ids) {
        QueryBuilder builder = QueryBuilder.start();
        builder.put(EDGE_SOURCE_QUERY).in(ids);
        builder.put(EDGE_TARGET_QUERY).notIn(ids);
        DBObject onlySource = builder.get();

        builder = QueryBuilder.start();
        builder.put(EDGE_SOURCE_QUERY).notIn(ids);
        builder.put(EDGE_TARGET_QUERY).in(ids);
        DBObject onlyTarget = builder.get();

        builder = QueryBuilder.start();
        builder.or(onlySource, onlyTarget);
        return builder;
    }

    public static DBObject getInEdgeQuery(ObjectId id) {
        QueryBuilder builder = QueryBuilder.start();
        builder.put(Helper.EDGE_TARGET_QUERY).is(id);
        return builder.get();
    }

    public static DBObject getOutEdgeQuery(ObjectId id) {
        QueryBuilder builder = QueryBuilder.start();
        builder.put(Helper.EDGE_SOURCE_QUERY).is(id);
        return builder.get();
    }

    public static ObjectId getParentId(DBObject vertex) {
        DBObject data = getInternalPayload(vertex);
        ObjectId id = (ObjectId) data.get(GROUP_ID);
        return id;
    }

    public static boolean isContainer(DBObject vertex) {
        DBObject data = getInternalPayload(vertex);
        boolean isContainer = data.containsField(META) && Boolean.TRUE.equals(data.get(META));
        return isContainer;
    }

    public static void setContainer(DBObject object, ObjectId containerId) {
        DBObject data = getInternalPayload(object);
        data.put(GROUP_ID, containerId);
    }

    public static void clearContainer(DBObject object) {
        DBObject data = getInternalPayload(object);
        if (data.containsField(GROUP_ID)) {
            data.removeField(GROUP_ID);
            data.removeField(ANCESTORS);
        }
    }

    public static Set<DBObject> asSet(Iterable<DBObject> iterable) {
        Set<DBObject> set = new HashSet<DBObject>();
        Iterator<DBObject> iterator = iterable.iterator();

        while (iterator.hasNext()) {
            set.add(iterator.next());
        }

        return set;
    }

    public static DBObject getIdQuery(ObjectId id) {
        QueryBuilder builder = QueryBuilder.start();
        builder.put(DOC_ID).is(id);
        return builder.get();
    }

    public static DBObject getNodeQuery(String key) {
        QueryBuilder builder = QueryBuilder.start();
        builder.put(APP_ID).is(key);
        return builder.get();
    }

    public static DBObject getIdQuery(Object id) {
        QueryBuilder builder = QueryBuilder.start();
        if (id instanceof String) {
            id = ObjectId.massageToObjectId(id);
        }
        builder.put(DOC_ID).is(id);
        return builder.get();
    }

    public static boolean hasEqualIds(DBObject that, DBObject other) {
        ObjectId id1 = getMongoId(that);
        ObjectId id2 = getMongoId(other);
        return id1.equals(id2);
    }

    public static void copyAncestorTo(DBObject vertex, DBObject container) {
        DBObject vData = getInternalPayload(vertex);
        DBObject cData = getInternalPayload(container);

        vData.put(GROUP_ID, cData.get(GROUP_ID));
        vData.put(ANCESTORS, cData.get(ANCESTORS));
    }

    public static boolean hasSuccess(WriteResult result) {
        return !hasError(result);
    }

    public static boolean hasEdgeData(DBObject edge) {
        if (edge == null) {
            return false;
        }
        DBObject data = (DBObject) edge.get(INTERNAL);
        if (data == null) {
            return false;
        }

        boolean hasData = data.containsField(SOURCE) && data.containsField(TARGET);
        return hasData;
    }

    public static DBObject getAnyQuery(ObjectId... ids) {
        QueryBuilder builder = QueryBuilder.start();
        builder.put(DOC_ID).in(ids);
        DBObject query = builder.get();
        return query;
    }

    public static ObjectId getEdgeSource(DBObject edge) {
        if (!hasEdgeData(edge)) {
            return null;
        }

        DBObject data = getInternalPayload(edge);
        ObjectId id = (ObjectId) data.get(SOURCE);
        return id;
    }

    public static ObjectId getEdgeTarget(DBObject edge) {
        if (!hasEdgeData(edge)) {
            return null;
        }

        DBObject data = getInternalPayload(edge);
        ObjectId id = (ObjectId) data.get(TARGET);
        return id;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static void setParentInfo(DBObject child, DBObject parent) {
        // TODO Auto-generated method stub
        ObjectId pid = getMongoId(parent);
        List<Object> parents = Helper.getAncestors(parent);
        ArrayList ancestors = new ArrayList(parents);
        ancestors.add(pid);

        DBObject data = getInternalPayload(child);
        data.put(GROUP_ID, pid);
        data.put(ANCESTORS, ancestors);
    }

    public static boolean isChildOf(GraphStorage<DBObject, DBObject> storage, DBObject parent, DBObject child) {
        ObjectId parentId = Helper.getMongoId(parent);
        ObjectId childId = Helper.getMongoId(child);

        QueryBuilder builder = QueryBuilder.start();
        builder.put(Helper.DOC_ID).is(childId);
        builder.put(Helper.PARENT_QUERY).is(parentId);
        DBObject query = builder.get();
        DBObject result = storage.findVertex(query);
        return result != null;
    }

    public static DBObject generateQueryFor(ObjectId pid) {
        QueryBuilder builder = QueryBuilder.start();
        builder.put(DOC_ID).is(pid);
        return builder.get();
    }

    public static boolean isMeta(DBObject obj) {
        DBObject data = getInternalPayload(obj);
        boolean isMeta = data.containsField(META) && Boolean.TRUE.equals(data.get(META));
        return isMeta;
    }

    public static DBObject getEdgeQuery(DBObject source, DBObject target) {
        QueryBuilder builder = QueryBuilder.start();
        builder.put(EDGE_SOURCE_QUERY).is(source.get(DOC_ID));
        builder.put(EDGE_TARGET_QUERY).is(target.get(DOC_ID));
        return builder.get();
    }

    public static Object getId(DBObject data) {
        return (data == null) ? null : data.get(DOC_ID);
    }

    public static DBObject buildData() {
        return new BasicDBObject();
    }

    public static void setPosition(DBObject vertex, double x, double y) {
        DBObject payload = getInternalPayload(vertex);
        BasicDBObjectBuilder builder = BasicDBObjectBuilder.start();

        builder.add(X, x);
        builder.add(Y, y);

        payload.put(POS, builder.get());

    }

    public static double[] getPosition(DBObject v) {
        double[] pos = new double[2];
        DBObject payload = getInternalPayload(v);
        if (payload.containsField(POS)) {
            DBObject data = (DBObject) payload.get(POS);
            pos[0] = (Double) data.get(X);
            pos[1] = (Double) data.get(Y);
        }
        return pos;
    }

}