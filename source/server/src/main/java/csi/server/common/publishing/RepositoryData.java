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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class RepositoryData {

    @Id
    private String id;

    @Column(columnDefinition = "TEXT")
    private String imageData;

    // Note: We don't provide a asset to data link to not end up fetching all the data when displaying
    // the asset list.
    @ManyToOne
    private RepositoryAsset asset;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getImageData() {
        return imageData;
    }

    public void setImageData(String imageData) {
        this.imageData = imageData;
    }

    public RepositoryAsset getAsset() {
        return asset;
    }

    public void setAsset(RepositoryAsset asset) {
        this.asset = asset;
    }

}
