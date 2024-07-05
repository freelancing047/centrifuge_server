package csi.security.monitors;

import javax.persistence.PrePersist;
import javax.persistence.PreRemove;

import csi.server.common.publishing.Asset;

public class AssetACLMonitor {
    @PrePersist
    public void assetAdded(Asset asset) {
/*
        Authorization authorization = CsiSecurityManager.getAuthorization();

        ACL myAcl = new ACL(authorization.getName(), asset.getAssetID().toString());
        List<AccessControlEntry> myAclList = new ArrayList<AccessControlEntry>();
        myAclList.add(new AccessControlEntry(AclControlType.READ, authorization.getName()));
        myAclList.add(new AccessControlEntry(AclControlType.DELETE, authorization.getName()));
        myAcl.setEntries(myAclList);

        CsiPersistenceManager.persist(myAcl);

        log.info(String.format("ACL added for asset '%s', uuid %s.  Current owner is '%s'", asset.getName(), myAcl.getUuid(), myAcl.getOwner()));
*/
    }

    @PreRemove
    public void assetRemoved(Asset asset) {
/*
        EntityManager entityManager = CsiPersistenceManager.getMetaEntityManager();

        Query find = entityManager.createQuery("from ACL acl where acl.uuid = :uuid");
        find.setParameter("uuid", asset.getAssetID());

        try {
            List<ACL> list = (List<ACL>) find.getResultList();
            for (ACL acl : list) {
                entityManager.remove(acl);
            }
        } catch (NoResultException nre) {
            // no acl found
        }

        log.info(String.format("Removed ACL for asset %s, uuid %s", asset.getName(), asset.getAssetID().toString()));
*/
    }

}
