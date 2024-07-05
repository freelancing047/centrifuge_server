package csi.server.common.model.visualization;

import javax.persistence.Entity;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import csi.server.common.model.ModelObject;

/**
 * @author Centrifuge Systems, Inc.
 */
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class BundleFunctionParameter extends ModelObject {

    int ordinal;

    private String functionParameter;

    public BundleFunctionParameter() {
        super();
    }

    public int getOrdinal() {
        return ordinal;
    }

    public void setOrdinal(int ordinal) {
        this.ordinal = ordinal;
    }

    public String getFunctionParameter() {
        return functionParameter;
    }

    public void setFunctionParameter(String functionParamter) {
        this.functionParameter = functionParamter;
    }

    @Override
    public BundleFunctionParameter clone() {
        
        BundleFunctionParameter myClone = new BundleFunctionParameter();
        
        super.cloneComponents(myClone);

        myClone.setFunctionParameter(getFunctionParameter());
        
        return myClone;
    }

	public BundleFunctionParameter copy() {
		BundleFunctionParameter myClone = new BundleFunctionParameter();
        
        super.copyComponents(myClone);

        myClone.setFunctionParameter(getFunctionParameter());
        
        return myClone;
	}
}
