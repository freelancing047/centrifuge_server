package csi.shared.gwt.dataview.export.kml.mapping;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Created by Patrick on 10/24/2014.
 */
public class KmlIcon implements IsSerializable {
    private String name;
    private String URL;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getURL() {
        return URL;
    }

    public void setURL(String URL) {
        this.URL = URL;
    }
}
