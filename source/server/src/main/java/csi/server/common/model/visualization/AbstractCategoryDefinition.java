/**
 *  Copyright (c) 2008 Centrifuge Systems, Inc.
 *  All rights reserved.
 *
 *  This software is the confidential and proprietary information of
 *  Centrifuge Systems, Inc. ("Confidential Information").  You shall
 *  not disclose such Confidential Information and shall use it only
 *  in accordance with the terms of the license agreement you entered
 *  into with Centrifuge Systems.
 *
 **/
package csi.server.common.model.visualization;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import csi.server.common.enumerations.CsiDataType;
import csi.server.common.model.FieldDef;
import csi.server.common.model.ModelObject;
import csi.server.common.model.visualization.chart.LabelDefinition;
import csi.server.util.sql.api.BundleFunction;
import csi.server.util.sql.api.HasBundleFunction;

/**
 * @author Centrifuge Systems, Inc.
 */
@SuppressWarnings("serial")
@MappedSuperclass
public abstract class AbstractCategoryDefinition extends AbstractAttributeDefinition implements HasBundleFunction {

    public AbstractCategoryDefinition() {
        super();
    }

    @ManyToOne
    private FieldDef fieldDef;

    private BundleFunction bundleFunction = BundleFunction.NONE;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
    private List<BundleFunctionParameter> bundleFunctionParameters = new ArrayList<BundleFunctionParameter>();

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private LabelDefinition labelDefinition = new LabelDefinition();

    private boolean allowNulls;
    private int listPosition;

    public BundleFunction getBundleFunction() {
        return bundleFunction;
    }

    public void setBundleFunction(BundleFunction bundleFunction) {
        this.bundleFunction = bundleFunction;
    }

    public FieldDef getFieldDef() {
        return fieldDef;
    }

    public void setFieldDef(FieldDef fieldDef) {
        this.fieldDef = fieldDef;
    }

    public LabelDefinition getLabelDefinition() {
        return labelDefinition;
    }

    public void setLabelDefinition(LabelDefinition labelDefinition) {
        this.labelDefinition = labelDefinition;
    }

    public List<BundleFunctionParameter> getBundleFunctionParameters() {
        return bundleFunctionParameters;
    }

    public void setBundleFunctionParameters(List<BundleFunctionParameter> bundleFunctionParameters) {
        this.bundleFunctionParameters = bundleFunctionParameters;
    }

    public List<String> getStringParamters(){
        List<String> params = new ArrayList<String>();
        for (BundleFunctionParameter bundleFunctionParamter : getBundleFunctionParameters()) {
            params.add(bundleFunctionParamter.getFunctionParameter());
        }
        return params;

    }

    @Override
    public CsiDataType getDataTypeForBundleFunction() {
        return getFieldDef().getValueType();
    }

    public boolean isAllowNulls() {
        return allowNulls;
    }

    public void setAllowNulls(boolean suppressNulls) {
        this.allowNulls = suppressNulls;
    }

    @Override
    public int getListPosition() {
        return listPosition;
    }

    public void setListPosition(int listPosition) {
        this.listPosition = listPosition;
    }

   @Override
   public String getComposedName() {
      String name = null;

      if (getBundleFunction() == BundleFunction.NONE) {
         name = getFieldDef().getFieldName();
      } else {
         StringBuilder sb = new StringBuilder(getBundleFunction().getLabel());

         if (!getBundleFunctionParameters().isEmpty()) {
            sb.append("(");

            boolean firstParam = true;

            for (BundleFunctionParameter bundleFunctionParamter : getBundleFunctionParameters()) {
               if (firstParam) {
                  firstParam = false;
               } else {
                  sb.append(", ");
               }
               sb.append(bundleFunctionParamter.getFunctionParameter());
            }
            sb.append(")");
         }
         name = sb.append(": ").append(getFieldDef().getFieldName()).toString();
      }
      return getLabelDefinition().getComposedName(name);
   }

    @Override
    public CsiDataType getDerivedType() {
        return getBundleFunction().getReturnType(getFieldDef());
    }

    @SuppressWarnings("unchecked")
    protected <T extends ModelObject> void cloneComponents(AbstractCategoryDefinition cloneIn, Map<String, T> fieldMapIn) {

        if (null != cloneIn) {

            super.cloneComponents(cloneIn);

            cloneIn.setFieldDef((FieldDef)cloneFromOrToMap(fieldMapIn, (T)getFieldDef(), fieldMapIn));
            if (null != this.getBundleFunction()) {
                cloneIn.setBundleFunction(this.getBundleFunction());
            }
            if (null != this.getLabelDefinition()) {
                cloneIn.setLabelDefinition(this.getLabelDefinition().clone());
            }
            cloneIn.setAllowNulls(isAllowNulls());
            cloneIn.setBundleFunctionParameters(cloneBundleFunctionParameters());
        }
    }

    @SuppressWarnings("unchecked")
    protected <T extends ModelObject> void copyComponents(AbstractCategoryDefinition copyIn) {

        if (null != copyIn) {

            super.copyComponents(copyIn);

            copyIn.setFieldDef(getFieldDef());
            if (null != this.getBundleFunction()) {
            	copyIn.setBundleFunction(this.getBundleFunction());
            }
            if (null != this.getLabelDefinition()) {
            	copyIn.setLabelDefinition(getLabelDefinition().copy());
            }
            copyIn.setAllowNulls(isAllowNulls());
            copyIn.setBundleFunctionParameters(cloneBundleFunctionParameters());
        }
    }

    private List<BundleFunctionParameter> cloneBundleFunctionParameters() {

        if (null != getBundleFunctionParameters()) {

            List<BundleFunctionParameter>  myList = new ArrayList<BundleFunctionParameter>();

            for (BundleFunctionParameter myItem : getBundleFunctionParameters()) {

                myList.add(myItem.clone());
            }

            return myList;
        }
        return null;
    }

	private List<BundleFunctionParameter> copyBundleFunctionParameters() {

	    if (null != getBundleFunctionParameters()) {

	        List<BundleFunctionParameter>  myList = new ArrayList<BundleFunctionParameter>();

	        for (BundleFunctionParameter myItem : getBundleFunctionParameters()) {

	            myList.add(myItem);
	        }

	        return myList;
	    }
	    return null;
	}
}
