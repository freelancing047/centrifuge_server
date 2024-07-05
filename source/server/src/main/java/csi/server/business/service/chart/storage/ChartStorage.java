package csi.server.business.service.chart.storage;


public interface ChartStorage<R> {

//    public void addCategories(List<C> categories);
//    public void addCategory(C categories);
//    public void addLimit(boolean limit);
//    public void addMeasures(List<List<M>> measures);
//    public void addMeasure(List<M> measure);
    
      public void addResult(R result);
      
      public R getResult();
}
