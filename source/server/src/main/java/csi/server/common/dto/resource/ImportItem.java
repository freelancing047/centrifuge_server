package csi.server.common.dto.resource;

import com.google.gwt.user.client.rpc.IsSerializable;
import csi.server.common.enumerations.AclResourceType;
import csi.server.common.enumerations.ConflictResolution;

/**
 * Created by centrifuge on 4/29/2019.
 */
public class ImportItem implements IsSerializable {

    private AclResourceType _type;
    private String _name;
    private String _remarks;
    private String _owner;
    private String _uuid;
    private String _file;
    private ConflictResolution _control;

    public ImportItem() {

    }

    public ImportItem(AclResourceType typeIn, String nameIn, String remarksIn, String ownerIn,
                      String uuidIn, String fileIn, ConflictResolution controlIn) {

        _type = typeIn;
        _name = nameIn;
        _remarks = remarksIn;
        _owner = ownerIn;
        _uuid = uuidIn;
        _file = fileIn;
        _control = controlIn;
    }

    public void setType(AclResourceType typeIn) {

        _type = typeIn;
    }

    public AclResourceType getType() {

        return _type;
    }

    public void setName(String nameIn) {

        _name = nameIn;
    }

    public String getName() {

        return _name;
    }

    public void setRemarks(String remarksIn) {

        _remarks = remarksIn;
    }

    public String getRemarks() {

        return _remarks;
    }

    public void setOwner(String ownerIn) {

        _owner = ownerIn;
    }

    public String getOwner() {

        return _owner;
    }

    public void setUuid(String uuidIn) {

        _uuid = uuidIn;
    }

    public String getUuid() {

        return _uuid;
    }

    public void setFile(String fileIn) {

        _file = fileIn;
    }

    public String getFile() {

        return _file;
    }

    public void setControl(ConflictResolution controlIn) {

        _control = controlIn;
    }

    public ConflictResolution getControl() {

        return _control;
    }
}
