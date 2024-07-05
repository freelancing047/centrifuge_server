package csi.server.business.service.matrix.storage;

public interface MatrixCacheStorage<R> {

    public void setResult(R result);

    public R getResult();


}
