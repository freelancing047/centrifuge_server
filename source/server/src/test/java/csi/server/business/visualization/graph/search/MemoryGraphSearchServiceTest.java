package csi.server.business.visualization.graph.search;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.junit.BeforeClass;
import org.junit.Test;

import prefuse.data.Graph;
import prefuse.data.Node;

import csi.server.business.visualization.graph.GraphManager;
import csi.server.business.visualization.graph.base.NodeStore;
import csi.server.business.visualization.graph.base.ObjectAttributes;
import csi.server.business.visualization.graph.base.property.Property;
import csi.server.common.dto.graph.search.AttributeCriterion;
import csi.server.common.dto.graph.search.GraphSearch;
import csi.server.common.dto.graph.search.GraphSearchResults;
import csi.server.common.dto.graph.search.NodeSearchCriterion;
import csi.server.common.dto.graph.search.SearchType;
import csi.server.common.enumerations.CsiDataType;
import csi.server.common.model.FieldDef;
import csi.server.common.model.attribute.AttributeDef;
import csi.server.common.model.filter.FilterOperatorType;
import csi.server.common.model.visualization.graph.GraphConstants;
import csi.server.common.model.visualization.graph.NodeDef;

public class MemoryGraphSearchServiceTest {

    static Graph testGraph;

    @BeforeClass
    static public void createTestGraph() {
        Function<Node, NodeStore> getNodeDetails = MemoryGraphSearchService.getNodeDetails;
        testGraph = new Graph();
        testGraph.addColumn(GraphConstants.NODE_DETAIL, NodeStore.class);
        Node ed = addNodeAndDetails();
        NodeStore details = getNodeDetails.apply(ed);
        addLabels(details, "Ed", "Saltelli");
        addTypes(details, "person", "developer", "alpha");
        addPropertyValues(details, "name", "ed", "eduardo");
        addPropertyValues(details, "last name", "saltelli");
        addPropertyValues(details, "state", "VA", "MD", "Baden-Wuertenburg");
        addPropertyValues(details, "children", 0L);
        addPropertyValues(details, "pets", 1L);
        addSupporting(details, "Person", 1, 2, 3, 4, 5);

        Node mitch = addNodeAndDetails();
        details = getNodeDetails.apply(mitch);

        addLabels(details, "Mitch", "Shue");
        addTypes(details, "person", "manager");
        addPropertyValues(details, "name", "mitch", "Mitch");
        addPropertyValues(details, "last name", "shue");
        addPropertyValues(details, "state", "VA", "Texas", "Japan");
        addPropertyValues(details, "children", 2L);
        addPropertyValues(details, "pets", 2L);
        addSupporting(details, "Person", 6, 7, 8, 9, 10);

        Node hoa = addNodeAndDetails();
        details = getNodeDetails.apply(hoa);
        addLabels(details, "hoa");
        addTypes(details, "person", "developer");
        addPropertyValues(details, "name", "Hoa", "hoa");
        addPropertyValues(details, "name", "nguyen");
        addPropertyValues(details, "state", "VA", "MD", "MO");
        addPropertyValues(details, "children", 2L);
        addPropertyValues(details, "pets", 5L);
        addSupporting(details, "Person", 11, 12, 13, 14, 15);

        testGraph.addEdge(ed, mitch);
        testGraph.addEdge(ed, hoa);

    }

    private static void addTypes(NodeStore details, String... types) {
        for (String type : types) {
            details.addType(type);
        }
    }

    private static void addSupporting(NodeStore details, String defName, int... values) {
        for (Integer row : values) {
            details.addSpecRow(defName, row);
        }
    }

    private static void addPropertyValues(NodeStore details, String name, Object... values) {
        Map<String, Property> attributes = details.getAttributes();
        Property property = attributes.get(name);
        if (property == null) {
            property = new Property(name);
            attributes.put(name, property);
        }

        for (Object string : values) {
            property.getValues().add(string);
        }

    }

    private static void addLabels(NodeStore details, String... values) {
        for (int i = 0; i < values.length; i++) {
            details.addLabel(values[i]);
        }

    }

    static private Node addNodeAndDetails() {
        Node node = testGraph.addNode();
        GraphManager.setNodeDetails(node, new NodeStore());
        return node;
    }

    @Test
    public void testSingleAttributeSearch() {

        GraphSearch search = buildGraphSearch();
        List<NodeSearchCriterion> nodeCriteria = search.getNodeCriteria();
        NodeSearchCriterion personCriteria = new NodeSearchCriterion();
        nodeCriteria.add(personCriteria);
        personCriteria.setNodeDef(getNodeDef("Person"));

        personCriteria.setAttributeCriteria(new ArrayList<AttributeCriterion>());
        List<AttributeCriterion> attributeCriteria = personCriteria.getAttributeCriteria();

        AttributeDef nameAttr = getAttributeDef("name");

        AttributeCriterion ac = new AttributeCriterion();
        ac.operator = FilterOperatorType.EQUALS;
        attributeCriteria.add(ac);

        ac.setAttribute(nameAttr);
        ac.staticValues = new ArrayList<String>();
        ac.staticValues.add("ed");
        ac.staticValues.add("hoa");

        GraphSearchService searchService = getSearchService();
        GraphSearchResults results = searchService.search(testGraph, search);

        assertEquals("Incorrect Search Results", 2, results.getNodes().size());
    }

    @Test
    public void testTwoAttributeSearch() {
        GraphSearch search = buildGraphSearch();
        List<NodeSearchCriterion> nodeCriteria = search.getNodeCriteria();
        NodeSearchCriterion personCriteria = new NodeSearchCriterion();
        nodeCriteria.add(personCriteria);
        personCriteria.setNodeDef(getNodeDef("Person"));

        personCriteria.setAttributeCriteria(new ArrayList<AttributeCriterion>());
        List<AttributeCriterion> attributeCriteria = personCriteria.getAttributeCriteria();

        AttributeDef nameAttr = getAttributeDef("name");

        AttributeCriterion ac = new AttributeCriterion();
        // attributeCriteria.add( ac );
        ac.setOperator(FilterOperatorType.EQUALS);
        ac.setAttribute(nameAttr);
        ac.staticValues = new ArrayList<String>();
        ac.staticValues.add("ed");
        ac.staticValues.add("hoa");

        AttributeDef stateAttr = getAttributeDef("state");
        AttributeCriterion state = new AttributeCriterion();
        state.setOperator(FilterOperatorType.EQUALS);
        attributeCriteria.add(state);

        state.setAttribute(stateAttr);
        state.staticValues = new ArrayList<String>();
        state.staticValues.add("VA");

        GraphSearchService searchService = getSearchService();
        GraphSearchResults results = searchService.search(testGraph, search);

        assertEquals("Incorrect Search Results", 3, results.getNodes().size());

        attributeCriteria.add(ac);
        state.staticValues.add("MD");

        results = searchService.search(testGraph, search);

        AttributeDef petAttr = getAttributeDef("pets");
        createCriteria(petAttr);
        assertEquals("VA and MD search", 2, results.getNodes().size());
    }

    private GraphSearch buildGraphSearch() {
        GraphSearch search = new GraphSearch();
        search.setSearchType(SearchType.NODES);
        search.setNodeCriteria(new ArrayList<NodeSearchCriterion>());
        return search;
    }

    @Test
    public void testStringAndNumericSearch() {
        GraphSearch search = buildGraphSearch();

        NodeSearchCriterion nsc = createNodeCriteria(search);

        NodeDef personDef = getNodeDef("Person");
        nsc.setNodeDef(personDef);

        AttributeDef petsAttr = getAttributeDef("pets");
        petsAttr.getFieldDef().setValueType(CsiDataType.Integer);
        AttributeDef nameAttr = getAttributeDef("name");

        AttributeCriterion pet = createCriteria(petsAttr);
        AttributeCriterion name = createCriteria(nameAttr);

        name.setOperator(FilterOperatorType.EQUALS);
        name.staticValues.add("ed");
        name.staticValues.add("mitch");

        pet.setOperator(FilterOperatorType.GEQ);
        pet.staticValues.add("2");

        nsc.getAttributeCriteria().add(pet);

        GraphSearchService searchService = getSearchService();
        GraphSearchResults results = searchService.search(testGraph, search);

        assertEquals("Failed on pet number comparison", 2, results.getNodes().size());

        pet.staticValues.clear();
        pet.staticValues.add("3");

        results = searchService.search(testGraph, search);
        assertEquals("Failed on pet number comparison", 1, results.getNodes().size());

        nsc.getAttributeCriteria().add(name);
        results = searchService.search(testGraph, search);

        assertEquals("Failed on pet number comparison", 0, results.getNodes().size());

        pet.staticValues.clear();
        pet.staticValues.add("1");
        results = searchService.search(testGraph, search);
        assertEquals("Failed on pet number comparison", 2, results.getNodes().size());
    }

    @Test
    public void testDegreePredicates() {
        GraphSearch search = buildGraphSearch();
        NodeDef personDef = getNodeDef("Person");
        NodeSearchCriterion criteria = createNodeCriteria(search);
        criteria.setNodeDef(personDef);
        AttributeDef degreeAttr = getAttributeDef("degree");
        degreeAttr.setFieldDef(new FieldDef());
        degreeAttr.getFieldDef().setValueType(CsiDataType.Integer);
        AttributeCriterion degree = createCriteria(degreeAttr);
        degree.setOperator(FilterOperatorType.GEQ);
        degree.staticValues.add("2");

        criteria.getAttributeCriteria().add(degree);

        GraphSearchService searchService = getSearchService();
        GraphSearchResults results = searchService.search(testGraph, search);

        assertEquals(1, results.getNodes().size());
    }

    private GraphSearchService getSearchService() {
        MemoryGraphSearchService searchService = new MemoryGraphSearchService();
        return searchService;
    }

    @Test
    public void testStringGreater() {
        GraphSearch search = buildGraphSearch();
        NodeDef personDef = getNodeDef("Person");
        NodeSearchCriterion criteria = createNodeCriteria(search);
        criteria.setNodeDef(personDef);

        AttributeDef attr = getAttributeDef("last name");
        attr.setFieldDef(new FieldDef());
        attr.getFieldDef().setValueType(CsiDataType.String);
        AttributeCriterion lastname = createCriteria(attr);
        lastname.setOperator(FilterOperatorType.GT);
        lastname.staticValues.add("rrr");

        criteria.getAttributeCriteria().add(lastname);

        GraphSearchService searchService = getSearchService();

        GraphSearchResults results = searchService.search(testGraph, search);
        assertEquals(2, results.getNodes().size());

    }

    @Test
    public void testStringLessThanEqual() {
        GraphSearch search = buildGraphSearch();
        NodeDef personDef = getNodeDef("Person");
        NodeSearchCriterion criteria = createNodeCriteria(search);
        criteria.setNodeDef(personDef);

        AttributeDef attr = getAttributeDef("last name");
        attr.setFieldDef(new FieldDef());
        attr.getFieldDef().setValueType(CsiDataType.String);
        AttributeCriterion lastname = createCriteria(attr);
        lastname.setOperator(FilterOperatorType.LEQ);
        lastname.staticValues.add("rrr");

        criteria.getAttributeCriteria().add(lastname);

        GraphSearchService searchService = getSearchService();

        GraphSearchResults results = searchService.search(testGraph, search);
        assertEquals(1, results.getNodes().size());

        //
        lastname.staticValues.clear();
        lastname.staticValues.add("sb");

        results = searchService.search(testGraph, search);
        assertEquals(2, results.getNodes().size());

    }

    @Test
    public void testLabelSearch() {
        GraphSearch search = buildGraphSearch();
        NodeDef personDef = getNodeDef("Person");
        NodeSearchCriterion criteria = createNodeCriteria(search);
        criteria.setNodeDef(personDef);

        AttributeDef labelAttr = getAttributeDef(ObjectAttributes.CSI_INTERNAL_LABEL);
        AttributeCriterion label = createCriteria(labelAttr);
        label.setOperator(FilterOperatorType.CONTAINS);
        label.staticValues.add("itch");

        criteria.getAttributeCriteria().add(label);

        GraphSearchService searchService = getSearchService();
        GraphSearchResults results = searchService.search(testGraph, search);

        assertEquals(1, results.getNodes().size());

    }

    @Test
    public void testTypeSearch() {
        GraphSearch search = buildGraphSearch();
        NodeDef personDef = getNodeDef("Person");
        NodeSearchCriterion criteria = createNodeCriteria(search);
        criteria.setNodeDef(personDef);

        AttributeDef labelAttr = getAttributeDef(ObjectAttributes.CSI_INTERNAL_TYPE);
        AttributeCriterion label = createCriteria(labelAttr);
        label.setOperator(FilterOperatorType.LT);
        label.staticValues.add("manager");

        criteria.getAttributeCriteria().add(label);

        GraphSearchService searchService = getSearchService();
        GraphSearchResults results = searchService.search(testGraph, search);

        assertEquals(2, results.getNodes().size());

        label.staticValues.clear();
        label.staticValues.add("developer");

        searchService = getSearchService();
        results = searchService.search(testGraph, search);

        assertEquals(1, results.getNodes().size());

    }

    private NodeSearchCriterion createNodeCriteria(GraphSearch graphSearch) {
        NodeSearchCriterion nsc = new NodeSearchCriterion();
        graphSearch.getNodeCriteria().add(nsc);
        nsc.setAttributeCriteria(new ArrayList<AttributeCriterion>());
        return nsc;
    }

    private AttributeCriterion createCriteria(AttributeDef attrDef) {
        AttributeCriterion criteria = new AttributeCriterion();
        criteria.setAttribute(attrDef);
        criteria.staticValues = new ArrayList<String>(5);
        return criteria;
    }

    private AttributeDef getAttributeDef(String name) {
        AttributeDef def = new AttributeDef();
        def.setName(name);
        def.setFieldDef(new FieldDef());
        return def;
    }

    private NodeDef getNodeDef(String name) {
        NodeDef def = new NodeDef();
        def.setName(name);

        return def;
    }

}
