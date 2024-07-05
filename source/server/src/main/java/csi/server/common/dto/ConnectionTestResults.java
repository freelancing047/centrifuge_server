package csi.server.common.dto;


import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;


public class ConnectionTestResults implements IsSerializable {

    public List<ConnectionTest> failedDrivers = new ArrayList<ConnectionTest>();
    public List<ConnectionTest> failedConnections = new ArrayList<ConnectionTest>();
    public List<ConnectionTest> authRequired = new ArrayList<ConnectionTest>();
}