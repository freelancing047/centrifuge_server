package csi.server.common.service.api;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.sencha.gxt.data.shared.loader.FilterPagingLoadConfig;
import com.sencha.gxt.data.shared.loader.PagingLoadResult;

import csi.server.business.visualization.graph.pattern.model.PatternMeta;
import csi.server.business.visualization.legend.GraphLegendInfo;
import csi.server.business.visualization.legend.GraphLinkLegendItem;
import csi.server.business.visualization.legend.GraphNodeLegendItem;
import csi.server.common.dto.CsiMap;
import csi.server.common.dto.graph.GraphInfo;
import csi.server.common.dto.graph.GraphOperation;
import csi.server.common.dto.graph.GraphRequest;
import csi.server.common.dto.graph.GraphStateFlags;
import csi.server.common.dto.graph.gwt.AnnotationDTO;
import csi.server.common.dto.graph.gwt.DragStartDTO;
import csi.server.common.dto.graph.gwt.EdgeListDTO;
import csi.server.common.dto.graph.gwt.FindItemDTO;
import csi.server.common.dto.graph.gwt.ItemInfoDTO;
import csi.server.common.dto.graph.gwt.NodeListDTO;
import csi.server.common.dto.graph.gwt.NodePositionDTO;
import csi.server.common.dto.graph.gwt.PatternHighlightRequest;
import csi.server.common.dto.graph.gwt.PlunkLinkDTO;
import csi.server.common.dto.graph.gwt.PlunkNodeDTO;
import csi.server.common.dto.graph.gwt.PlunkedItemsToDeleteDTO;
import csi.server.common.dto.graph.path.FindAllNodesMetaRequest;
import csi.server.common.dto.graph.path.FindPathRequest;
import csi.server.common.dto.graph.path.FindPathResponse;
import csi.server.common.dto.graph.pattern.PatternResultSet;
import csi.server.common.dto.graph.search.NodeInfo;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.graphics.shapes.ShapeType;
import csi.server.common.model.visualization.graph.Annotation;
import csi.server.common.model.visualization.graph.GraphCachedState;
import csi.server.common.model.visualization.graph.PlunkedLink;
import csi.server.common.model.visualization.graph.PlunkedNode;
import csi.server.common.model.visualization.selection.SelectionModel;
import csi.shared.gwt.viz.graph.GraphGetDisplayResponse;
import csi.shared.gwt.viz.graph.MultiTypeInfo;
import csi.shared.gwt.viz.graph.tab.pattern.settings.GraphPattern;
import csi.shared.gwt.vortex.CsiPair;
import csi.shared.gwt.vortex.VortexService;

public interface GraphActionServiceProtocol extends VortexService {

	public void doAppearanceEditTask(String vizUuid, String dvUuid, String elementId, String name, String value,
			String type) throws CentrifugeException;

	public void dohideUnhideTask(String vizUuid, String dvUuid, String elementId, String type, String flag)
			throws CentrifugeException;

	public void unhideSelection(String dvUuid, String vizUuid) throws CentrifugeException;

	public void unhideAll(String dvUuid, String vizUuid) throws CentrifugeException;

	public List<Integer> hideSelection(String dvUuid, String vizUuid) throws CentrifugeException;

	public void hideUnSelected(String dvUuid, String vizUuid) throws CentrifugeException;

	public Boolean isEmpty(String vizUuid);

	public void loadGraph(String vizUuid, String dvUuid, String viewWidth, String viewHeight)
			throws CentrifugeException;

	public GraphInfo summary(String vizId) throws CentrifugeException;

	public void visualizeNodes(String vizId, GraphRequest request) throws CentrifugeException;

	public void retractNodes(String vizId, String selection) throws CentrifugeException;

	public GraphStateFlags getGraphStateFlags(String vizId) throws CentrifugeException;

	public SelectionModel clearSelection(String id, String vizuuid) throws CentrifugeException;

	public SelectionModel select(String vizuuid, Boolean resetSelection, Boolean resetNodes, Boolean resetLinks,
			GraphRequest request) throws CentrifugeException;

	public SelectionModel selectAll(String id, String vizuuid) throws CentrifugeException;

	public SelectionModel invertSelection(String id, String vizuuid) throws CentrifugeException;

	public SelectionModel selectRegion(String vizuuid, Double x, Double y, Double x2, Double y2, String selid,
			Boolean reset) throws CentrifugeException;

	public SelectionModel deselectRegion(String vizuuid, Double x, Double y, Double x2, Double y2, String selid,
			Boolean reset) throws CentrifugeException;

	public int degrees(String vizuuid, Integer nSteps, String id) throws CentrifugeException;

	public void selectLinkById(String vizuuid, String id, String resetString) throws CentrifugeException;

	public void selectNodeById(String vizuuid, Integer id, String resetString) throws CentrifugeException;

	public SelectionModel selectionByPoint(String vizuuid, Double x, Double y, Boolean reset)
			throws CentrifugeException;

	public void selectItemAt(String vizuuid, Double x, Double y, Boolean reset) throws CentrifugeException;

	public SelectionModel selectNodesByType(String vizuuid, String dvUuid, String nodeType, Boolean addToSelection)
			throws CentrifugeException;

	public SelectionModel toggleNodeSelectionByType(String vizuuid, String dvUuid, String nodeKey,
			String selectionOperation) throws CentrifugeException;

	public SelectionModel toggleLinkSelectionByType(String vizuuid, String dvUuid, String linkKey,
			String selectionOperation) throws CentrifugeException;

	public void clearMergeHighlights(String vizuuid) throws CentrifugeException;

	public void clearGraphBeforeLoad(String vizUuid, String value) throws CentrifugeException;

	public List<CsiMap<String, String>> componentLayoutAction(String vizUuid, String componentIDParam, String x,
			String y, String action, String value, String asyncParam) throws CentrifugeException;

	public DragStartDTO gwtDragStart(String startx, String starty, String vizUuid);

	public String dragEnd(String endx, String endy, String vizUuid) throws CentrifugeException;

	public void graphMeta(String vizUuid, Boolean nodesOnly) throws CentrifugeException, IOException;

	public List<NodeListDTO> gwtNodeListing(String vizUuid);

	public Collection<Integer> nodeNeighbors(String vizUuid, Integer nodeId) throws CentrifugeException;

	public List<CsiMap<String, Integer>> getNodeNeighborCounts(String vizUuid, Integer nodeId)
			throws CentrifugeException;

	public List<EdgeListDTO> gwtEdgeListing(String vizUuid) throws CentrifugeException;

	public void fitToSelection(String vizUuid) throws CentrifugeException;

	public void fitToSize(String vizUuid, int width, int height) throws CentrifugeException;

	public GraphLegendInfo legendData(String vizUuid) throws CentrifugeException;

	public void zoomTo(String zoom, String vizUuid) throws CentrifugeException;

	public void zoomPercent(String percent, String vizUuid) throws CentrifugeException;

	public FindItemDTO gwtFindItem2(int x1, int y1, String vizUuid, boolean all) throws CentrifugeException;
	public FindItemDTO getItem(String vizUuid, FindItemDTO item, boolean all);

	public FindItemDTO gwtFindItem(String id, String x1, String y1, String vizUuid) throws CentrifugeException;
	public FindItemDTO gwtFindItemNear(String id, String x1, String y1, String vizUuid);

	public SelectionModel getSelectionModel(String id, String vizuuid) throws CentrifugeException;

	public void panTo(String x1, String y1, String vizUuid) throws CentrifugeException;

	public boolean hasBundleSelected(String vizUuid, String id) throws CentrifugeException;

	public Map<String, List<Map<String, String>>> selectionInfo(String vizUuid) throws CentrifugeException;

	public NodePositionDTO gwtGetNodePosition(String vizUuid, Integer nodeid) throws CentrifugeException;

	public void setNodePosition(String vizUuid, Integer nodeid, String x, String y, Boolean abs)
			throws CentrifugeException;

	public ItemInfoDTO gwtItemInfo(String vizUuid, Integer itemid, String itemType, Boolean includeTooltips)
			throws CentrifugeException;

	public void bundleSelectionBySpec(String vizUuid, String dvUuid) throws CentrifugeException;

	public void bundleEntireGraphBySpec(String vizUuid, String dvUuid) throws CentrifugeException;

	public void unbundleEntireGraph(String vizUuid, String dvUuid) throws CentrifugeException;

	public void unbundleSelection(String vizUuid, String dvUuid) throws CentrifugeException;

	public CsiMap<String, String> operateOn(String vizUuid, String dvUuid, GraphOperation operation)
			throws CentrifugeException;

	public void unbundleSingleNode(String vizId, Integer id) throws CentrifugeException;

	public void zoomToRegion(String vizUuid, String dvUuid, Double x1, Double x2, Double y1, Double y2)
			throws CentrifugeException;

	public void componentAction(String action, String value, String vizId) throws CentrifugeException;

	public int showAdjacentFor(String vizId, String type) throws CentrifugeException;

	public int showAdjacentFor(String vizId, String id, String type) throws CentrifugeException;

	public void computeSNA(String vizId, String metric) throws CentrifugeException;

	public String getTile(String vzuuid, String tileWidthStr, String tileHeightStr, String zoomLevelStr,
			String tileXStr, String tileYStr) throws CentrifugeException, IOException;

	public GraphGetDisplayResponse getDisplay(String vizuuid, String viewWidth, String viewHeight)
			throws CentrifugeException, IOException;

	public String getDragImage(String vizUuid) throws CentrifugeException, IOException;

	public void showLabels(String vizUuid, Boolean showNodeLabels) throws CentrifugeException;

	public void saveGraph(String vizUuid) throws CentrifugeException, IOException;

	public byte[] exportGraph(String vizUuid) throws CentrifugeException, IOException;

	public int selectVisibleNeighbors(String vizUuid, Integer degreesAway);

	public int selectVisibleNeighbors(String vizUuid, Integer degreesAway, Integer nodeId);

	public void manuallyBundleSelection(String vizUuid, String dvUuid, String bundleName) throws CentrifugeException;

	public boolean hasSelection(String vizUuid) throws CentrifugeException;

	public FindPathResponse findPaths(String vizId, FindPathRequest request) throws CentrifugeException;

	public List<NodeInfo> findAllNodesMeta(FindAllNodesMetaRequest request) throws CentrifugeException;

	public CsiMap<String, String> findNodeMeta(String idStr, String xStr, String yStr, String vizUuid)
			throws CentrifugeException;

	public boolean highlightPaths(String vizId, List<String> pathIds) throws CentrifugeException;

	public SelectionModel selectPaths(String vizId, List<String> pathIds, String addStr, String selNode, String selLink)
			throws CentrifugeException;

	public String getNodeAsImage(String iconURI, ShapeType shape, int color, int size, double iconScale);

	public PagingLoadResult<NodeListDTO> getPageableNodeListing(String vizUuid, FilterPagingLoadConfig loadConfig);

	public String exportNodeList(String vizUuid, FilterPagingLoadConfig loadConfig, List<String> visibleCols);

	public String exportLinkList(String vizUuid, FilterPagingLoadConfig loadConfig, List<String> visibleCols);

	public PagingLoadResult<EdgeListDTO> getPageableEdgeListing(String vizUuid, FilterPagingLoadConfig loadConfig);

	public void panTo(int xOffset, int yOffset, String vizUuid);

	public void loadGraph(String vizUuid, String dataviewUuid, int width, int height) throws CentrifugeException;

	void fitToRegion(String vizUUID, int x, int y, int width, int height);

	void hideNodeById(String vizUuid, ArrayList<Integer> nodeIds);

	void unhideNodeById(String vizUuid, ArrayList<Integer> nodeIds);

	void manuallyBundleNodesById(String vizUuid, ArrayList<Integer> nodeIds, String bundleName);

	void unbundleNodesById(String vizId, ArrayList<Integer> nodeIds) throws CentrifugeException;

	void showOnlySelection(String dvUuid, String vizUuid);

	public void revealNeighborsOfSelectedNodes(String uuid);

	void unhideLinkById(String vizUuid, ArrayList<Integer> LinkIds);

	void hideLinkById(String vizUuid, ArrayList<Integer> linkIds);

	void showOnlyPaths(String vizUUID, List<String> pathIds);

	public PlunkedNode plunkNewNode(PlunkNodeDTO plunkNodeDTO) throws CentrifugeException;

	public PlunkedLink plunkLink(PlunkLinkDTO plunkLinkDTO) throws CentrifugeException;

	public PlunkedItemsToDeleteDTO deletePlunkedItem(String vizUuid, String itemKey, String objectType) throws CentrifugeException;

	public void deleteAllPlunkedItems(String vizUuid) throws CentrifugeException;

	public void savePlunkedNode(String vizUuid, PlunkedNode plunkedNode) throws CentrifugeException;

	public void savePlunkedLink(String vizUuid, PlunkedLink plunkedNodeDef) throws CentrifugeException;

	List<String> getNodeTypes(String uuid);

	public boolean isDuplicate(String vizUuid, String name, String type);

	List<String> getLinkTypes(String uuid);

	public Boolean ensureViewport(String uuid, int width, int height);

	public Annotation addAnnotation(AnnotationDTO annotationDTO) throws CentrifugeException;

	public Annotation retrieveAnnotation(AnnotationDTO annotationDTO);

	public void removeAnnotation(AnnotationDTO annotationDTO) throws CentrifugeException;

	PatternResultSet findPatterns(String uuid, GraphPattern pattern);

	void highlightPatterns(String uuid, List<PatternHighlightRequest> patternHighlightRequests);

	Boolean validateBundleName(String graphUuid, String suggestedName);

	void showOnlyPatterns(String uuid, List<PatternMeta> patterns);

	void selectPatterns(String uuid, List<PatternMeta> patterns, boolean selectNodes, boolean selectLinks);

	void addSelectPatterns(String uuid, List<PatternMeta> selectedPatterns, boolean selectNodes, boolean selectLinks);

	String getNodeAsImageNew(String iconId, ShapeType shape, int color, int size, double iconScale)
			throws CentrifugeException;

	String getNodeAsImageNew(String iconId, boolean isMap, ShapeType shape, int color, float alpha, boolean isSelected,
			boolean isHighlighted, boolean isCombined, int size, double iconScale, int strokeSize, boolean useSummary,
			boolean isNew, boolean isUpdated) throws CentrifugeException;

	GraphCachedState saveLegendCache(String vizUuid, List<String> itemOrderList, List<GraphNodeLegendItem> list, List<GraphLinkLegendItem> list2) throws CentrifugeException;

	String getBundleIcon(int size, double iconScale) throws CentrifugeException;

	MultiTypeInfo gwtFindItemTypes(int x1, int y1, String vizUuid) throws CentrifugeException;

    CsiPair<Boolean, Boolean> showLastLinkupHighlights(String vizUuid, String dvUuid);

    void clearCache(String vizUuid);
}
