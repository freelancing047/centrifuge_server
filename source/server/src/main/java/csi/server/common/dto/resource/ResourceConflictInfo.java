package csi.server.common.dto.resource;

import csi.security.CsiSecurityManager;
import csi.security.jaas.JAASRole;
import csi.server.common.enumerations.AclResourceType;
import csi.server.common.enumerations.ConflictResolution;

/**
 * Created by centrifuge on 4/19/2019.
 */
public class ResourceConflictInfo extends MinResourceInfo {

    String _conflictId;
    String _conflictName;
    String _conflictOwner;
    boolean _authorized;

    public static ResourceConflictInfo createIconEntry() {

        ResourceConflictInfo myInfo = new ResourceConflictInfo();

        myInfo.setType(AclResourceType.ICON);
        myInfo.setName("All Icons");
        myInfo.setOwner(JAASRole.ADMIN_GROUP_NAME);
        return myInfo;
    }

    public static ResourceConflictInfo createMapEntry() {

        ResourceConflictInfo myInfo = new ResourceConflictInfo();

        myInfo.setType(AclResourceType.MAP_BASEMAP);
        return myInfo;
    }

    public ResourceConflictInfo() {

    }

    public ResourceConflictInfo(MinResourceInfo baseInfoIn) {

        super(baseInfoIn);
    }

    public ResourceConflictInfo(MinResourceInfo baseInfoIn, String conflictIdIn, String conflictNameIn,
                                String conflictOwnerIn, boolean authorizedIn) {

        super(baseInfoIn);

        _conflictId = conflictIdIn;
        _conflictName = conflictNameIn;
        _conflictOwner = conflictOwnerIn;
        _authorized = authorizedIn;
    }

    public String getConflictId() {

        return _conflictId;
    }

    public void setConflictId(String conflictIdeIn) {

        _conflictId = conflictIdeIn;
    }

    public String getConflictName() {

        return _conflictName;
    }

    public void setConflictName(String conflictNameIn) {

        _conflictName = conflictNameIn;
    }

    public String getConflictOwner() {

        return _conflictOwner;
    }

    public void setConflictOwner(String conflictOwnerIn) {

        _conflictOwner = conflictOwnerIn;
    }

    public boolean getAuthorized() {

        return _authorized;
    }

    public void setAuthorized(boolean authorizedIn) {

        _authorized = authorizedIn;
    }
}
