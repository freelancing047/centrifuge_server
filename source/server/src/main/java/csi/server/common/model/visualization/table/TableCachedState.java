package csi.server.common.model.visualization.table;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.ArrayList;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Cascade;

import csi.server.common.model.ModelObject;
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name="tablecachedstate")
public class TableCachedState extends ModelObject {


    private Integer verticalScrollPosition = 0;
    private Integer horizontalScrollPosition = 0;
    private Integer page = 0;
    
    @OneToMany(cascade = CascadeType.ALL)
    @Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
    @Cascade(value = { org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
    List<ColumnState> columnStates = new ArrayList<>();
    public TableCachedState() {
    }

    public List<ColumnState> getColumnStates() {
        return columnStates;
    }

    public void setColumnStates(List<ColumnState> columnStates) {
        this.columnStates = columnStates;
    }

    public Integer getHorizontalScrollPosition() {
        return horizontalScrollPosition;
    }

    public void setHorizontalScrollPosition(Integer horizontalScrollPosition) {
        this.horizontalScrollPosition = horizontalScrollPosition;
    }

    public Integer getVerticalScrollPosition() {
        return verticalScrollPosition;
    }

    public void setVerticalScrollPosition(Integer verticalScrollPosition) {
        this.verticalScrollPosition = verticalScrollPosition;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

}