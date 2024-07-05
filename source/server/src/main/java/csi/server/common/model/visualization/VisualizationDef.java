package csi.server.common.model.visualization;

import java.util.Map;

import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import csi.server.common.enumerations.AclResourceType;
import csi.server.common.model.CsiUUID;
import csi.server.common.model.ModelObject;
import csi.server.common.model.Resource;
import csi.server.common.model.broadcast.BroadcastListener;
import csi.server.common.model.filter.Filter;
import csi.server.common.model.filter.HasFilter;
import csi.server.common.model.visualization.selection.Selectable;

@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class VisualizationDef extends Resource implements Selectable, BroadcastListener, HasFilter {

    @ManyToOne
    private Filter filter;

    private VisualizationType type;

    private boolean broadcastListener = true;

    private boolean suppressLoadAtStartup = false;

	protected String localId;

    @Transient
    protected boolean readOnly = false;

    protected String themeUuid;

    protected Boolean hideOverview =false;

    public VisualizationDef() {
        this("Unnamed Visualization");
    }

    public VisualizationDef(String name) {
        super(AclResourceType.VISUALIZATION);
        setName(name);
        localId = CsiUUID.randomUUID();
    }

    @Override
    public abstract <T extends ModelObject, S extends ModelObject> VisualizationDef clone(Map<String, T> fieldMapIn, Map<String, S> filterMapIn);

    @Override
    public abstract <T extends ModelObject, S extends ModelObject> VisualizationDef copy(Map<String, T> fieldMapIn, Map<String, S> filterMapIn);


    public VisualizationType getType() {
        return type;
    }

    public void setType(VisualizationType type) {
        this.type = type;
    }

    public Filter getFilter() {
        return filter;
    }

    public void setFilter(Filter filter) {
        this.filter = filter;
    }

    @Transient
    public String getFilterUuid() {
        if (filter == null) {
            return null;
        }
        return filter.getUuid();
    }

    @Override
    public boolean isBroadcastListener() {
        return broadcastListener;
    }

    @Override
    public void setBroadcastListener(boolean broadcastListener) {
        this.broadcastListener = broadcastListener;
    }

    public boolean isSuppressLoadAtStartup() {
		return suppressLoadAtStartup;
	}

	public void setSuppressLoadAtStartup(boolean suppressLoadAtStartup) {
		this.suppressLoadAtStartup = suppressLoadAtStartup;
	}

    public void setLocalId(String localIdIn) {

        localId = localIdIn;
    }

    public String getLocalId() {

        return localId;
    }

    @SuppressWarnings("unchecked")
    protected <T extends ModelObject, S extends ModelObject> void cloneComponents(VisualizationDef cloneIn, Map<String, T> fieldMapIn, Map<String, S> filterMapIn) {

        if (null != cloneIn)
        {
            super.cloneComponents(cloneIn);

            cloneIn.setName(getName());
            cloneIn.setType(getType());
            cloneIn.setLocalId(getLocalId());
            cloneIn.setFilter((Filter)cloneFromOrToMap(filterMapIn, (S)getFilter(), fieldMapIn));
            cloneIn.setSuppressLoadAtStartup(isSuppressLoadAtStartup());
        }
    }

    @Override
    protected void debugContents(StringBuilder bufferIn, String indentIn) {

        debugObject(bufferIn, name, indentIn, "name");
        debugObject(bufferIn, type, indentIn, "type");
//        doDebug(filter, bufferIn, indentIn, "filter", "Filter");
    }

    public <T extends ModelObject, S extends ModelObject> VisualizationDef copyComponents(VisualizationDef copyIn, Map<String, T> fieldMapIn, Map<String, S> filterMapIn) {
		if (null != copyIn)
	        {
	            super.copyComponents(copyIn);

	            copyIn.setName(getName());
	            copyIn.setType(getType());
                //copyIn.setLocalId(getLocalId());
                copyIn.setSuppressLoadAtStartup(isSuppressLoadAtStartup());
	            if(getFilter() != null) {
                  copyIn.setFilter(getFilter());
               }
	        }
		return copyIn;
	}

    public void setReadOnly() {

        readOnly = true;
    }

    public boolean isReadOnly() {

        return readOnly;
    }

    public String getThemeUuid() {
        return themeUuid;
    }

    public void setThemeUuid(String themeUuid) {
        this.themeUuid = themeUuid;
    }

    public Boolean getHideOverview() {
        return hideOverview;
    }

    public void setHideOverview(Boolean hideOverview) {
        this.hideOverview = hideOverview;
    }


}
