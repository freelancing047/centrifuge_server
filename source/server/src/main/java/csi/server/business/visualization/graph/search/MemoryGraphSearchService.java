package csi.server.business.visualization.graph.search;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.data.Tuple;

import csi.server.business.visualization.graph.GraphManager;
import csi.server.business.visualization.graph.base.AbstractGraphObjectStore;
import csi.server.business.visualization.graph.base.LinkStore;
import csi.server.business.visualization.graph.base.NodeStore;
import csi.server.business.visualization.graph.base.ObjectAttributes;
import csi.server.business.visualization.graph.base.property.Property;
import csi.server.common.dto.graph.search.AttributeCriterion;
import csi.server.common.dto.graph.search.EdgeInfo;
import csi.server.common.dto.graph.search.GraphSearch;
import csi.server.common.dto.graph.search.GraphSearchResults;
import csi.server.common.dto.graph.search.LinkSearchCriterion;
import csi.server.common.dto.graph.search.NodeInfo;
import csi.server.common.dto.graph.search.NodeSearchCriterion;
import csi.server.common.dto.graph.search.SearchType;
import csi.server.common.enumerations.CsiDataType;
import csi.server.common.model.FieldDef;
import csi.server.common.model.attribute.AttributeDef;
import csi.server.common.model.attribute.AttributeKind;
import csi.server.common.model.filter.FilterOperatorType;
import csi.server.common.model.visualization.graph.GraphConstants;
import csi.server.common.model.visualization.graph.LinkDef;
import csi.server.common.model.visualization.graph.NodeDef;
import csi.server.util.CsiTypeUtil;
import csi.server.util.StringUtils;

public class MemoryGraphSearchService implements GraphSearchService {
   private static final Logger LOG = LogManager.getLogger(GraphSearchService.class);

   private Predicate<Node> nodeFilter = new Predicate<Node>() {
      @Override
      public boolean test(Node node) {
         return true;
      }
   };

   private Predicate<Edge> edgeFilter =  new Predicate<Edge>() {
      @Override
      public boolean test(Edge edge) {
         return true;
      }
   };

   public Predicate<Node> getNodeFilter() {
      return nodeFilter;
   }

   public void setNodeFilter(Predicate<Node> nodeFilter) {
      this.nodeFilter = nodeFilter;
   }

   public void setEdgeFilter(Predicate<Edge> filter) {
      this.edgeFilter = filter;
   }

    @Override
    public GraphSearchResults search(Graph graph, GraphSearch search) {
        GraphSearchResults results = new GraphSearchResults();
        Predicate<Tuple> nodeSearch;

        if (search.getSearchType() == SearchType.NODES) {
            NodeSearchExpressionBuilder builder = new NodeSearchExpressionBuilder();
            nodeSearch = builder.buildExpression(search);

            Map<String,Node> lookup = (Map<String,Node>) graph.getClientProperty(GraphManager.NODE_HASH_TABLE);
            // post process the search results replace nodes that are bundled with their parent...
            Set<Node> topLevelNodes = new HashSet<Node>();

            for (Iterator<Node> nodes = graph.nodes(); nodes.hasNext();) {
            // UnmodifiableIterator<Node> filtered = Iterators.filter( unfiltered, IsNodeDisplayable );
               Node node = nodes.next();

               if (nodeFilter.test(node) && nodeSearch.test(node)) {
                  Node topLevelNode = null;
                  NodeStore details = getNodeDetails.apply(node);

                  if (details.isBundled()) {
                     NodeStore parent = (NodeStore) details.getParent();

                     while (parent.getParent() != null) {
                        parent = (NodeStore) parent.getParent();
                     }
                     topLevelNode = lookup.get(parent.getKey());
                  }
                  if (topLevelNode != null) {
                     topLevelNodes.add(node);
                  }
               }
            }
            Collection<NodeInfo> nodes = results.getNodes();

            for (Node node : topLevelNodes) {
               nodes.add(nodeToNodeInfoTransform.apply(node));
            }
        } else {
            LinkSearchExpressionBuilder builder = new LinkSearchExpressionBuilder();
            Predicate<Tuple> linkSearch = builder.buildExpression(search);

            List<EdgeInfo> matchedLinks = results.getLinks();

            for (Iterator<Edge> edges = graph.edges(); edges.hasNext();) {
               Edge edge = edges.next();

               if (edgeFilter.test(edge) && linkSearch.test(edge)) {
                  matchedLinks.add(edgeToEdgeInfoTransform.apply(edge));
               }
            }
        }
        return results;
    }

   static Predicate<Edge> couldDisplayEdge = new Predicate<Edge>() {
      @Override
      public boolean test(Edge edge) {
         LinkStore details = GraphManager.getEdgeDetails(edge);
         return details.isDisplayable();
      }
   };

   static Function<Node,NodeInfo> nodeToNodeInfoTransform = new Function<Node,NodeInfo>() {
      @Override
      public NodeInfo apply(Node node) {
         NodeStore details = getNodeDetails.apply(node);
         NodeInfo info = new NodeInfo();
         info.label = details.getLabel();
         info.id = node.getRow();
         info.type = getAllTypes(details);

         return info;
      }

      private String getAllTypes(NodeStore details) {
         Map<String, Integer> typeMap = details.getTypes();
         List<String> types = new ArrayList<String>(typeMap.size());

         types.addAll(typeMap.keySet());
         Collections.sort(types);
         return types.stream().collect(Collectors.joining(", "));
      }
   };

   static Function<Edge,EdgeInfo> edgeToEdgeInfoTransform = new Function<Edge,EdgeInfo>() {
      @Override
      public EdgeInfo apply(Edge edge) {
         LinkStore details = getEdgeDetails.apply(edge);
         EdgeInfo edgeInfo = new EdgeInfo();
         edgeInfo.sourceLabel = details.getFirstEndpoint().getLabel();
         edgeInfo.targetLabel = details.getSecondEndpoint().getLabel();
         edgeInfo.id = edge.getRow();

         return edgeInfo;
      }
   };

   static Function<Tuple,Integer> nodeId = new Function<Tuple,Integer>() {
      @Override
      public Integer apply(Tuple node) {
         return node.getRow();
      }
   };

   static Function<Node,NodeStore> getNodeDetails = new Function<Node,NodeStore>() {
      @Override
      public NodeStore apply(Node node) {
         NodeStore details = null;

         if (node != null) {
            details = GraphManager.getNodeDetails(node);
         }
         return details;
      }
   };

   static Function<Edge,LinkStore> getEdgeDetails = new Function<Edge,LinkStore>() {
      @Override
      public LinkStore apply(Edge edge) {
         LinkStore details = null;

         if (edge != null) {
            details = GraphManager.getEdgeDetails(edge);
         }
         return details;
      }
   };

   static Predicate<AbstractGraphObjectStore> isDefinedBy(final String name) {
      Predicate<AbstractGraphObjectStore> p = new Predicate<AbstractGraphObjectStore>() {
         @Override
         public boolean test(AbstractGraphObjectStore details) {
            Map<String,List<Integer>> rows = details.getRows();

            return ((rows != null) && rows.containsKey(name));
         }
      };
      return p;
   }

   private Predicate<Tuple> tupleAlwaysFalse = new Predicate<Tuple>() {
      @Override
      public boolean test(Tuple tuple) {
         return false;
      }
   };

   class NodeSearchExpressionBuilder {
      public Predicate<Tuple> buildExpression(GraphSearch search) {
         Predicate<Tuple> expression = null;

         if (search.getNodeCriteria().isEmpty()) {
            LOG.debug("No search criteria provided; search results will include all nodes");

            // TODO: determine proper case for handling a search request with no criteria...
            // for now we'll default to return all nodes....
            expression = tupleAlwaysFalse;
         } else {
            for (NodeSearchCriterion nsc : search.getNodeCriteria()) {
               NodePredicate nodePredicate = new NodePredicate(nsc);

               expression = (expression == null) ? nodePredicate : expression.or(nodePredicate);
            }
         }
         return expression;
      }
   }

   class LinkSearchExpressionBuilder {
      public Predicate<Tuple> buildExpression(GraphSearch search) {
         Predicate<Tuple> expression = null;

         if (search.getLinkCriteria().isEmpty()) {
            LOG.debug("No search criteria provided; search results will include all nodes");
            expression = tupleAlwaysFalse;
         } else {
            for (LinkSearchCriterion lsc : search.getLinkCriteria()) {
               LinkPredicate linkPredicate = new LinkPredicate(lsc);

               expression = (expression == null) ? linkPredicate : expression.or(linkPredicate);
            }
         }
         return expression;
      }
   }

   class NodePredicate implements Predicate<Tuple> {
      private Predicate<AbstractGraphObjectStore> definedBy;
      private Predicate<Tuple> attributesExpr;

      public NodePredicate(NodeSearchCriterion criteria) {
         NodeDef nodeDef = criteria.getNodeDef();

         if (nodeDef == null) {
            throw new IllegalArgumentException();
         }
         definedBy = isDefinedBy(nodeDef.getName());
         attributesExpr = null;

         for (AttributeCriterion ac : criteria.getAttributeCriteria()) {
            attributesExpr = (attributesExpr == null)
                                ? new AttributePredicate(ac)
                                : attributesExpr.and(new AttributePredicate(ac));
         }
      }

      @Override
      public boolean test(Tuple tuple) {
         boolean result = false;

         if (tuple instanceof Node) {
            Node node = (Node) tuple;
            NodeStore details = getNodeDetails.apply(node);

            if (definedBy.test(details)) {
            // NodeStore details = getNodeDetails.apply( node );
               result = attributesExpr.test(node);
            }
         }
         return result;
      }
   }

   class LinkPredicate implements Predicate<Tuple> {
      private Predicate<AbstractGraphObjectStore> definedBy;
      private Predicate<Tuple> attributesExpr;
      private Predicate<Tuple> sourceNodeExpr;
      private Predicate<Tuple> targetNodeExpr;

      public LinkPredicate(LinkSearchCriterion criteria) {
         LinkDef linkDef = criteria.getLinkDef();

         if (linkDef == null) {
            throw new IllegalArgumentException();
         }
         definedBy = isDefinedBy(linkDef.getName());
         attributesExpr = null;

         for (AttributeCriterion ac : criteria.getAttributeCriteria()) {
            attributesExpr = (attributesExpr == null)
                                ? new AttributePredicate(ac, GraphConstants.LINK_DETAIL)
                                : attributesExpr.and(new AttributePredicate(ac, GraphConstants.LINK_DETAIL));
         }
         if (criteria.getNode1() != null) {
            sourceNodeExpr = new NodePredicate(criteria.getNode1());
         }
         if (criteria.getNode2() != null) {
            targetNodeExpr = new NodePredicate(criteria.getNode2());
         }
      }

      @Override
      public boolean test(Tuple tuple) {
         boolean result = false;

         if (tuple instanceof Edge) {
            Edge edge = (Edge) tuple;
            LinkStore details = getEdgeDetails.apply(edge);

            if (definedBy.test(details)) {
               result = attributesExpr.test(edge) &&
                        ((sourceNodeExpr == null) || sourceNodeExpr.test(edge.getSourceNode())) &&
                        ((targetNodeExpr == null) || targetNodeExpr.test(edge.getTargetNode()));
            }
         }
         return result;
      }
   }

    static Map<String, String> DetailAttributes;

    static Map<String, String> GraphAttributes;

    static Map<String, CsiDataType> AttributeTypes;

//    static Map<String, String> SNAMetricAttributes;
    static {
        DetailAttributes = new HashMap<String, String>();
        DetailAttributes.put(ObjectAttributes.CSI_INTERNAL_LABEL, ObjectAttributes.CSI_INTERNAL_LABEL);
        DetailAttributes.put(ObjectAttributes.CSI_INTERNAL_TYPE, ObjectAttributes.CSI_INTERNAL_TYPE);
        DetailAttributes.put("label", ObjectAttributes.CSI_INTERNAL_LABEL);
        DetailAttributes.put("type", ObjectAttributes.CSI_INTERNAL_TYPE);
        DetailAttributes.put("weight", "weight");
        DetailAttributes.put("count", "weight");

        GraphAttributes = new HashMap<String, String>();
        GraphAttributes.put("degree", "degree");

        AttributeTypes = new HashMap<String, CsiDataType>();
        AttributeTypes.put("weight", CsiDataType.Integer);
        AttributeTypes.put("degree", CsiDataType.Integer);
        AttributeTypes.put("count", CsiDataType.Integer);
        AttributeTypes.put("betweenness", CsiDataType.Number);
        AttributeTypes.put("closeness", CsiDataType.Number);
        AttributeTypes.put("eigenvector", CsiDataType.Number);

       /* SNAMetricAttributes = new HashMap<String, String>();
        SNAMetricAttributes.put("Betweenness", "Betweenness");
        SNAMetricAttributes.put("Closeness", "Closeness");
        SNAMetricAttributes.put("Eigenvector", "Eigenvector");*/
    }

    static Function<Tuple, Object> getAttributeAccessor(String container, String name) {
        // handle standard Graph attributes/properties...
        if (GraphAttributes.containsKey(name)) {
            if (name.equals("degree")) {
                return new DegreeFunction();
            } else {
                return new TupleAccessor(GraphAttributes.get(name));
            }
        } else {
            // pretty kludgy here...need to account for attributes that are
            // specially handled for nodes and links...
            if (DetailAttributes.containsKey(name)) {
                return new NativeDetailAccessor(container, DetailAttributes.get(name));
            } else {
                return new DetailAccessor(container, name);
            }
        }
    }

    public static class TupleAccessor implements Function<Tuple, Object> {

        String name;

        TupleAccessor(String name) {
            this.name = name;
        }

        @Override
        public Object apply(Tuple tuple) {
            Object value = tuple.get(name);
            return value;
        }

    }

    public static class DegreeFunction implements Function<Tuple, Object> {

        @Override
        public Object apply(Tuple tuple) {
            int degree = 0;
            if (tuple instanceof Node) {
                degree = ((Node) tuple).getDegree();
            }

            return (long) degree;
        }

    }

    public static class DetailAccessor implements Function<Tuple, Object> {

        private String container;

        private String name;

        DetailAccessor(String container, String name) {
            this.container = container;
            this.name = name;
        }

        @Override
        public Object apply(Tuple tuple) {
            AbstractGraphObjectStore details = (AbstractGraphObjectStore) tuple.get(container);
            Map<String, Property> attributes = details.getAttributes();
            Property property = attributes.get(name);

            Object val = getValues(property);
            return val;
        }

        protected Object getValues(Property property) {
            return (property != null) ? property.getValues() : property;
        }

    }

    public static class SimplePropertyAccessor extends DetailAccessor {

        SimplePropertyAccessor(String container, String name) {
            super(container, name);
        }
    }

    /*
     * Accessor for retrieving flattened properties against a GraphObjectStore. This includes things like the ID/Key.
     */
    public static class NativeDetailAccessor implements Function<Tuple, Object> {

        private String container;

        private String name;

        NativeDetailAccessor(String container, String name) {
            if (name == null) {
                throw new IllegalArgumentException();
            }

            this.container = container;
            this.name = name;
        }

        @Override
        public Object apply(Tuple tuple) {
            Object value = null;

            AbstractGraphObjectStore details = (AbstractGraphObjectStore) tuple.get(container);
            if (name.equalsIgnoreCase(ObjectAttributes.CSI_INTERNAL_ID)) {
                value = details.getKey();
            } else if (name.equalsIgnoreCase(ObjectAttributes.CSI_INTERNAL_LABEL)) {
                value = details.getLabel();
            } else if (name.equalsIgnoreCase(ObjectAttributes.CSI_INTERNAL_TYPE)) {
                if (details.getTypes() != null) {
                    value = details.getTypes().keySet();
                }
            } else if (name.equalsIgnoreCase("weight")) {
                value = (long) details.getWeight();
            }

            return value;
        }

    }

    private static Predicate<Object> isNull = new Predicate<Object>() {
       @Override
       public boolean test(Object obj) {
          return (obj == null);
       }
    };

    class AttributePredicate implements Predicate<Tuple> {

        private String name;

        private List<String> values;

        private Predicate<Object> expr;

        private Function<Tuple, Object> accessor;

        public AttributePredicate(AttributeCriterion criteria) {
            this(criteria, GraphConstants.NODE_DETAIL);
        }

        public AttributePredicate(AttributeCriterion criteria, String containerName) {

            AttributeDef attribute = criteria.getAttribute();
            if (attribute == null) {
                throw new IllegalArgumentException();
            }
            name = attribute.getName();
            FilterOperatorType op = criteria.getOperator();

            accessor = getAttributeAccessor(containerName, name);
            if (op == FilterOperatorType.ISNULL) {
                expr = isNull;
            } else if (op == FilterOperatorType.EMPTY) {
                expr = new EmptyPredicate();
            } else if (op == FilterOperatorType.NULL_OR_EMPTY) {
                expr = isNull.or(new EmptyPredicate());
            } else {

                AttributeKind kind = attribute.getKind();
                CsiDataType dataType = CsiDataType.String;
                if (isComputedAttribute(attribute)) {
                    dataType = CsiDataType.Number;
                } else if ((kind == null) || (kind == AttributeKind.NORMAL) || (kind == AttributeKind.REFERENCE)) {
                    dataType = resolveDataType(name, attribute.getFieldDef());
                }
                expr = null;

                for (String val : criteria.staticValues) {
                    Object coercedValue = CsiTypeUtil.coerceType(val, dataType, null);
                    Predicate<Object> p = getOperationPredicate(criteria.getOperator(), coercedValue, val);
                    expr = (expr == null) ? p : expr.or(p);
                }
            }

            if (criteria.getExclude()) {
                expr = expr.negate();
            }
        }

        private boolean isComputedAttribute(AttributeDef attribute) {
            boolean results = false;
            if (attribute != null) {
                results = (attribute.getKind() == AttributeKind.COMPUTED) || (attribute.getAggregateFunction() != null);
            }
            return results;
        }

        @Override
        public boolean test(Tuple node) {
            Object attrs = accessor.apply(node);

            boolean results = false;

            if ((attrs != null) && Iterable.class.isAssignableFrom(attrs.getClass())) {
                Iterator iterator = ((Iterable) attrs).iterator();
                while (iterator.hasNext() && !results) {
                    results = expr.test(iterator.next());
                }
            } else {
                results = expr.test(attrs);
            }
            return results;
        }

    }

    static CsiDataType resolveDataType(String name, FieldDef field) {
        if ((field != null) && (field.getValueType() != null)) {
            return field.getValueType();
        }

        if (AttributeTypes.containsKey(name.toLowerCase())) {
            return AttributeTypes.get(name.toLowerCase());
        } else {
            return CsiDataType.String;
        }
    }

   static class OperationEqualsPredicate implements Predicate<Object> {
      private Object obj;

      public OperationEqualsPredicate(Object obj) {
         this.obj = obj;
      }

      @Override
      public boolean test(Object objArg) {
         return (((obj == null) && (objArg == null)) || obj.equals(objArg));
      }
   }

   static Predicate<Object> getOperationPredicate(FilterOperatorType filterOperatorType, Object rhs, String orig) {
      Predicate<Object> p;

      switch (filterOperatorType) {
         case EQUALS:
            p = new OperationEqualsPredicate(rhs).or(new EqualsPredicate(orig));
            break;
         case BEGINS_WITH:
            p = new BeginsWithPredicate(rhs);
            break;
         case CONTAINS:
            p = new ContainsPredicate(rhs);
            break;
         case ENDS_WITH:
            p = new EndsWithPredicate(rhs);
            break;
         case GT:
            p = new GreaterPredicate(rhs);
            break;
         case GEQ:
            p = new GreaterOrEqualPredicate(rhs);
            break;
         case LT:
            p = new LesserPredicate(rhs);
            break;
         case LEQ:
            p = new LesserOrEqualPredicate(rhs);
            break;
         case ISNULL:
         case NULL_OR_EMPTY:
         case EMPTY:
            p = isNull;
            break;
         default:
            p = new Predicate<Object>() {
               @Override
               public boolean test(Object obj) {
                  return false;
               }
            };
            break;
      }
      return p;
   }

   static Function<Node,Property> getAccessor(String name) {
      return new DetailsAccessor(name);
   }

   static class DetailsAccessor implements Function<Node,Property> {
      private String name;

      public DetailsAccessor(String name) {
         this.name = name;
      }

      @Override
      public Property apply(Node node) {
         NodeStore details = GraphManager.getNodeDetails(node);
         Map<String,Property> attributes = details.getAttributes();

         return attributes.get(name);
      }
   }

    /*
     * Poor man's equal check. This attempts to handle instances where comparison value is a String, but the run-time
     * instance that we compare to ( object in apply ) is strongly-typed.
     *
     * In that instance the standard equality check always fails; e.g. <Number>object.equals( "10" ) always fails since
     * "10" is not an instance of Number.
     *
     * The call to StringUtils.asString uses the default number formatter to produce a trimmed string for comparison
     * purposes.
     */
   static class EqualsPredicate implements Predicate<Object> {
      private String value;

      public EqualsPredicate(Object value) {
         if (value == null) {
            throw new IllegalArgumentException();
         }
         this.value = CsiTypeUtil.coerceString(value, (String) null);
      }

      @Override
      public boolean test(Object object) {
         return value.equalsIgnoreCase(CsiTypeUtil.coerceString(object, null));
      }
   }

    // FIXME: for all string operations need to ignore case
   static class BeginsWithPredicate implements Predicate<Object> {
      private String value;

      public BeginsWithPredicate(Object o) {
         if (o == null) {
            throw new IllegalArgumentException();
         }
         this.value = StringUtils.asString(o);
         this.value = this.value.toLowerCase();
      }

      @Override
      public boolean test(Object object) {
         boolean result = false;

         if (object != null) {
            // FIXME: CsiTypeUtil.tostring...
            String s = StringUtils.asString(object).toLowerCase();
            result = s.startsWith(value);
         }
         return result;
      }
   }

   static class ContainsPredicate implements Predicate<Object> {
      private String value;

      public ContainsPredicate(Object o) {
         if (o == null) {
            throw new IllegalArgumentException();
         }
         value = StringUtils.asString(o).toLowerCase();
      }

      @Override
      public boolean test(Object object) {
         boolean result = false;

         if (object != null) {
            String s = StringUtils.asString(object).toLowerCase();
            result = s.contains(value);
         }
         return result;
      }
   }

   static class EmptyPredicate implements Predicate<Object> {
      @Override
      public boolean test(Object object) {
         String s = StringUtils.asString(object);
         return (s != null) && (s.length() == 0);
      }
   }

   static class EndsWithPredicate implements Predicate<Object> {
      private String value;

      public EndsWithPredicate(Object o) {
         if (o == null) {
            throw new IllegalArgumentException();
         }
         value = StringUtils.asString(o).toLowerCase();
      }

      @Override
      public boolean test(Object object) {
         boolean result = false;

         if (object != null) {
            String s = StringUtils.asString(object).toLowerCase();
            result = s.endsWith(value);
         }
         return result;
      }
   }

    /*
     * NOTE: For the base comparisons Greater and GreaterOrEqual we are using the supplied/query condition. Since we
     * compare against this value, we need to look on the other side. i.e. we are constructing something like:
     *
     * Admission Date is greater than 1996-01-01.
     *
     * So for all values prior to 1996, we return true. At runtime we are doing the following comparison:
     *
     * 1996-01-01.compareTo( instance ).
     *
     * so a negative value indicates that the instance is greater than our query value.
     */
   static class GreaterPredicate implements Predicate<Object> {
      protected Comparable value;

      public GreaterPredicate(Object o) {
         if ((o == null) || !(o instanceof Comparable)) {
            throw new IllegalArgumentException();
         }
         value = (Comparable) o;
      }

      @Override
      public boolean test(Object o) {
         boolean result = false;

         if (o != null) {
            try {
               result = (value.compareTo(o) < 0);
            } catch (IllegalArgumentException iae) {
            }
         }
         return result;
      }
   }

   static class GreaterOrEqualPredicate extends GreaterPredicate {
      public GreaterOrEqualPredicate(Object o) {
         super(o);
      }

      @Override
      public boolean test(Object o) {
         boolean result = false;

         if (o != null) {
            try {
               result = (value.compareTo(o) <= 0);
            } catch (IllegalArgumentException iae) {
            }
         }
         return result;
      }
   }

   static class LesserPredicate implements Predicate<Object> {
      private Predicate<Object> inverse;

      public LesserPredicate(Object o) {
         inverse = new GreaterOrEqualPredicate(o).negate();
      }

      @Override
      public boolean test(Object o) {
         return inverse.test(o);
      }
   }

   static class LesserOrEqualPredicate implements Predicate<Object> {
      private Predicate<Object> inverse;

      public LesserOrEqualPredicate(Object o) {
         inverse = new GreaterPredicate(o).negate();
      }

      @Override
      public boolean test(Object o) {
         return inverse.test(o);
      }
   }
}
