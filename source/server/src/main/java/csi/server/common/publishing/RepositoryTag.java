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
import javax.persistence.GeneratedValue;
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
public class RepositoryTag {

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private String value;

    @ManyToOne
    private RepositoryAsset asset;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public RepositoryAsset getAsset() {
        return asset;
    }

    public void setAsset(RepositoryAsset asset) {
        this.asset = asset;
    }

}
