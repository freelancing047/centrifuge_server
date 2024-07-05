package csi.server.common.util.uploader;

/**
 * Created by centrifuge on 10/5/2015.
 */
public interface CacheLoadedCallBack {

    public void onCacheLoaded(int countIn);

    public void onCacheLoadError(Exception ExceptionIn);
}
