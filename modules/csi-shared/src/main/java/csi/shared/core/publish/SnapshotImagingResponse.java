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
package csi.shared.core.publish;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
@SuppressWarnings("serial")
public class SnapshotImagingResponse implements Serializable {

    private List<String> imageData = new ArrayList<String>();

    public List<String> getImageData() {
        return imageData;
    }

    public void setImageData(List<String> imageData) {
        this.imageData = imageData;
    }

}
