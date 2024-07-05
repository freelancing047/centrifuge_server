package csi.server.common.model.visualization.graph;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Cascade;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import csi.server.common.model.ModelObject;

@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class BundleDef extends ModelObject {

    @OneToMany(cascade = CascadeType.ALL)
	@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
    @Cascade(org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
    @OrderBy(value = "priority")
    protected List<BundleOp> operations;

    public BundleDef() {
        super();
    }

    public List<BundleOp> getOperations() {
        if (operations == null) {
            operations = new ArrayList<BundleOp>();
        }
        return operations;
    }

    public void setOperations(List<BundleOp> expressions) {
        this.operations = expressions;
    }

    public BundleDef copy(Map<String, Object> copies) {
        if (copies == null) {
            copies = Maps.newHashMap();
        }
        {
            Object copyOfThis = copies.get(this.getUuid());
            if (copyOfThis != null) {
                return (BundleDef) copyOfThis;
            }
        }
        BundleDef copy = new BundleDef();
        copies.put(getUuid(), copy);
        copy.operations = Lists.newArrayList();
        for (BundleOp bundleOp : this.getOperations()) {
            copy.operations.add(bundleOp.copy(copies));
        }
        copy.uuid = this.uuid;
        return copy;
    }

    public <T extends ModelObject, S extends ModelObject> BundleDef clone(Map<String, T> fieldMapIn, Map<String, S> nodeMapIn, boolean isCopy) {
        
        BundleDef myClone = new BundleDef();
        
        super.cloneComponents(myClone);

        myClone.setOperations(cloneOperations(fieldMapIn, nodeMapIn, isCopy));

        return myClone;
    }
        
    private <T extends ModelObject, S extends ModelObject> List<BundleOp> cloneOperations(Map<String, T> fieldMapIn, Map<String, S> nodeMapIn, boolean isCopy) {
        
        if (null != getOperations()) {
            
            List<BundleOp>  myList = new ArrayList<BundleOp>();
            
            for (BundleOp myItem : getOperations()) {
                
                myList.add(myItem.clone(fieldMapIn, nodeMapIn, isCopy));
            }
            
            return myList;
            
        } else {
            
            return null;
        }
    }

}
