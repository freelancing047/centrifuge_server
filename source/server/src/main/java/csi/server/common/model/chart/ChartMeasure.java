package csi.server.common.model.chart;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import csi.server.common.model.ModelObject;
import csi.server.common.model.FieldDef;
import csi.server.common.model.GenericProperties;
import csi.server.common.model.SortOrder;

@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Deprecated
public class ChartMeasure extends ModelObject {

    protected String name;

    protected int ordinal;

    protected String measureFunction;

    protected String bundleFunction;

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

    @OneToOne(cascade = CascadeType.ALL)
    protected GenericProperties bundleParams;

    @ManyToOne(cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH })
    protected FieldDef measureField;

    protected SortOrder sort;

    public ChartMeasure() {
        super();
    }

    public String getName() {
        return name;
    }

    public void setName(String metricName) {
        this.name = metricName;
    }

    public String getMeasureFunction() {
        return measureFunction;
    }

    public void setMeasureFunction(String metricFunction) {
        this.measureFunction = metricFunction;
    }

    public int getOrdinal() {
        return ordinal;
    }

    public void setOrdinal(int value) {
        this.ordinal = value;
    }

    public FieldDef getMeasureField() {
        return measureField;
    }

    public void setMeasureField(FieldDef metricField) {
        this.measureField = metricField;
    }

    public synchronized SortOrder getSort() {
        return sort;
    }

    public synchronized void setSort(SortOrder sort) {
        this.sort = sort;
    }

    @Transient
    public String getComposedName() {
        return getMeasureFunction() + " of " + getName();
    }
}
