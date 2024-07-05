package csi.server.common.dto.graph;


import com.google.gwt.user.client.rpc.IsSerializable;


public class GraphStateFlags implements IsSerializable {

    public boolean hasHiddenItems = false;
    public boolean hasVisibleItems = false;
    public boolean hasHiddenItemsInSelection = false;
    public boolean hasBundles = false;
}
