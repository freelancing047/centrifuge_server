package csi.server.common.util.uploader.zip;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Created by centrifuge on 8/11/2015.
 */

public class CsiZipEntry implements IsSerializable {

    String _name;
    long _basePointer;
    long _dataPointer;
    long _deflatedSize;
    long _inflatedSize;
    int _version;
    int _dateTime;
    int _method;
    int _crc;

    public CsiZipEntry() {

    }

    public CsiZipEntry(String nameIn, long basePointerIn, long dataPointerIn,
                       long deflatedSizeIn, long inflatedSizeIn, int versionIn,
                       int dateTimeIn, int methodIn, int crcIn) {

        _name = nameIn;
        _basePointer = basePointerIn;
        _dataPointer = dataPointerIn;
        _deflatedSize = deflatedSizeIn;
        _inflatedSize = inflatedSizeIn;
        _version = versionIn;
        _dateTime = dateTimeIn;
        _method = methodIn;
        _crc = crcIn;
    }

    public void setName(String nameIn) {

        _name = nameIn;
    }

    public String getName() {

        return _name;
    }

    public void setBasePointer(long basePointerIn) {

        _basePointer = basePointerIn;
    }

    public long getBasePointer() {

        return _basePointer;
    }

    public void setDataPointer(long dataPointerIn) {

        _dataPointer = dataPointerIn;
    }

    public long getDataPointer() {

        return _dataPointer;
    }

    public void setDeflatedSize(long deflatedSizeIn) {

        _deflatedSize = deflatedSizeIn;
    }

    public long getDeflatedSize() {

        return _deflatedSize;
    }

    public void setInflatedSize(long inflatedSizeIn) {

        _inflatedSize = inflatedSizeIn;
    }

    public long getInflatedSize() {

        return _inflatedSize;
    }

    public void setVersion(int versionIn) {

        _version = versionIn;
    }

    public int getVersion() {

        return _version;
    }

    public void setDateTime(int dateTimeIn) {

        _dateTime = dateTimeIn;
    }

    public int getDateTime() {

        return _dateTime;
    }

    public void setMethod(int methodIn) {

        _method = methodIn;
    }

    public int getMethod() {

        return _method;
    }

    public void setCrc(int crcIn) {

       _crc = crcIn;
    }

    public int getCrc() {

        return _crc;
    }
}
