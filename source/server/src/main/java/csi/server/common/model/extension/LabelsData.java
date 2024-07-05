package csi.server.common.model.extension;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class LabelsData
    extends ExtensionData
{

    @ElementCollection
    protected List<String> labels = new ArrayList<String>();

    public LabelsData() {
    	super();
    }

    public List<String> getLabels() {
        if( labels == null ) {
            return new ArrayList<String>();
        }
        return labels;
    }

    public void setLabels(List<String> labels) {
        this.labels = labels;
    }

}
