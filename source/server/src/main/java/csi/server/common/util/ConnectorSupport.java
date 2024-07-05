package csi.server.common.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import csi.server.common.dto.SelectionListData.DriverBasics;
import csi.server.common.interfaces.DataContainer;
import csi.server.common.interfaces.DataDefinition;
import csi.server.common.model.ConnectionDef;
import csi.server.common.model.DataSourceDef;
import csi.server.common.model.SqlTableDef;
import csi.server.common.model.dataview.DataViewDef;

/**
 * Created by centrifuge on 6/4/2015.
 */
public class ConnectorSupport {

    private static boolean _notFoundAuthorizationResponse = false;
    private static boolean _notFoundRestrictionResponse = true;

    private static ConnectorSupport _instance = null;
    private static Map<String, ConnectorSupport> _userMap = null;

    private Map<String, DriverBasics> _driverMap = new HashMap<String, DriverBasics>();
    private List<DriverBasics> _driverList = new ArrayList<DriverBasics>();

    public static void setInstance(List<DriverBasics> listIn) {

        _instance = new ConnectorSupport(listIn);
    }

    public static ConnectorSupport getInstance() {

        return  _instance;
    }

    public static void addUser(String userNameIn, List<DriverBasics> listIn) {

        if (null != userNameIn) {

            getUserMap().put(userNameIn.toLowerCase(), new ConnectorSupport(listIn));
        }
    }

    public static ConnectorSupport getUser(String userNameIn) {

        return (null != userNameIn) ? getUserMap().get(userNameIn.toLowerCase()) : null;
    }

    private static Map<String, ConnectorSupport> getUserMap() {

        if (null == _userMap) {

            _userMap = new TreeMap<String, ConnectorSupport>();
        }
        return _userMap;
    }

    public ConnectorSupport(List<DriverBasics> listIn) {

        setDriverList(listIn);
    }

    private void setDriverList(List<DriverBasics> listIn) {

        _driverMap = new HashMap<String, DriverBasics>();

        if (null != listIn) {

            _driverList = listIn;

            for (DriverBasics myDriver : listIn) {

                _driverMap.put(myDriver.getKey(), myDriver);
            }

        } else {

            _driverList = new ArrayList<DriverBasics>();
        }
    }

    public List<DriverBasics> getDriverList(boolean restrictedIn) {

        if (restrictedIn) {

            List<DriverBasics> myNewList = new ArrayList<DriverBasics>();

            for (DriverBasics myDriver : _driverList) {

                if (canEditDataSource(myDriver.getKey())) {

                    myNewList.add(myDriver);
                }
            }
            return myNewList;

        } else {

            return _driverList;
        }
    }

    public boolean hasHiddenParameters(DataSourceDef dataSourceIn) {

        ConnectionDef myConnection = (null != dataSourceIn) ? dataSourceIn.getConnection() : null;
        String myKey = (null != myConnection) ? myConnection.getType() : null;
        DriverBasics myDriver = (null != myKey) ? _driverMap.get(myKey) : null;

        return (null != myDriver) ? myDriver.getHiddenFlag() : _notFoundRestrictionResponse;
    }

    public boolean hasHiddenParameters(String keyIn) {

        DriverBasics myDriver = (null != keyIn) ? _driverMap.get(keyIn) : null;

        return (null != myDriver) ? myDriver.getHiddenFlag() : _notFoundRestrictionResponse;
    }

    public boolean canUseDriver(String keyIn) {

        DriverBasics myDriver = (null != keyIn) ? _driverMap.get(keyIn) : null;

        return (null != myDriver) ? myDriver.canUseDriver() : _notFoundAuthorizationResponse;
    }

    public boolean canExport(String keyIn) {

        DriverBasics myDriver = (null != keyIn) ? _driverMap.get(keyIn) : null;

        return (null != myDriver) ? myDriver.canExport() : _notFoundAuthorizationResponse;
    }

    public boolean canEditDataSource(String keyIn) {

        DriverBasics myDriver = (null != keyIn) ? _driverMap.get(keyIn) : null;

        return (null != myDriver) ? myDriver.canEditDataSource() : _notFoundAuthorizationResponse;
    }

    public boolean canEditConnection(String keyIn) {

        DriverBasics myDriver = (null != keyIn) ? _driverMap.get(keyIn) : null;

        return (null != myDriver) ? myDriver.canEditConnection() : _notFoundAuthorizationResponse;
    }

    public boolean canEditQuery(String keyIn) {

        DriverBasics myDriver = (null != keyIn) ? _driverMap.get(keyIn) : null;

        return (null != myDriver) ? myDriver.canEditQuery() : _notFoundAuthorizationResponse;
    }

    public boolean canExecuteQuery(String keyIn) {

        DriverBasics myDriver = (null != keyIn) ? _driverMap.get(keyIn) : null;

        return (null != myDriver) ? myDriver.canExecuteQuery() : _notFoundAuthorizationResponse;
    }

    public boolean canPreviewData(String keyIn) {

        DriverBasics myDriver = (null != keyIn) ? _driverMap.get(keyIn) : null;

        return (null != myDriver) ? myDriver.canPreviewData() : _notFoundAuthorizationResponse;
    }

   public boolean isRestricted(String keyIn) {
      return hasHiddenParameters(keyIn) || !canUseDriver(keyIn) || !canExport(keyIn) || !canEditDataSource(keyIn) ||
             !canEditConnection(keyIn) || !canEditQuery(keyIn) || !canExecuteQuery(keyIn);
   }

    public boolean isRestricted(DataSourceDef dataSourceIn) {

        if (null != dataSourceIn) {

            ConnectionDef myConnection = dataSourceIn.getConnection();
            String myKey = (null != myConnection) ? myConnection.getType() : null;

            return isRestricted(myKey);
        }
        return _notFoundRestrictionResponse;
    }

   public boolean isRestricted(List<DataSourceDef> sourceListIn) {
      boolean restricted = false;

      if (sourceListIn != null) {
         for (DataSourceDef myDataSource : sourceListIn) {
            if (isRestricted(myDataSource)) {
               restricted = true;
               break;
            }
         }
      }
      return restricted;
   }

   public boolean isRestricted(DataContainer containerIn) {
      return (containerIn != null) && isRestricted(containerIn.getDataSources());
   }

   public boolean isRestricted(DataDefinition definitionIn) {
      return (definitionIn != null) && isRestricted(definitionIn.getDataSources());
   }

    public boolean canEnterSourceEditor(DataViewDef metaDataIn) {

        Boolean myOkFlag = false;

        if (null != metaDataIn) {

            List<DataSourceDef> mySourceList = metaDataIn.getDataSources();

            myOkFlag = canEnterSourceEditor(mySourceList);
        }
        return myOkFlag;
    }

    public boolean canEnterSourceEditor(List<DataSourceDef> sourceListIn) {

        Boolean myOkFlag = true;

        if (null != sourceListIn) {

            for (DataSourceDef mySource : sourceListIn) {

                if (!canEditDataSource(mySource)) {

                    myOkFlag = false;
                    break;
                }
            }
        }
        return myOkFlag;
    }

    public boolean canExport(DataViewDef metaDataIn) {

        Boolean myOkFlag = false;

        if (null != metaDataIn) {

            List<DataSourceDef> mySourceList = metaDataIn.getDataSources();

            myOkFlag = canExport(mySourceList);
        }
        return myOkFlag;
    }

    public boolean canExport(List<DataSourceDef> sourceListIn) {

        Boolean myOkFlag = true;

        if (null != sourceListIn) {

            for (DataSourceDef mySource : sourceListIn) {

                if (!canExport(mySource)) {

                    myOkFlag = false;
                    break;
                }
            }
        }
        return myOkFlag;
    }

    public boolean canUseDriver(DataSourceDef dataSourceIn) {

        Boolean myOkFlag = false;

        if (null != dataSourceIn) {

            ConnectionDef myConnection = dataSourceIn.getConnection();

            if (null != myConnection) {

                String myKey = myConnection.getType();

                if (canUseDriver(myKey)) {

                    myOkFlag = true;
                }
            }
        }
        return myOkFlag;
    }

    public boolean canEditDataSource(DataSourceDef dataSourceIn) {

        Boolean myOkFlag = false;

        if (null != dataSourceIn) {

            ConnectionDef myConnection = dataSourceIn.getConnection();

            if (null != myConnection) {

                String myKey = myConnection.getType();

                if (canEditDataSource(myKey)) {

                    myOkFlag = true;
                }
            }
        }
        return myOkFlag;
    }

    public boolean canExport(DataSourceDef dataSourceIn) {

        Boolean myOkFlag = false;

        if (null != dataSourceIn) {

            ConnectionDef myConnection = dataSourceIn.getConnection();

            if (null != myConnection) {

                String myKey = myConnection.getType();

                if (canExport(myKey)) {

                    myOkFlag = true;
                }
            }
        }
        return myOkFlag;
    }

    public boolean canEditDataSource(DataSourceDef dataSourceIn, SqlTableDef tableIn) {

        Boolean myOkFlag = false;

        if (null != dataSourceIn) {

            ConnectionDef myConnection = dataSourceIn.getConnection();

            if (null != myConnection) {

                String myKey = myConnection.getType();

                if (canEditDataSource(myKey) && ((!tableIn.getIsCustom()) || canEditQuery(myKey))) {

                    myOkFlag = true;
                }
            }
        }
        return myOkFlag;
    }

    public boolean canEditConnection(DataSourceDef dataSourceIn) {

        boolean myOkFlag = false;

        if (null != dataSourceIn) {

            ConnectionDef myConnection = dataSourceIn.getConnection();

            if (null != myConnection) {

                String myKey = myConnection.getType();

                if (canEditConnection(myKey)) {

                    myOkFlag = true;
                }
            }
        }

        return myOkFlag;
    }

    public boolean canEditQuery(DataSourceDef dataSourceIn) {

        boolean myOkFlag = false;

        if (null != dataSourceIn) {

            ConnectionDef myConnection = dataSourceIn.getConnection();

            if (null != myConnection) {

                String myKey = myConnection.getType();

                if (canEditQuery(myKey)) {

                    myOkFlag = true;
                }
            }
        }

        return myOkFlag;
    }

    public boolean canExecuteQuery(DataSourceDef dataSourceIn) {

        boolean myOkFlag = false;

        if (null != dataSourceIn) {

            ConnectionDef myConnection = dataSourceIn.getConnection();

            if (null != myConnection) {

                String myKey = myConnection.getType();

                if (canExecuteQuery(myKey)) {

                    myOkFlag = true;
                }
            }
        }

        return myOkFlag;
    }

    public String formatTwoDigits(int valueIn) {

        StringBuilder myBuffer = new StringBuilder();

        myBuffer.append(valueIn / 10);
        myBuffer.append(valueIn % 10);

        return myBuffer.toString();
    }

    public String formatThreeDigits(int valueIn) {

        StringBuilder myBuffer = new StringBuilder();

        myBuffer.append(valueIn / 100);
        myBuffer.append((valueIn % 100) / 10);
        myBuffer.append(valueIn % 10);

        return myBuffer.toString();
    }
}
