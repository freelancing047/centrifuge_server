package csi.server.common.model.chart;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import csi.server.business.visualization.SupportingRows;
import csi.server.common.dto.TypeNames;
import csi.server.common.model.ModelObject;
import csi.server.common.model.DimensionField;
import csi.server.common.model.GenericProperties;
import csi.server.common.model.SortOrder;

@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Deprecated
public class ChartDimension extends ModelObject {

    protected String dimName;

    protected int ordinal;

    protected SortOrder sortOrder = SortOrder.ASC;
    protected String bundleFunction;

    @OneToOne(cascade = CascadeType.ALL)
    protected GenericProperties bundleParams;

    @OneToMany(cascade = CascadeType.ALL)
	@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
    protected List<DimensionField> dimensionFields;

    public ChartDimension() {
    	super();
    }

    public String getDimName() {
        return dimName;
    }

    public void setDimName(String value) {
        this.dimName = value;
    }

    public int getOrdinal() {
        return ordinal;
    }

    public void setOrdinal(int value) {
        this.ordinal = value;
    }

    public SortOrder getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(SortOrder sortOrder) {
        this.sortOrder = sortOrder;
    }

    public String getBundleFunction() {
        return bundleFunction;
    }

    public void setBundleFunction(String bundleFunction) {
        this.bundleFunction = bundleFunction;
    }

    public GenericProperties getBundleParams() {
        return bundleParams;
    }

    public void setBundleParams(GenericProperties bundleParams) {
        this.bundleParams = bundleParams;
    }

    public List<DimensionField> getDimensionFields() {
        if (dimensionFields == null) {
            dimensionFields = new ArrayList<DimensionField>();
        }
        return dimensionFields;
    }

    public void setDimensionFields(List<DimensionField> dimensionFields) {
        this.dimensionFields = dimensionFields;
    }

    @Transient
    public boolean isNumeric() {
        boolean numeric;
        if (SupportingRows.LENGTH.equals(bundleFunction)) {
            numeric = true;
        } else if ((dimensionFields == null) || dimensionFields.isEmpty()) {
            numeric = false;
        } else {
            DimensionField firstField = dimensionFields.get(0);
            numeric = TypeNames.isNumeric(firstField.getFieldDef().getValueType());
        }

        return numeric;
    }
}
