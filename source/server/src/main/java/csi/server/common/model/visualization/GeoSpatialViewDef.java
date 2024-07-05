package csi.server.common.model.visualization;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import csi.server.common.model.FieldDef;
import csi.server.common.model.GeoAddressFieldDef;
import csi.server.common.model.GeoDetailsFieldDef;
import csi.server.common.model.GeoNamesFieldDef;
import csi.server.common.model.ModelObject;
import csi.server.common.model.visualization.selection.NullSelection;
import csi.server.common.model.visualization.selection.Selection;

@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Deprecated
public class GeoSpatialViewDef extends VisualizationDef {

    protected String geoIqUserid;
    
    protected String geoIqPasswd;
    
    protected String geoIqHostname;
    
    protected int geoIqPort = 80;
    
    protected String geoDsName;
    
	protected String iconUrl;
	
	protected boolean inDecimalNotation = true;
	
	protected boolean inDegreeNotation = false;
	
	protected boolean latlonLocSelected = false;
	
	protected boolean addressLocSelected = true;
	
	protected boolean sendToGeoIQ = false;
	
	protected boolean sendToGoogleEarth = true;
	
	protected boolean autoGenOnBroadcast = false;
	
    @OneToOne(cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH })
    protected FieldDef timeField;
    
    @OneToMany(cascade = CascadeType.ALL)
	@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
    protected List<GeoAddressFieldDef> addresses;
     
    @OneToOne(cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH })
    protected FieldDef latitude;
    
    @OneToOne(cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH })
    protected FieldDef longitude;
    
    @OneToMany(cascade = CascadeType.ALL)
	@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
    protected List<GeoNamesFieldDef> geoNames;
    
    @OneToMany(cascade = CascadeType.ALL)
	@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
    protected List<GeoDetailsFieldDef> details;
     
    public GeoSpatialViewDef() {
    	super();
    }

    public GeoSpatialViewDef(String name) {
        super(name);
    }

	public FieldDef getTimeField() {
		return timeField;
	}

	public void setTimeField(FieldDef timeField) {
		this.timeField = timeField;
	}

	public String getGeoDsName() {
		return geoDsName;
	}

	public void setGeoDsName(String geoDsName) {
		this.geoDsName = geoDsName;
	}

	public String getIconUrl() {
		return iconUrl;
	}

	public void setIconUrl(String iconUrl) {
		this.iconUrl = iconUrl;
	}

	public List<GeoAddressFieldDef> getAddresses() {
		if (addresses == null) {
			addresses = new ArrayList<GeoAddressFieldDef>();
		}
		return addresses;
	}

	public void setAddresses(List<GeoAddressFieldDef> addresses) {
		this.addresses = addresses;
	}

	public FieldDef getLatitude() {
		return latitude;
	}

	public void setLatitude(FieldDef latitude) {
		this.latitude = latitude;
	}

	public FieldDef getLongitude() {
		return longitude;
	}

	public void setLongitude(FieldDef longitude) {
		this.longitude = longitude;
	}

	public List<GeoNamesFieldDef> getGeoNames() {
		if (geoNames == null) {
			geoNames = new ArrayList<GeoNamesFieldDef>();
		}
		return geoNames;
	}

	public void setGeoNames(List<GeoNamesFieldDef> geoNames) {
		this.geoNames = geoNames;
	}

	public List<GeoDetailsFieldDef> getDetails() {
		if (details == null) {
			details = new ArrayList<GeoDetailsFieldDef>();
		}
		return details;
	}

	public void setDetails(List<GeoDetailsFieldDef> details) {
		this.details = details;
	}


	public String getGeoIqUserid() {
		return geoIqUserid;
	}

	public void setGeoIqUserid(String geoIqUserid) {
		this.geoIqUserid = geoIqUserid;
	}

	public String getGeoIqPasswd() {
		return geoIqPasswd;
	}

	public void setGeoIqPasswd(String geoIqPasswd) {
		this.geoIqPasswd = geoIqPasswd;
	}

	public String getGeoIqHostname() {
		return geoIqHostname;
	}

	public void setGeoIqHostname(String geoIqHostname) {
		this.geoIqHostname = geoIqHostname;
	}

	public int getGeoIqPort() {
		return geoIqPort;
	}

	public void setGeoIqPort(int geoIqPort) {
		this.geoIqPort = geoIqPort;
	}

	public boolean isInDecimalNotation() {
		return inDecimalNotation;
	}

	public void setInDecimalNotation(boolean inDecimalNotation) {
		this.inDecimalNotation = inDecimalNotation;
	}

	public boolean isSendToGeoIQ() {
		return sendToGeoIQ;
	}

	public void setSendToGeoIQ(boolean sendToGeoIQ) {
		this.sendToGeoIQ = sendToGeoIQ;
	}

	public boolean isSendToGoogleEarth() {
		return sendToGoogleEarth;
	}

	public void setSendToGoogleEarth(boolean sendToGoogleEarth) {
		this.sendToGoogleEarth = sendToGoogleEarth;
	}
	public boolean isInDegreeNotation() {
		return inDegreeNotation;
	}

	public void setInDegreeNotation(boolean inDegreeNotation) {
		this.inDegreeNotation = inDegreeNotation;
	}

	public boolean isLatlonLocSelected() {
		return latlonLocSelected;
	}

	public void setLatlonLocSelected(boolean latlonLocSelected) {
		this.latlonLocSelected = latlonLocSelected;
	}

	public boolean isAddressLocSelected() {
		return addressLocSelected;
	}

	public void setAddressLocSelected(boolean addressLocSelected) {
		this.addressLocSelected = addressLocSelected;
	}

	public boolean isAutoGenOnBroadcast() {
		return autoGenOnBroadcast;
	}

	public void setAutoGenOnBroadcast(boolean autoGenOnBroadcast) {
		this.autoGenOnBroadcast = autoGenOnBroadcast;
	}
    
    @Override
    public Selection getSelection() {
        return NullSelection.instance;
    }

    @Override
    public <T extends ModelObject, S extends ModelObject> VisualizationDef clone(Map<String, T> fieldMapIn, Map<String, S> filterMapIn) {
        
        //log.error("Attempting to clone unsupported visualization type \"GeoSpatialViewDef\"");
        return null;
    }

	@Override
	public <T extends ModelObject, S extends ModelObject> VisualizationDef copy(Map<String, T> fieldMapIn,
			Map<String, S> filterMapIn) {
		// TODO Auto-generated method stub
		return null;
	}
}
