package csi.server.common.model.themes;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import csi.security.monitors.ResourceACLMonitor;
import csi.server.common.enumerations.AclResourceType;
import csi.server.common.model.CsiUUID;
import csi.server.common.model.Resource;
import csi.server.common.model.visualization.VisualizationType;
import csi.server.common.util.ValuePair;

@SuppressWarnings("serial")
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@EntityListeners(ResourceACLMonitor.class)
public abstract class Theme extends Resource implements Serializable {

    @Enumerated(EnumType.STRING)
    protected VisualizationType visualizationType;


    public Theme() {

    }

    public Theme(AclResourceType resourceTypeIn, VisualizationType visualizationTypeIn) {

        super(resourceTypeIn);
        setVisualizationType(visualizationTypeIn);
        setUuid(CsiUUID.formatThemeId(getUuid()));
    }

    public String getTypeString() {
        return this.visualizationType.toString();
    }

    public VisualizationType getVisualizationType() {
        return visualizationType;
    }

    public void setVisualizationType(VisualizationType visualizationTypeIn) {
        visualizationType = visualizationTypeIn;
    }

    public <T extends VisualItemStyle> void addStyle(T styleIn, String itemIn, Map<String, T> styleMapIn) {

        if ((null != styleIn) && (null != itemIn) && (0 < itemIn.length())) {

            styleIn.getFieldNames().add(itemIn);
            styleMapIn.put(itemIn, styleIn);
        }
    }

    public <T extends VisualItemStyle> void removeStyle(T styleIn, String itemIn, Map<String, T> styleMapIn) {

        if ((null != styleIn) && (null != itemIn) && (0 < itemIn.length())) {

            styleIn.getFieldNames().remove(itemIn);
            styleMapIn.remove(itemIn);
        }
    }

    public <T extends VisualItemStyle> void removeConflicts(List<ValuePair<String, T>> listIn) {

        if ((null != listIn) && !listIn.isEmpty()) {

            for (ValuePair<String, T> myPair : listIn) {

                String myItem = myPair.getValue1();
                T myStyle = myPair.getValue2();
                List<String> myList = myStyle.getFieldNames();

                if (null != myList) {

                    myList.remove(myItem);
                }
            }
        }
    }

    protected <T extends VisualItemStyle> void buildStyleMaps(List<T> styleListIn, Map<String, T> styleMapIn,
                                                              List<ValuePair<String, T>> overflowIn) {

        for (T myStyle : styleListIn) {

            List<String> myItemList = myStyle.getFieldNames();

            for (String myItem : myItemList) {

                if (styleMapIn.containsKey(myItem)) {

                    overflowIn.add(new ValuePair<String, T>(myItem, myStyle));

                } else {

                    styleMapIn.put(myItem, myStyle);
                }
            }
        }
    }

    protected void cloneComponents(Theme cloneIn) {

        super.cloneComponents(cloneIn);
        cloneValues(cloneIn);
    }

    private void cloneValues(Theme cloneIn) {

        cloneIn.setVisualizationType(visualizationType);
    }
}
