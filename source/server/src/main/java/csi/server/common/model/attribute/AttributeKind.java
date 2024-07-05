package csi.server.common.model.attribute;



import com.google.gwt.user.client.rpc.IsSerializable;


public enum AttributeKind implements IsSerializable {
    STATIC, NORMAL, REFERENCE, COMPUTED, LAZY, DIRECTED
}
