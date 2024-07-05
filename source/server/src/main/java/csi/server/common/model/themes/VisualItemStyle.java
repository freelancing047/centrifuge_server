package csi.server.common.model.themes;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import csi.server.common.model.ModelObject;

@SuppressWarnings("serial")
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public abstract class VisualItemStyle extends ModelObject implements Serializable{

    private String name;

    @ElementCollection(fetch=FetchType.EAGER)
    @Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
    @Fetch(value = FetchMode.SUBSELECT)
    protected List<String> fieldNames = new ArrayList<String>();

    public abstract String genKey();
    public abstract VisualItemStyle genEmpty();

    public VisualItemStyle() {

    }

    public VisualItemStyle(String nameIn) {

        this();
        name = nameIn;
    }

    public List<String> getFieldNames() {

        if (null == fieldNames) {

            fieldNames = new ArrayList<String>();
        }
        return fieldNames;
    }

    public void addName(String nameIn) {

        getFieldNames().add(nameIn);
    }

    public void setFieldNames(List<String> fieldNames) {
        this.fieldNames = fieldNames;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
