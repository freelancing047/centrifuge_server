package csi.server.common.model.visualization.map;

import csi.server.common.model.ModelObject;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.Entity;
import java.util.Map;

@SuppressWarnings("serial")
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class MapTileLayer extends ModelObject {
    private String layerId;
    private boolean visible;
    private int opacity;
    private int listPosition;

    public MapTileLayer() {
        super();
    }

    public String getLayerId() {
        return layerId;
    }

    public void setLayerId(String layerId) {
        this.layerId = layerId;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public int getOpacity() {
        return opacity;
    }

    public void setOpacity(int opacity) {
        this.opacity = opacity;
    }

    public int getListPosition() {
        return listPosition;
    }

    public void setListPosition(int listPosition) {
        this.listPosition = listPosition;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (visible ? 1231 : 1237);
        result = prime * result + ((layerId == null) ? 0 : layerId.hashCode());
        result = prime * result + listPosition;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        MapTileLayer other = (MapTileLayer) obj;
        if (visible != other.visible) {
            return false;
        }
        if (layerId == null) {
            if (other.layerId != null) {
                return false;
            }
        } else if (!layerId.equals(other.layerId)) {
            return false;
        }
        return listPosition == other.listPosition;
    }

    public <T extends ModelObject> MapTileLayer copy(Map<String, T> fieldMapIn) {
        if (fieldMapIn.containsKey(this.getUuid())) {
            return (MapTileLayer) fieldMapIn.get(this.getUuid());
        }

        MapTileLayer myCopy = new MapTileLayer();
        copyComponents(myCopy);
        myCopy.setListPosition(listPosition);
        myCopy.setLayerId(layerId);
        myCopy.setVisible(visible);
        myCopy.setOpacity(opacity);
        fieldMapIn.put(this.getUuid(), (T) myCopy);
        return myCopy;
    }
}
