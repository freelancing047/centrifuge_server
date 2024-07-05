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
package csi.server.common.model.visualization.matrix;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import com.thoughtworks.xstream.annotations.XStreamOmitField;
import csi.server.common.model.visualization.selection.MatrixCellSelection;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import csi.server.common.model.ModelObject;
import csi.server.common.model.visualization.VisualizationDef;
import csi.server.common.model.visualization.VisualizationType;

/**
 * Definition of a Matrix
 *
 * @author Centrifuge Systems, Inc.
 */
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class MatrixViewDef extends VisualizationDef {

    @OneToOne(cascade = CascadeType.ALL)
    private MatrixSettings matrixSettings;

    @Transient
    @XStreamOmitField
    private MatrixCellSelection cellSelection = new MatrixCellSelection();
    public MatrixViewDef() {
        super();
        setType(VisualizationType.MATRIX);
    }

    public String getCacheKey() {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] bytes = md.digest(getUuid().getBytes());
            return new String(bytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public MatrixSettings getMatrixSettings() {
        return matrixSettings;
    }

    public void setMatrixSettings(MatrixSettings matrixSettings) {
        this.matrixSettings = matrixSettings;
    }

    
    @Override
    public MatrixCellSelection getSelection() {
        return cellSelection;
    }

    @Override
    public <T extends ModelObject, S extends ModelObject> MatrixViewDef clone(Map<String, T> fieldMapIn, Map<String, S> filterMapIn) {

        MatrixViewDef myClone = new MatrixViewDef();

        super.cloneComponents(myClone, fieldMapIn, filterMapIn);

        if (null != getMatrixSettings()) {
            myClone.setMatrixSettings(getMatrixSettings().clone(fieldMapIn));
        }

        return myClone;
    }

	@Override
	public <T extends ModelObject, S extends ModelObject> MatrixViewDef copy(Map<String, T> fieldMapIn,
			Map<String, S> filterMapIn) {
		MatrixViewDef myCopy = new MatrixViewDef();

        super.copyComponents(myCopy, fieldMapIn, filterMapIn);

        if (null != getMatrixSettings()) {
        	myCopy.setMatrixSettings(getMatrixSettings().copy(fieldMapIn));
        }

        return myCopy;
	}

}
