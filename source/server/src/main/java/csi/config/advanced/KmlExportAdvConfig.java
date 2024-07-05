package csi.config.advanced;

import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

import csi.shared.gwt.dataview.export.kml.mapping.KmlIcon;

/**
 * Created by Patrick on 10/24/2014.
 */
public class KmlExportAdvConfig implements IsSerializable{
    private List<KmlIcon> availableIcons;

    public void setAvailableIcons(List<KmlIcon> availableIcons) {
        this.availableIcons = availableIcons;
    }

    public List<KmlIcon> getAvailableIcons() {
        return availableIcons;
    }
}
