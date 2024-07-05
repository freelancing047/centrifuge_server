package csi.server.common.model.visualization.map;

import java.io.Serializable;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import csi.server.common.model.ModelObject;
import csi.server.common.model.visualization.VisualizationDef;
import csi.server.common.model.visualization.VisualizationType;
import csi.server.common.model.visualization.selection.AbstractMapSelection;
import csi.server.common.model.visualization.selection.DetailMapSelection;
import csi.server.common.model.visualization.selection.Selection;

@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class MapViewDef extends VisualizationDef implements Serializable {
    @Transient
    private AbstractMapSelection selection;

    @OneToOne(cascade = CascadeType.ALL)
    private MapSettings mapSettings;

    public MapViewDef() {
        super();
        setType(VisualizationType.GEOSPATIAL_V2);
        selection = new DetailMapSelection(getUuid());
    }

    @Override
    public Selection getSelection() {
        return selection;
    }

    @Override
    public <T extends ModelObject, S extends ModelObject> VisualizationDef clone(Map<String, T> fieldMapIn, Map<String, S> filterMapIn) {
        MapViewDef myClone = new MapViewDef();

        super.cloneComponents(myClone, fieldMapIn, filterMapIn);

        if (null != getMapSettings()) {
            myClone.setMapSettings(getMapSettings().clone(fieldMapIn));
        }

        return myClone;
    }

    @Override
    public <T extends ModelObject, S extends ModelObject> VisualizationDef copy(Map<String, T> fieldMapIn,
                                                                                Map<String, S> filterMapIn) {
        MapViewDef myCopy = new MapViewDef();

        super.copyComponents(myCopy, fieldMapIn, filterMapIn);

        if (null != getMapSettings()) {
            myCopy.setMapSettings(getMapSettings().copy(fieldMapIn));
        }

        return myCopy;
    }

    public MapSettings getMapSettings() {
        return mapSettings;
    }

    public void setMapSettings(MapSettings mapSettings) {
        this.mapSettings = mapSettings;
    }
}
