package csi.server.common.model.chart;


import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import csi.server.common.model.ModelObject;
import csi.server.common.model.FieldDef;
import csi.server.common.model.GenericProperties;
import csi.server.common.model.SortOrder;

@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Deprecated
public class ChartField extends ModelObject {

    protected String dimName;

    protected int ordinal;

    protected SortOrder sortOrder = SortOrder.ASC;
    protected String bundleFunction;

    @OneToOne(cascade = CascadeType.ALL)
    protected GenericProperties bundleParams;

    @ManyToOne(cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH })
    protected FieldDef dimension;

    public ChartField() {
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

    public FieldDef getDimension() {
        return dimension;
    }

    public void setDimension(FieldDef dimension) {
        this.dimension = dimension;
    }
}
