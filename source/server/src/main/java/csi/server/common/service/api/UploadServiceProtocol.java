package csi.server.common.service.api;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import csi.server.common.dto.*;
import csi.server.common.dto.SelectionListData.ResourceBasics;
import csi.server.common.dto.installed_tables.TableInstallRequest;
import csi.server.common.dto.installed_tables.TableInstallResponse;
import csi.server.common.dto.resource.ImportRequest;
import csi.server.common.dto.resource.ImportResponse;
import csi.server.common.dto.resource.MinResourceInfo;
import csi.server.common.dto.resource.ResourceConflictInfo;
import csi.server.common.enumerations.AclResourceType;
import csi.server.common.enumerations.ConflictResolution;
import csi.server.common.enumerations.CsiFileType;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.exception.CsiSecurityException;
import csi.server.common.model.column.InstalledColumn;
import csi.server.common.model.tables.InstalledTable;
import csi.server.common.util.ValuePair;
import csi.shared.gwt.vortex.VortexService;

public interface UploadServiceProtocol extends VortexService {

    public Response<Integer, FileUploadBlock> receiveFileBlock(int handleIn, FileUploadBlock uploadBlockIn);

    public List<ResourceConflictInfo> identifyImportConflicts(List<MinResourceInfo> listIn,
                                                              boolean hasIcons, boolean hasMaps);

    public Response<Integer, List<ImportResponse>> beginImport(Integer handleIn, ImportRequest requestIn);

    public void discardResource(String uuidIn);

    public Response<String, InstalledTable> prepareForRefresh(String uuidIn);

    public Response<String, ValuePair<Boolean, InstalledTable>> updateInstalledTable(String uuidIn,
                                                                                     List<AuthDO> credentialsIn,
                                                                                     List<LaunchParam> parametersIn);

    public Response<Integer, TableInstallResponse> installFile(int handleIn, TableInstallRequest installBlockIn);

    public Response<Integer, TableInstallResponse> updateInstalledFile(int handleIn, TableInstallRequest installBlockIn);

    public void cancelUpload(int handleIn, FileUploadBlock uploadBlockIn);

    public void deleteUserFile(String fileNameIn);

    public void cancelInstall(String taskIdIn);

    public void cancelImport(String taskIdIn);

    public Response<CsiFileType, List<String>> listUserTableNames(CsiFileType fileTypeIn);

    public List<ResourceBasics> getTableOverWriteControlList(CsiFileType fileType) throws CsiSecurityException;

    public List<ResourceBasics> getTableSelectionList(CsiFileType fileTypeIn) throws CsiSecurityException;

    public List<InstalledColumn> getTableColumnList(String tableIdIn);

    public Response<String, InstalledTable> getInstalledTable(String tableIdIn);
}
