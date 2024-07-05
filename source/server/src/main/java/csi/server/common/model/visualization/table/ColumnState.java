package csi.server.common.model.visualization.table;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import csi.server.common.model.FieldDef;
import csi.server.common.model.ModelObject;

@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name="tablecolumnstate")
public class ColumnState extends ModelObject {


    @ManyToOne
    private FieldDef fieldDef;
    private Integer width;
    private Integer index;
        
    public Integer getIndex() {
        return index;
    }
    public void setIndex(Integer index) {
        this.index = index;
    }
    public Integer getWidth() {
        return width;
    }
    public void setWidth(Integer width) {
        this.width = width;
    }
    public FieldDef getFieldDef() {
        return fieldDef;
    }
    public void setFieldDef(FieldDef fieldDef) {
        this.fieldDef = fieldDef;
    }


    
}
