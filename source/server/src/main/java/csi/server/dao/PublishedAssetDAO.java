package csi.server.dao;

import java.util.List;

import csi.server.common.publishing.Asset;

public interface PublishedAssetDAO extends GenericDAO<Asset, Long> {

    List<Asset> findByName(String name);

    List<Asset> findByUser(String user);

}
