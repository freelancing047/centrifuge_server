package csi.server.common.dto;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Created by centrifuge on 11/17/2016.
 */
public class AccessRights implements IsSerializable {

    private boolean _isOwner = false;
    private boolean _canRead = false;
    private boolean _canWrite = false;
    private boolean _canDelete = false;

    public void setIsOwner(boolean isOwnerIn) {

        _isOwner = isOwnerIn;
    }

    public boolean getIsOwner() {

        return _isOwner;
    }

    public void setCanRead(boolean canReadIn) {

        _canRead = canReadIn;
    }

    public boolean getCanRead() {

        return _canRead;
    }

    public void setCanWrite(boolean canWriteIn) {

        _canWrite = canWriteIn;
    }

    public boolean getCanWrite() {

        return _canWrite;
    }

    public void setCanDelete(boolean canDeleteIn) {

        _canDelete = canDeleteIn;
    }

    public boolean getCanDelete() {

        return _canDelete;
    }
}
