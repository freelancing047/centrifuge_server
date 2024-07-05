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
package csi.server.common.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.google.common.base.Objects;

import csi.server.common.enumerations.CsiDataType;
import csi.server.common.model.visualization.BundleFunctionParameter;
import csi.server.util.sql.api.BundleFunction;

/**
 * Wraps a field and bundle function together.
 * @author Centrifuge Systems, Inc.
 *
 */
@SuppressWarnings("serial")
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class BundledFieldReference extends ModelObject implements Serializable {


    public BundledFieldReference() {
        super();
    }

    @ManyToOne
    private FieldDef fieldDef;
    @Enumerated(value = EnumType.STRING)
    private BundleFunction bundleFunction = BundleFunction.NONE;

    @OneToMany
	@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
    private List<BundleFunctionParameter> bundleFunctionParameters = new ArrayList<BundleFunctionParameter>();



    public FieldDef getFieldDef() {
        return fieldDef;
    }

    public void setFieldDef(FieldDef fieldDef) {
        this.fieldDef = fieldDef;
    }

    public BundleFunction getBundleFunction() {
        return bundleFunction;
    }

    public void setBundleFunction(BundleFunction bundleFunction) {
        this.bundleFunction = bundleFunction;
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
    public int hashCode() {
        return Objects.hashCode(getFieldDef(), getBundleFunction(), getBundleFunctionParameters());
    }

   @Override
   public boolean equals(Object obj) {
      return (this == obj) ||
             ((obj != null) &&
              (obj instanceof BundledFieldReference) &&
              Objects.equal(getFieldDef(), ((BundledFieldReference) obj).getFieldDef()) &&
              (getBundleFunction() == ((BundledFieldReference) obj).getBundleFunction()) &&
              Objects.equal(getBundleFunctionParameters(), ((BundledFieldReference) obj).getBundleFunctionParameters()));
   }

    public CsiDataType getDataType() {
        return bundleFunction.getReturnType(getFieldDef());
    }
}
