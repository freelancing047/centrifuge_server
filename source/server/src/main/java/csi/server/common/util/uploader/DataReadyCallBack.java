package csi.server.common.util.uploader;

/**
 * Created by centrifuge on 10/2/2015.
 */
public interface DataReadyCallBack {

    public void onDataReady(byte[] blockIn, int countIn);

    public void onError(Exception ExceptionIn);
}
