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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import csi.security.monitors.RepositoryAssetACLMonitor;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@EntityListeners(RepositoryAssetACLMonitor.class)
public abstract class RepositoryAsset {

    @Id
    private String id;

    private String name;
    private String dataViewUuid;
    private String dataViewName;

    private String createdBy;
    private Date creationTime;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String thumbnail;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "asset")
	@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
    private List<RepositoryComment> comments = new ArrayList<RepositoryComment>();

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "asset")
	@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
    private List<RepositoryTag> tags = new ArrayList<RepositoryTag>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDataViewUuid() {
        return dataViewUuid;
    }

    public void setDataViewUuid(String dataViewUuid) {
        this.dataViewUuid = dataViewUuid;
    }

    public String getDataViewName() {
        return dataViewName;
    }

    public void setDataViewName(String dataViewName) {
        this.dataViewName = dataViewName;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Date getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public List<RepositoryComment> getComments() {
        return comments;
    }

    public void setComments(List<RepositoryComment> comments) {
        this.comments = comments;
    }

    public List<RepositoryTag> getTags() {
        return tags;
    }

    public void setTags(List<RepositoryTag> tags) {
        this.tags = tags;
    }

    @Transient
    public abstract AssetTypes getAssetType();
}
