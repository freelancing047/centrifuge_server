package csi.server.common.dto.resource;

import com.google.gwt.user.client.rpc.IsSerializable;
import csi.server.common.enumerations.ConflictResolution;

import java.util.List;

/**
 * Created by centrifuge on 4/16/2019.
 */
public class ImportRequest implements IsSerializable {

    private String _fileHandle;
    private List<ImportItem> _importList;
    boolean _rawXml;

    public ImportRequest() {

    }

    public ImportRequest(String fileHandleIn, List<ImportItem> importListIn, boolean rawXmlIn) {

        _fileHandle = fileHandleIn;
        _importList = importListIn;
        _rawXml = rawXmlIn;
    }

    public void setFileHandle(String fileHandleIn) {

        _fileHandle = fileHandleIn;
    }

    public String getFileHandle() {

        return _fileHandle;
    }

    public void setImportList(List<ImportItem> importListIn) {

        _importList = importListIn;
    }

    public List<ImportItem> getImportList() {

        return _importList;
    }

    public void setRawXml(boolean rawXmlIn) {

        _rawXml = rawXmlIn;
    }

    public boolean getRawXml() {

        return _rawXml;
    }
}
