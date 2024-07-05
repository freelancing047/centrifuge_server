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

import csi.shared.core.imaging.ImagingRequest;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
@SuppressWarnings("serial")
public class SnapshotImagingRequest implements Serializable {

    private List<ImagingRequest> imagingRequests = new ArrayList<ImagingRequest>();

    public List<ImagingRequest> getImagingRequests() {
        return imagingRequests;
    }

    public void setImagingRequests(List<ImagingRequest> imagingRequests) {
        this.imagingRequests = imagingRequests;
    }

}
