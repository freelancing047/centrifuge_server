package csi.server.common.dto.installed_tables;

import com.google.gwt.user.client.rpc.IsSerializable;

import csi.server.common.enumerations.ServerResponse;
import csi.server.common.model.tables.InstalledTable;

import java.util.List;

/**
 * Created by centrifuge on 12/2/2015.
 */
public class TableInstallResponse implements IsSerializable {

    private InstalledTable _table;
    private ServerResponse _status;
    private String _message;

    public TableInstallResponse(InstalledTable tableIn) {

        _status = ServerResponse.SUCCESS;
        _message = null;
        _table = tableIn;
    }

    public TableInstallResponse(ServerResponse statusIn, String messageIn, InstalledTable tableIn) {

        _status = statusIn;
        _message = messageIn;
        _table = tableIn;
    }

    public TableInstallResponse(ServerResponse statusIn) {

        _status = statusIn;
        _message = null;
        _table = null;
    }

    public TableInstallResponse(ServerResponse statusIn, String messageIn) {

        _status = statusIn;
        _message = messageIn;
        _table = null;
    }

    public TableInstallResponse(ServerResponse statusIn, InstalledTable tableIn) {

        _status = statusIn;
        _message = null;
        _table = tableIn;
    }

    public TableInstallResponse() {

    }

    public void setTable(InstalledTable tableIn) {

        _table = tableIn;
    }

    public InstalledTable getTable() {

        return _table;
    }

    public void setStatus(ServerResponse statusIn) {

        _status = statusIn;
    }

    public ServerResponse getStatus() {

        return _status;
    }

    public void setMessage(String messageIn) {

        _message = messageIn;
    }

    public String getMessage() {

        return _message;
    }
}
