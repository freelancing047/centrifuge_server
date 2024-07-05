package csi.server.common.model.attribute;

import com.google.gwt.user.client.rpc.IsSerializable;

public enum AttributeAggregateType implements IsSerializable {
    COUNT, COUNT_DISTINCT, MIN, MAX, SUM, AVG, ABS_AVG, ABS_SUM, NORMALIZE
}
