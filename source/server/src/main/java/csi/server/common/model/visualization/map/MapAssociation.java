package csi.server.common.model.visualization.map;

import javax.persistence.Column;
import javax.persistence.Entity;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Type;

import csi.server.common.model.ModelObject;
import csi.shared.core.color.ColorModel;
import csi.shared.core.color.ContinuousColorModel;

@SuppressWarnings("serial")
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class MapAssociation extends ModelObject {
	private String name;
	private String source;
	private String destination;
    private String styleTypeString;
    
    private boolean useDefaultWidthSetting = true;
    private Integer width = 1;

    private boolean useDefaultColorSetting = true;
    private String colorString = "333333";
    
    private Boolean showDirection = false;

	@Type(type = "csi.server.dao.jpa.xml.SerializedXMLType")
    @Column(columnDefinition = "TEXT")
    private ColorModel colorModel = new ContinuousColorModel();

    private int listPosition;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getDestination() {
		return destination;
	}

	public void setDestination(String destination) {
		this.destination = destination;
	}

	public String getColorString() {
		return colorString;
	}

	public void setColorString(String colorString) {
		this.colorString = colorString;
	}

	public String getStyleTypeString() {
		return styleTypeString;
	}

	public void setStyleTypeString(String styleTypeString) {
		this.styleTypeString = styleTypeString;
	}

	public ColorModel getColorModel() {
		return colorModel;
	}

	public void setColorModel(ColorModel colorModel) {
		this.colorModel = colorModel;
	}

    public Integer getWidth() {
		return width;
	}

	public void setWidth(Integer width) {
		this.width = width;
	}
	
	public Boolean isShowDirection() {
		return showDirection;
	}

	public void setShowDirection(Boolean showDirection) {
		this.showDirection = showDirection;
	}

	public MapAssociation clone() {
		MapAssociation clone = new MapAssociation();

        super.cloneComponents(clone);

        clone.setName(getName());
		clone.setSource(getSource());
		clone.setDestination(getDestination());
		clone.setStyleTypeString(getStyleTypeString());
		clone.setUseDefaultWidthSetting(isUseDefaultWidthSetting());
        clone.setWidth(getWidth());
        clone.setUseDefaultColorSetting(isUseDefaultColorSetting());
		clone.setColorString(getColorString());
        clone.setColorModel(getColorModel());
        clone.setShowDirection(isShowDirection());

		return clone;
    }

	public int getListPosition() {
		return listPosition;
	}

	public void setListPosition(int listPosition) {
		this.listPosition = listPosition;
	}

	public boolean isUseDefaultWidthSetting() {
		return useDefaultWidthSetting;
	}

	public void setUseDefaultWidthSetting(boolean useDefaultWidthSetting) {
		this.useDefaultWidthSetting = useDefaultWidthSetting;
	}

	public boolean isUseDefaultColorSetting() {
		return useDefaultColorSetting;
	}

	public void setUseDefaultColorSetting(boolean useDefaultColorSetting) {
		this.useDefaultColorSetting = useDefaultColorSetting;
	}

	public void voidFN(){
        //needed for grid
    }
}
