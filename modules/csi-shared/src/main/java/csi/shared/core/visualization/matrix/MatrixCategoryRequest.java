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
package csi.shared.core.visualization.matrix;

import java.io.Serializable;

/**
 *
         *  Basic object containting two strings for ids
         * @author Centrifuge Systems, Inc.
        */
@SuppressWarnings("serial")
public class MatrixCategoryRequest implements Serializable {

    private String dvUuid;
    private String vizUuid;

    public String getDvUuid() {
        return dvUuid;
    }

    public void setDvUuid(String dvUuid) {
        this.dvUuid = dvUuid;
    }

    public String getVizUuid() {
        return vizUuid;
    }

    public void setVizUuid(String vizUuid) {
        this.vizUuid = vizUuid;
    }

}
