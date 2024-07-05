package csi.server.common.dto;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import csi.server.common.model.listener.CreateKmlRequestListener;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.google.common.collect.Lists;
import com.google.gwt.user.client.rpc.IsSerializable;

import csi.server.common.model.FieldDef;
import csi.server.common.model.ModelObject;
import csi.server.common.model.filter.Filter;
import csi.server.common.model.kml.KmlMapping;
import csi.server.common.model.visualization.VisualizationDef;

/**
* Created by Patrick on 10/29/2014.
*/
@Entity
@EntityListeners(CreateKmlRequestListener.class)
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class CreateKmlRequest extends ModelObject implements IsSerializable{

    @OneToMany(cascade = CascadeType.ALL,orphanRemoval=true)
	@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
    private List<KmlMapping> kmlMappings;
    private String baseURL;
    @OneToOne
    private VisualizationDef visualizationFilter = null;
    @OneToOne
    private Filter filter;

    public CreateKmlRequest(){
        kmlMappings = Lists.newArrayList();
    }

    private String dataviewUuid;
    private String fieldDefIds;

    public String getDataviewUuid() {
        return dataviewUuid;
    }

    public void setDataviewUuid(String dataviewUuid) {
        this.dataviewUuid = dataviewUuid;
    }

    public String getFieldDefIds() {
        return fieldDefIds;
    }

    public void setFieldDefIds(String fieldDefIdsIn) {
        this.fieldDefIds = fieldDefIdsIn;
    }

    public List<FieldDef> getFields() {
        List<FieldDef> fieldDefs = Lists.newArrayList();
        for (KmlMapping kmlMapping : kmlMappings) {
            fieldDefs.addAll(kmlMapping.getAddressFields());
            fieldDefs.addAll(kmlMapping.getDetailFields());
            fieldDefs.addAll(kmlMapping.getDurationFields());
            fieldDefs.addAll(kmlMapping.getEndTimeFields());
            fieldDefs.addAll(kmlMapping.getStartTimeFields());
            fieldDefs.addAll(kmlMapping.getIconFields());
            fieldDefs.addAll(kmlMapping.getLabelFields());
            fieldDefs.addAll(kmlMapping.getLatFields());
            fieldDefs.addAll(kmlMapping.getLongFields());
        }
        return fieldDefs;
    }

    public List<KmlMapping> getKmlMappings() {
        return kmlMappings;
    }

    public void setKmlMappings(List<KmlMapping> kmlMappings) {
        this.kmlMappings = kmlMappings;
    }

    public String getBaseURL() {
        return baseURL;
    }

    public void setBaseURL(String baseURL) {
        this.baseURL = baseURL;
    }

    public VisualizationDef getVisualizationFilter() {
        return visualizationFilter;
    }

    public void setVisualizationFilter(VisualizationDef visualizationFilter) {
        this.visualizationFilter = visualizationFilter;
    }

    public void setFilter(Filter filter) {
        this.filter = filter;
    }

    public Filter getFilter() {
        return filter;
    }
}
