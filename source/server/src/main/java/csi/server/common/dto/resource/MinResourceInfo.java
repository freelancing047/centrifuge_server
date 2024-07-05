package csi.server.common.dto.resource;

import com.google.gwt.user.client.rpc.IsSerializable;
import csi.server.common.enumerations.AclResourceType;

/**
 * Created by centrifuge on 4/19/2019.
 */
public class MinResourceInfo implements IsSerializable {

    private AclResourceType _type;
    private String _name;
    private String _uuid;
    private String _owner;
    private String _remarks;
    private String _fileName;

    public MinResourceInfo() {

    }

    public MinResourceInfo(MinResourceInfo infoIn) {

        _type = infoIn.getType();
        _name = infoIn.getName();
        _uuid = infoIn.getUuid();
        _owner = infoIn.getOwner();
        _fileName = infoIn.getFileName();
    }

    public MinResourceInfo(AclResourceType typeIn, String fileNameIn, String nameIn, String uuidIn, String ownerIn, String ramarksIn) {

        _type = typeIn;
        _name = nameIn;
        _uuid = uuidIn;
        _owner = ownerIn;
        _remarks = ramarksIn;
        _fileName = fileNameIn;
    }

    public AclResourceType getType() {

        return _type;
    }

    public void setType(AclResourceType typeIn) {

        _type = typeIn;
    }

    public String getFileName() {

        return _fileName;
    }

    public void setFileName(String fileNameIn) {

        _fileName = fileNameIn;
    }

    public String getName() {

        return _name;
    }

    public void setName(String nameIn) {

        _name = nameIn;
    }

    public String getUuid() {

        return _uuid;
    }

    public void setUuid(String uuidIn) {

        _uuid = uuidIn;
    }

    public String getOwner() {

        return _owner;
    }

    public void setOwner(String ownerIn) {

        _owner = ownerIn;
    }

    public String getRemarks() {

        return _remarks;
    }

    public void setRemarks(String ramarksIn) {

        _remarks = ramarksIn;
    }
}
