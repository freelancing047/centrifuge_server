package csi.shared.gwt.viz.viewer.LensImage;

import com.google.gwt.user.client.rpc.IsSerializable;

public interface LensImage extends IsSerializable{
    String getLensDef();

    void setLensDef(String id);

    String getLabel();
    void setLabel(String s);
}
