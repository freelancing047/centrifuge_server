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
package csi.client.gwt.dataview.resources;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.resources.client.ImageResource;

import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.server.common.enumerations.CsiDataType;
import csi.server.common.enumerations.JdbcDriverType;
import csi.server.common.model.ConnectionDef;
import csi.server.common.model.DataSourceDef;
import csi.server.common.model.SqlTableDef;
import csi.server.common.model.column.ColumnDef;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class DataSourceClientUtil {

    public static CentrifugeConstants i18n = CentrifugeConstantsLocator.get();
    private static EditorResource editorResource = GWT.create(EditorResource.class);

    public static ImageResource get(SqlTableDef tableIn, boolean disabled) {

        DataSourceDef mySource = (null != tableIn) ? tableIn.getSource() : null;
        ConnectionDef myConnection = (null != mySource) ? mySource.getConnection() : null;

        return (null != myConnection) ? get(myConnection, disabled) : editorResource.unknown();
    }

    public static ImageResource getAddIcon() {
        return editorResource.addIcon();
    }

    public static ImageResource getCustomQueryIcon() {
        return editorResource.customQueryIcon();
    }

    public static ImageResource getInstallerIcon() {
        return editorResource.installerIcon();
    }

    public static ImageResource getRemoveIcon() {
        return editorResource.removeIcon();
    }

    public static ImageResource getEditIcon() {
        return editorResource.editIcon();
    }


    public static ImageResource get(ConnectionDef connectionIn, boolean disabledIn) {

        return get(JdbcDriverType.extractValue(connectionIn.getType()), disabledIn);
    }

    public static ImageResource get(JdbcDriverType typeIn, boolean disabledIn) {

        switch (typeIn) {
            
            case ACCESS :
                
                return disabledIn ? editorResource.accessDisabled() : editorResource.access();
            
            case CUSTOM :
                
                return disabledIn ? editorResource.customDisabled() : editorResource.custom();
                
            case EXCEL :
                
                return disabledIn ? editorResource.excelDisabled() : editorResource.excel();
                
            case IMPALA :
                
                return disabledIn ? editorResource.impalaDisabled() : editorResource.impala();
                
            case LDAP :
                
                return disabledIn ? editorResource.icon_LDAPdisabled() : editorResource.icon_LDAP();
                
            case ORACLE :
                
                return disabledIn ? editorResource.oracleDisabled() : editorResource.oracle();
                
            case POSTGRESS :
                
                return disabledIn ? editorResource.postgresDisabled() : editorResource.postgres();
                
            case SQLSERVER :
                
                return disabledIn ? editorResource.sqlDisabled() : editorResource.sql();
                
            case TEXT :
                
                return disabledIn ? editorResource.textDisabled() : editorResource.text();
                
            case XML :
                
                return disabledIn ? editorResource.xmlDisabled() : editorResource.xml();
                
            case MYSQL :
                
                return disabledIn ? editorResource.mysqlDisabled() : editorResource.mysql();
                
            case WEB :
                
                return disabledIn ? editorResource.webServiceDisabled() : editorResource.webService();
                
            default :
                
                return disabledIn ? editorResource.genericDisabled() : editorResource.generic();
        }
    }

    public static ImageResource get(ColumnDef columnIn) {

        return get(columnIn.getCsiType());
    }

    public static ImageResource get(CsiDataType dataTypeIn) {

        switch (dataTypeIn) {
            
            case String :
                
                return editorResource.icon2string();
            
            case Boolean :
                
                return editorResource.icon2boolean();
                
            case Integer :
                
                return editorResource.icon2integer();
                
            case Number :
                
                return editorResource.icon2number();
                
            case DateTime :
                
                return editorResource.icon2date_time();
                
            case Date :
                
                return editorResource.icon2date();

            case Time :

                return editorResource.icon2time();

            case Unsupported :

                return editorResource.icon2unknown();

            default :

                return editorResource.icon2unknown();
        }
    }

    public static ImageResource getTableImageResource() {
        return editorResource.table();
    }

    public static ImageResource getFolderImageResource() {
        return editorResource.defaultFolder();
    }

    public static String getConnectionTypeName(String driverKey) {

        if ("access".equalsIgnoreCase(driverKey)) { //$NON-NLS-1$
            return i18n.dataSourceClientUtilAccessDriverName(); //$NON-NLS-1$
        } else if ("excel".equalsIgnoreCase(driverKey)) { //$NON-NLS-1$
            return i18n.dataSourceClientUtilExcelDriverName(); //$NON-NLS-1$
        } else if ("generic".equalsIgnoreCase(driverKey)) { //$NON-NLS-1$
            return i18n.dataSourceClientUtilGenericDriverName(); //$NON-NLS-1$
        } else if ("linked".equalsIgnoreCase(driverKey)) { //$NON-NLS-1$
            return i18n.dataSourceClientUtilLinkedDriverName(); //$NON-NLS-1$
        } else if ("mysql".equalsIgnoreCase(driverKey)) { //$NON-NLS-1$
            return i18n.dataSourceClientUtilMySqlDriverName(); //$NON-NLS-1$
        } else if ("oracle".equalsIgnoreCase(driverKey)) { //$NON-NLS-1$
            return i18n.dataSourceClientUtilOracleDriverName(); //$NON-NLS-1$
        } else if ("postgresql".equalsIgnoreCase(driverKey)) { //$NON-NLS-1$
            return i18n.dataSourceClientUtilPostgresSqlDriverName(); //$NON-NLS-1$
        } else if ("reserved".equalsIgnoreCase(driverKey)) { //$NON-NLS-1$
            return i18n.dataSourceClientUtilReservedDriverName(); //$NON-NLS-1$
        } else if ("sql".equalsIgnoreCase(driverKey)) { //$NON-NLS-1$
            return i18n.dataSourceClientUtilSqlDriverName(); //$NON-NLS-1$
        } else if ("table".equalsIgnoreCase(driverKey)) { //$NON-NLS-1$
            return i18n.dataSourceClientUtilTableDriverName(); //$NON-NLS-1$
        } else if ("text".equalsIgnoreCase(driverKey)) { //$NON-NLS-1$
            return i18n.dataSourceClientUtilTextDriverName(); //$NON-NLS-1$
        } else if ("view".equalsIgnoreCase(driverKey)) { //$NON-NLS-1$
            return i18n.dataSourceClientUtilViewDriverName(); //$NON-NLS-1$
        } else if ("webservice".equalsIgnoreCase(driverKey)) { //$NON-NLS-1$
            return i18n.dataSourceClientUtilWebServiceDriverName(); //$NON-NLS-1$
        } else if ("xml".equalsIgnoreCase(driverKey)) { //$NON-NLS-1$
            return i18n.dataSourceClientUtilXmlDriverName(); //$NON-NLS-1$
        } else if ("installedtabledriver".equalsIgnoreCase(driverKey)) {
            return i18n.dataSourceClientUtilInstalledTableDriverName();
        } else {
            return i18n.dataSourceClientUtilCustomDriverName(); //$NON-NLS-1$
        }
    }

    public static String getConnectionTypeName(ConnectionDef connection) {
        String driverKey = connection.getType();
        return getConnectionTypeName(driverKey);
    }

    public static String getConnectionTypeName(DataSourceDef dataSourceIn) {
        ConnectionDef myConnection = dataSourceIn.getConnection();
        return getConnectionTypeName(myConnection);
    }

    public static boolean isInstalledTable(DataSourceDef dataSourceIn) {

        ConnectionDef myConnection = (null != dataSourceIn) ? dataSourceIn.getConnection() : null;
        String myDriverKey = (null != myConnection) ? myConnection.getType() : null;
        return (null != myDriverKey) ? "installedtabledriver".equalsIgnoreCase(myDriverKey) : false;
    }
}
