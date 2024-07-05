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
import javax.persistence.Transient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class LiveRepositoryAsset extends RepositoryAsset {

    private String liveDataViewUuid;

    public String getLiveDataViewUuid() {
        return liveDataViewUuid;
    }

    public void setLiveDataViewUuid(String liveDataViewUuid) {
        this.liveDataViewUuid = liveDataViewUuid;
    }

    @Override
    @Transient
    public AssetTypes getAssetType() {
        return AssetTypes.LIVE;
    }
}
