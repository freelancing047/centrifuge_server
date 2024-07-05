package csi.server.common.dto;

import csi.server.common.model.dataview.DataView;
import csi.server.common.model.dataview.DataViewDef;
import csi.server.common.model.security.CapcoInfo;
import csi.server.common.model.security.SecurityTagsInfo;

/**
 * Created by centrifuge on 12/10/2015.
 */
public class DataPackage {

    private static String _basePrefix = "recovery_";
    private static long _baseValue = 0;

    private String _prefix = null;
    private String _baseName = null;
    private String[][] _linkups = { null, null };
    private String[][] _tables = { null, null };
    private String[][] _views = { null, null };
    private String[] _installedTables = null;

    private String linkupMappings = null;
    private int _nextLinkupId;
    private long _nextLinkupRowId;

    private CapcoInfo _capcoInfo = null;
    private SecurityTagsInfo _securityTagsInfo = null;

    public DataPackage() {

        _prefix = _basePrefix + Long.toString(_baseValue++) + "_";
    }

    public static DataPackage extractDataPackage(DataView dataViewIn) {

        DataPackage myDataPackage = null;

        if (!dataViewIn.getNeedsRefresh()) {

            DataViewDef myMeta = dataViewIn.getMeta();

            myDataPackage = new DataPackage();

            myDataPackage.setBaseName(dataViewIn.getUuid());
            myDataPackage.setLinkups(0, dataViewIn.clearLinkups());
            myDataPackage.setTables(0, dataViewIn.clearTables());
            myDataPackage.setViews(0, dataViewIn.clearViews());
            myDataPackage.setInstalledTables(dataViewIn.clearInstalledTables());
            myDataPackage.setNextLinkupId(dataViewIn.getNextLinkupId());
            myDataPackage.setNextLinkupRowId(dataViewIn.getNextLinkupRowId());

            if (null != myMeta) {

                myDataPackage.setCapcoInfo(myMeta.getCapcoInfo());
                myDataPackage.setSecurityTagsInfo(myMeta.getSecurityTagsInfo());
            }

            dataViewIn.setNeedsRefresh(true);
        }
        return myDataPackage;
    }

    public String getPrefix() {

        return _prefix;
    }

    public void setLinkups(int idIn, String[] linkupsIn) {

        _linkups[idIn] = linkupsIn;
    }

    public String[] getLinkups(int idIn) {

        return _linkups[idIn];
    }

    public void setTables(int idIn, String[] tablesIn) {

        _tables[idIn] = tablesIn;
    }

    public String[] getTables(int idIn) {

        return _tables[idIn];
    }

    public void setViews(int idIn, String[] viewsIn) {

        _views[idIn] = viewsIn;
    }

    public String[] getViews(int idIn) {

        return _views[idIn];
    }

    public void setInstalledTables(String[] installedTablesIn) {

        _installedTables = installedTablesIn;
    }

    public String[] getInstalledTables() {

        return _installedTables;
    }

    public void setBaseName(String baseNameIn) {

        _baseName = baseNameIn;
    }

    public String getBaseName() {

        return _baseName;
    }

    public String getLinkupMappings() {

        return linkupMappings;
    }

    public void setLinkupMappings(String linkupMappingsIn) {

        linkupMappings = linkupMappingsIn;
    }

    public void setNextLinkupId(int nextLinkupIdIn) {

        _nextLinkupId = nextLinkupIdIn;
    }

    public int getNextLinkupId() {

        return _nextLinkupId;
    }

    public void setNextLinkupRowId(long nextRowIdIn) {

        _nextLinkupRowId = nextRowIdIn;
    }

    public long getNextLinkupRowId() {

        return _nextLinkupRowId;
    }

    public void setCapcoInfo(CapcoInfo capcoInfoIn) {

        _capcoInfo = capcoInfoIn;
    }

    public CapcoInfo getCapcoInfo() {

        return _capcoInfo;
    }

    public void setSecurityTagsInfo(SecurityTagsInfo securityTagsInfoIn) {

        _securityTagsInfo = securityTagsInfoIn;
    }

    public SecurityTagsInfo getSecurityTagsInfo() {

        return _securityTagsInfo;
    }

    private String buildString(String[] listIn) {

        StringBuilder myBuffer = null;

        if ((null != listIn) && (0 < listIn.length)) {

            myBuffer = new StringBuilder();

            myBuffer.append(listIn[0]);

            for (int i = 1; listIn.length > i; i++) {

                myBuffer.append('|');
                myBuffer.append(listIn[i]);
            }
        }
        return (null != myBuffer) ? myBuffer.toString() : null;
    }
}
