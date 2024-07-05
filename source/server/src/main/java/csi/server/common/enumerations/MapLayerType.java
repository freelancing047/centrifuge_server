package csi.server.common.enumerations;

import java.io.Serializable;

public enum MapLayerType implements Serializable {
	ARCGIS_TILED("ArcGISTiled"),
	WMS("WMS"),
    OPENSTREETMAP("OpenStreetMap");
    
    private String _label;

    public String getLabel() {

        return _label;
    }

    private MapLayerType(String labelIn) {
        _label = labelIn;
    }
}
