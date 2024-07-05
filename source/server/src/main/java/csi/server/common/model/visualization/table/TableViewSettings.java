package csi.server.common.model.visualization.table;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Cascade;

import csi.server.common.model.DataModelDef;
import csi.server.common.model.FieldDef;
import csi.server.common.model.ModelObject;

@SuppressWarnings("serial")
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class TableViewSettings extends ModelObject implements Serializable {

    public TableViewSettings() {
        super();
    }

    @OneToMany(cascade = CascadeType.ALL)
    @OrderBy("listPosition")
	@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
    @Cascade(value = { org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
    private List<VisibleTableField> visibleFields;

    @OneToMany(cascade = CascadeType.ALL)
    @OrderBy("listPosition")
	@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
    @Cascade(value = { org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
    private List<TableViewSortField> sortFields;

    private int pageSize;

    public List<VisibleTableField> getVisibleFields() {
        if (visibleFields == null) {
            visibleFields = new ArrayList<VisibleTableField>();
        }

        return this.visibleFields;
    }

    public List<FieldDef> getVisibleFieldDefs(DataModelDef dataModelIn) {
        List<FieldDef> list = new ArrayList<FieldDef>();
        List<VisibleTableField> vFields = getVisibleFields();
        for (VisibleTableField vf : vFields) {
            FieldDef fieldDef = vf.getFieldDef(dataModelIn);
            if (fieldDef != null) {
                list.add(fieldDef);
            }
        }
        return list;
    }

    public List<TableViewSortField> getSortFieldDefs(DataModelDef dataModelIn) {
        List<TableViewSortField> myBaseList = getSortFields();
        List<TableViewSortField> myFinalList = new ArrayList<TableViewSortField>();
        for (TableViewSortField myField : myBaseList) {
        	TableViewSortField tableViewSortField = new TableViewSortField(myField.getFieldDef(dataModelIn));
        	tableViewSortField.setSortOrder(myField.getSortOrder());
            myFinalList.add(tableViewSortField);
        }
        return myFinalList;
    }

    public List<TableViewSortField> getSortFields() {
        if (sortFields == null) {
            sortFields = new ArrayList<TableViewSortField>();
        }
        return this.sortFields;
    }

    public void setVisibleFields(List<VisibleTableField> visibleFields) {
        this.visibleFields = visibleFields;
    }

    public void setSortFields(List<TableViewSortField> sortFields) {
        this.sortFields = sortFields;
    }

    public int getPageSize() {
        if (pageSize <= 0) {
            pageSize = 50;
        }
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    @Override
    public TableViewSettings clone() {

        TableViewSettings myClone = new TableViewSettings();

        super.cloneComponents(myClone);

        myClone.setPageSize(getPageSize());
        myClone.setVisibleFields(cloneVisibleFields());
        myClone.setSortFields(cloneSortFields());

        return myClone;
    }

    public TableViewSettings copy() {

        TableViewSettings myCopy = new TableViewSettings();

        super.copyComponents(myCopy);

        myCopy.setPageSize(getPageSize());
        myCopy.setVisibleFields(copyVisibleFields());
        myCopy.setSortFields(copySortFields());

        return myCopy;
    }

    private List<VisibleTableField> cloneVisibleFields() {
        if (null != getVisibleFields()) {
            List<VisibleTableField>  myList = new ArrayList<VisibleTableField>();

            for (VisibleTableField myItem : getVisibleFields()) {
                myList.add(myItem.clone());
            }

            return myList;
        }
        return null;
    }

    private List<TableViewSortField> cloneSortFields() {
        if (null != getSortFields()) {
            List<TableViewSortField>  myList = new ArrayList<TableViewSortField>();
            for (TableViewSortField myItem : getSortFields()) {
                myList.add(myItem.clone());
            }

            return myList;
        }
        return null;
    }

    private List<VisibleTableField> copyVisibleFields() {
        if (null != getVisibleFields()) {
            List<VisibleTableField>  myList = new ArrayList<VisibleTableField>();

            for (VisibleTableField myItem : getVisibleFields()) {
                myList.add(myItem.copy());
            }

            return myList;
        }
        return null;
    }

    private List<TableViewSortField> copySortFields() {
        if (null != getSortFields()) {
            List<TableViewSortField>  myList = new ArrayList<TableViewSortField>();
            for (TableViewSortField myItem : getSortFields()) {
                myList.add(myItem.copy());
            }

            return myList;
        }
        return null;
    }

}
