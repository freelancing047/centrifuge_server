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

import java.util.ArrayList;
import java.util.List;

/**
 * Snapshot of visualization, worksheet or data view (for the later two cases, this is simply a collection of 
 * visualization snapshots). 
 * @author Centrifuge Systems, Inc.
 */
@SuppressWarnings("serial")
public class SnapshotPublishRequest extends PublishRequest {

    private List<String> imageData = new ArrayList<String>();
    private SnapshotType snapshotType;

    public List<String> getImageData() {
        return imageData;
    }

    public void setImageData(List<String> imageData) {
        this.imageData = imageData;
    }

    public SnapshotType getSnapshotType() {
        return snapshotType;
    }

    public void setSnapshotType(SnapshotType snapshotType) {
        this.snapshotType = snapshotType;
    }

}
