package csi.shared.core.visualization.timeline;

import java.io.Serializable;

import com.google.gwt.user.client.rpc.IsSerializable;

public interface Track<T extends Object> extends Serializable, IsSerializable, Comparable<T>{
    
    public String getName();
    public void setName(String name);
    public boolean isVisible();

}
