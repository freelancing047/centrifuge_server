package com.csi.chart.data;

public class DataServiceFactory
{
    public DataService getDataService( String chartId ) throws Exception
    {
        // DataService service = new JDBCDataService(chartId);
        // service = new JsonDataService();
        DataService service = new ModelDataService(chartId);
        return service;
    }

}
