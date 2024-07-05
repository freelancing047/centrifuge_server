/*
* @(#) GraphConstants.java,  18.02.2010
*
*/
package csi.server.common.model.visualization.graph;


public class GraphConstants {

    public static final String NEW_GENERATION_FIELD_TYPE = "Newly Added";
    public static final String UPDATED_GENERATION_FIELD_TYPE = "In Common";
    public static final String ALL_FIELD_TYPE = "all";

    public static enum eLayoutAlgorithms {
        forceDirected, circular, circle, treeRadial, treeNodeLink, scramble, centrifuge, applyForce, grid
        // TODO etc.
    }

    public static final String COMPONENT_ID = "component_id";
    public static final String NODE_DETAIL = "nodeDetail";
    public static final String LINK_DETAIL = "linkDetail";
    public static final String COMPONENT_COUNT = "numberComponents";
    public static final String COMPONENTS_DIRECTED = "componentsConsiderDirection";
    public static final String COMPONENT_ROOT_SELECTION = "componentsRootSelection";
    public static final String VISUALIZATION = "prefuse.visualization";
    public static final String SUBGRAPH_NODE_ID = "subgraph.node.id";
    public static final String ORIG_NODE_ID = "parent.node.id";
    public static final String COMPONENTS = "subgraph.components";
    public static final String NODE_COUNT = "nodeCount";
    public static final String LOWER_XCOORD = "lowerXCoord";
    public static final String LOWER_YCOORD = "lowerYCoord";
    public static final String PATCH_DIMENSION = "patchDimension";

    public static final int DIMENSION_WIDTH = 1000;
    public static final String ROOT_GRAPH = "visualGraph";

    public static final String PATCH_REGIONS = "patchRegions";
    public static final String PATCH_REGION = "patchRegion";
    public static final String STORE = "object";
    public static final String PATCH_BOUNDS = "patchRegionBounds";
    public static final String CSI_INTERNAL_NAMESPACE = "csi.internal";

    public static final String SELECTIONS = "selections";

    @Deprecated
    public static final String SELECTED_NODES = "selectedNodes";
    @Deprecated
    public static final String SELECTED_LINKS = "selectedLinks";
    public static final String KEY_CONTEXTS_GRAPH = "context.graphs";
    public static final String KEY_CONTEXTS_MAP = "context.map";
    public static final String KEY_CONTEXTS_MAP_PLACE_DYNAMICTYPEINFO = "context.map.place.dynamictypeinfo";
    public static final String KEY_CONTEXTS_MAP_TRACK_SUMMARY_DYNAMICTYPEINFO = "context.map.track.summary.dynamictypeinfo";
    public static final String KEY_CONTEXTS_MAP_TRACK_DYNAMICTYPEINFO = "context.map.track.dynamictypeinfo";

    public static final String NEW_GENERATION = "newGeneration";
    public static final String UPDATED_GENERATION = "updatedGeneration";
    public static final String PATH_HIGHLIGHT = "pathHighlight";
    public static final String PATTERN_HIGHLIGHT = "patternHighlight";

    public static final String UNSPECIFIED_LINK_TYPE = "Link";
    public static final String UNSPECIFIED_TYPE = "Unspecified Type";
    public static final String UNSPECIFIED_NODE_TYPE = "Node";
    public static final String BUNDLED_NODES = "Bundle";
    public static final String BUNDLED_LINKS = "Derived Link";

    public static final String DRAG_ITEMS = "dragItems";

    public static final String COUNT_IN_DISPEDGES = "count.in.disp.edges";

    public static final String SELECTION_OPERATION_APPEND = "selectionOperationAppend";
    public static final String SELECTION_OPERATION_CLEAR = "selectionOperationClear";
    public static final String SELECTION_OPERATION_DESELECT = "selectionOperationDeselect";
    public static final String LABEL = "label";
    public static final String TYPE = "type";
    public static final String DOC_ID = "doc.id";
    public static final String STORAGE = "storage";

    public static final String MultiTypeKey = "urn:csi:graph:multi-types";
    public static final String MultiTypeLinkKey = "urn:csi:graph:multi-types:links";

}
