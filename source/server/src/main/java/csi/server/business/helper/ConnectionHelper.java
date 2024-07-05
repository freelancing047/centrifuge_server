package csi.server.business.helper;

import java.util.ArrayList;
import java.util.List;

import csi.config.Configuration;
import csi.security.CsiSecurityManager;
import csi.server.common.dto.SelectionListData.DriverBasics;
import csi.server.common.enumerations.JdbcDriverParameterKey;
import csi.server.common.enumerations.JdbcDriverRestrictions;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.util.ConnectorSupport;
import csi.server.common.util.StringUtil;
import csi.server.connector.ConnectionFactory;
import csi.server.connector.ConnectionFactoryManager;
import csi.server.connector.config.JdbcDriver;
import csi.server.task.api.TaskHelper;

/**
 * Created by centrifuge on 8/22/2019.
 */
public class ConnectionHelper {

    public static List<DriverBasics> listConnectionDescriptors() {
        String myUserName = CsiSecurityManager.getUserName();
        List<DriverBasics> myDrivers = new ArrayList<DriverBasics>();

        for (JdbcDriver myDriver : Configuration.getInstance().getDbConfig().getDrivers().getDrivers()) {
            String myRestrictionString = (null != myDriver.getHiddenProperties())
                    ? myDriver.getHiddenProperties().getProperty("accessRestriction") : null;
            String[] myAccessRestrictions = (null != myRestrictionString)
                    ? myRestrictionString.split(",") : null;

            if ((null == myAccessRestrictions) || CsiSecurityManager.hasAllRoles(myAccessRestrictions)) {

                // **** Cancellation check point ****
                TaskHelper.checkForCancel();

                try {

                    String myDriverKey = myDriver.getKey();
                    ConnectionFactory myFactory = ConnectionFactoryManager.getInstance().getFactoryForType(myDriverKey);

                    if (null != myFactory) {

                        boolean mySingleTableFlag = "true".equalsIgnoreCase(myFactory.getDefaultProperties().getProperty(JdbcDriverParameterKey.DISTINCT_SOURCES.getLabel(), "false"));
                        boolean mySimpleLoaderFlag = myFactory.isSimpleLoader();
                        boolean myInPlaceFlag = myFactory.isInPlace();
                        boolean myBlockCustomQueries = myFactory.getBlockCustomQueries();
                        boolean myHasHiddenFlag = myFactory.getHasHiddenFlag();
                        String myName = myDriver.getName();
                        String[] myNameList = StringUtil.split(myName);

                        if (CsiSecurityManager.canCreateConnectionType(myDriverKey, null)) {

                            for (int i = 0; myNameList.length > i; i++) {

                                myDrivers.add(new DriverBasics(myDriverKey, myNameList[i], myDriver.getRemarks(),
                                        myDriver.getUiConnectionConfig(), myInPlaceFlag, mySingleTableFlag,
                                        mySimpleLoaderFlag, myBlockCustomQueries, myHasHiddenFlag,
                                        getDriverRestrictionFlags(myDriver)));
                            }
                        }
                    }

                } catch (CentrifugeException e) {
                    // Silently fail so we don't fill up logs when implementing security on connection factories.
                }
            }
        }
        ConnectorSupport.addUser(myUserName, myDrivers);

        return myDrivers;
    }

    private static int getDriverRestrictionFlags(JdbcDriver driverIn) {

        int myFlags = 0;

        if ((null == driverIn.getDriverAccessRole())
                || CsiSecurityManager.hasRole(driverIn.getDriverAccessRole())) {

            myFlags |= (1 << JdbcDriverRestrictions.DRIVER_ACCESS.ordinal());
        }
        if ((null == driverIn.getSourceEditRole())
                || CsiSecurityManager.hasRole(driverIn.getSourceEditRole())) {

            myFlags |= (1 << JdbcDriverRestrictions.SOURCE_EDIT.ordinal());
        }
        if ((null == driverIn.getConnectionEditRole())
                || CsiSecurityManager.hasRole(driverIn.getConnectionEditRole())) {

            myFlags |= (1 << JdbcDriverRestrictions.CONNECTION_EDIT.ordinal());
        }
        if ((null == driverIn.getQueryEditRole())
                || CsiSecurityManager.hasRole(driverIn.getQueryEditRole())) {

            myFlags |= (1 << JdbcDriverRestrictions.QUERY_EDIT.ordinal());
        }
        if ((null == driverIn.getDataViewingRole())
                || CsiSecurityManager.hasRole(driverIn.getDataViewingRole())) {

            myFlags |= (1 << JdbcDriverRestrictions.DATA_PREVIEWING.ordinal());
        }
        return myFlags;
    }
}
