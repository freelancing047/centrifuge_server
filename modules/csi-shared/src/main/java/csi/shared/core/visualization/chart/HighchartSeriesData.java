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
import java.util.ArrayList;
import java.util.List;


/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class HighchartSeriesData implements Serializable {

    private List<Number> data = new ArrayList<Number>();

    public List<Number> getData() {
        return data;
    }

    public void setData(List<Number> data) {
        this.data = data;
    }

    public Number get(int index){
        return data.get(index);
    }
    
    public void remove (int index) {
    	data.remove(index);
    }
}
