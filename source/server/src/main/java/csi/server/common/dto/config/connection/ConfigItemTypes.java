package csi.server.common.dto.config.connection;


import com.google.gwt.user.client.rpc.IsSerializable;


public enum ConfigItemTypes implements IsSerializable {
    STRING, PASSWORD, FILE, SELECTLIST, BOOLEAN, CSIQUERYCOMMANDS, CSIPARMSTABLE, CSINAMESPACETABLE, CSIXMLCOLUMNSTABLE, CSILEGACYCONNECTIONSTRING
};