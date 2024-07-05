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
package csi.shared.core.visualization.chart;

import java.io.Serializable;
import java.util.List;

/**
 * Chart request.
 * @author Centrifuge Systems, Inc.
 */
public class HighchartRequest implements Serializable {

    private List<String> drillDimensions;
    private String dvUuid;
    private String vizUuid;

    public List<String> getDrillDimensions() {
        return drillDimensions;
    }

    public void setDrillDimensions(List<String> dimensions) {
        this.drillDimensions = dimensions;
    }

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
