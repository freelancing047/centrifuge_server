package csi.server.business.visualization.deprecated.timeline;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;

import javax.script.Bindings;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.Multimap;
import com.google.common.collect.TreeMultimap;

import csi.server.business.cachedb.script.CacheRowSet;
import csi.server.business.cachedb.script.CsiScriptRunner;
import csi.server.business.cachedb.script.ecma.EcmaScriptRunner;
import csi.server.business.visualization.graph.base.ObjectAttributes;
import csi.server.business.visualization.graph.base.property.Property;
import csi.server.common.model.ConditionalExpression;
//import csi.server.common.model.DataModelDef;
import csi.server.common.model.EventDef;
import csi.server.common.model.FieldDef;
import csi.server.common.model.attribute.AttributeDef;
import csi.server.common.model.dataview.DataViewDef;
import csi.server.common.model.visualization.TimelineViewDef_V1;
import csi.server.common.model.visualization.graph.NodeDef;
import csi.server.util.CsiTypeUtil;

/*
 *
 * TODO:
 *
 * 1.  Events assume there's only a start and end date.  Creator does not current
 * account for separated fields defining start date and start time.
 *
 * 2.  Combinations of start + duration, end + duration, etc. are not handled
 *
 * 3.  Sequences are not directly handled at this time.  Note that internally the
 * events are ordered via their millis since epoch -- so sequences aren't that far off.
 *
 */
public class EventCreator implements Callable<Void> {
   private static final Logger LOG = LogManager.getLogger(EventCreator.class);

    private static final String END_DATE = ObjectAttributes.CSI_INTERNAL_END_DATE;

    private static final String START_DATE = ObjectAttributes.CSI_INTERNAL_START_DATE;

    @SuppressWarnings("unused")
    private static final String GROUP_ID = ObjectAttributes.CSI_INTERNAL_GROUP_ID;

    @SuppressWarnings("unused")
    private static final String LABEL = ObjectAttributes.CSI_INTERNAL_LABEL;

    static final String ID = ObjectAttributes.CSI_INTERNAL_ID;

    // FIXME: need some other way of indicating a non-existent date.
    private static Date NoDate = new Date(Long.MIN_VALUE);

    private CsiScriptRunner scriptRunner;

    private ResultSet data;

    private Multimap<Long, TemporalEvent> events;

    private Map<String, TemporalEvent> objectsById;

    private DataViewDef meta;
    private TimelineViewDef_V1 timelineDef;

    protected IEventIDGenerator idStrategy;
    protected IEventIDGenerator groupedIdStrategy;

    public Multimap<Long, TemporalEvent> getEvents() {
        return events;

    }

    public EventCreator(DataViewDef meta, TimelineViewDef_V1 viewDef, ResultSet data) {
        this.events = TreeMultimap.create();
        this.timelineDef = viewDef;
        this.idStrategy = new SimpleEventIdentifierStrategy();
        this.groupedIdStrategy = new GroupingIdentifierStrategy();

        this.meta = meta;
        this.data = data;

        if (timelineDef == null) {
           LOG.warn("No timeline metadata present");
            throw new IllegalStateException();
        }

//        if (timelineDef.getEventDefs() == null || timelineDef.getEventDefs().size() == 0) {
//            log.warn("No events are defined.");
//            throw new IllegalStateException();
//        }

        if (meta == null) {
           LOG.warn("no.event.metadata");
            throw new IllegalStateException();
        }

        if (this.data == null) {
           LOG.warn("no.event.data");
            throw new IllegalStateException();
        }
    }

    protected void setup() throws Exception {
        scriptRunner = new EcmaScriptRunner();

    }

    public Void call() throws Exception {

//        DataModelDef modelDef = meta.getModelDef();
//
//        List<EventDef> eventDefs = timelineDef.getEventDefs();
//
//        objectsById = new HashMap<String, TemporalEvent>();
//
//        CacheRowSet rowSet = new CacheRowSet(modelDef.getFieldDefs(), data);
//        while (rowSet.nextRow()) {
//
//            int currentRowIndex = data.getInt(DATA_INTERNAL_ID);
//
//            modelDef.getFieldDefs();
//
//            for (EventDef eventDef : eventDefs) {
//                createEventOnRow(objectsById, currentRowIndex, rowSet, eventDef);
//            }
//        }

        return null;

    }

    public DataViewDef getMetadata() {
        return meta;
    }

    public void setMetadata(DataViewDef metadata) {
        this.meta = metadata;
    }

    private void createEventOnRow(Map<String, TemporalEvent> objectsById, int currentRowIndex, CacheRowSet rowSet, EventDef spec) throws Exception, SQLException {

        if (!passesConditionals(spec, rowSet)) {
            return;
        }

        // Create a shallow copies since we'll remove specially processed
        // attributes before
        // fully processing the event.
        NodeDef baseDef = spec.getEventNode();
        Map<String, FieldDef> remainingAttributes = baseDef.getAttributeDefsAsMap();

        Map<String, Property> properties = new HashMap<String, Property>();
        processAttributes(currentRowIndex, rowSet, remainingAttributes, properties);

        String eventID;
        if (remainingAttributes.containsKey(ObjectAttributes.CSI_INTERNAL_GROUP_ID)) {
            eventID = groupedIdStrategy.generate(properties, currentRowIndex);
        } else {
            eventID = idStrategy.generate(properties, currentRowIndex);
        }

        // FIXME: when refactoring the Data/Configuration/Meta reorder attribute
        // computation based off of ordering/priorities. We'll compute all
        // attribute values
        // then use a plugin strategy for determining the unique key for an
        // object.
        //
        // The 'strategy' enables us to have a dynamic mechanism for which
        // computed attributes
        // take precedence--with a default precedence of ID then label.
        // Assumption made is that
        // at least one strategy is provided that nominates one of the
        // attributes--failing creation
        // of the object if no attributes are present!
        // FieldDef attributeDeclaration;
        // attributeDeclaration = remainingAttributes.get( ID );
        //
        // if( attributeDeclaration == null ) {
        // attributeDeclaration = remainingAttributes.get( LABEL );
        // }
        //
        // Object retrievedValue = computeAttributeValue( attributeDeclaration, rowSet, currentRowIndex );
        // String objectID = null;
        // if( retrievedValue != null ) {
        // objectID = retrievedValue.toString();
        // }
        // // String objectID = computeAttributeValue( attributeDeclaration, rowSet, currentRowIndex )
        //
        // attributeDeclaration = remainingAttributes.get( GROUP_ID );
        // String groupID = (String)computeAttributeValue( attributeDeclaration, rowSet, currentRowIndex );
        //
        // // get rid of these so that we don't process them later...note this is
        // // only a ref copy of the actual mappings.
        // remainingAttributes.remove( ID );
        // remainingAttributes.remove( GROUP_ID );

        // duration & other options not processed yet.
        Date startTimestamp = getStartInstant(spec, rowSet);
        Date endTimestamp = getEndInstant(spec, rowSet);

        // FIXME: temp hack to address entries that have no date boundaries....
        if ((startTimestamp == null) || (startTimestamp == NoDate)) {
            if ((endTimestamp == null) || (endTimestamp == NoDate)) {
                return;
            } else {
                startTimestamp = endTimestamp;
            }
        }

        if ((endTimestamp != null) && ((endTimestamp == NoDate) || endTimestamp.before(startTimestamp))) {
            endTimestamp = startTimestamp;
        }

        TemporalEvent event = null;

        // Differ from RelGraph -- only if the ID is explicitly specified do we
        // attempt to associate updated information. Labels here
        // are distinct and separate from
        event = getOrCreateObject(objectsById, eventID);

        event.setStart(startTimestamp.getTime());
        if (endTimestamp != null) {
            event.setEnd(endTimestamp.getTime());
        } else {
            event.setEnd(event.getStart());
        }

        event.addSupportingRow(spec.getName(), currentRowIndex);

        events.put(startTimestamp.getTime(), event);

        mergeProperties(event.getProperties(), properties);

    }

    protected void mergeProperties(Map<String, Property> target, Map<String, Property> source) {
        for (Map.Entry<String, Property> entry : source.entrySet()) {
            if (!target.containsKey(entry.getKey())) {
                target.put(entry.getKey(), entry.getValue());
            } else {
                Property targetProp = target.get(entry.getKey());
                List<Object> existing = targetProp.getValues();

                Property sourceProp = entry.getValue();
                List<Object> potentialNewValues = sourceProp.getValues();
                for (Object object : potentialNewValues) {
                    if (!existing.contains(object)) {
                        existing.add(object);
                    }
                }
            }
        }

    }

    private void processAttributes(int currentRowIndex, CacheRowSet rowSet, Map<String, FieldDef> builtinSpecs, Map<String, Property> properties) throws Exception {
        Iterator<Entry<String, FieldDef>> builtinIterator = builtinSpecs.entrySet().iterator();
        while (builtinIterator.hasNext()) {
            Entry<String, FieldDef> entry = builtinIterator.next();
            Property property = properties.get(entry.getKey());
            if (property == null) {
                property = new Property(entry.getKey());
                properties.put(entry.getKey(), property);
            }

            FieldDef attributeDef = entry.getValue();
            Object value = computeAttributeValue(attributeDef, rowSet, currentRowIndex);

            if ((value != null) && !property.getValues().contains(value)) {
                property.getValues().add(value);
            }
        }
    }

    private TemporalEvent getOrCreateObject(Map<String, TemporalEvent> objectsById, String objectKey) {
        TemporalEvent event;
        if (!objectsById.containsKey(objectKey)) {
            // TODO: this should serve as a template for a generic framework of
            // creating objects using either a factory method or sub-classing
            // approach to
            // enable impls for Events (here) and Nodes. Links comprise special
            // case wrt current processing since they ignore direction in
            // non-directed graphs.

            // Longer term links need to track their count in 3 dimensions:
            // in-->out, out-->in, and node<-->node.
            // this enables us to track relative weighting in either dimension.
            // NB: this implies tracking Links/Edges as distinct 'objects'. This
            // can be accomplished either by
            // a distinct counts on outbound/inbound for each node,
            event = new TemporalEvent();
            event.setId(objectKey);
            objectsById.put(objectKey, event);

            Map<String, Property> properties = event.getProperties();

            Property property = new Property(ID);
            property.setIncludeInTooltip(false);
            property.getValues().add(objectKey);
            properties.put(ID, property);

        } else {
            event = objectsById.get(objectKey);

        }
        return event;
    }

    // Implement Desktop's notion of unique events. The event
    // is unique by objectID + separator + groupID.
    // If an event exists, then double check the start & end if they're not
    // the same, the event key is constructed by replacing the existing
    // separator with the
    // strings of start and end with the separator
    // i.e. the final key is similar to:
    //
    // objectID = objectID + start.toString + end.toString + separator +
    // groupID
    // private String computeKey( String objectID, String groupID, Date startTimestamp, Date endTimestamp )
    // {
    //
    // // String eventKey = idStrategy.execute( properties );
    //
    // String objectKey;
    // Date now = new Date();
    //
    // if( objectID == null || objectID.length() == 0 ) {
    // objectKey = UUID.randomUUID().toString();
    // }
    //
    // if( groupID == null || groupID.length() == 0 ) {
    // groupID = UUID.randomUUID().toString();
    // }
    //
    // if( startTimestamp == null ) {
    // startTimestamp = now;
    // }
    //
    // if( endTimestamp == null ) {
    // endTimestamp = now;
    // }
    //
    // StringBuffer buf = new StringBuffer();
    // buf.append( objectID ).append( "::" );
    // buf.append( startTimestamp ).append( "::" ).append( endTimestamp );
    // buf.append( "::" ).append( groupID );
    // objectKey = buf.toString();
    //
    // return objectKey;
    // }

    private boolean passesConditionals(EventDef spec, CacheRowSet rowSet) throws Exception {
        NodeDef nodeDef = spec.getEventNode();
        ConditionalExpression conditionalExpression = nodeDef.getCreateConditional();
        if (conditionalExpression == null) {
            return true;
        }

        String expression = conditionalExpression.getExpression();
        if ((expression == null) || (expression.length() == 0)) {
            return true;
        }

        Bindings bindings = scriptRunner.createBindings();
        bindings.put("csiRow", rowSet);
        Object evalExpression = scriptRunner.evalExpression(expression, bindings);
        boolean results = true;

        if (evalExpression != null) {
            results = Boolean.parseBoolean(evalExpression.toString());
        }

        return results;
    }

    protected String computeAttributeValue(FieldDef attributeDeclaration, CacheRowSet rowSet, int currentRowIndex) throws Exception {
        String value = rowSet.getString(attributeDeclaration);
        return value;

    }

    protected Date getStartInstant(EventDef spec, CacheRowSet rowSet) throws SQLException, Exception {
        AttributeDef attributeDef = spec.getEventNode().getAttributeDef(START_DATE);
        FieldDef field = (attributeDef == null) ? null : attributeDef.getFieldDef();
        return computeTimeInstant(field, rowSet);
    }

    protected Date getEndInstant(EventDef spec, CacheRowSet rowSet) throws SQLException, Exception {
        AttributeDef attributeDef = spec.getEventNode().getAttributeDef(END_DATE);
        FieldDef field = (attributeDef == null) ? null : attributeDef.getFieldDef();
        if (field == null) {
            return null;
        }

        return computeTimeInstant(field, rowSet);
    }

    private Date computeTimeInstant(FieldDef timeField, CacheRowSet rowSet) throws Exception {
        if (timeField != null) {
            Object value = rowSet.get(timeField);
            return CsiTypeUtil.coerceDate(value);

        }

        return NoDate;
    }

    public Map<String, TemporalEvent> getEventsById() {
        return objectsById;
    }
}
