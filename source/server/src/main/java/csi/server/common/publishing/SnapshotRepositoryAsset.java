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
package csi.server.common.publishing;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Transient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import csi.shared.core.publish.SnapshotType;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class SnapshotRepositoryAsset extends RepositoryAsset {

    @Enumerated(EnumType.STRING)
    private SnapshotType snapshotType;
    private int snapshotCount;

    @Override
    @Transient
    public AssetTypes getAssetType() {
        return AssetTypes.SNAPSHOT;
    }

    public SnapshotType getSnapshotType() {
        return snapshotType;
    }

    public void setSnapshotType(SnapshotType snapshotType) {
        this.snapshotType = snapshotType;
    }

    public int getSnapshotCount() {
        return snapshotCount;
    }

    public void setSnapshotCount(int snapshotCount) {
        this.snapshotCount = snapshotCount;
    }

}
