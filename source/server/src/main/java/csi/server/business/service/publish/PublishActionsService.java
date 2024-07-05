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
package csi.server.business.service.publish;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import com.google.common.base.Throwables;

import csi.security.CsiSecurityManager;
import csi.server.business.helper.DataViewHelper;
import csi.server.business.service.export.png.PNGImageCreator;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.model.dataview.DataView;
import csi.server.common.publishing.LiveRepositoryAsset;
import csi.server.common.publishing.RepositoryAsset;
import csi.server.common.publishing.RepositoryData;
import csi.server.common.publishing.RepositoryTag;
import csi.server.common.publishing.SnapshotRepositoryAsset;
import csi.server.common.service.api.PublishingActionsServiceProtocol;
import csi.server.dao.CsiPersistenceManager;
import csi.server.util.ImageUtil;
import csi.shared.core.imaging.ImagingRequest;
import csi.shared.core.publish.LivePublishRequest;
import csi.shared.core.publish.PublishRequest;
import csi.shared.core.publish.SnapshotImagingRequest;
import csi.shared.core.publish.SnapshotImagingResponse;
import csi.shared.core.publish.SnapshotPublishRequest;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class PublishActionsService implements PublishingActionsServiceProtocol {

    @Override
    public SnapshotImagingResponse getImages(SnapshotImagingRequest request) {
        SnapshotImagingResponse response = new SnapshotImagingResponse();
        PNGImageCreator pngImageCreator = new PNGImageCreator();
        for (ImagingRequest ir : request.getImagingRequests()) {
            BufferedImage image = pngImageCreator.createImage(ir);
            response.getImageData().add(ImageUtil.toBase64String(image));
        }
        return response;
    }

    @Override
    public void publish(PublishRequest request) {
        if (request instanceof SnapshotPublishRequest) {
            publishSnapshot((SnapshotPublishRequest) request);
        } else {
            publishLive((LivePublishRequest) request);
        }
    }

    private void publishSnapshot(SnapshotPublishRequest request) {
        SnapshotRepositoryAsset asset = new SnapshotRepositoryAsset();
        populate(asset, request);

        asset.setSnapshotType(request.getSnapshotType());
        asset.setSnapshotCount(request.getImageData().size());
        List<RepositoryData> dataList = new ArrayList<RepositoryData>();
        for (String imageData : request.getImageData()) {
            RepositoryData rd = new RepositoryData();
            rd.setId(UUID.randomUUID().toString());
            rd.setAsset(asset);
            rd.setImageData(imageData);
            dataList.add(rd);
        }

        CsiPersistenceManager.persist(asset);
        CsiPersistenceManager.persist(dataList);
    }

    private void publishLive(LivePublishRequest request) {
        LiveRepositoryAsset asset = new LiveRepositoryAsset();
        populate(asset, request);
        try {
            DataView liveAsset = new DataViewHelper().createLiveAsset(request.getDataViewUuid(), 0);
            asset.setLiveDataViewUuid(liveAsset.getUuid());
        } catch (CentrifugeException e) {
            throw Throwables.propagate(e);
        }
        CsiPersistenceManager.persist(asset);
    }

    private void populate(RepositoryAsset asset, PublishRequest request) {
        DataView dataView = CsiPersistenceManager.findObject(DataView.class, request.getDataViewUuid());
        asset.setId(UUID.randomUUID().toString());
        asset.setCreatedBy(CsiSecurityManager.getUserName());
        asset.setCreationTime(new Date());
        asset.setDataViewUuid(dataView.getUuid());
        asset.setDataViewName(dataView.getName());
        asset.setName(request.getName());
        asset.setDescription(request.getDescription());

        // Tags.
        for (String tag : request.getTags()) {
            RepositoryTag rt = new RepositoryTag();
            rt.setAsset(asset);
            rt.setValue(tag);
            asset.getTags().add(rt);
        }
    }
}
