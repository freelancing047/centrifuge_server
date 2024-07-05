package csi.server.common.dto.resource;


import com.google.gwt.user.client.rpc.IsSerializable;


public enum ImportStatusType implements IsSerializable {
    OK, DUPLICATE, V1_XML, FAILURE
};
