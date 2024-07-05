package csi.client.gwt.file_access.uploader.zip;

import java.util.Map;

import csi.server.common.util.uploader.zip.CsiZipEntry;

/**
 * Created by centrifuge on 10/5/2015.
 */
public interface ZipDirectoryCompleteCallBack {

    public void onDirectoryComplete(Map<String, CsiZipEntry> mapIn);

    public void onDirectoryCompleteError(Exception ExceptionIn);
}
