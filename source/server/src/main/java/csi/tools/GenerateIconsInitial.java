package csi.tools;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom.JDOMException;

import liquibase.change.custom.CustomTaskChange;
import liquibase.database.Database;
import liquibase.exception.CustomChangeException;
import liquibase.exception.DatabaseException;
import liquibase.exception.SetupException;
import liquibase.exception.ValidationErrors;
import liquibase.integration.spring.SpringLiquibase.SpringResourceOpener;
import liquibase.resource.ResourceAccessor;

import csi.security.ACL;
import csi.security.queries.AclRequest;
import csi.server.business.helper.theme.OldThemeHelper;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.model.Resource;
import csi.server.dao.CsiPersistenceManager;

public class GenerateIconsInitial implements CustomTaskChange {
   private static final Logger LOG = LogManager.getLogger(GenerateIconsInitial.class);

   private static final String BASELINE_ICON_TAG = "Baseline";
   private static final String CIRCULAR_ICON_TAG = "Circular";

    private ResourceAccessor resourceAccessor;
    private String graphCircularPath;
    private String mapCircularPath;
    private String graphBaselinePath;
    private ValidationErrors validationErrors = null;

    @Override
    public String getConfirmationMessage() {
        // TODO Auto-generated method stub
        return "Theme imported";
    }

    @Override
    public void setUp() throws SetupException {
        // TODO Auto-generated method stub

    }

    @Override
    public void setFileOpener(ResourceAccessor resourceAccessor) {
        this.resourceAccessor = resourceAccessor;
    }

    @Override
    public ValidationErrors validate(Database database) {
        return validationErrors;
    }

    @Override
    public void execute(Database database) throws CustomChangeException {


        OldThemeHelper themeHelper = new OldThemeHelper();

        try {
            if(resourceAccessor instanceof SpringResourceOpener){

                SpringResourceOpener opener = (SpringResourceOpener)resourceAccessor;
                Map<String,String> iconpathsToUuids = new HashMap<String, String>();
                uploadTheme(themeHelper, opener, graphCircularPath, CIRCULAR_ICON_TAG, iconpathsToUuids);
                uploadTheme(themeHelper, opener, graphBaselinePath, BASELINE_ICON_TAG, iconpathsToUuids);
                uploadTheme(themeHelper, opener, mapCircularPath, CIRCULAR_ICON_TAG, iconpathsToUuids);
            }

        } catch (IOException e1) {
            LOG.error(e1);
            if(validationErrors == null) {
               validationErrors = new ValidationErrors();
            }

            validationErrors.addError(e1.getMessage());
        } catch (Exception e) {
            LOG.error(e);

            if(validationErrors == null) {
               validationErrors = new ValidationErrors();
            }
            validationErrors.addError(e.getMessage());
        } finally {
            CsiPersistenceManager.commit();
            CsiPersistenceManager.close();
            if((validationErrors != null) && validationErrors.hasErrors()){
                try {
                    database.rollback();
                } catch (DatabaseException e) {
                    LOG.error("Failed to rollback GenerateIconsInitial");

                }

                throw new CustomChangeException();
            } else {
            }


        }


    }

    public void uploadTheme(OldThemeHelper themeHelper, SpringResourceOpener opener, String path, String name, Map<String, String> iconpathsToUuids) throws ZipException, IOException,
            JDOMException, CentrifugeException {
        org.springframework.core.io.Resource resource;
        Resource imported;
        resource = opener.getResource(path);

        CsiPersistenceManager.begin();
        ZipFile zipFile = new ZipFile(resource.getFile());
        imported = OldThemeHelper.oldUnzipTheme(zipFile, name, false, iconpathsToUuids);
        zipFile.close();
        themeHelper.saveTheme(imported, true);

        CsiPersistenceManager.commit();

        CsiPersistenceManager.begin();
        //We put this theme to the everyone role
        ACL myAcl = AclRequest.getResourceACL(imported.getUuid());
        //myAcl.getEntries().add(new AccessControlEntry(AclControlType.READ, JAASRole.EVERYONE_ROLE_NAME.toLowerCase()));
        CsiPersistenceManager.merge(myAcl);
        CsiPersistenceManager.commit();
    }

    public String getGraphCircularPath() {
        return graphCircularPath;
    }

    public void setGraphCircularPath(String graphCircularPath) {
        this.graphCircularPath = graphCircularPath;
    }

    public String getMapCircularPath() {
        return mapCircularPath;
    }

    public void setMapCircularPath(String mapCircularPath) {
        this.mapCircularPath = mapCircularPath;
    }

    public String getGraphBaselinePath() {
        return graphBaselinePath;
    }

    public void setGraphBaselinePath(String graphBaselinePath) {
        this.graphBaselinePath = graphBaselinePath;
    }



}