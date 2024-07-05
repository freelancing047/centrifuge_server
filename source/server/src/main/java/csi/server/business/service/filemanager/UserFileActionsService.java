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
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FileCleaningTracker;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.base.Splitter;
import com.google.common.base.Throwables;

import csi.security.CsiSecurityManager;
import csi.server.business.helper.DataViewHelper;
import csi.server.business.helper.DeepCloner;
import csi.server.business.helper.ModelHelper;
import csi.server.business.service.GraphActionsService;
import csi.server.common.dto.SelectionListData.SelectorBasics;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.model.Resource;
import csi.server.common.model.dataview.DataView;
import csi.server.common.model.dataview.DataViewDef;
import csi.server.common.service.api.UserFileActionsServiceProtocol;
import csi.server.common.util.Format;
import csi.server.util.DateUtil;
import csi.server.util.FieldReferenceValidator;
import csi.shared.core.Constants;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class UserFileActionsService implements Filter, UserFileActionsServiceProtocol {
   private static final Logger LOG = LogManager.getLogger(UserFileActionsService.class);

   private static boolean _doDebug = true; //log.isDebugEnabled();

    private static final String PARAM_EXTRA_PATH = "p";
    private String topLevelFolder;
    private int maxFileSize;
    private DiskFileItemFactory diskFileItemFactory;
    private FileCleaningTracker fileCleaningTracker;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        String _topLevelFolder = filterConfig
                .getInitParameter(Constants.FileConstants.UPLOAD_INIT_PARAM_TOP_LEVEL_FOLDER);
        if (StringUtils.isNotBlank(_topLevelFolder)) {
            this.topLevelFolder = _topLevelFolder;
        } else {
            this.topLevelFolder = Constants.FileConstants.UPLOAD_DEFAULT_TOP_LEVEL_FOLDER;
        }
        {
            File dir = new File(topLevelFolder);
            if (!dir.exists()) {
                dir.mkdirs();
            }
        }

        String tempFolder = filterConfig.getInitParameter(Constants.FileConstants.UPLOAD_INIT_PARAM_TEMP_FOLDER);
        if (StringUtils.isBlank(tempFolder)) {
            tempFolder = Constants.FileConstants.UPLOAD_DEFAULT_TEMP_FOLDER;
        }
        {
            File dir = new File(tempFolder);
            if (!dir.exists()) {
                dir.mkdirs();
            }
        }

        int memoryThreshold = Constants.FileConstants.UPLOAD_DEFAULT_MEMORY_THRESHOLD * 1024 * 1024;
        String _memThreshold = filterConfig
                .getInitParameter(Constants.FileConstants.UPLOAD_INIT_PARAM_MEMORY_THRESHOLD);
        if (StringUtils.isNotBlank(_memThreshold)) {
            memoryThreshold = Integer.parseInt(_memThreshold) * 1024 * 1024;
        }

        String _maxFileSize = filterConfig.getInitParameter(Constants.FileConstants.UPLOAD_INIT_PARAM_MAX_FILE_SIZE);
        if (StringUtils.isNotBlank(_maxFileSize)) {
            maxFileSize = Integer.parseInt(_maxFileSize) * 1024 * 1024;
        } else {
            maxFileSize = Constants.FileConstants.UPLOAD_DEFAULT_MAX_FILE_SIZE * 1024 * 1024;
        }

        diskFileItemFactory = new DiskFileItemFactory(memoryThreshold, new File(tempFolder));
        diskFileItemFactory.setFileCleaningTracker(getFileCleaningTracker());
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException,
            ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) resp;
        if (ServletFileUpload.isMultipartContent(request)) {
            try {
                handleUpload(request, response);
            } catch (RuntimeException e) {
               LOG.error(e.getMessage(), e);
            }
        } else {
            chain.doFilter(request, response);
        }
    }

    public Integer getMaxUploadSize() {

        return (maxFileSize / 1024) / 1024;
    }

    private void handleUpload(HttpServletRequest request, HttpServletResponse response) {

        if (_doDebug) {
         LOG.debug(">> >> >>  UserFileActionsService::handleUpload()");
      }

        ServletFileUpload fileUpload = new ServletFileUpload(diskFileItemFactory);
        fileUpload.setSizeMax(maxFileSize);

        try {
            List<FileItem> items = fileUpload.parseRequest(request);
            // We send in the filename mapping as a separate part of the multi-part form post. Process that for
            // actual filenames to use.
            Map<String, String> filenameMapping = getFileNameMappings(items);
            for (FileItem item : items) {
                if (!item.isFormField()) {
                    if (item.getSize() > maxFileSize) {
                        response.sendError(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE, "File size exceeds limit");
                        return;
                    }

                    File outputFile = getOutputFile(pathless(item.getName()), filenameMapping, request);

                    if (_doDebug) {
                     LOG.debug("            -- save " + Format.value(item.getName()) + " as " + Format.value(outputFile.getName()));
                  }

                    item.write(outputFile);

                    if (!item.isInMemory()) {
                        item.delete();
                    }
                }
            } // end for (FileItem ...

            response.setStatus(HttpServletResponse.SC_OK);
        } catch (FileUploadException e) {
            throw Throwables.propagate(e);
        } catch (IOException e) {
            throw Throwables.propagate(e);
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }

        if (_doDebug) {
         LOG.debug("<< << <<  UserFileActionsService::handleUpload()");
      }

    }

    private Map<String, String> getFileNameMappings(List<FileItem> items) {
        Map<String, String> map = new HashMap<String, String>();
        for (FileItem fileItem : items) {
            if (fileItem.isFormField()
                    && fileItem.getFieldName().equals(Constants.FileConstants.UPLOAD_FILE_MAPPING_PART)) {
                Iterable<String> parts = Splitter.on('|').trimResults().omitEmptyStrings().split(fileItem.getString());
                for (String part : parts) {
                    String[] mapping = part.split(":");
                    map.put(mapping[0], mapping[1]);
                }
            }
        }
        return map;
    }

    private File getOutputFile(String filename, Map<String, String> filenameMapping, HttpServletRequest request) {
        String dirPath = topLevelFolder + File.separator + CsiSecurityManager.getUserName();
        String extraPath = request.getParameter(PARAM_EXTRA_PATH);
        if (StringUtils.isNotBlank(extraPath)) {
            dirPath += File.separator + extraPath;
        }
        File directory = new File(dirPath);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        String mapped = filenameMapping.get(filename);
        String name = mapped == null ? filename : mapped;
        return new File(dirPath + File.separator + name);
    }

    /**
     * Strip any path included in the filename (some browsers - Opera - are known to include path information).
     * @param name
     * @return
     */
    private String pathless(String name) {
        if (name.indexOf(File.separator) != -1) {
            return name.substring(name.lastIndexOf(File.separator) + 1);
        } else {
            return name;
        }
    }

    @Override
    public void destroy() {
        // noop
    }

    public FileCleaningTracker getFileCleaningTracker() {
        return fileCleaningTracker;
    }

    public void setFileCleaningTracker(FileCleaningTracker fileCleaningTracker) {
        this.fileCleaningTracker = fileCleaningTracker;
    }

    @Override
    public List<List<SelectorBasics>> getFileOverWriteControlLists(String subPathIn, List<String> extensionListIn) {

        if (_doDebug) {
         LOG.debug(">> >> >>  UserFileActionsService::getFileOverWriteControlLists(" + Format.value(subPathIn) + ")");
      }

        List<SelectorBasics> myConflicts = new ArrayList<SelectorBasics>();
        List<SelectorBasics> myFileList = new ArrayList<SelectorBasics>();
        String myUserName = CsiSecurityManager.getUserName();
        for (File myFile : getUserFiles(subPathIn)) {

            String myFileName = myFile.getName();

            StringBuilder myBuffer = new StringBuilder();
            String myFilePath = buildUserFilePath(myUserName, subPathIn, myFileName);

            myBuffer.append("path: ").append(myFilePath);
            myBuffer.append("<br>size: ").append(Long.toString(myFile.length()));
            myBuffer.append("<br>modified: ").append(getDateModified(myFile));

            if (_doDebug) {
               LOG.debug("            -- file name: " + Format.value(myFileName) + "");
            }

            myConflicts.add(new SelectorBasics(myFilePath, myFileName, myBuffer.toString()));
            if (extensionMatch(myFileName, extensionListIn)) {

                myFileList.add(new SelectorBasics(myFilePath, myFileName, myBuffer.toString()));
            }
        }

        if (_doDebug) {
         LOG.debug("<< << <<  DataViewActionsService::getFileOverWriteControlLists returns: " + Format.value(myFileList.size()) + " items");
      }

        return ModelHelper.generateOverWriteControlLists(myFileList, myConflicts, myConflicts);
    }

    public static void validateAndRepair(DataViewDef importedDataViewDef)
            throws CentrifugeException, GeneralSecurityException {
        if (importedDataViewDef == null) {
            throw new CentrifugeException("Imported resource does not have a definition.");
        }
        GraphActionsService.augmentRelGraphViewDef(importedDataViewDef);
        FieldReferenceValidator validator = new FieldReferenceValidator(importedDataViewDef);
        boolean invalid = false;
        try {
            validator.isValid();
        } catch (FieldReferenceValidator.ValidationException ve) {
            invalid = true;
        }

        if (invalid) {
            DataViewHelper.repairCorruptDataview(importedDataViewDef);
        }
    }

    private static DataViewDef getImportedDataViewDef(Resource imported) {
        DataViewDef def = null;

        if (imported instanceof DataView) {
            def = ((DataView) imported).getMeta();
        } else if (imported instanceof DataViewDef) {
            def = (DataViewDef) imported;
        }
        return def;
    }

    public static Resource importResourceFromStream(BufferedInputStream streamIn, String nameIn) throws CentrifugeException, IOException {

        Resource myImport = DataViewHelper.importXML(streamIn);
        if (!CsiSecurityManager.canEditResourceConnections(myImport)) {
            throw new CentrifugeException(
                    "Not authorized to import resource. The data definition contains one or more unauthorized connection types.");
        }

        myImport = DeepCloner.clone(myImport, DeepCloner.CloneType.NEW_ID);
        myImport.setName(nameIn);
        return myImport;
    }

    private String getFileUploadPath() {
        String userName = CsiSecurityManager.getUserName();
        return topLevelFolder + File.separator + userName + File.separator
                + Constants.FileConstants.UPLOAD_DATAVIEW_FOLDER + File.separator;
    }

   private static String getDateModified(File file) {
      return ZonedDateTime.ofInstant(Instant.ofEpochMilli(file.lastModified()), ZoneId.systemDefault())
                .format(DateUtil.FULL_DATE_TIME_FORMATTER);
   }

    private List<File> getUserFiles(String subPath) {
        String userName = CsiSecurityManager.getUserName();
        List<File> fileList = new ArrayList<File>();
        String path = buildUserFilePath(userName, subPath, null);
        File userDataPath = new File(path);
        File[] files = userDataPath.listFiles();
        if ((null != files) && (0 < files.length)) {

            List<File> myTempList = Arrays.asList(files);
            for (File file : myTempList) {
                if (file.isFile() && (!file.isHidden())) {
                    fileList.add(file);
                }
            }
        }
        return fileList;
    }

    private boolean extensionMatch(String nameIn, List<String> listIn) {

        boolean myMatch = false;

        if (null != nameIn) {

            if ((null != listIn) && !listIn.isEmpty()) {

                String[] myParts = nameIn.split("\\.");

                if (1 < myParts.length) {

                    String myExtension = myParts[myParts.length - 1];

                    for (String myTest : listIn) {

                        if (myExtension.equalsIgnoreCase(myTest)) {

                            myMatch = true;
                            break;
                        }
                    }
                }

            } else {

                myMatch = true;
            }
        }
        return myMatch;
    }

    private String buildUserFilePath(String userNameIn, String subPathIn, String fileNameIn) {

        StringBuilder myBuffer = new StringBuilder();

        myBuffer.append(topLevelFolder).append(File.separator).append(userNameIn).append(File.separator).append(subPathIn).append(File.separator);

        if (null != fileNameIn) {

            myBuffer.append(fileNameIn);
        }
        return myBuffer.toString();
    }
}
