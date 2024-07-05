package csi.server.common.dto.SelectionListData;

import csi.server.common.dto.config.connection.DriverConfigInfo;
import csi.server.common.enumerations.JdbcDriverRestrictions;


public class DriverBasics extends SelectorBasics {

    DriverConfigInfo _driverInfo;
    private boolean _inPlace = false;
    private boolean _singleTable = false;
    private boolean _simpleLoader = false;
    private boolean _blockCustomQueries = false;
    private boolean _hiddenFlag = false;
    private int _authorizationFlags = 0;

    public DriverBasics() {

        super();
    }

    public DriverBasics(String keyIn, String nameIn, String remarksIn, DriverConfigInfo driverInfoIn,
                        boolean inPlaceIn, boolean singleTableIn, boolean simpleLoaderIn,
                        boolean blockCustomQueriesIn, boolean hiddenFlagIn, int authorizationFlagsIn) {

        super(keyIn, nameIn, remarksIn);

        _driverInfo = driverInfoIn;
        _inPlace = inPlaceIn;
        _singleTable = singleTableIn;
        _simpleLoader = simpleLoaderIn;
        _authorizationFlags = authorizationFlagsIn;
        _blockCustomQueries = blockCustomQueriesIn;
        _hiddenFlag = hiddenFlagIn;
    }

    public DriverConfigInfo getDriverInfo() {

        return _driverInfo;
    }

    public void setDriverInfo(DriverConfigInfo driverInfoIn) {

        _driverInfo = driverInfoIn;
    }

    public boolean isInPlace() {

        return _inPlace;
    }

    public void setInPlace(boolean inPlaceIn) {

        _inPlace = inPlaceIn;
    }

    public boolean isSingleTable() {

        return _singleTable;
    }

    public void setSingleTable(boolean singleTableIn) {

        _singleTable = singleTableIn;
    }

    public boolean isSimpleLoader() {

        return _simpleLoader;
    }

    public void setSimpleLoader(boolean simpleLoaderIn) {

        _simpleLoader = simpleLoaderIn;
    }

    public boolean getBlockCustomQueries() {

        return _blockCustomQueries;
    }

    public void setBlockCustomQueries(boolean blockCustomQueriesIn) {

        _blockCustomQueries = blockCustomQueriesIn;
    }

    public boolean getHiddenFlag() {

        return _hiddenFlag;
    }

    public void setHiddenFlag(boolean hiddenFlagIn) {

        _hiddenFlag = hiddenFlagIn;
    }

    public boolean canExecuteQuery() {

        return !_blockCustomQueries;
    }

    public int getAuthorizationFlags() {

        return _authorizationFlags;
    }

    public void setAuthorizationFlags(int authorizationFlagsIn) {

        _authorizationFlags = authorizationFlagsIn;
    }

    public boolean canUseDriver() {

        return (((1 << JdbcDriverRestrictions.DRIVER_ACCESS.ordinal()) & _authorizationFlags) != 0);
    }

    public boolean canExport() {

        return canEditDataSource() && canEditConnection();
    }

    public boolean canEditDataSource() {

        return canUseDriver() && (((1 << JdbcDriverRestrictions.SOURCE_EDIT.ordinal()) & _authorizationFlags) != 0);
    }

    public boolean canEditConnection() {

        return canUseDriver() && (((1 << JdbcDriverRestrictions.CONNECTION_EDIT.ordinal()) & _authorizationFlags) != 0);
    }

    public boolean canEditQuery() {

        return canEditDataSource() && (((1 << JdbcDriverRestrictions.QUERY_EDIT.ordinal()) & _authorizationFlags) != 0);
    }

    public boolean canPreviewData() {

        return (((1 << JdbcDriverRestrictions.DATA_PREVIEWING.ordinal()) & _authorizationFlags) != 0);
    }
}
