package csi.server.common.util.uploader;

/**
 * Created by centrifuge on 10/5/2015.
 */
public interface BlockLoadedCallBack {

    public void onBlockLoaded(byte[] blockIn, int countIn);

    public void onBlockLoadError(Exception ExceptionIn);
}
