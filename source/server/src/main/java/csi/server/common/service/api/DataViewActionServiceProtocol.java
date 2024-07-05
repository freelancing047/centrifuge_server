package csi.server.common.service.api;

import java.security.GeneralSecurityException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import csi.server.business.service.annotation.QueryParam;
import csi.server.common.dto.*;
import csi.server.common.dto.SelectionListData.SharingInitializationRequest;
import csi.server.common.dto.SelectionListData.ResourceBasics;
import csi.server.common.dto.config.connection.DriverConfigInfo;
import csi.server.common.dto.installed_tables.TableInstallResponse;
import csi.server.common.dto.user.RecentAccess;
import csi.server.common.enumerations.AclControlType;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.exception.CsiSecurityException;
import csi.server.common.interfaces.DataContainer;
import csi.server.common.linkup.LinkupDataTransfer;
import csi.server.common.linkup.LinkupRequest;
import csi.server.common.linkup.LinkupResponse;
import csi.server.common.model.DataModelDef;
import csi.server.common.model.FieldDef;
import csi.server.common.model.SqlTableDef;
import csi.server.common.model.column.InstalledColumn;
import csi.server.common.model.dataview.AdHocDataSource;
import csi.server.common.model.dataview.DataView;
import csi.server.common.model.filter.Filter;
import csi.server.common.model.query.QueryParameterDef;
import csi.server.common.model.security.CapcoInfo;
import csi.server.common.model.security.SecurityTagsInfo;
import csi.server.common.model.visualization.selection.Selection;
import csi.server.common.util.ValuePair;
import csi.shared.gwt.exception.SecurityException;
import csi.shared.gwt.vortex.VortexService;

public interface DataViewActionServiceProtocol extends VortexService {

    public List<ResourceBasics> getRecentDataViews(int limitIn) throws CentrifugeException;

    public List<ResourceBasics> getRecentDataViews(String userIn, int limitIn) throws CentrifugeException, SecurityException;

    public List<ResourceBasics> listRecentlyOpenedDataViews2() throws CentrifugeException;

    public List<ResourceBasics> listUserResourceBasics() throws CsiSecurityException;

    public List<ResourceBasics> listDataViewBasics(AclControlType accessModeIn) throws CentrifugeException;

    public List<ResourceBasics> listDataViewExportBasics() throws CentrifugeException;

    public List<List<ResourceBasics>> getDataViewOverWriteControlLists() throws CentrifugeException;

    public List<String> listDataViewNames(AclControlType accessModeIn) throws CentrifugeException;

    public Boolean dataviewNameExists(String resName) throws CentrifugeException;

    // Used to update data sources -- follows security protocol
    public Response<String, DataView> getDataview(String dataViewIdIn, AclControlType modeIn);

    // Used to update data sources -- follows security protocol
    public Response<String, DataView> editDataview(DataView dataViewIn);

    // Used to update data sources -- follows security protocol
    // public Response<String, DataView> editDataview(String dataViewIdIn, DataSetOp dataTreeIn, List<DataSourceDef> sourceListIn, List<FieldDef> fieldListIn, List<QueryParameterDef> parameterListIn);

    // Used to relaunch dataview with new settings -- follows security protocol
    public DataView save(DataView view) throws CentrifugeException;

    /**
     * @param uuid Uuid of the dataview to delete. 
     * @return Uuid of the dataview deleted.
     */
    // Used to delete a dataview from the menu bar or if create fails -- follows security protocol
    public String deleteDataView(String uuid);

    public Response<String, DataView> createSimpleDataView(String nameIn, String remarksIn, SqlTableDef tableIn,
                                                           List<QueryParameterDef> dataSetParametersIn,
                                                           List<LaunchParam> parametersIn, List<AuthDO> credentialsIn,
                                                           CapcoInfo capcoInfoIn, SecurityTagsInfo tagInfoIn,
                                                           boolean forceOverwrite,
                                                           SharingInitializationRequest sharingRequestIn);

    public Response<String, DataView> createDataView(String requestIdIn, String nameIn, String commentsIn,
                                                     AdHocDataSource dataSourceIn, List<LaunchParam> parametersIn,
                                                     List<AuthDO> credentialsIn, boolean overwriteIn,
                                                     SharingInitializationRequest sharingRequestIn);

    public void closeDataview(String uuid);

    public Response<String, TableInstallResponse> spawnTable(String nameIn, String remarksIn, String dataViewIdIn,
                                                             String visualizationIdIn, Selection selectionIn,
                                                             List<FieldDef> fieldListIn);

    public Response<String, TableInstallResponse> updateTable(String uuidIn, String dataViewIdIn,
                                                              String visualizationIdIn, Selection selectionIn,
                                                              List<ValuePair<InstalledColumn, FieldDef>> pairedListIn);

    // Used to create a dataview -- no security protocols established
    public Response<String, DataView> spinoff(SpinoffRequestV2 request, String spinoffName) throws CentrifugeException;

    // Used to create or modify a dataview -- no security protocols established
    public Response<String, LinkupResponse> executeLinkup(LinkupRequest linkupRequest) throws CentrifugeException, GeneralSecurityException;

    // Used to refresh dataview -- shaky on security protocols
    public Response<String, DataView> relaunch(String dataviewUuidIn, List<LaunchParam> parametersIn, List<AuthDO> credentialsIn);

    public List<String> testFieldReferences(String dvUuidIn, List<String> fieldUuidsIn);

    public CsiMap<String, List<String>> listFieldReferencesMulti(List<String> fieldIds, String dvUuid);
    
    public CompilationResult testScript(String dvUuid, FieldDef scriptedField) throws CentrifugeException;

    public Response<String, Boolean> share(String resourceIn, SharingInitializationRequest sharingRequestIn);

    public Response<String, DataView> launchUrlTemplate(LaunchRequest launchRequest);

    public Response<String, DataView> launchTemplate(LaunchRequest launchRequest);

    public Response<String, DataView> launchTemplate(LaunchRequest launchRequest,
                                                     SharingInitializationRequest sharingRequestIn);

    public Response<String, DataView> openDataView(String uuid) throws CentrifugeException;

    public Response<String, DriverConfigInfo> getConnectionUIConfigByKey(String keyIn) throws CentrifugeException;

    // Used to add new linkup definition -- follows security protocols
    public LinkupDataTransfer addLinkupInformation(LinkupDataTransfer linkupDataIn) throws CentrifugeException;

    // Used to update a linkup definition -- follows security protocols
    public LinkupDataTransfer updateLinkupInformation(LinkupDataTransfer linkupDataIn) throws CentrifugeException;

    // Used to remove a linkup definition -- follows security protocols
    public LinkupDataTransfer removeLinkupInformation(LinkupDataTransfer linkupDataIn) throws CentrifugeException;

    public DataView getShadowCopyOfDataView(@QueryParam(value = "uuid") String uuid) throws CentrifugeException;

    // Used to update filter list -- follows security protocol
    void saveFilters(String DvUuid, Set<Filter> filters) throws CentrifugeException;

    public Response<String, Map<String, DataView>> getDataviewsByName(DataviewRequest request) throws CentrifugeException;

    public DataModelDef reorderWorksheet(int newIndex, int oldIndex, String dvUuid) throws CentrifugeException;

    public Response<String, List<FieldDef>> updateFieldList(String dataViewIdIn, List<FieldDef> newListIn,
                                                            List<FieldDef> changeListIn, List<FieldDef> discardListIn,
                                                            List<String> sortOrderIn);
    
    public Filter createSelectionFilter(String dataViewUuid, String vizUuid, String filterName, Selection selection) throws CentrifugeException;

    public Boolean filterNameExists(String dataViewUuid, String filterName) throws CentrifugeException;

    public List<FieldDef> getFullFieldList(String dataViewIdIn);

    List<FieldDef> getLinkupDiscardedFields(String dataViewUuid);

    void removeLinkup(LinkupDataTransfer linkupDataIn) throws CentrifugeException;

    void removeLinkupDiscardedFields(LinkupDataTransfer linkupDataIn) throws CentrifugeException;

    long getNewAnnotationCount(String dvUuid, Date date);
}
