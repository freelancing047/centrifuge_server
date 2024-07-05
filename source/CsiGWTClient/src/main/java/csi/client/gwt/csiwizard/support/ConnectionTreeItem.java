package csi.client.gwt.csiwizard.support;

import com.google.gwt.core.client.GWT;

import csi.client.gwt.dataview.resources.DataSourceClientUtil;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.model.ConnectionDef;
import csi.server.common.model.DataSourceDef;
import csi.server.common.model.ModelObject;
import csi.server.common.model.SqlTableDef;
import csi.server.common.model.UUID;
import csi.server.common.model.column.ColumnDef;


public class ConnectionTreeItem {
    
    public ConnectionItemType type;
    public ConnectionTreeItem parent;

    public String key = UUID.randomUUID();
    public boolean retrievingChildren = false;
    public boolean hasAllChildren = false;
    private Object _value = null;
    private boolean _isSpecial = false;

    private static final CentrifugeConstants i18n = CentrifugeConstantsLocator.get();

    public ConnectionTreeItem(ConnectionItemType typeIn) {
        
        
        type = typeIn;

        switch (type) {

            case DATA_SOURCE_DEF:
            case CATALOG:
            case SCHEMA:
            case TABLE_TYPE:
            case TABLE:

                hasAllChildren = false;
                break;

            default:

                hasAllChildren = true;
                break;
        }
    }

    public ConnectionTreeItem(ModelObject objectIn, ConnectionItemType typeIn) throws CentrifugeException {

        this(typeIn);

        switch (type) {
            case DATA_SOURCE_DEF:
                _value = (objectIn instanceof DataSourceDef) ? objectIn : null;
                break;

            case CONNECTION_DEF:
                _value = (objectIn instanceof ConnectionDef) ? objectIn : null;
                break;

            case TABLE:
            // A special type of table def
            case INSTALLER:
            case CUSTOM_QUERY:
                _value = (objectIn instanceof SqlTableDef) ? objectIn : null;
                break;

            case COLUMN:
                _value = (objectIn instanceof ColumnDef) ? objectIn : null;
                break;

            default:
                
                _value = null;
                break;
         }
        
        if (null == _value) {
            
            throw new CentrifugeException(i18n.connectionTreeItemTypeException(typeIn.toString())); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    public ConnectionTreeItem(String nameIn, ConnectionItemType typeIn) throws CentrifugeException {

        this(typeIn);
        
        switch (type) {

            case ROOT:
            case CONNECTION_TYPE:
            case CATALOG:
            case SCHEMA:
            case TABLE_TYPE:
            case OTHER:
                _value = nameIn;
                break;

            default:
                
                throw new CentrifugeException(i18n.connectionTreeItemNameException() + typeIn.toString() + "."); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    public ConnectionTreeItem getParent() {

        return parent;
    }

    public void setIsSpecial(boolean isSpecialIn) {

        _isSpecial = isSpecialIn;
    }

    public boolean getIsSpecial() {

        return _isSpecial;
    }

    public boolean isSpecial() {

        return _isSpecial;
    }

    public ConnectionTreeItem insertParent(ConnectionTreeItem parentIn) {
        
        parent = parentIn;
        
        return this;
    }

    public String getCatalog() {

        String myResult = null;

        if (ConnectionItemType.CATALOG.equals(type)) {

            myResult = getName();

        } else if (null != parent) {

            myResult = parent.getCatalog();
        }

        return myResult;
    }

    public String getSchema() {

        String myResult = null;

        if (ConnectionItemType.SCHEMA.equals(type)) {

            myResult = getName();

        } else if (null != parent) {

            myResult = parent.getSchema();
        }

        return myResult;
    }

    public String getTableType() {

        String myResult = null;

        if (ConnectionItemType.TABLE_TYPE.equals(type)) {

            myResult = getName();

        } else if (null != parent) {

            myResult = parent.getTableType();
        }

        return myResult;
    }

    public DataSourceDef getDataSourceDef() {
        
        DataSourceDef myResult = null;
        
        if ((ConnectionItemType.DATA_SOURCE_DEF == type)
                && (_value instanceof DataSourceDef)) {
            
            myResult = (DataSourceDef)_value;

        } else if (null != parent) {

            myResult = parent.getDataSourceDef();
        }
        return myResult;
    }
    
    public ConnectionDef getConnectionDef() {
        
        ConnectionDef myResult = null;
        
        if ((ConnectionItemType.CONNECTION_DEF == type)
                && (_value instanceof ConnectionDef)) {
            
            myResult = (ConnectionDef)_value;
            
        } else if ((ConnectionItemType.DATA_SOURCE_DEF == type)
                && (_value instanceof DataSourceDef)) {
            
            myResult = ((DataSourceDef)_value).getConnection();

        } else if (null != parent) {

            myResult = parent.getConnectionDef();
        }
        return myResult;
    }
    
    public SqlTableDef getSqlTableDef() {
        
        SqlTableDef myResult = null;
        
        if (((ConnectionItemType.CUSTOM_QUERY == type)
                || (ConnectionItemType.INSTALLER == type)
                || (ConnectionItemType.TABLE == type))
                && (_value instanceof SqlTableDef)) {
            
            myResult = (SqlTableDef)_value;
        }
        return myResult;
    }

    public ColumnDef getColumnDef() {
        
        ColumnDef myResult = null;
        
        if ((ConnectionItemType.COLUMN == type)
                && (_value instanceof ColumnDef)) {
            
            myResult = (ColumnDef)_value;
        }
        return myResult;
    }

    /**
     * returns true if the ConnectionTreeItem is of a type that can have children, regardless of weather it actually has children.
     */
    public boolean canHaveChildren() {
        switch (type) {
            case ROOT:
            case CONNECTION_TYPE:
            case DATA_SOURCE_DEF:
            case CONNECTION_DEF:
            case CATALOG:
            case SCHEMA:
            case TABLE_TYPE:
            case TABLE:
                return true;

            case COLUMN:
            case CUSTOM_QUERY:
            case INSTALLER:
            default:
                return false;
        }
    }

    public String getValue() {
        
        return getName();
    }

    public String getName() {
        
        String myName = null;
        
        switch (type) {

            case ROOT:
            case CONNECTION_TYPE:
            case CATALOG:
            case SCHEMA:
            case TABLE_TYPE:
            case OTHER:
                
                myName = (String)_value;
                break;

            case TABLE:
            case INSTALLER:

                myName = getTableName(_value);
                break;

            case CUSTOM_QUERY:
                
                myName = getQueryName(_value);
                break;
            
            case COLUMN:
                
                myName = getColumnName(_value);
                break;
            
            case DATA_SOURCE_DEF:
                
                myName = getDataSourceName(_value);
                break;

            case CONNECTION_DEF:
                
                myName = getConnectionName(_value);
                break;
                
            default:
                GWT.log(i18n.connectionTreeItemBadConnectionLog()); //$NON-NLS-1$
        }
        return myName;
    }

    @Override
    public String toString() {

        return (null != type) ? getName() + " (" + type.name() +")" : getName();
    }

    private String getDataSourceName(Object objectIn) {
        
        DataSourceDef mySource = (DataSourceDef)objectIn;
        String myName = mySource.getName();
        
        return (null != myName) ? myName : getConnectionName(mySource.getConnection());
    }
    
    private String getConnectionName(Object objectIn) {
        
        String myName = (objectIn instanceof ConnectionDef) ? ((ConnectionDef)objectIn).getName() : null;
        if ((null == myName) && (objectIn instanceof ConnectionDef)) {
            myName = DataSourceClientUtil.getConnectionTypeName((ConnectionDef)objectIn);
        }
        
        return (null != myName) ? myName : ((ConnectionDef)objectIn).getUuid();
    }
    
    private String getQueryName(Object objectIn) {
        
        String myName = (objectIn instanceof SqlTableDef) ? ((SqlTableDef)objectIn).getQueryName() : null;
        
        return (null != myName) ? myName : getTableName(objectIn);
    }
    
    private String getColumnName(Object objectIn) {
        
        String myName = (objectIn instanceof ColumnDef) ? ((ColumnDef)objectIn).getColumnName() : null;
        
        return myName;
    }
    
    private String getTableName(Object objectIn) {
        
        String myName = (objectIn instanceof SqlTableDef) ? ((SqlTableDef)objectIn).getDisplayName() : null;
        
        return myName;
    }
}
