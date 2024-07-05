package csi.server.common.model.visualization.graph;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;

import csi.server.common.model.FieldType;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import csi.server.common.dto.CsiMap;
import csi.server.common.model.FieldDef;
import csi.server.common.model.ModelObject;
import csi.server.common.model.listener.DirectionDefListener;
import csi.shared.gwt.viz.graph.LinkDirection;

@Entity
@EntityListeners(DirectionDefListener.class)
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class DirectionDef extends ModelObject {

    @ManyToOne(cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH })
    protected FieldDef fieldDef;

    @Basic(fetch = FetchType.LAZY)
    @Column(length = 2147483647)
    @Lob
    /**
     * The declared type of these lists must stay ArrayList (or another concrete implementation),
     * because otherwise (declared as an interface), hibernate doesn't persist it correctly.
     */
    private ArrayList<String> forwardValues = new ArrayList<String>();

    @Basic(fetch = FetchType.LAZY)
    @Column(length = 2147483647)
    @Lob
    private ArrayList<String> reverseValues = new ArrayList<String>();
    private String fieldDefId = null;

    public DirectionDef() {
        super();
    }

    public LinkDirection resolveDirectionByValue(String value) {
        if (containsCaseless(forwardValues, value)) {
            return LinkDirection.FORWARD;
        }
        if (containsCaseless(reverseValues, value)) {
            return LinkDirection.REVERSE;
        }

        return LinkDirection.NONE;
    }

    private boolean containsCaseless(ArrayList<String> values, String value) {
        for (String listValue : values) {
            if (listValue.equalsIgnoreCase(value)) {
                return true;
            }
        }
        return false;
    }

    public FieldDef getFieldDef() {
        return fieldDef;
    }

    public ArrayList<String> getForwardValues() {
        return forwardValues;
    }

    public void setForwardValues(ArrayList<String> forwardValues) {
        this.forwardValues = forwardValues;
    }

    public ArrayList<String> getReverseValues() {
        return reverseValues;
    }

    public void setReverseValues(ArrayList<String> reverseValues) {
        this.reverseValues = reverseValues;
    }

    public void setFieldDef(FieldDef fieldDef) {
        this.fieldDef = fieldDef;
    }

    public DirectionDef copy(Map<String, Object> copies) {
        if (copies == null) {
            copies = Maps.newHashMap();
        }
        {
            Object copyOfThis = copies.get(this.getUuid());
            if (copyOfThis != null) {
                return (DirectionDef) copyOfThis;
            }
        }
        DirectionDef copy = new DirectionDef();
        copies.put(getUuid(), copy);
        copy.clientProperties = new CsiMap<String, String>();
        copy.clientProperties.putAll(this.getClientProperties());
        if (this.getFieldDef() != null) {
            if(this.getFieldDef().getFieldName() == null){
                copy.setFieldDef(this.getFieldDef().copy(copies));
            } else {
                copy.setFieldDef(getFieldDef());
            }
        }
        copy.setForwardValues(Lists.newArrayList(this.getForwardValues()));
        copy.setReverseValues(Lists.newArrayList(this.getReverseValues()));
        copy.uuid = this.uuid;
        return copy;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends ModelObject> DirectionDef clone(Map<String, T> fieldMapIn) {
        DirectionDef myClone = new DirectionDef();
        super.cloneComponents(myClone);
        if (FieldType.STATIC != getFieldDef().getFieldType()) {
            myClone.setFieldDef(getFieldDef());
        } else {
            Map<String, FieldDef> uuidFieldDefMap = Maps.newConcurrentMap();
            for (Map.Entry<String, T> entry : fieldMapIn.entrySet()) {
                T value = entry.getValue();
                if (value instanceof FieldDef) {
                    uuidFieldDefMap.put(value.getUuid(), (FieldDef) value);
                }
            }
            myClone.setFieldDef(getFieldDef().clone(uuidFieldDefMap));
        }
        myClone.setForwardValues(cloneStringList(getForwardValues()));
        myClone.setReverseValues(cloneStringList(getReverseValues()));

        return myClone;
    }

    public void setFieldDefId(String uuidIn) {

        fieldDefId = uuidIn;
    }

    public String getFieldDefId() {

        return fieldDefId;
    }

    private ArrayList<String> cloneStringList(List<String> listIn) {
        
        if (null != listIn) {
            
            ArrayList<String>  myList = new ArrayList<String>();
            
            for (String myItem : listIn) {
                
                myList.add(myItem);
            }
            
            return myList;
            
        } else {
            
            return null;
        }
    }
}
