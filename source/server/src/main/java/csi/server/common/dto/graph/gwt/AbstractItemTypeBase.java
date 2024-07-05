package csi.server.common.dto.graph.gwt;

import com.google.gwt.user.client.rpc.IsSerializable;

public abstract class AbstractItemTypeBase implements IsSerializable {
   public abstract String getAsString();

   public abstract boolean isDate();

   public abstract boolean isDouble();

   public abstract boolean isInteger();

   public abstract boolean isString();

   public abstract boolean isLong();

   public abstract boolean isBoolean();

}
