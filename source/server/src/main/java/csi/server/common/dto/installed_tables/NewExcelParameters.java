package csi.server.common.dto.installed_tables;

import csi.server.common.util.uploader.zip.CsiZipEntry;

/**
 * Created by centrifuge on 11/13/2015.
 */
public class NewExcelParameters extends TableParameters {

    private CsiZipEntry _entry;
    private int _dataStart = 0;

    public NewExcelParameters() {

    }

    public NewExcelParameters(CsiZipEntry entryIn) {

        _entry = entryIn;
    }

    public void setEntry(CsiZipEntry entryIn) {

        _entry = entryIn;
    }

    public CsiZipEntry getEntry() {

        return _entry;
    }

    public void setDataStart(int dataStartIn) {

        _dataStart = dataStartIn;
    }

    public int getDataStart() {

        return _dataStart;
    }
}
