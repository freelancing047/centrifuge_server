package csi.server.business.helper;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.thoughtworks.xstream.XStream;

import prefuse.visual.EdgeItem;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualGraph;

import csi.security.CsiSecurityManager;
import csi.security.queries.AclRequest;
import csi.server.business.cachedb.DataSyncListener;
import csi.server.business.cachedb.dataset.DataSetUtil;
import csi.server.business.cachedb.script.CsiScriptRunner;
import csi.server.business.cachedb.script.ecma.EcmaScriptRunner;
import csi.server.business.helper.DeepCloner.CloneType;
import csi.server.business.helper.field.FieldCycleDetector;
import csi.server.business.helper.field.FieldReferencesFromDataView;
import csi.server.business.helper.linkup.LinkupSelectionHelper;
import csi.server.business.helper.linkup.ParameterSetFactory;
import csi.server.business.selection.cache.SelectionBroadcastCache;
import csi.server.business.selection.torows.SelectionToRowsCoverterFactory;
import csi.server.business.service.VisualizationActionsService;
import csi.server.business.visualization.graph.GraphContext;
import csi.server.business.visualization.graph.GraphManager;
import csi.server.business.visualization.graph.GraphServiceUtil;
import csi.server.business.visualization.graph.base.LinkStore;
import csi.server.business.visualization.graph.base.NodeStore;
import csi.server.business.visualization.graph.data.GraphDataManager;
import csi.server.common.codec.xstream.XStreamHelper;
import csi.server.common.dto.AuthDO;
import csi.server.common.dto.FieldListAccess;
import csi.server.common.dto.LaunchParam;
import csi.server.common.dto.Response;
import csi.server.common.dto.resource.ImportStatus;
import csi.server.common.dto.resource.ImportStatusType;
import csi.server.common.dto.system.ReleaseInfo;
import csi.server.common.enumerations.CsiDataType;
import csi.server.common.enumerations.LinkupMode;
import csi.server.common.enumerations.ServerMessage;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.linkup.LinkupHelper;
import csi.server.common.linkup.LinkupResponse;
import csi.server.common.linkup.LinkupValidationReport;
import csi.server.common.model.ConnectionDef;
import csi.server.common.model.CsiUUID;
import csi.server.common.model.DataModelDef;
import csi.server.common.model.DataSetOp;
import csi.server.common.model.DataSourceDef;
import csi.server.common.model.FieldDef;
import csi.server.common.model.FieldType;
import csi.server.common.model.GenericProperties;
import csi.server.common.model.ModelObject;
import csi.server.common.model.ParamMapEntry;
import csi.server.common.model.Property;
import csi.server.common.model.Resource;
import csi.server.common.model.SqlTableDef;
import csi.server.common.model.SqlTokenTreeItem;
import csi.server.common.model.SqlTokenTreeItemList;
import csi.server.common.model.column.ColumnDef;
import csi.server.common.model.column.ColumnFilter;
import csi.server.common.model.dataview.DataView;
import csi.server.common.model.dataview.DataViewDef;
import csi.server.common.model.dataview.DataViewType;
import csi.server.common.model.extension.Classification;
import csi.server.common.model.extension.ExtensionData;
import csi.server.common.model.extension.Labels;
import csi.server.common.model.extension.SimpleExtension;
import csi.server.common.model.filter.Filter;
import csi.server.common.model.filter.FilterDefinition;
import csi.server.common.model.filter.FilterExpression;
import csi.server.common.model.functions.ScriptFunction;
import csi.server.common.model.linkup.LinkupMapDef;
import csi.server.common.model.linkup.LooseMapping;
import csi.server.common.model.map.Basemap;
import csi.server.common.model.operator.OpJoinType;
import csi.server.common.model.operator.OpMapItem;
import csi.server.common.model.operator.OpMapType;
import csi.server.common.model.query.QueryDef;
import csi.server.common.model.query.QueryParameterDef;
import csi.server.common.model.themes.Theme;
import csi.server.common.model.visualization.VisualizationDef;
import csi.server.common.model.visualization.graph.RelGraphViewDef;
import csi.server.common.model.visualization.map.MapViewDef;
import csi.server.common.model.visualization.selection.NullSelection;
import csi.server.common.model.visualization.selection.Selection;
import csi.server.common.model.visualization.selection.SelectionModel;
import csi.server.common.util.ConnectorSupport;
import csi.server.common.util.Format;
import csi.server.common.util.ValuePair;
import csi.server.connector.ConnectionFactory;
import csi.server.connector.ConnectionFactoryManager;
import csi.server.dao.CsiPersistenceManager;
import csi.server.task.api.TaskHelper;
import csi.server.util.CacheUtil;
import csi.server.util.CsiUtil;
import csi.server.util.DateUtil;
import csi.server.util.FieldReferenceAugmentor;
import csi.server.util.FieldReferenceValidator.ValidationException;
import csi.server.util.SqlUtil;
import csi.shared.core.field.FieldReferences;


public class DataViewHelper {
   private static final Logger LOG = LogManager.getLogger(DataViewHelper.class);

    private VisualizationActionsService visualizationActionsService = null;

    public DataViewHelper() {
    }

    public DataViewHelper(VisualizationActionsService visualizationActionsServiceIn) {

        visualizationActionsService = visualizationActionsServiceIn;
    }

    // TODO: clean me up...post mako, create should not save
    public static DataView createDataView(String name, DataViewDef def) throws CentrifugeException {
        return createDataView(name, def, true);
    }

    public static DataView createDataView(String name, DataViewDef def, boolean save) throws CentrifugeException {
        DataView dv = new DataView(ReleaseInfo.version);
        dv.resetDates();
        dv.setName(name);
        dv.setRemarks(def.getRemarks());

        if (def.isTemplate()) {
            DataViewDef clone = DeepCloner.clone(def, CloneType.NEW_ID);
            clone.resetDates();
            clone.setTemplate(false);
            dv.setMeta(clone);
        } else {
            dv.setMeta(def);
        }

        if (save) {
            fixupPersistenceLinkage(dv);
            CsiPersistenceManager.persist(dv);
        }

        return dv;
    }

    public DataView createDataViewfromTemplate(String name, String templateUUID) throws CentrifugeException {
        DataViewDef def = CsiPersistenceManager.findObject(DataViewDef.class, templateUUID);
        if (def == null) {
            throw new CentrifugeException("No dataview template found with id = " + templateUUID);
        }
        return createDataView(name, def);
    }

    public Response<String, DataView> openDataView(String requestIdIn, DataView dv, List<LaunchParam> params,
                                                   List<AuthDO> auths) throws CentrifugeException, GeneralSecurityException {

        Long myRowCount = 0L;
        Boolean myMissingData = false;
        if (DataCacheHelper.cacheExists(dv.getUuid())) {
            dv.setLastOpenDate(new Date());
        } else {
            SecurityHelper.resetSecurityInfo(dv);
            ValuePair<Long, Boolean> myResults = launchDataView(dv, params, auths,
                    new ParameterSetFactory(dv.getMeta().getDataSetParameters(), params));
            myRowCount = myResults.getValue1();
            myMissingData = myResults.getValue2();
            DataViewHelper.mergeParameterData(dv.getParameterList(), params);
        }
        return new Response<String, DataView>(requestIdIn, dv, myRowCount, myMissingData);
    }
/*
    public DataView openDataView(ConnectionDef connectionIn, DataView dv, List<LaunchParam> params, List<AuthDO> auths) throws CentrifugeException, GeneralSecurityException {
        DataCacheHelper launcher = new DataCacheHelper();
        if (DataCacheHelper.cacheExists(dv.getUuid())) {
            dv.setLastOpenDate(new Date());
        } else {
            launchDataView(dv, params, auths, new ParameterSetFactory(dv.getMeta().getDataSetParameters(), params));
        }
        return dv;
    }

    public DataView copyLiveAsset(String assetUuid, String dvUuid) throws CentrifugeException {
        DataView assetdv = CsiPersistenceManager.findAsset(DataView.class, assetUuid, dvUuid);
        if (assetdv == null) {
            String eMsg = String.format("copyLiveAsset: cannot find source dataview '%s'", dvUuid);
            LOG.warn(eMsg);
            throw CentrifugeException.getLoggedException(eMsg);
        }

        // TODO: verify that it is a live asset?

        return copyDataView(assetdv);
    }
*/
    public DataView createLiveAsset(String dvUuid, int initialViewIndex) throws CentrifugeException {
        DataView srcdv = findDataView(dvUuid);

        DataView cloned = copyDataView(srcdv);
        cloned.getMeta().getModelDef().setInitialViewIndex(initialViewIndex);
        cloned.setType(DataViewType.LIVE);

        return cloned;
    }

    private DataView findDataView(String dvUuid) throws CentrifugeException {
        DataView srcdv = CsiPersistenceManager.findObject(DataView.class, dvUuid);
        if (srcdv == null) {
            String eMsg = String.format("createLiveAsset: cannot find source Dataview '%s'", dvUuid);
            LOG.warn(eMsg);
            throw CentrifugeException.getLoggedException(eMsg);
        }

        return srcdv;
    }

    private DataView copyDataView(DataView srcdv) throws CentrifugeException {
        DataView clonedv = DeepCloner.clone(srcdv, CloneType.NEW_ID);
        clonedv.setNeedsRefresh(0 == copyDvData(srcdv, clonedv));

        CsiPersistenceManager.persist(clonedv);

        return clonedv;
    }

    public long copyDvData(DataView srcdv, DataView targetdv) throws CentrifugeException {

        return 0L;
    }

    public static Map<String, String> retrieveResourceIcons(DataViewDef metaDataIn) {

        Map<String, String> myIconMap = new TreeMap<String, String>();

        DataModelDef myModel = (null != metaDataIn) ? metaDataIn.getModelDef() : null;
        List<VisualizationDef> myVisualizationList = (null != myModel) ? myModel.getVisualizations() : null;

        if (myVisualizationList != null) {

            for (VisualizationDef myVisualization : myVisualizationList) {

                if (myVisualization instanceof RelGraphViewDef) {


                } else if (myVisualization instanceof MapViewDef) {

                }
            }
        }
        return myIconMap;
    }

    public static void deleteCacheData(DataView dv) {
        if (dv == null) {
            return;
        }
        deleteDvData(dv);
        deleteVizData(dv, true);
        deleteFileArtifacts(dv.getUuid());
    }

    public  static void mergeParameterData(List<QueryParameterDef> parameterListIn, List<LaunchParam> parameterDataIn) {

        if ((null != parameterListIn) && !parameterListIn.isEmpty()
                && (null != parameterDataIn) && !parameterDataIn.isEmpty()) {

            Map<String, List<String>> myMap = new TreeMap<>();

            for (LaunchParam myParameterData : parameterDataIn) {

                myMap.put(myParameterData.getName(), myParameterData.getValues());
            }
            for (QueryParameterDef myParameter : parameterListIn) {

                List<String> myParameterData = myMap.get(myParameter.getName());

                if ((null != myParameterData) && !myParameterData.isEmpty()) {

                    ArrayList<String> myParameterValues = new ArrayList<>();

                    for (String myValue : myParameterData) {

                        myParameterValues.add(myValue);
                    }
                    myParameter.setValues(myParameterValues);
                }
            }
        }
    }

    public static boolean deleteDvData(DataView dataViewIn) {

        boolean mySuccess = true;

        if (null != dataViewIn) {

            try {

                DataCacheBuilder.clearCache(dataViewIn);

            } catch (CentrifugeException e) {

                LOG.warn("Failed to remove cache for delete Dataview: " + Format.value(dataViewIn.getName()));
                mySuccess = false;
            }
        }
        return mySuccess;
    }

    public static boolean deleteVizData(DataView dataViewIn, boolean includeAllIn) {

        boolean mySuccess = true;

        if (null != dataViewIn) {

            try {

                List<VisualizationDef> list = dataViewIn.getMeta().getModelDef().getVisualizations();
                if (null != list) {
                    for (VisualizationDef def : list) {
                        if (def instanceof RelGraphViewDef) {
                            if (includeAllIn || def.isBroadcastListener()) {
                                GraphDataManager.deleteContext(def.getUuid());
                            }

                            RelGraphViewDef rgDef = (RelGraphViewDef) def;
                            rgDef.setOldSelection(null);
                            //rgDef.setPlayerSettings(new GraphPlayerSettings());
                        }
                    }
                }

            } catch (Exception myException) {

                LOG.warn("Failed to remove visualization cache for delete Dataview: " + Format.value(dataViewIn.getName()));
                mySuccess = false;
            }
        }
        return mySuccess;
    }

    private static DataView beginSpinOff(DataView sourceDataViewIn, String spinOffNameIn) {

        DataView mySpinOff = DataViewHelper.fixupPersistenceLinkage(DeepCloner.clone(sourceDataViewIn, CloneType.NEW_ID));
        mySpinOff.setName(spinOffNameIn);
        mySpinOff.setSpinoff(true);

        clearDataReferences(mySpinOff);

        return mySpinOff;
    }

    private static DataView continueSpinOff(DataView spinOffIn, long rowCountIn) throws CentrifugeException, SQLException {

        if (rowCountIn == 0) {
            deleteCacheData(spinOffIn);
            return null;
        }
        spinOffIn.setSize(rowCountIn);
        clearSpinoffSelections(spinOffIn);

        CsiPersistenceManager.persist(spinOffIn);
        CsiPersistenceManager.begin();

        DataCacheBuilder.finalizeSpinOff(spinOffIn);
        spinOffIn.setViews(CacheUtil.getCacheTableName(spinOffIn.getUuid()));
        spinOffIn.setTables(CacheUtil.getCacheTableName(spinOffIn.getUuid(), 0));
        spinOffIn.setNeedsRefresh(false);
        CsiPersistenceManager.merge(spinOffIn);

        return spinOffIn;
    }

    public static DataView createSpinOff(DataView sourceDataViewIn, String spinOffNameIn,
                                         VisualizationDef visualizationIn, Selection selectionIn)
            throws CentrifugeException, GeneralSecurityException, SQLException {

        DataView mySpinOff = beginSpinOff(sourceDataViewIn, spinOffNameIn);
        Set<Integer> mySelectionIds = prepareSelection(sourceDataViewIn, visualizationIn, selectionIn);
        long myRowCount = DataCacheBuilder.initializeSpinOffCache(sourceDataViewIn, null, mySpinOff, mySelectionIds);

        return continueSpinOff(mySpinOff, myRowCount);
    }

    public static Set<Integer> prepareSelection(DataView sourceDataViewIn, VisualizationDef visualizationIn, Selection selectionIn) {

        if (visualizationIn instanceof RelGraphViewDef) {
            GraphContext context = GraphServiceUtil.getGraphContext(visualizationIn.getUuid());
            VisualGraph visualGraph = context.getVisualGraph();

            SelectionModel graphSelection = (SelectionModel) getGraphSelection(visualizationIn.getUuid()).copy();
            for (Integer nodeId : new ArrayList<Integer>(graphSelection.nodes)) {
                NodeItem node = (NodeItem) visualGraph.getNode(nodeId);
                NodeStore nodeDetails = GraphManager.getNodeDetails(node);
                if (nodeDetails.isHidden()) {
                    graphSelection.nodes.remove(nodeId);
                }
            }
            for (Integer linkid : new ArrayList<Integer>(graphSelection.links)) {
                EdgeItem edge = (EdgeItem) visualGraph.getEdge(linkid);
                LinkStore details = GraphManager.getEdgeDetails(edge);
                if (details.isHidden()) {
                    graphSelection.links.remove(linkid);
                }
            }
            SelectionBroadcastCache.getInstance().addSelection(visualizationIn.getUuid(), graphSelection);
        }
        return getSelectionIds(sourceDataViewIn, visualizationIn, selectionIn);
    }

    private static Set<Integer> getSelectionIds(DataView oldDataViewIn, VisualizationDef vizDefIn, Selection selectionIn) {

        Set<Integer> myRowIds = null;

        if (null != vizDefIn) {

            if(null == selectionIn){

                SelectionBroadcastCache myCache = SelectionBroadcastCache.getInstance();

                selectionIn = myCache.getSelection(vizDefIn.getUuid());
                if(selectionIn instanceof NullSelection){

                    selectionIn = vizDefIn.getSelection();
                }
            }
            myRowIds
                    = new SelectionToRowsCoverterFactory(oldDataViewIn, vizDefIn).create().convertToRows(selectionIn, false);
        }
        return myRowIds;
    }

    private static Selection getGraphSelection(String graphUuid) {
        GraphContext gc = GraphServiceUtil.getGraphContext(graphUuid);
        if (gc != null) {
            synchronized (gc) {
                return gc.getSelection(GraphManager.DEFAULT_SELECTION);
            }
        }
        return NullSelection.instance;
    }

    private static void clearSpinoffSelections(DataView spindv) {
        SelectionBroadcastCache cache = SelectionBroadcastCache.getInstance();
        for (VisualizationDef viz : spindv.getMeta().getModelDef().getVisualizations()) {
            Selection selection = cache.getSelection(viz.getUuid());
            selection.clearSelection();
        }
    }

   @SuppressWarnings("unchecked")
   public DataView getDataViewByName(String name) throws CentrifugeException {
      return AclRequest.findResourceByName(DataView.class, name);
   }

   public String getDataViewUuid(String name) throws CentrifugeException {
      DataView dv = getDataViewByName(name);

      return (dv == null) ? null : dv.getUuid();
   }

   public <T extends ModelObject> T cloneModelObject(T obj, CloneType cloneType) {
      return DeepCloner.clone(obj, cloneType);
   }

    public static boolean deleteFileArtifacts(String uuid) {

        boolean mySuccess = true;

        try {

            File snapshotDir = new File("snapshots");
            File assetDir = new File("webapps/Centrifuge/assets");

            File pdffile = new File(assetDir, uuid + ".pdf");
            if (pdffile.exists()) {
                pdffile.delete();
            }

            File snapfile = new File(snapshotDir, uuid + ".png");
            if (snapfile.exists()) {
                snapfile.delete();
            }

        } catch(Exception myException) {

            LOG.warn("Failed to remove artifacts for delete Dataview: " + uuid);
            mySuccess = false;
        }
        return mySuccess;
    }

    public static void handleChangingNames(FieldListAccess modelIn, List<FieldDef> newListIn, List<FieldDef> changeListIn, List<FieldDef> discardListIn)
            throws CentrifugeException {

        List<FieldDef> myScriptedList = new ArrayList<FieldDef>();
        Set<String> myDisregardMap = new HashSet<String>();

        for (FieldDef myItem : newListIn) {

            myDisregardMap.add(myItem.getUuid());

            if ((FieldType.DERIVED == myItem.getFieldType())
                    || (FieldType.SCRIPTED == myItem.getFieldType())) {

                myScriptedList.add(myItem);
            }
        }

        for (FieldDef myItem : changeListIn) {

            myDisregardMap.add(myItem.getUuid());

            if ((FieldType.DERIVED == myItem.getFieldType())
                    || (FieldType.SCRIPTED == myItem.getFieldType())) {

                myScriptedList.add(myItem);
            }
        }

        for (FieldDef myItem : discardListIn) {

            myDisregardMap.add(myItem.getUuid());
        }

        for (FieldDef myItem : modelIn.getFieldDefList()) {

            if (((FieldType.DERIVED == myItem.getFieldType())
                    || (FieldType.SCRIPTED == myItem.getFieldType()))
                    && (!myDisregardMap.contains(myItem.getUuid()))) {

                myScriptedList.add(myItem);
            }
        }

        if (!myScriptedList.isEmpty()) {

            for (FieldDef myNewField : changeListIn) {

                String myNewName = myNewField.getFieldName();
                FieldDef myOldField = modelIn.getFieldDefByLocalId(myNewField.getLocalId());

                if (null != myOldField) {

                    String myOldName = myOldField.getFieldName();

                    if (!myOldName.equals(myNewName)) {

                        updateFieldNameReferences(myScriptedList, myOldName, myNewName);
                    }

                } else {

                    throw new CentrifugeException("Attempting to update a non-existent field definition: "
                                                    + Format.value(myNewName));
                }
            }
        }
    }

    public static boolean addFieldDef(DataView dataViewIn, FieldDef fieldIn)
            throws CentrifugeException, SQLException {

        FieldListAccess myModel = dataViewIn.getMeta().getModelDef().getFieldListAccess();

        fieldIn.setDirty(true);
        myModel.addFieldDef(fieldIn);

        return true;
    }

    public static boolean updateFieldDef(DataView dataViewIn, FieldDef fieldIn)
            throws CentrifugeException, SQLException {

        FieldListAccess myModel = dataViewIn.getMeta().getModelDef().getFieldListAccess();
        FieldDef myOldField = myModel.getFieldDefByUuid(fieldIn.getUuid());
        List<ScriptFunction> myOldList = myOldField.getFunctions();
        boolean myTypeChange = false;

        fieldIn.setOrdinal(myOldField.getOrdinal());
        if (fieldIn.isPreCalculated()) {

            fieldIn.setDirty(true);
            if ((FieldType.DERIVED != fieldIn.getFieldType())
                    && (FieldType.SCRIPTED != fieldIn.getFieldType())) {

                myTypeChange = (myOldField.tryStorageType() != fieldIn.getValueType());
            }
        }
        myOldField.updateInPlace(fieldIn);
        if (null == myOldField.getFunctions()) {

            myOldField.setFunctions(myOldList);

            for (ScriptFunction myFunction:myOldList) {

                CsiPersistenceManager.deleteObject(myFunction);
            }
            myOldList.clear();
        }
        return myTypeChange;
    }

    public static void checkForCycles(FieldListAccess modelIn, Collection<FieldDef> listIn) throws CentrifugeException {

        for (FieldDef myField : listIn) {

            if ((new FieldCycleDetector(modelIn)).detectCycle(myField)) {
                throw new CentrifugeException("Failed to add/update field "
                        + Format.value(myField.getFieldName()) + ": A cycle has been detected.");
            }
        }
    }

    public static void updateFieldOrder(FieldListAccess modelIn, Collection<String> listIn) {

        if (null != listIn) {

            int myOrdinal = 0;
//            List<FieldDef> myList = new ArrayList<FieldDef>(listIn.size());

            for (String myKey : listIn) {

                FieldDef myField = modelIn.getFieldDefByUuid(myKey);

                if (null != myField) {

                    myField.setOrdinal(myOrdinal++);
//                    myList.add(myField);
                }
            }
//            modelIn.setFieldDefList(myList);
        }
    }

    public void invalidateAffectedVisualizations(DataView dataViewIn, List<FieldDef> listIn) {

        invalidateAffectedVisualizations(dataViewIn, listIn, visualizationActionsService);
    }

    public static void invalidateAffectedVisualizations(DataView dataViewIn, List<FieldDef> listIn,
                                                        VisualizationActionsService visualizationActionsServiceIn) {

        DataViewDef myMeta = dataViewIn.getMeta();
        DataModelDef myModel = myMeta.getModelDef();

        for (VisualizationDef myVisualization : myModel.getVisualizations()) {

            for (FieldDef myField : listIn) {

                FieldReferencesFromDataView fieldReferencesFromDataView = new FieldReferencesFromDataView(myMeta, myField);

                if (fieldReferencesFromDataView.doesVisualizationHaveFieldDef(myVisualization)) {
                    if(visualizationActionsServiceIn != null){
                        visualizationActionsServiceIn.clearVizCache(myVisualization);
                    }
                    GraphDataManager.deleteContext(myVisualization.getUuid());
                }
            }
        }
    }

    public static void updateFieldNameReferences(List<FieldDef> listIn, String oldNameIn, String newNameIn) throws CentrifugeException {

        CsiScriptRunner runner = new EcmaScriptRunner();
        for (FieldDef myField : listIn) {
            String myScript = runner.updateFieldReferences(myField.getScriptText(), oldNameIn, newNameIn);
            myField.setScriptText(myScript);
        }

    }

    public static Resource importXML(BufferedInputStream iStream) throws CentrifugeException, IOException {
        try{
            XStream codec = XStreamHelper.getImportExportCodec();
            return (Resource)codec.fromXML(iStream);
        }catch(Exception e){
            e.printStackTrace();
            Throwables.propagate(e);
        }
        finally {
            CsiUtil.quietClose(iStream);
        }
        return null;
    }

    public static ImportStatus saveResource(Resource imported, boolean forceSave) throws CentrifugeException {
        ImportStatus importStatus = new ImportStatus();
        importStatus.className = imported.getClass().getName();

        try {
            List<? extends Resource> existing = null;

            if (imported instanceof DataViewDef) {
                existing = AclRequest.listUserTemplateConflicts(imported.getName());
                fixupPersistenceLinkage((DataViewDef)imported);
            } else if (imported instanceof DataView) {
                existing = AclRequest.listUserDvConflicts(imported.getName());
                fixupPersistenceLinkage((DataView)imported);
            } else if (imported instanceof Theme){
                existing = AclRequest.listUserThemeConflicts(imported.getName());
            } else if (imported instanceof Basemap){
                existing = AclRequest.listUserBasemapConflicts(imported.getName());
            } else {
                throw new CentrifugeException("Import does not support importing of type: " + imported.getClass().getName());
            }

            //TODO: CHECK FOR AUTHORIZATION TO OVERWRITE !!!!!!!!!!!!!

            if ((existing == null) || forceSave) {

                Resource saved = CsiPersistenceManager.persistResource(imported, existing);

                importStatus.status = ImportStatusType.OK;
                importStatus.itemName = imported.getName();
                importStatus.message = String.format("Import of '%s' succeeded.", importStatus.itemName);
                importStatus.uuid = saved.getUuid();

            } else {
                importStatus.status = ImportStatusType.DUPLICATE;
                importStatus.itemName = imported.getName();
                String entityType;
                if (importStatus.className.contains("DataViewDef")) {
                    entityType = "Dataview";
                } else {
                    int inx = importStatus.className.lastIndexOf('.');
                    entityType = importStatus.className.substring(inx + 1);
                }

                importStatus.message = String.format("Import failure because of duplicate name: %s '%s' already exists on server", entityType, importStatus.itemName);
            }

        } catch (Throwable ex) {
            Throwables.propagate(ex);
        }

        return importStatus;
    }

    public static boolean okToDelete(DataView dataViewIn, FieldDef fieldDefIn) {

        return okToDelete(dataViewIn.getMeta(), fieldDefIn);
    }

    public static boolean okToDelete(DataViewDef metaIn, FieldDef fieldDefIn) {

        FieldReferencesFromDataView referenceFromDataView = new FieldReferencesFromDataView(metaIn, fieldDefIn);
        FieldReferences fieldReferences = referenceFromDataView.buildFieldReferences();

        return !fieldReferences.hasReferences();
    }

   public static boolean deleteFieldDef(DataView dataViewIn, FieldDef fieldDefIn) throws CentrifugeException, SQLException {
      boolean success = false;

      if (okToDelete(dataViewIn, fieldDefIn)) {
         dataViewIn.getMeta().getModelDef().getFieldListAccess().removeFieldDef(fieldDefIn);
         success = true;
      }
      return success;
   }

    public static List<String> testTemplateCoreFieldReferences(String templateUuidIn) {

        DataViewDef myTemplate = CsiPersistenceManager.findForDelete(DataViewDef.class, templateUuidIn);

        return testFieldReferences(myTemplate, myTemplate.getCoreFieldIds());
    }

    public static List<String> testTemplateFieldReferences(String templateUuidIn) {

        DataViewDef myTemplate = CsiPersistenceManager.findForDelete(DataViewDef.class, templateUuidIn);

        return testFieldReferences(myTemplate, myTemplate.getFieldIds());
    }

    public static List<String> testTemplateFieldReferences(String templateUuidIn, List<String> fieldUuidsIn) {

        DataViewDef myTemplate = CsiPersistenceManager.findForDelete(DataViewDef.class, templateUuidIn);

        return testFieldReferences(myTemplate, fieldUuidsIn);
    }


    public static List<String> testTemplateFieldReferencesAndReturnVis(String templateUuidIn, String fieldUuidIn) {

        DataViewDef myTemplate = CsiPersistenceManager.findForDelete(DataViewDef.class, templateUuidIn);

        return testFieldReferencesAndReturnVis(myTemplate, fieldUuidIn);
    }


    public static List<String> testDataViewFieldReferences(String dvUuidIn, List<String> fieldUuidsIn) {

        DataView myDataView = CsiPersistenceManager.findForDelete(DataView.class, dvUuidIn);

        return testFieldReferences(myDataView.getMeta(), fieldUuidsIn);
    }

    public String makeUniqueDataViewName(String name) {

        return AclRequest.makeUniqueResourceName(DataView.class, name);
    }

    public static List<QueryParameterDef> cloneParameters(List<QueryParameterDef> listIn) {

        List<QueryParameterDef> myList = null;

        if (null != listIn) {

            myList = new ArrayList<QueryParameterDef>();

            for (QueryParameterDef myParameter : listIn) {

                myList.add(myParameter.clone());
            }
        }
        return myList;
    }

    // Select values for launching DataView
    //
    // Order of preference:
    // 1. User entered parameters -- paramValues
    // 2. Current values -- paramDefs.values
    // 3. Default values -- paramDefs,defaultValues
    //
    public static void applyLaunchParameters(List<QueryParameterDef> paramDefs, List<LaunchParam> paramValues) {

        Map<String, LaunchParam> paramMap = new HashMap<String, LaunchParam>();

        if (paramValues != null) {
            for (LaunchParam pdo : paramValues) {
                paramMap.put(pdo.getName().toLowerCase(), pdo);
            }
        }

        if (null != paramDefs) {

            for (QueryParameterDef pdef : paramDefs) {
                List<String> curvals = pdef.getValues();

                LaunchParam lp = paramMap.get(pdef.getName().toLowerCase());
                if (lp != null) {
                    curvals.clear();
                    if ((lp.getValues() != null) && !lp.getValues().isEmpty()) {

                        if (pdef.getTrimValues().booleanValue()) {

                            for (String myValue : lp.getValues()) {

                                curvals.add(myValue.trim());
                            }

                        } else {

                            curvals.addAll(lp.getValues());
                        }
                    }
                }

                if (curvals.isEmpty() && (pdef.getDefaultValues() != null) && !pdef.getDefaultValues().isEmpty()) {
                   curvals.addAll(pdef.getDefaultValues());
                }
            }
        }
    }

    public static DataView fixupPersistenceLinkage(DataView dataViewIn) {

        fixupPersistenceLinkage(dataViewIn.getMeta());

        return dataViewIn;
    }

    public static DataViewDef fixupPersistenceLinkage(DataViewDef metaDataIn) {
        List<QueryParameterDef> myParameterList = metaDataIn.getDataSetParameters();
        int myOrdinal = 0;
        if (null != myParameterList) {
            for (QueryParameterDef myItem : myParameterList) {
                if (!myItem.isSystemParam()) {
                    myItem.setOrdinal(myOrdinal++);
                    myItem.setParent(metaDataIn);
                }
            }
        }

        List<LinkupMapDef> myLinkupList = metaDataIn.getLinkupDefinitions();
        if (null != myLinkupList) {
            myOrdinal = 0;
            for (LinkupMapDef myItem : myLinkupList) {
                myItem.setOrdinal(myOrdinal++);
            }
        }

        List<DataSourceDef> mySourceList = metaDataIn.getDataSources();
        if (null != mySourceList) {
            myOrdinal = 0;
            for (DataSourceDef myItem : mySourceList) {
                myItem.setOrdinal(myOrdinal++);

                ConnectionDef myConnection = myItem.getConnection();
                if ((null != myConnection) && (null != myConnection.getProperties())) {
                    GenericProperties myProperties = myConnection.getProperties();
                    List<Property> myPropertyList = myProperties.getProperties();
                    if (null != myPropertyList) {
                        int mySubOrdinal = 0;
                        for (Property myProperty : myPropertyList) {
                            myProperty.setOrdinal(mySubOrdinal++);
                        }
                    }
                }
            }
        }

        List<Filter> myFilterList = metaDataIn.getFilters();
        if (null != myFilterList) {
            myOrdinal = 0;
            for (Filter myItem : myFilterList) {
                myItem.setOrdinal(myOrdinal++);

                FilterDefinition myDefinition = myItem.getFilterDefinition();
                if (null != myDefinition) {
                    List<FilterExpression> myExpressions = myDefinition.getFilterExpressions();
                    if (null != myExpressions) {
                        int mySubOrdinal = 0;
                        for (FilterExpression myExpression : myExpressions) {
                            myExpression.setOrdinal(mySubOrdinal++);
                        }
                    }
                }
            }
        }

        List<SimpleExtension> mySimpleExtensionList = metaDataIn.getExtensionConfigs();
        if (null != mySimpleExtensionList) {
            myOrdinal = 0;
            for (SimpleExtension myItem : mySimpleExtensionList) {
                myItem.setOrdinal(myOrdinal++);
            }
        }

        List<ExtensionData> myExtensionDataList = metaDataIn.getExtensionData();
        if (null != myExtensionDataList) {
            myOrdinal = 0;
            for (ExtensionData myItem : myExtensionDataList) {
                myItem.setOrdinal(myOrdinal++);
            }
        }

        DataModelDef myDataModel = metaDataIn.getModelDef();
        fixupDataModel(myDataModel);

        DataSetOp myDataTree = metaDataIn.getDataTree();
        fixupDataTree(myDataTree);

        return metaDataIn;
    }

    private static void fixupDataModel(DataModelDef dataModelIn) {
        if (null != dataModelIn) {
            List<FieldDef> myFieldList = dataModelIn.getFieldDefs();
            if (null != myFieldList) {
                int myOrdinal = 0;
                for (FieldDef myField : myFieldList) {
                    myField.setOrdinal(myOrdinal++);

                    List<ScriptFunction> myFunctionList = myField.getFunctions();
                    if (null != myFunctionList) {
                        int mySubOrdinal = 0;
                        for (ScriptFunction myFunction : myFunctionList) {
                            myFunction.setOrdinal(mySubOrdinal++);
                        }
                    }

                    SqlTokenTreeItemList myExpression = myField.getSqlExpression();
                    if ((myExpression != null) && (myExpression.size() > 0)) {
                        SqlTokenTreeItem myItem = myExpression.get(0);
                        if ((null != myItem) && (0 == myItem.getDataTypeMask())) {
                            myItem.setRequiredDataType(myField.getValueType().getMask());
                        }
                    }
                }
            }
        }
    }


    private static void fixupDataTree(DataSetOp dataTreeIn) {
        if (null != dataTreeIn) {
            SqlTableDef myTable = dataTreeIn.getTableDef();
            if (null != myTable) {
                if (myTable.getIsCustom()) {
                    QueryDef customQuery = myTable.getCustomQuery();
                    if ((null != customQuery.getQueryText()) && (null == customQuery.getLinkupText())) {
                        customQuery.setLinkupText(QueryHelper.genLinkupQuery(customQuery.getQueryText()));
                    }
                }
                if (null != myTable.getColumns()) {
                    int myOrdinal = 0;
                    for (ColumnDef myColumn : myTable.getColumns()) {
                        myColumn.setOrdinal(myOrdinal++);
                        myColumn.setTableDef(myTable);

                        List<ColumnFilter> myFilterList = myColumn.getColumnFilters();
                        if (null != myFilterList) {
                            int mySubOrdinal = 0;
                            for (ColumnFilter myFilter : myFilterList) {
                                myFilter.setOrdinal(mySubOrdinal++);
                                myFilter.setParent(myColumn);
                            }
                        }
                    }
                }
            }

            List<OpMapItem> myList = dataTreeIn.getMapItems();
            if (null != myList) {
                for (OpMapItem myItem : myList) {
                    myItem.setParent(dataTreeIn);
                }
            }

            fixupDataTree(dataTreeIn.getLeftChild());
            fixupDataTree(dataTreeIn.getRightChild());
        }
    }

    public static void applyLaunchAuthInfo(DataSetOp op, List<AuthDO> auths) throws CentrifugeException {

        if (null != auths) {

            Map<String, AuthDO> authmap = new HashMap<String, AuthDO>();

            for (AuthDO auth : auths) {
                authmap.put(auth.getDsLocalId().toLowerCase(), auth);
            }

            Set<DataSourceDef> sources = DataSetUtil.getDistinctSources(op);
            for (DataSourceDef dsdef : sources) {
                AuthDO a = authmap.get(dsdef.getLocalId().toLowerCase());
                if (a != null) {
                    dsdef.getConnection().addCredentials(a.getUsername(), a.getPassword());
                }
            }
        }
    }

    public static ValuePair<Long, Boolean> launchDataView(DataView dv, List<LaunchParam> params, List<AuthDO> auths,
                                                   ParameterSetFactory parameterValuesIn)
            throws CentrifugeException, GeneralSecurityException {

        return launchDataView(null, dv, params, auths, parameterValuesIn);
    }

    public static ValuePair<Long, Boolean> launchDataView(ConnectionDef connectionIn, DataView dv, List<LaunchParam> params,
                                                   List<AuthDO> auths, ParameterSetFactory parameterValuesIn)
            throws CentrifugeException, GeneralSecurityException {

        // HACK: refactor cache launch and connection factory
        // to take the ParamDO and AuthDO directly so we don't have
        // to copy the info into the model objects and then clear
        // them afterwards
        DataViewDef meta = dv.getMeta();

        DataSetOp rootOp = meta.getDataTree();
        if (null == rootOp) {
            throw new ValidationException("Invalid dataview.  No data operations defined.");
        }
        try {
            ResetDataAccessIds(dv);
            applyLaunchAuthInfo(rootOp, auths);
            applyLaunchParameters(meta.getDataSetParameters(), params);

            DataCacheBuilder.clearCache(dv);

            ValuePair<Long, Boolean> myResults = DataCacheBuilder.initializeCache(dv, connectionIn, parameterValuesIn,
                    auths, null, null, 0);
            dv.setSize(myResults.getValue1());
//            dv.setNeedsRefresh(0 == rowCount);
            dv.setLastOpenDate(new Date());
            deleteVizData(dv, true);
            return myResults;
        } finally {
            try {
                dv.getMeta().clearTransientValues();
            } catch(Exception myException) {
                // Ignore exception
            }
        }
    }

    public static long trimDataView(DataView dataViewIn) {

        try {
            ResetDataAccessIds(dataViewIn);
            DataCacheBuilder.discardLinkupData(dataViewIn);

            long rowCount = DataCacheHelper.getRowCount(CacheUtil.getCacheTableName(dataViewIn.getUuid()));

            dataViewIn.setSize(rowCount);
//            dataViewIn.setNeedsRefresh(0 == rowCount);
            dataViewIn.setLastOpenDate(new Date());
            deleteVizData(dataViewIn, true);
            return rowCount;
        } finally {
            try {
                dataViewIn.getMeta().clearTransientValues();
            } catch(Exception myException) {
                // Ignore exception
            }
        }
    }

    public ValuePair<Long, Boolean> createDataSource(DataView sourceDataViewIn, List<LaunchParam> parametersIn,
                                 List<AuthDO> authorizationIn, ParameterSetFactory parameterValuesIn,
                                 DataView targetIn, LinkupMapDef linkupIn, int generationIn)
            throws CentrifugeException, GeneralSecurityException {

        // HACK: refactor cache launch and connection factory
        // to take the ParamDO and AuthDO directly so we don't have
        // to copy the info into the model objects and then clear
        // them afterwards
        DataViewDef meta = sourceDataViewIn.getMeta();
        DataSetOp rootOp = meta.getDataTree();

        if (null == rootOp) {
            throw new ValidationException("Invalid dataview.  No data operations defined.");
        }
        applyLaunchAuthInfo(rootOp, authorizationIn);
        applyLaunchParameters(meta.getDataSetParameters(), parametersIn);

        return DataCacheBuilder.initializeCache(sourceDataViewIn, null, parameterValuesIn,
                                                authorizationIn, targetIn, linkupIn, generationIn);
    }

    private void deleteDataSource(Connection connectionIn, String tableNameIn) {
        DataCacheBuilder.dropCacheTable(connectionIn, tableNameIn);
    }

    public static boolean validateExtensions(DataViewDef def) {
        boolean classValid = true;
        boolean labelsValid = true;

        String errorMsg = "";

        Set<DataSyncListener> listeners = new DataCacheHelper().getSyncListeners();
        for (DataSyncListener l : listeners) {
            if (l.isRequired()) {
                if ((l.providesSupport(Classification.NAME))) {
                    Classification c = (Classification) retrieveExtension(def, Classification.NAME);
                    classValid = (c != null)
                            && (((c.getDefaultValue() != null) && (c.getDefaultValue().length() > 0)) || (c.getFieldRef() != null));
                    if (!classValid) {
                        errorMsg = errorMsg + "\n" + "Classification is required, but not configured.";
                    }
                } else if ((l.providesSupport(Labels.NAME))) {
                    Labels labels = (Labels) retrieveExtension(def, Labels.NAME);
                    labelsValid = (labels != null)
                            && ((labels.getFieldRef() != null) || ((labels.getDefaultValues() != null) && !labels
                            .getDefaultValues().isEmpty()));
                    if (!labelsValid) {
                        errorMsg = errorMsg + "\n" + "Security Labels are required, but not configured.";
                    }
                }
            }
        }


        if (LOG.isInfoEnabled() && !"".equals(errorMsg)) {
            LOG.info(errorMsg);
        }

        return labelsValid && classValid;


    }

    private static SimpleExtension retrieveExtension(DataViewDef def, String name) {
        List<SimpleExtension> configs = def.getExtensionConfigs();
        for (SimpleExtension ext : configs) {
            if (ext.getName().equalsIgnoreCase(name)) {
                return ext;
            }
        }

        return null;
    }

    public static String repairCorruptDataview(DataViewDef def) {

        String repairDetails = null;
        FieldReferenceAugmentor changer = new FieldReferenceAugmentor(new File("./"), def);
        changer.augment();
        Collection<String> msgs = changer.getMessages();
        String logMsg = "";

        if (msgs.isEmpty()) {
            repairDetails = "";
        } else if (msgs.size() < 5) {
            String msg = msgs.stream().collect(Collectors.joining("\n"));
            repairDetails = msg;
            logMsg = msg;
        } else if (msgs.size() >= 5) {
            logMsg = String.format("Multiple changes made to %s to ensure field consistency", def.getName());
            repairDetails = String.format("Multiple changes made to %s to ensure field consistency", def.getName());
        }

        if (LOG.isInfoEnabled()) {
            LOG.info(logMsg);
        }

        return repairDetails;
    }

//    /**
//     * Copy the GraphPlayerSettings objects from each RelGraphViewDef into another RelGraphViewDef.
//     * This is necessary when creating a DataView from a Template.
//     *
//     * @param to   - the target DataViewDef
//     * @param from - the source DataViewDef
//     */
//    public void cloneGraphPlayerSettings(DataViewDef to, DataViewDef from) {
//        for (VisualizationDef visualizationDef : to.getModelDef().getVisualizations()) {
//            if (visualizationDef instanceof RelGraphViewDef) {
//                RelGraphViewDef toRelGraph = (RelGraphViewDef) visualizationDef;
//                DataModelDef moddef = from.getModelDef();
//                RelGraphViewDef fromRelGraph = DataModelDefHelper.getVisualizationDef(moddef, toRelGraph.getName(), RelGraphViewDef.class);
//                //copyTo(fromRelGraph.getPlayerSettings(), toRelGraph.getPlayerSettings());
//                //The copyTo method uses the uuid of the startField/endField to locate the actual fieldDef objects.
//                //Unfortunately, the the fieldDefs have been cloned, and the startField/endField fieldDefs do not
//                //exist in the cloned DataView. So, the startField/endField fieldDefs must be fixed.
//                if (toRelGraph.getPlayerSettings().startField != null) {
//                    toRelGraph.getPlayerSettings().startField = findFieldDefByName(to, toRelGraph.getPlayerSettings().startField.getFieldName());
//                }
//                if (toRelGraph.getPlayerSettings().endField != null) {
//                    toRelGraph.getPlayerSettings().endField = findFieldDefByName(to, toRelGraph.getPlayerSettings().endField.getFieldName());
//                }
//            }
//        }
//    }
/*
    private FieldDef findFieldDefByName(DataViewDef dataViewDef, String fieldName) {
        for (FieldDef fieldDef : dataViewDef.getFieldList()) {
            if (fieldName.equalsIgnoreCase(fieldDef.getFieldName())) {
                return fieldDef;
            }
        }
        return null;
    }
*/
   public LinkupValidationReport validateLinkupRequest(LinkupMapDef linkupIn) {
      return new LinkupValidationReport();
   }

    public Response<String, LinkupResponse> executeLinkup(DataView dataViewIn, VisualizationDef vizDefIn,
                                                          LinkupMapDef linkupIn, List<LinkupHelper> parameterBuilderListIn,
                                                          String newDataviewNameIn, Set<Integer> selectionListIn,
                                                          List<LaunchParam> parameterValuesIn, List<AuthDO> authorizationListIn,
                                                          LinkupMode modeIn, boolean discardNullsIn) {

        String myLinkupUuid = null;
        DataView myReturnDataView = null;
        DataView myLinkupDataView = null;
        ParameterSetFactory myParameterValues = null;
        Long myRowCount = 0L;
        Boolean myMissingData = false;
        try {//LinkinField Logic
            String myDataViewUuid = dataViewIn.getUuid();
            List<LinkupHelper> myParameterBuilderList = parameterBuilderListIn;
            DataViewDef myLinkupTemplate = ModelHelper.locateTemplate(linkupIn.getTemplateUuid(),
                    linkupIn.getTemplateName(), linkupIn.getTemplateOwner());
            if ((modeIn == LinkupMode.MERGE) && !Strings.isNullOrEmpty(linkupIn.getLinkin())) {

                String linkin = linkupIn.getLinkin();
                List<String> linkinList = Splitter.on(",").splitToList(linkin);
                myLinkupTemplate = ModelHelper.cloneObject(myLinkupTemplate);
                String uuidLinkupIn = linkupIn.getUuid();
                linkupIn = ModelHelper.cloneObject(linkupIn);
                linkupIn.setUuid(uuidLinkupIn);
                int howMany = linkinList.size();

                for (int i = 0; i < howMany; i++) {
                    linkin = linkinList.get(i);
                    List<LooseMapping> fieldsMap = linkupIn.getFieldsMap();
                    boolean skip = false;
                    for (LooseMapping looseMapping : fieldsMap) {
                        if (looseMapping.getMappedLocalId().equals(linkin)) {
                            skip = true;
                            break;
                        }
                    }
                    if(skip){
                        continue;
                    }
                    String fduuid = CsiUUID.randomUUID();
                    String fdlid = CsiUUID.randomUUID();
                    String dslid = CsiUUID.randomUUID();
                    String table_lid = CsiUUID.randomUUID();
                    String column_lid = CsiUUID.randomUUID();
                    String mapping_uuid = CsiUUID.randomUUID();
                    String parameter_id = CsiUUID.randomUUID();
                    {//update meta with new parameter
                        List<QueryParameterDef> dataSetParameters = myLinkupTemplate.getDataSetParameters();

                        QueryParameterDef qpd = new QueryParameterDef();
                        {
                            qpd.setLocalId(parameter_id);
                            qpd.setParent(myLinkupTemplate);
                            qpd.setName("input"+i);
                            qpd.setType(CsiDataType.String);
                        }
                        dataSetParameters.add(qpd);

                    }
                    {//augment Fields Map
                        LooseMapping lm;
                        lm = new LooseMapping();
                        {
                            lm.setUuid(mapping_uuid);
                            lm.setMappedLocalId(linkin);

                            lm.setMappingLocalId(fdlid);
                            lm.setMappingName("MyDerivedField"+i);
                        }
                        fieldsMap.add(lm);
                    }
                    {//Create Template FieldDef Mapping

                        FieldDef fieldDef = new FieldDef();
                        fieldDef.setUuid(fduuid);
                        fieldDef.setLocalId(fdlid);
                        fieldDef.setFieldType(FieldType.COLUMN_REF);
                        fieldDef.setFieldName("MyDerivedField"+i);
                        fieldDef.setDsLocalId(dslid);
                        fieldDef.setValueType(CsiDataType.String);
                        fieldDef.setStorageType(CsiDataType.String);
                        fieldDef.setTableLocalId(table_lid);
                        fieldDef.setColumnLocalId(column_lid);
                        myLinkupTemplate.getFieldListAccess().addFieldDef(fieldDef);
                    }
                    {
                        ParamMapEntry entry = new ParamMapEntry();
                        {
                            entry.setParamId(parameter_id);
                            entry.setParamName("input"+i);
                            entry.setFieldLocalId(linkin);

                        }

                        List<ParamMapEntry> linkupParms = linkupIn.getLinkupParms();
                        linkupParms.add(entry);
                        LinkupHelper linkupHelper = parameterBuilderListIn.get(0);
                        linkupHelper.getParameterList().add(entry);
                    }
                    {
                        DataSetOp right = myLinkupTemplate.getDataTree();
                        DataSetOp dt = new DataSetOp();
                        DataSetOp left = new DataSetOp();
                        {
                            SqlTableDef tableDef = new SqlTableDef();
                            tableDef.setLocalId(table_lid);
                            {
                                DataSourceDef sourceIn = new DataSourceDef();
                                sourceIn.setLocalId(dslid);
                                {
                                    ConnectionDef conn = new ConnectionDef();
                                    {
                                        conn.setType("text");
                                        GenericProperties gp = new GenericProperties();
                                        {
                                            gp.getProperties().add(new Property("csi.filePath", "temp/123"));
                                            gp.getProperties().add(new Property("csi.schema.rowDelim", "CR-LF"));
                                            gp.getProperties().add(new Property("csi.schema.firstRowHeaders", "false"));
                                            gp.getProperties().add(new Property("csi.schema.charset", "UTF-8"));
                                            gp.getProperties().add(new Property("csi.schema.cellDelim", ","));
                                        }
                                        conn.setProperties(gp);
                                    }
                                    sourceIn.setConnection(conn);
                                }
                                tableDef.setSource(sourceIn);
                                tableDef.setIsCustom(true);
                                QueryDef customQuery = new QueryDef();
                                {
                                    String query = "Select " + "{:input"+i+"}" + " as \"val\"";
                                    customQuery.setQueryText(query);
                                    customQuery.setLinkupText(query);
                                    ArrayList<String> parameters = new ArrayList<String>();
                                    {

                                        parameters.add(parameter_id);
                                    }
                                    customQuery.setParameters(parameters);

                                }
                                tableDef.setCustomQuery(customQuery);
                                ArrayList<ColumnDef> columnsIn = new ArrayList<ColumnDef>();
                                {
                                    ColumnDef c = new ColumnDef();
                                    {
                                        c.setLocalId(column_lid);
                                        c.setTableName("1"+i);
                                        c.setColumnName("val");
                                        c.setCsiType(CsiDataType.String);
                                        c.setSelected(true);
                                        c.setTableDef(tableDef);
                                        c.setColumnFilters(new ArrayList<ColumnFilter>());

                                    }
                                    columnsIn.add(c);
                                }
                                tableDef.setColumns(columnsIn);
                            }
                            left.setTableDef(tableDef);
                            left.setLocalId(CsiUUID.randomUUID());
                            left.setParent(dt);
                            right.setParent(dt);
                        }
                        dt.setMapType(OpMapType.JOIN);
                        dt.setJoinType(OpJoinType.RIGHT_OUTER);
                        dt.setLeftChild(left);
                        dt.setRightChild(right);
                        dt.setForceLocal(true);
                        dt.setLocalId(CsiUUID.randomUUID());

                        myLinkupTemplate.setDataTree(dt);
                    }
                }
            }
            LinkupValidationReport myValidation = validateLinkupRequest(myLinkupTemplate, linkupIn, parameterBuilderListIn);

            if (myValidation.isOK()) {

                String myNewDataviewName = ((null != newDataviewNameIn) && (0 < newDataviewNameIn.length()))
                        ? newDataviewNameIn
                        : new StringBuilder("DV[").append(dataViewIn.getName())
                                    .append("] DS[").append(myLinkupTemplate.getName())
                                    .append("] ON[").append(ZonedDateTime.now().format(DateUtil.YYYY_SLASH_MM_SLASH_DD_DASH_TIME_FORMATTER)).append("]")
                                    .toString();

                if ((null != selectionListIn) && !selectionListIn.isEmpty()) {
                    myParameterBuilderList = identifySupportingRows(selectionListIn, parameterBuilderListIn);
                } else if (vizDefIn instanceof RelGraphViewDef) {
                    myParameterBuilderList = identifySupportingRows((RelGraphViewDef) vizDefIn, parameterBuilderListIn);
                }
                // create a dataview for the linkup
                myLinkupDataView = createLinkupDataView(myLinkupTemplate, myNewDataviewName);
                myParameterValues = new ParameterSetFactory(dataViewIn, myLinkupDataView, myParameterBuilderList,
                        parameterValuesIn, discardNullsIn);
                myLinkupUuid = myLinkupDataView.getUuid();
                myLinkupDataView.setSpinoff(true);

                try {

                    List<DataSourceDef> myRequiredAuthorizations
                            = listAuthorizationsRequired(myLinkupTemplate.getDataSources(), authorizationListIn);

                    if ((null != myRequiredAuthorizations) && !myRequiredAuthorizations.isEmpty()) {

                        return new Response<String, LinkupResponse>(myLinkupUuid, myRequiredAuthorizations);
                    }

                } catch (Exception myException) {

                    return new Response<String, LinkupResponse>(myLinkupUuid,
                            ServerMessage.CREDENTIALS_EXCEPTION, myException.getMessage());
                }

                switch (modeIn) {

                    case MERGE: {

                        int myGeneration = dataViewIn.getNextLinkupId();
                        String myTableName = CacheUtil.getCacheTableName(dataViewIn.getUuid(), myGeneration);

                        try {

                            // launch the linkup
                            ValuePair<Long, Boolean>myResults = createDataSource(myLinkupDataView,
                                                                new ArrayList<LaunchParam>(), authorizationListIn,
                                                                myParameterValues, dataViewIn, linkupIn, myGeneration);
                            myRowCount = myResults.getValue1();
                            myMissingData = myResults.getValue2();
                            Connection myConnection = null;

                            try {

                                myConnection = CsiPersistenceManager.getCacheConnection();

                                if (0L < myRowCount) {
                                    dataViewIn.addLinkup(myTableName);
                                    dataViewIn.incrementNextLinkupId();
                                    dataViewIn.incrementNextLinkupRowId(myRowCount);

//                                DataCacheBuilder.applyChangesToCache(myConnection, dataViewIn, myTableName);
                                    DataCacheBuilder.applyChangesToCache(dataViewIn);

                                    LOG.info(String.format("Merge %d rows into dataview %s", myRowCount, myDataViewUuid));
                                    if (0L < myRowCount) {
                                        updateGraphData(myConnection, dataViewIn, vizDefIn.getUuid(), myGeneration);
                                    }
                                    myConnection.commit();
                                    linkupIn.incrementUseCount();

                                    if(visualizationActionsService != null) {
                                        for(VisualizationDef vizDef: dataViewIn.getMeta().getModelDef().getVisualizations()) {
                                            visualizationActionsService.clearVizCache(vizDef);
                                        }
                                    }
                                    CsiPersistenceManager.merge(dataViewIn);
                                    CsiPersistenceManager.commit();
                                    CsiPersistenceManager.begin();

                                } else {

                                    deleteDataSource(myConnection, myTableName);
                                }

                            } catch (Exception myException) {

                                CsiPersistenceManager.rollback();
                                SqlUtil.quietRollback(myConnection);
                                deleteDataSource(myConnection, myTableName);

                                return new Response<String, LinkupResponse>(myLinkupUuid, ServerMessage.CAUGHT_EXCEPTION, myException.getMessage());

                            } finally {
                                SqlUtil.quietCloseConnection(myConnection);
                            }

                        } catch (GeneralSecurityException myException) {

                            try {

                                return new Response<String, LinkupResponse>(myLinkupUuid,
                                        listAuthorizationsRequired(myLinkupTemplate.getDataSources(), authorizationListIn));

                            } catch (Exception myNewException) {

                                return new Response<String, LinkupResponse>(myLinkupUuid, ServerMessage.CREDENTIALS_EXCEPTION, myNewException.getMessage());
                            }

                        } catch (Exception myException) {

                            return new Response<String, LinkupResponse>(myLinkupUuid,
                                    ServerMessage.LINKUP_DATAVIEW_EXCEPTION, Format.value(myException));

                        }
                        break;
                    }
                    case SPINOFF: {


                        myReturnDataView = beginSpinOff(dataViewIn, myNewDataviewName);
                        clearSpinoffSelections(myReturnDataView);
                        CsiPersistenceManager.persist(myReturnDataView);
                        myLinkupUuid = myReturnDataView.getUuid();

                        applyLaunchParameters(myLinkupDataView.getMeta().getDataSetParameters(),
                                                new ArrayList<LaunchParam>());

                        try {

                            // launch the linkup
                            ValuePair<Long, Boolean>myResults = createDataSource(myLinkupDataView, new ArrayList<LaunchParam>(),
                                                                                    authorizationListIn, myParameterValues,
                                    myReturnDataView, linkupIn, 0);
                            myRowCount = myResults.getValue1();
                            myMissingData = myResults.getValue2();
                            myReturnDataView.setSize(myRowCount);
                            myReturnDataView.setViews(CacheUtil.getCacheTableName(myReturnDataView.getUuid()));
                            myReturnDataView.setTables(CacheUtil.getCacheTableName(myReturnDataView.getUuid(), 0));
                            myReturnDataView.setNeedsRefresh(false);
                            CsiPersistenceManager.merge(myReturnDataView);

                        } catch (GeneralSecurityException myException) {

                            try {

                                return new Response<String, LinkupResponse>(myLinkupUuid,
                                        listAuthorizationsRequired(myLinkupTemplate.getDataSources(), authorizationListIn));

                            } catch (Exception myNewException) {

                                return new Response<String, LinkupResponse>(myLinkupUuid, ServerMessage.CREDENTIALS_EXCEPTION, myNewException.getMessage());
                            }

                        } catch (Exception myException) {

                            return new Response<String, LinkupResponse>(myLinkupUuid,
                                    ServerMessage.LINKUP_DATAVIEW_EXCEPTION, Format.value(myException));

                        }
                        break;
                    }
                    case SPINUP: {

                        // save the dataview for potential opening as a new DataView
                        myReturnDataView = myLinkupDataView;
                        CsiPersistenceManager.persist(fixupPersistenceLinkage(myLinkupDataView));

                        try {

                            // launch the linkup
                            ValuePair<Long, Boolean> myResults = launchDataView(myLinkupDataView, new ArrayList<LaunchParam>(),
                                                                                authorizationListIn, myParameterValues);
                            myRowCount = myResults.getValue1();
                            myMissingData = myResults.getValue2();

                            CsiPersistenceManager.merge(myReturnDataView);
                        } catch (Exception myException) {

                            return new Response<String, LinkupResponse>(myLinkupUuid,
                                    ServerMessage.LINKUP_DATAVIEW_EXCEPTION, myException.getMessage());
                        }
                        break;
                    }
                }
                return new Response<String, LinkupResponse>(myLinkupUuid,
                                        new LinkupResponse(myReturnDataView, myRowCount, dataViewIn.getNextLinkupId(),
                                                            dataViewIn.getLinkups(), linkupIn.getUuid(),
                                                            linkupIn.getUseCount(), dataViewIn.getMeta().getCapcoInfo(),
                                                            dataViewIn.getMeta().getSecurityTagsInfo()), myRowCount, myMissingData);
            } else {

                return new Response<String, LinkupResponse>(myLinkupUuid, new LinkupResponse(myValidation));
            }

        } catch (CentrifugeException myException) {

            return new Response<String, LinkupResponse>(myLinkupUuid, ServerMessage.CAUGHT_EXCEPTION, myException.getMessage());
        }
    }

    public static void ResetDataAccessIds(DataView dataViewIn) {

        dataViewIn.resetNextLinkupRowId();
        dataViewIn.resetNextLinkupId();
    }

    private LinkupValidationReport validateLinkupRequest(DataViewDef templateIn, LinkupMapDef linkupIn,
                                                         List<LinkupHelper> helperListIn) {

        LinkupValidationReport myReport = new LinkupValidationReport();

        if (null != templateIn) {

            FieldListAccess myModel = templateIn.getModelDef().getFieldListAccess();
            List<LooseMapping> myRequiredFields = linkupIn.getFieldsMap();
            List<ParamMapEntry> myRequiredParameters = linkupIn.getLinkupParms();
            Map<String, Integer> myMissingParameterMap = new TreeMap<String, Integer>();

            if (myRequiredFields != null) {

                for (LooseMapping myMapping : myRequiredFields) {

                    String myLocalId = myMapping.getMappingLocalId();

                    if (null == myModel.getFieldDefByLocalId(myLocalId)) {

                        String myName = myMapping.getMappingName();

                        if (null == myModel.getFieldDefByName(myName)) {

                            myReport.addMissingField(myName, myLocalId);

                        } else {

                            myReport.addQuestionableField(myName, myLocalId);
                        }
                    }
                }
            }
            if (myRequiredParameters != null) {

                for (ParamMapEntry myEntry : myRequiredParameters) {

                    String myName = myEntry.getParamName();

                    if (null== templateIn.getParameterByName(myName)) {

                        myMissingParameterMap.put(myName, 0);
                    }
                }
            }
            for (LinkupHelper myHelper : helperListIn) {

                myRequiredParameters = myHelper.getParameterList();

                if (myRequiredParameters != null) {

                    for (ParamMapEntry myEntry : myRequiredParameters) {

                        String myName = myEntry.getParamName();

                        if (null== templateIn.getParameterByName(myName)) {

                            myMissingParameterMap.put(myName, 0);
                        }
                    }
                }
            }
            if (!myMissingParameterMap.isEmpty()) {

                for (String myName : myMissingParameterMap.keySet()) {

                    myReport.addMissingParameter(myName);
                }
            }

        } else {

            myReport.setMissingTemplate(linkupIn.getTemplateName(), linkupIn.getTemplateOwner(), linkupIn.getTemplateUuid());
        }

        return myReport;
    }

    private DataView createLinkupDataView(DataViewDef templateIn, String newDataviewNameIn) throws CentrifugeException {

        DataViewDef myClonedTemplate = ModelHelper.cloneObject(templateIn);

        myClonedTemplate.setTemplate(false);
        myClonedTemplate.clearAllRuntimeValues();
        return DataViewHelper.createDataView(newDataviewNameIn, myClonedTemplate, false);
    }

    public void updateGraphData(Connection conn, DataView targetdv, String vizUuid, int nextGeneration) throws CentrifugeException {
        GraphManager graphMgr = GraphManager.getInstance();
        List<VisualizationDef> vizList = targetdv.getMeta().getModelDef().getVisualizations();
        for (VisualizationDef viz : vizList) {
            if (viz instanceof RelGraphViewDef) {
                GraphContext gc = GraphServiceUtil.getGraphContext(viz.getUuid());
                if (gc != null) {
                    // Augment graph for any graph viz that is not attached or
                    // is attached but has no broadcast data. Only make new
                    // nodes visible for the vis that initiated
                    // the merge.
                    synchronized (gc) {
                        // WARNING: order of the or conditions matter
                        graphMgr.augmentGraph(conn, gc, nextGeneration, true);
                        // graphMgr.updateSelectionModels(gc, gc.getVisibleGraph());
                        GraphDataManager.saveGraphData(gc);
                    }
                }
            }
        }
    }

    //    public void copyFrom(GraphPlayerSettings gpSettings, TimePlayerSettings settings) {
//
//        if (gpSettings.getUuid() == null) {
//            gpSettings.setUuid(settings.uuid);
//        }
//        if (settings.startField != null && settings.startField.trim().length() > 0) {
//        	gpSettings.startField = CsiPersistenceManager.findObject(FieldDef.class, settings.startField.trim());
//        }
//
//        if (settings.endField != null && settings.endField.trim().length() > 0) {
//        	gpSettings.endField = CsiPersistenceManager.findObject(FieldDef.class, settings.endField.trim());
//        }
//        gpSettings.durationNumber = (int) settings.durationNumber;
//        gpSettings.durationPeriod = settings.durationPeriod;
//        gpSettings.stepSizeNumber = (int)  settings.stepSizeNumber;
//        gpSettings.stepSizePeriod = settings.stepSizePeriod;
//        gpSettings.stepMode = settings.stepMode;
//        gpSettings.playbackMode = settings.playbackMode;
//        gpSettings.hideNonVisibleItems = settings.hideNonVisibleItems;
//        gpSettings.frameSizeNumber = (int) settings.frameSizeNumber;
//        gpSettings.frameSizePeriod = settings.frameSizePeriod;
//        gpSettings.speed = settings.speed;
//        gpSettings.playbackStart = settings.playbackStart;
//        gpSettings.playbackEnd = settings.playbackEnd;
//        gpSettings.playbackMin = settings.playbackMin;
//        gpSettings.playbackMax = settings.playbackMax;
//        gpSettings.initializedByClient = settings.initializedByClient;
//    }
//
//    public void copyTo(GraphPlayerSettings gpSettings, GraphPlayerSettings settings) {
//        settings.durationNumber = gpSettings.durationNumber;
//        settings.durationUnit = gpSettings.durationUnit;
//        settings.stepSizeNumber = gpSettings.stepSizeNumber;
//        settings.stepSizePeriod = gpSettings.stepSizePeriod;
//        settings.stepMode = gpSettings.stepMode;
//        settings.playbackMode = gpSettings.playbackMode;
//        settings.hideNonVisibleItems = gpSettings.hideNonVisibleItems;
//        settings.frameSizeNumber = gpSettings.frameSizeNumber;
//        settings.frameSizePeriod = gpSettings.frameSizePeriod;
//        settings.speed_ms = gpSettings.speed_ms;
//        settings.playbackStart = gpSettings.playbackStart;
//        settings.playbackEnd = gpSettings.playbackEnd;
//        settings.playbackMin = gpSettings.playbackMin;
//        settings.playbackMax = gpSettings.playbackMax;
//    }

    //
    // Identify the data source rows mapped to each node and link
    // within the selection whose type has been identified for linkup
    //                           OR
    // Identify the data source rows mapped to each node and link
    // within the selection
    //
    private List<LinkupHelper> identifySupportingRows(RelGraphViewDef visualizationIn,
                                                      List<LinkupHelper> parameterBuilderListIn)
            throws CentrifugeException {

        LinkupSelectionHelper mySelection = new LinkupSelectionHelper(visualizationIn);
        List<LinkupHelper> myParameterBuildListOut = null;

        synchronized (mySelection) {
            myParameterBuildListOut = mySelection.identifySupportingRows(parameterBuilderListIn);
        }

        return myParameterBuildListOut;
    }

    private List<LinkupHelper> identifySupportingRows(Set<Integer> selectionListIn,
                                                      List<LinkupHelper> parameterBuilderListIn) {

        List<LinkupHelper> myParameterBuildListOut = new ArrayList<LinkupHelper>();

        for (LinkupHelper myHelper : parameterBuilderListIn) {

            myParameterBuildListOut.add(new LinkupHelper(myHelper.getParameterList(), selectionListIn));
        }
        return myParameterBuildListOut;
    }

    public static List<DataSourceDef> listAuthorizationsRequired(SqlTableDef tableIn, List<AuthDO> credentialsIn) throws CentrifugeException {

        return listAuthorizationsRequired(tableIn.getSource(), credentialsIn);
    }

    public static List<DataSourceDef> listAuthorizationsRequired(DataSourceDef sourceIn, List<AuthDO> credentialsIn) throws CentrifugeException {

        List<DataSourceDef> myList = new ArrayList<DataSourceDef>();
        String myKey = sourceIn.getLocalId();

        if (null != credentialsIn) {

            for (AuthDO myCredentials : credentialsIn) {

                if (myCredentials.getDsLocalId().equals(myKey)) {

                    sourceIn.getConnection().addCredentials(myCredentials.getUsername(), myCredentials.getPassword());
                }
            }
        }
        if (isAuthorizationFailure(sourceIn.getConnection())) {

            ConnectorSupport mySupport = getSupport();

            if ((null != mySupport) && (!mySupport.isRestricted(sourceIn))) {

                myList.add(sourceIn);
            }
        }
        return myList;
    }

    public static List<DataSourceDef> listAuthorizationsRequired(List<DataSourceDef> sourcesIn, List<AuthDO> credentialsIn) throws GeneralSecurityException {

        List<DataSourceDef> myList = new ArrayList<DataSourceDef>();
        Map<String, AuthDO> myMap = new HashMap<String, AuthDO>();
        boolean myRestrictedError = false;

        if (null != credentialsIn) {

            for (AuthDO myCredentials : credentialsIn) {

                myMap.put(myCredentials.getDsLocalId(), myCredentials);
            }
        }
        if ((null != sourcesIn) && !sourcesIn.isEmpty()) {

            ConnectorSupport mySupport = getSupport();

            for (DataSourceDef mySource : sourcesIn) {

                String myKey = mySource.getLocalId();

                if (myMap.containsKey(myKey)) {

                    AuthDO myCredentials = myMap.get(myKey);

                    mySource.getConnection().addCredentials(myCredentials.getUsername(), myCredentials.getPassword());
                }
                try {

                    if (isAuthorizationFailure(mySource.getConnection())) {

                        throw new GeneralSecurityException();
                    }

                } catch (Exception myException) {

                    if ((null != mySupport) && (!mySupport.isRestricted(mySource))) {

                        myList.add(mySource);

                    } else {

                        myList.add(mySource.redactAll());
                    }
                }
            }
            if (myRestrictedError && myList.isEmpty()) {

                throw new GeneralSecurityException("Failed logon.");
            }
        }
        return myList;
    }

   public static boolean isAuthorizationFailure(ConnectionDef connectionIn) throws CentrifugeException {
      boolean authorizationFailure = false;

      try {
         TaskHelper.checkForCancel();

         ConnectionFactory factory = ConnectionFactoryManager.getInstance().getConnectionFactory(connectionIn);

         if (factory != null) {
            try (Connection connection = factory.getConnection(connectionIn)) {
            } catch (SQLException sqle) {
            }
         }
      } catch (GeneralSecurityException gse) {
         authorizationFailure = true;
      }
      return authorizationFailure;
   }

    public static void clearDataReferences(DataView dataViewIn) {

        // Remove data references
        dataViewIn.setNeedsSource(false);
        dataViewIn.setNeedsRefresh(true);

        dataViewIn.setLinkups(null);        // List of Linkup Table UUIDs
        dataViewIn.setTables(null);         // Base table UUID
        dataViewIn.setViews(null);          // top view name
        dataViewIn.resetNextLinkupRowId();

        dataViewIn.setNextLinkupId(1);
    }

    public static List<String> testFieldReferences(DataViewDef metaIn, List<String> fieldUuidsIn) {

        List<String> myInUseFields = new ArrayList<String>();
        FieldListAccess myModelDef = metaIn.getModelDef().getFieldListAccess();

        for (String myFieldId : fieldUuidsIn) {

            FieldDef myField = myModelDef.findFieldDefByUuid(myFieldId);

            if (null != myField) {

                FieldReferencesFromDataView myFieldChecker = new FieldReferencesFromDataView(metaIn, myField);
                FieldReferences fieldReferences = myFieldChecker.buildFieldReferences();
                if (fieldReferences.hasReferences()) {
                    myInUseFields.add(myFieldId);
                }
            }
        }

        return myInUseFields;
    }

    public static List<String> testFieldReferencesAndReturnVis(DataViewDef metaIn, String fieldUuidIn) {

        List<String> myInUseVisualizations = new ArrayList<String>();
        FieldListAccess myModelDef = metaIn.getModelDef().getFieldListAccess();


            FieldDef myField = myModelDef.findFieldDefByUuid(fieldUuidIn);

            if (null != myField) {

                FieldReferencesFromDataView myFieldChecker = new FieldReferencesFromDataView(metaIn, myField);
                FieldReferences fieldReferences = myFieldChecker.buildFieldReferences();
                if (fieldReferences.hasReferences()) {
                    myInUseVisualizations.addAll(fieldReferences.getVisualizationNames());
                }
            }


        return myInUseVisualizations;
    }

    public void setVizActionsService(VisualizationActionsService visualizationActionsService) {
        this.visualizationActionsService = visualizationActionsService;
    }

    public static void removeAllSelectionFilter(DataView myDataView) {
        List<VisualizationDef> defs = myDataView.getMeta().getModelDef().getVisualizations();
        List<Filter> filters = myDataView.getMeta().getFilters();
        List<Filter> filtersToRemove = new ArrayList<Filter>();
        for (Filter filter: filters) {
            for (FilterExpression filterExpression : filter.getFilterDefinition().getFilterExpressions()) {
                if (filterExpression.isSelectionFilter()) {
                    filtersToRemove.add(filter);
                    for (VisualizationDef def : defs) {
                        Filter attachedFilter = def.getFilter();
                        if ((attachedFilter != null) && attachedFilter.equals(filter)) {
                            def.setFilter(null);
                        }
                    }
                    break;
                }
            }
        }
        filters.removeAll(filtersToRemove);
        myDataView.getMeta().setFilters(filters);
    }

    public static void clearVisualizationCaches(DataView myDataView,
                                                VisualizationActionsService visualizationActionsServiceIn) {
        for(VisualizationDef viz: myDataView.getMeta().getModelDef().getVisualizations()){
            visualizationActionsServiceIn.clearVizCache(viz);
        }
    }
    public static ConnectorSupport getSupport() {

        ConnectorSupport mySupport = null;
        String myUserName = CsiSecurityManager.getUserName();

        if (null != myUserName) {

            mySupport = ConnectorSupport.getUser(myUserName.toLowerCase());

            if (null == mySupport) {

                try {

                    ConnectionHelper.listConnectionDescriptors();
                    mySupport = ConnectorSupport.getUser(CsiSecurityManager.getUserName().toLowerCase());

                } catch (Exception myException) {

                    LOG.error("Caught exception retrieving user access information: ", myException);
                }
            }
        }
        return mySupport;
    }
}
