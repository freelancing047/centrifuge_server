/**
 *  Copyright (c) 2008 Centrifuge Systems, Inc.
 *  All rights reserved.
 *
 *  This software is the confidential and proprietary information of
 *  Centrifuge Systems, Inc. ("Confidential Information").  You shall
 *  not disclose such Confidential Information and shall use it only
 *  in accordance with the terms of the license agreement you entered
 *  into with Centrifuge Systems.
 *
 **/
package csi.server.business.service.filemanager;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

import csi.security.CsiSecurityManager;
import csi.security.queries.AclRequest;
import csi.server.business.helper.DataViewHelper;
import csi.server.business.helper.FileHelper;
import csi.server.business.helper.SharedDataSourceHelper;
import csi.server.business.helper.theme.ThemeHelper;
import csi.server.business.service.AbstractService;
import csi.server.common.dto.AuthDO;
import csi.server.common.dto.FileUploadBlock;
import csi.server.common.dto.LaunchParam;
import csi.server.common.dto.Response;
import csi.server.common.dto.SelectionListData.ResourceBasics;
import csi.server.common.dto.installed_tables.NewExcelInstallRequest;
import csi.server.common.dto.installed_tables.NonBinaryInstallRequest;
import csi.server.common.dto.installed_tables.TableInstallRequest;
import csi.server.common.dto.installed_tables.TableInstallResponse;
import csi.server.common.dto.resource.ExportImportHelper;
import csi.server.common.dto.resource.ImportItem;
import csi.server.common.dto.resource.ImportRequest;
import csi.server.common.dto.resource.ImportResponse;
import csi.server.common.dto.resource.MinResourceInfo;
import csi.server.common.dto.resource.ResourceConflictInfo;
import csi.server.common.enumerations.AclControlType;
import csi.server.common.enumerations.AclResourceType;
import csi.server.common.enumerations.ConflictResolution;
import csi.server.common.enumerations.CsiFileType;
import csi.server.common.enumerations.ServerMessage;
import csi.server.common.enumerations.ServerResponse;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.exception.CsiSecurityException;
import csi.server.common.model.Resource;
import csi.server.common.model.UUID;
import csi.server.common.model.column.InstalledColumn;
import csi.server.common.model.dataview.DataView;
import csi.server.common.model.dataview.DataViewDef;
import csi.server.common.model.tables.InstalledTable;
import csi.server.common.service.api.UploadServiceProtocol;
import csi.server.common.util.Format;
import csi.server.common.util.ValuePair;
import csi.server.dao.CsiPersistenceManager;
import csi.server.task.api.TaskHelper;
import csi.server.task.exception.TaskCancelledException;
import csi.server.util.CacheUtil;
import csi.server.util.SystemInfo;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class UploadService extends AbstractService implements UploadServiceProtocol {
   private static final Logger LOG = LogManager.getLogger(UploadService.class);


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private static ConflictResolution conflictResoutionDefault = null;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public Response<Integer, FileUploadBlock> receiveFileBlock(int handleIn, FileUploadBlock uploadBlockIn) {

        try {

            long mySize = uploadBlockIn.getBlockSize();

            if (0L < mySize) {

                byte[] myData = uploadBlockIn.getBlock();

                if ((null != myData) && (mySize <= myData.length)) {

                    String myFileName = (0 < uploadBlockIn.getBlockNumber()) ? uploadBlockIn.getFileName() : UUID.randomUUID();

                    try (FileOutputStream myFile = FileHelper.getOutputFile(myFileName, (0 < uploadBlockIn.getBlockNumber()))) {

                       if (null != myFile) {

                          myFile.write(myData, 0, (int) mySize);

                          uploadBlockIn.setFileName(myFileName);

                       } else {

                          throw new CentrifugeException("Unable to open file!");
                       }
                    }

                } else {

                    throw new CentrifugeException("Data size problem!");
                }
            }

            uploadBlockIn.setBase64Block(null);
            return new Response<Integer, FileUploadBlock>(Integer.valueOf(handleIn), uploadBlockIn);

        } catch(Exception myException) {

            uploadBlockIn.setBase64Block(null);
            LOG.error("Caught exception while receiving file block:\n" + Format.value(myException));
            return new Response<Integer, FileUploadBlock>(Integer.valueOf(handleIn), uploadBlockIn,
                    ServerMessage.FILE_UPLOAD_ERROR,
                    Format.value(myException));
        }
    }

    public List<ResourceConflictInfo> identifyImportConflicts(List<MinResourceInfo> listIn,
                                                              boolean hasIconsIn, boolean hasMapsIn) {

        List<ResourceConflictInfo> myList = new ArrayList<ResourceConflictInfo>();

        if ((null != listIn) && !listIn.isEmpty()) {

            if (CsiSecurityManager.isAdmin()) {

                for (MinResourceInfo myInfo : listIn) {

                    Resource myResource = AclRequest.getResource(myInfo.getUuid());

                    if (null != myResource) {

                        myList.add(new ResourceConflictInfo(myInfo, myResource.getUuid(), myResource.getName(),
                                                            myResource.getOwner(), true));
                    } else {

                        myList.add(new ResourceConflictInfo(myInfo));
                    }
                }

            } else {

                for (MinResourceInfo myInfo : listIn) {

                    Resource myResource = AclRequest.getResource(myInfo.getUuid());

                    if (null != myResource) {

                        AclControlType[] myAcl = new AclControlType[]{AclControlType.EDIT, AclControlType.DELETE};

                        boolean myAuthorizedFlag = CsiSecurityManager.isAuthorizedAll(myInfo.getUuid(), myAcl, false);

                        myList.add(new ResourceConflictInfo(myInfo, myResource.getUuid(), myResource.getName(),
                                                            myResource.getOwner(), myAuthorizedFlag));
                    } else {

                        myList.add(new ResourceConflictInfo(myInfo));
                    }
                }
            }
        }
        if (hasIconsIn && CsiSecurityManager.isIconAdmin()) {

            myList.add(ResourceConflictInfo.createIconEntry());
        }
        return myList.isEmpty() ? null : myList;
    }

   public Response<Integer,List<ImportResponse>> beginImport(Integer handleIn, ImportRequest requestIn) {
      Response<Integer,List<ImportResponse>> result = null;

      try {
         String myFileHandle = requestIn.getFileHandle();
         String myFilePath = FileHelper.buildUserFilePath(myFileHandle);
         File myFile = new File(myFilePath);
         List<ImportItem> myImportList = requestIn.getImportList();
         List<ImportResponse> myResultList = new ArrayList<ImportResponse>();

         if (requestIn.getRawXml()) {
            ImportItem myItem = myImportList.get(0);
            AclResourceType myType = myItem.getType();

            try (FileInputStream myFileStream = new FileInputStream(myFile);
                 BufferedInputStream myInputStream = new BufferedInputStream(myFileStream)) {
               Resource resource = UserFileActionsService.importResourceFromStream(myInputStream, myItem.getName());

               if (AclResourceType.DATAVIEW == myType) {
                  myResultList.add(saveImportedDataView((DataView) resource, myItem));
               } else if (AclResourceType.TEMPLATE == myType) {
                  myResultList.add(saveImportedTemplate((DataViewDef) resource, myItem));
               }
            }
         } else {
            try (ZipFile myZipFile = new ZipFile(myFile)) {
               myResultList = importFromCollection(myZipFile, myImportList);
            }
         }
         result = new Response<Integer, List<ImportResponse>>(handleIn, myResultList);
      } catch (Exception myException) {
         result = new Response<Integer, List<ImportResponse>>(handleIn, ServerMessage.CAUGHT_EXCEPTION,
                                                              Format.value(myException));
      }
      return result;
   }

    public Response<String, AclResourceType> activateResource(String uuidIn, String nameIn, String remarksIn) {

        try {

            AclResourceType myResourceType = AclRequest.findOwnedResourceType(uuidIn);

            if (null != myResourceType) {

                if (myResourceType.canBeImported()) {

                    Resource myResource = CsiPersistenceManager.findObject(myResourceType.getObjectClass(), uuidIn);

                    Resource myConflict = AclRequest.findOwnedResourceByName(myResource.getClass(), nameIn);

                    if (null != myConflict) {

                       LOG.info("Removing " + myResourceType.getLabel() + Format.value(nameIn)
                                + " to enable replacement by imported " + myResourceType.getLabel()
                                + " " + Format.value(myResource.getName()) + ".");
                        CsiPersistenceManager.deleteObject(myConflict);
                    }
                    LOG.info("Renaming imported " + myResourceType.getLabel()
                            + Format.value(myResource.getName()) + " to " + Format.value(nameIn) + ".");
                    myResource.setName(nameIn);
                    myResource.setRemarks(remarksIn);
                    myResource.setResourceType(myResourceType);
                    myResource.setPriorType(null);

                    if (AclResourceType.DATAVIEW == myResourceType) {

                        DataViewHelper.clearDataReferences((DataView)myResource);
                    }
                    CsiPersistenceManager.merge(myResource);

                    return new Response<String, AclResourceType>(uuidIn, myResourceType);

                } else {

                    Class<? extends Resource> myClass = myResourceType.getObjectClass();

                    if (null != myClass) {

                        CsiPersistenceManager.deleteObject(myClass, uuidIn);
                    }
                    return new Response<String, AclResourceType>(uuidIn, ServerMessage.RESOURCE_NOT_IMPORTABLE);
                }

            } else {

                return new Response<String, AclResourceType>(uuidIn, ServerMessage.RESOURCE_NOT_FOUND);
            }

        } catch (Exception myException) {

           LOG.error("Caught exception while activating upload:\n" + Format.value(myException));
            return new Response<String, AclResourceType>(uuidIn, ServerMessage.CAUGHT_EXCEPTION,
                    Format.value(myException));
        }
    }

    public void discardResource(String uuidIn) {

        try {

            AclResourceType myResourceType = AclRequest.findOwnedResourceType(uuidIn);

            if (null != myResourceType) {

                Class<? extends Resource> myClass = myResourceType.getObjectClass();

                if (null != myClass) {

                    CsiPersistenceManager.deleteObject(myClass, uuidIn);
                }
            }

        } catch (Exception myException) {

           LOG.error("Caught exception while discarding upload:\n" + Format.value(myException));
        }
    }

    public Response<String, InstalledTable> prepareForRefresh(String uuidIn) {
        try {

            InstalledTable myTable = CsiPersistenceManager.findObjectAvoidingSecurity(InstalledTable.class, uuidIn, AclControlType.EDIT);

            if (null != myTable) {

                return new Response<String, InstalledTable>(uuidIn, myTable);
            }

        } catch (Exception myException) {

            return new Response<String, InstalledTable>(uuidIn, ServerMessage.CAUGHT_EXCEPTION,
                    Format.value(myException));
        }
        return new Response<String, InstalledTable>(uuidIn, ServerMessage.RESOURCE_NOT_FOUND);
    }

    public Response<String, ValuePair<Boolean, InstalledTable>> updateInstalledTable(String uuidIn,
                                                                                     List<AuthDO> credentialsIn,
                                                                                     List<LaunchParam> parametersIn) {
        try {

            InstalledTable myTable = CsiPersistenceManager.findObjectAvoidingSecurity(InstalledTable.class, uuidIn, AclControlType.EDIT);

            if (null != myTable) {

                return SharedDataSourceHelper.updateInstalledTable(myTable, credentialsIn, parametersIn);
            }

        } catch (Exception myException) {

            return new Response<String, ValuePair<Boolean, InstalledTable>>(uuidIn, ServerMessage.CAUGHT_EXCEPTION,
                    Format.value(myException));
        }
        return new Response<String, ValuePair<Boolean, InstalledTable>>(uuidIn, ServerMessage.RESOURCE_NOT_FOUND);
    }

    public Response<Integer, TableInstallResponse> installFile(int handleIn, TableInstallRequest requestIn) {

        String myTableName = CacheUtil.generateInstalledTableName();
        CsiFileType myDataSource = requestIn.getFileType();

        try {

            switch (myDataSource) {

                case NEW_EXCEL:

                    return SharedDataSourceHelper.installExcelFile(handleIn, requestIn, myTableName);

                case OLD_EXCEL:

                    break;

                case CSV:

                    return SharedDataSourceHelper.installCsvFile(handleIn, requestIn, myTableName);

                case TEXT:

                    return SharedDataSourceHelper.installCsvFile(handleIn, requestIn, myTableName);

                case DUMP:

                    break;

                case XML:

                    break;

                case JSON:

                    break;

                case ADHOC:

                    break;
            }

            return new Response<Integer, TableInstallResponse>(handleIn, new TableInstallResponse(ServerResponse.FAILED,
                    "Uploaded unsupported file type!"), ServerMessage.FILE_UPLOAD_ERROR,
                    Format.value("File type " + Format.value(requestIn.getFileType().getLabel()) + " not supported!"));

        } catch (TaskCancelledException myException) {

           LOG.info("Upload cancelled.");
            return new Response<Integer, TableInstallResponse>(handleIn,
                    new TableInstallResponse(ServerResponse.CANCELLED));

        } catch(Exception myException) {

            String myError = "Caught exception installing " + myDataSource.getDescription();

            LOG.error(myError +":\n" + Format.value(myException));

            return new Response<Integer, TableInstallResponse>(handleIn,
                    new TableInstallResponse(ServerResponse.FAILED, myError),
                    ServerMessage.CAUGHT_EXCEPTION, Format.value(myException));
        }
    }

    public Response<Integer, TableInstallResponse> updateInstalledFile(int handleIn, TableInstallRequest installBlockIn) {

        CsiFileType myDataSource = installBlockIn.getFileType();

        try {

            switch (myDataSource) {

                case NEW_EXCEL:

                    return SharedDataSourceHelper.updateExcelFile(handleIn, (NewExcelInstallRequest) installBlockIn);

                case OLD_EXCEL:

                    break;

                case CSV:

                    return SharedDataSourceHelper.updateCsvFile(handleIn, (NonBinaryInstallRequest) installBlockIn);

                case TEXT:

                    return SharedDataSourceHelper.updateCsvFile(handleIn, (NonBinaryInstallRequest) installBlockIn);

                case DUMP:

                    break;

                case XML:

                    break;

                case JSON:

                    break;

                case ADHOC:

                    break;
            }

            return new Response<Integer, TableInstallResponse>(handleIn, new TableInstallResponse(ServerResponse.FAILED,
                    "Uploaded unsupported file type!"), ServerMessage.FILE_UPLOAD_ERROR,
                    Format.value("File type " + Format.value(installBlockIn.getFileType().getLabel()) + " not supported!"));

        } catch (TaskCancelledException myException) {

           LOG.info("Upload cancelled.");
            return new Response<Integer, TableInstallResponse>(handleIn,
                    new TableInstallResponse(ServerResponse.CANCELLED));

        } catch(Exception myException) {

            String myError = "Caught exception installing " + myDataSource.getDescription();

            LOG.error(myError +":\n" + Format.value(myException));

            return new Response<Integer, TableInstallResponse>(handleIn,
                    new TableInstallResponse(ServerResponse.FAILED, myError),
                    ServerMessage.CAUGHT_EXCEPTION, Format.value(myException));
        }
    }

    public void cancelUpload(int handleIn, ImportRequest requestIn) {

        FileHelper.deleteUserFile(requestIn.getFileHandle());
    }

    public void cancelUpload(int handleIn, FileUploadBlock uploadBlockIn) {

        FileHelper.deleteUserFile(uploadBlockIn.getFileName());
    }

    public void deleteUserFile(String fileNameIn) {

        FileHelper.deleteUserFile(fileNameIn);
    }

    public void cancelInstall(String taskIdIn) {

        TaskHelper.taskController.cancelTask(taskIdIn);
    }

    public void cancelImport(String taskIdIn) {

        TaskHelper.taskController.cancelTask(taskIdIn);
    }

    public String delete(String uuid) throws CentrifugeException {

        if (CsiSecurityManager.isAuthorized(uuid, AclControlType.DELETE)) {

            SharedDataSourceHelper.delete(CsiPersistenceManager.findForDelete(InstalledTable.class, uuid));
            return uuid;

        } else {

            throw new CentrifugeException("Not authorized to delete this Dataview");
        }
    }

    public Response<CsiFileType, List<String>> listUserTableNames(CsiFileType fileTypeIn) {

        try {

            return new Response<CsiFileType, List<String>>(fileTypeIn, AclRequest.listUserTableNames(fileTypeIn));

        } catch(Exception myException) {

            return new Response<CsiFileType, List<String>>(fileTypeIn, ServerMessage.CAUGHT_EXCEPTION, Format.value(myException));
        }
    }

   private List<ImportResponse> importFromCollection(ZipFile zipFileIn, List<ImportItem> listIn) {
      List<ImportResponse> myResultList = new ArrayList<ImportResponse>();

      for (ImportItem myItem : listIn) {
         ImportResponse myResponse = null;
         String myUuid = myItem.getUuid();
         String myName = myItem.getName();
         String myOwner = myItem.getOwner();
         AclResourceType myType = myItem.getType();
         Map<String, String> grabBag = ExportImportHelper.buildIconListFileName();
         String myFileName = (AclResourceType.ICON == myType) ? grabBag.get("type") : myItem.getFile();

         try {
            ZipEntry myEntry = zipFileIn.getEntry(myFileName);

             if (myEntry == null) {

                 int hits = 0;
                 Enumeration<? extends ZipEntry> entries = zipFileIn.entries();
                 while(entries.hasMoreElements()){
                     ArrayList<String> nameParts = Lists.newArrayList(Splitter.on("_").split(myFileName));
                     ZipEntry zipEntry = entries.nextElement();
                     int matchingParts = 0;
                     for (String s : Splitter.on("_").split(zipEntry.getName())) {
                         if (nameParts.contains(s)) {
                             nameParts.remove(s);
                             matchingParts++;
                         }
                     }
                     if (matchingParts > hits) {
                         hits = matchingParts;
                         myEntry = zipEntry;
                     }

                 }

             }


            TaskHelper.reportProgress(TaskHelper.getCurrentContext().getTaskId(),
                                      (((100 * myResultList.size()) + 50) / listIn.size()));

            if (myEntry == null) {
               String myMessage = "Unable to find resource entry within zip file.";
               LOG.error(myMessage);
               myResponse = new ImportResponse(myUuid, myName, myOwner, myType, myMessage, true);
            } else {
               try (InputStream myFileStream = zipFileIn.getInputStream(myEntry)) {
                  if (AclResourceType.DATAVIEW == myType) {
                     try (BufferedInputStream myInputStream = new BufferedInputStream(myFileStream)) {
                        Resource myResource = UserFileActionsService.importResourceFromStream(myInputStream, myName);
                        myResponse = saveImportedDataView((DataView) myResource, myItem);
                     }
                  } else if (AclResourceType.TEMPLATE == myType) {
                     try (BufferedInputStream myInputStream = new BufferedInputStream(myFileStream)) {
                        Resource myResource = UserFileActionsService.importResourceFromStream(myInputStream, myName);
                        myResponse = saveImportedTemplate((DataViewDef) myResource, myItem);
                     }
                  } else if ((AclResourceType.GRAPH_THEME == myType) || (AclResourceType.MAP_THEME == myType)) {
                     SAXBuilder myDocumentBuilder = new SAXBuilder();
                     Document myDocument = myDocumentBuilder.build(myFileStream);
                     Element myElement = myDocument.getRootElement();
                     String myMessage = ThemeHelper.importTheme(myElement, myItem);
                     myResponse = new ImportResponse(myUuid, myName, myOwner, myType, myMessage, (myMessage != null));
                  } else if (AclResourceType.ICON == myType) {
                     SAXBuilder myDocumentBuilder = new SAXBuilder();
                     Document myDocument = myDocumentBuilder.build(myFileStream);
                     Element myElement = myDocument.getRootElement();
                     String myMessage = ThemeHelper.importIcons(myElement);
                     myResponse = new ImportResponse(myUuid, myName, myOwner, myType, myMessage, (myMessage != null));
                  } else if (AclResourceType.MAP_BASEMAP == myType) {
                     SAXBuilder myDocumentBuilder = new SAXBuilder();
                     Document myDocument = myDocumentBuilder.build(myFileStream);
                     Element myElement = myDocument.getRootElement();
                     String myMessage = ThemeHelper.importBasemap(myElement, myItem);
                     myResponse = new ImportResponse(myUuid, myName, myOwner, myType, myMessage, (myMessage != null));
                  }
               }
            }
         } catch (Exception myException) {
            String myMessage = "Caught exception processing " + myType.getDescriptor() + ", "
                                    + Format.value(myName) + "\n" + Format.value(myException);
            LOG.error(myMessage);
            myResponse = new ImportResponse(myUuid, myName, myOwner, myType, myMessage, true);
         }
         myResultList.add(myResponse);
         TaskHelper.reportProgress(TaskHelper.getCurrentContext().getTaskId(),
                                   ((100 * myResultList.size()) / listIn.size()));
      }
      return myResultList;
   }

    private ImportResponse saveImportedDataView(DataView dataViewIn, ImportItem requestIn) {

        String myUuid = dataViewIn.getUuid();
        String myName = dataViewIn.getName();
        String myOwner = dataViewIn.getOwner();
        AclResourceType myType = AclResourceType.DATAVIEW;
        String myMessage = null;
        boolean myErrorFlag = false;
        String myVersion = dataViewIn.getVersion();

        try {

            if (!SystemInfo.getReleaseVersion().equals(myVersion)) {

                myMessage = "Import version " + Format.value(myVersion)
                        + " does not match expected version " + Format.value(myVersion)
                        + " for " + myType.getDescriptor() + " " + Format.value(myName);
                LOG.warn(myMessage);
            }
            UserFileActionsService.validateAndRepair(dataViewIn.getMeta());
            DataViewHelper.saveResource(dataViewIn, true);
            LOG.info("Importing " + myType.getLabel() + " with name " + Format.value(myName));

        } catch (Exception myException) {

            myErrorFlag = true;
            myMessage = "Caught exception while saving resource:\n" + Format.value(myException);
            LOG.error(myMessage);
        }

        return new ImportResponse(myUuid, myName, myOwner, myType, myMessage, myErrorFlag);
    }

    private ImportResponse saveImportedTemplate(DataViewDef templateIn, ImportItem requestIn) {

        String myUuid = templateIn.getUuid();
        String myName = templateIn.getName();
        String myOwner = templateIn.getOwner();
        AclResourceType myType = AclResourceType.TEMPLATE;
        String myMessage = null;
        boolean myErrorFlag = false;
        String myVersion =  templateIn.getVersion();

        try {

            if (!SystemInfo.getReleaseVersion().equals(myVersion)) {

                myMessage = "Import version " + Format.value(myVersion)
                        + " does not match expected version " + Format.value(myVersion)
                        + " for " + myType.getDescriptor() + " " + Format.value(myName);
                LOG.warn(myMessage);
            }
            UserFileActionsService.validateAndRepair(templateIn);
            templateIn.setTemplate(true);
            DataViewHelper.fixupPersistenceLinkage(templateIn);
            LOG.info("Importing " + myType.getLabel() + " with name " + Format.value(templateIn.getName()));
            myMessage = ThemeHelper.processConflictResolution(templateIn, requestIn);

        } catch (Exception myException) {

            myErrorFlag = true;
            myMessage = "Caught exception while saving resource:\n" + Format.value(myException);
            LOG.error(myMessage);
        }

        return new ImportResponse(myUuid, myName, myOwner, myType, myMessage, myErrorFlag);
    }

    // Used for creating new Installed Tables
    @Override
    public List<ResourceBasics> getTableOverWriteControlList(CsiFileType fileTypeIn) throws CsiSecurityException {

        String myPattern = CsiSecurityManager.getUserName().toLowerCase() + "." + fileTypeIn.getExtension() + ".%";
        List<ResourceBasics> myListOut = AclRequest.listMatchingTablesAvoidingSecurity(myPattern, null);

        return ((null != myListOut) && !myListOut.isEmpty()) ? myListOut : null;
    }

    // Used for updating existing Installed Tables
    @Override
    public List<ResourceBasics> getTableSelectionList(CsiFileType fileTypeIn) throws CsiSecurityException {

        String myTest = "." + fileTypeIn.getExtension() + ".";
        String myPattern = "%" + myTest + "%";
        List<ResourceBasics> myResults = AclRequest.listMatchingTablesAvoidingSecurity(myPattern, new AclControlType[] {AclControlType.EDIT});
        List<ResourceBasics> myListOut = new ArrayList<>();

        for (ResourceBasics myItem : myResults) {

            String myName = myItem.getName();

            if (myName.indexOf('.') == myName.indexOf(myTest)) {

                myListOut.add(myItem);
            }
        }
        return myListOut.isEmpty() ? null : myListOut;
    }

    public List<InstalledColumn> getTableColumnList(String tableIdIn) {

        InstalledTable myTable = CsiPersistenceManager.findObjectAvoidingSecurity(InstalledTable.class, tableIdIn);

        return (null != myTable) ? myTable.getColumns() : null;
    }

    public Response<String, InstalledTable> getInstalledTable(String tableIdIn) {

        InstalledTable myTable = null;

        try {

            myTable = CsiPersistenceManager.findObject(InstalledTable.class, tableIdIn);

        } catch (Exception myException) {

            new Response<String, InstalledTable>(tableIdIn, ServerMessage.CAUGHT_EXCEPTION, Format.value(myException));
        }
        return new Response<String, InstalledTable>(tableIdIn, myTable);
    }

    public static ConflictResolution getConflictResoutionDefault() {

        if (null == conflictResoutionDefault) {

            conflictResoutionDefault = ConflictResolution.MERGE_KEEP;
        }
        return conflictResoutionDefault;
    }

    private String getTempName(String baseIn) {

        return "@temp_" + baseIn;
    }
}
