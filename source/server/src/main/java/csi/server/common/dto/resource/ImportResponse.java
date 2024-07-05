package csi.server.common.dto.resource;

import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

import csi.server.common.dto.SelectionListData.ResourceBasics;
import csi.server.common.enumerations.AclResourceType;

/**
 * Created by centrifuge on 5/19/2016.
 */
public class ImportResponse implements IsSerializable {

    String _uuid;
    String _name;
    String _owner;
    String _message;
    AclResourceType _type;
    boolean _error;

    public ImportResponse() {

    }

    public ImportResponse(String uuidIn, String nameIn, String ownerIn, AclResourceType typeIn, String messageIn, boolean errorIn) {

        _uuid = uuidIn;
        _name = nameIn;
        _type = typeIn;
        _owner = ownerIn;
        _message = messageIn;
        _error = errorIn;
    }

    public ImportResponse(String uuidIn, String nameIn, String ownerIn, AclResourceType typeIn, String messageIn) {

        _uuid = uuidIn;
        _name = nameIn;
        _owner = ownerIn;
        _type = typeIn;
        _message = messageIn;
        _error = false;
    }

    public ImportResponse(String uuidIn, String nameIn, String ownerIn, AclResourceType typeIn) {

        _uuid = uuidIn;
        _name = nameIn;
        _type = typeIn;
        _message = null;
        _error = false;
    }

    public void setUuid(String uuidIn) {

        _uuid = uuidIn;
    }

    public String getUuid() {

        return _uuid;
    }

    public void setName(String nameIn) {

        _name = nameIn;
    }

    public String getName() {

        return _name;
    }

    public void setOwner(String ownerIn) {

        _owner = ownerIn;
    }

    public String getOwner() {

        return _owner;
    }

    public void setMessage(String messageIn) {

        _message = messageIn;
    }

    public String getMessage() {

        return _message;
    }

    public void setError(boolean errorIn) {

        _error = errorIn;
    }

    public boolean getError() {

        return _error;
    }

    public void setType(AclResourceType typeIn) {

        _type = typeIn;
    }

    public AclResourceType getType() {

        return _type;
    }
}
