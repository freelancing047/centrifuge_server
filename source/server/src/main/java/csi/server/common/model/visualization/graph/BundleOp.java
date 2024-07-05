package csi.server.common.model.visualization.graph;

import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.google.common.collect.Maps;

import csi.server.common.dto.CsiMap;
import csi.server.common.model.FieldDef;
import csi.server.common.model.ModelObject;

@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class BundleOp extends ModelObject {

    protected int priority;

    @ManyToOne(cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH })
    protected NodeDef nodeDef;

    @ManyToOne(cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH })
    protected FieldDef field;

    public BundleOp() {
        super();
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public FieldDef getField() {
        return field;
    }

    public void setField(FieldDef field) {
        this.field = field;
    }

    public NodeDef getNodeDef() {
        return nodeDef;
    }

    public void setNodeDef(NodeDef nodeDef) {
        this.nodeDef = nodeDef;
    }

    public BundleOp copy(Map<String, Object> copies) {

        if (copies == null) {
            copies = Maps.newHashMap();
        }
        {
            Object copyOfThis = copies.get(this.getUuid());
            if (copyOfThis != null) {
                return (BundleOp) copyOfThis;
            }
        }
        BundleOp copy = new BundleOp();
        copies.put(getUuid(), copy);
        copy.clientProperties = new CsiMap<String, String>();
        copy.clientProperties.putAll(this.getClientProperties());
        copy.setField(this.getField());
        if (this.getNodeDef() != null) {
            copy.setNodeDef(this.getNodeDef().copy(copies));
        }
        copy.setPriority(this.getPriority());
        copy.uuid = this.uuid;
        return copy;
    }

    @SuppressWarnings("unchecked")
    public <T extends ModelObject, S extends ModelObject> BundleOp clone(Map<String, T> fieldMapIn, Map<String, S> nodeMapIn, boolean isCopy) {
        
        BundleOp myClone = new BundleOp();
        
        super.cloneComponents(myClone);

        myClone.setPriority(getPriority());
        if(isCopy){
            myClone.setField(getField());
        } else {
            myClone.setField((FieldDef)cloneFromOrToMap(fieldMapIn, (T)getField(), fieldMapIn));
        }

    	myClone.setNodeDef((NodeDef)cloneFromOrToMap(nodeMapIn, (S)getNodeDef()));

        return myClone;
    }
}
