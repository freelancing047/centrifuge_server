package csi.server.common.dto.graph.gwt;

import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

import csi.server.common.exception.CentrifugeException;

public abstract class AbstractVisualItemTypeBase implements IsSerializable {
   public abstract String getString() throws CentrifugeException;

   public abstract List<String> getArray() throws CentrifugeException;
}
