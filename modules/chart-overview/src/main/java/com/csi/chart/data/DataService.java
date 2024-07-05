package com.csi.chart.data;

import org.jfree.data.category.CategoryDataset;
import org.jfree.data.xy.XYDataset;

import com.csi.chart.dto.*;

public interface DataService
{
    public void sortByValue( boolean flag );

    public CategoryDataset getCategoryData(BaseRequest request) throws Exception;

    public XYDataset getXYData(BaseRequest request) throws Exception;

    public boolean isCategoryChart( );
    public boolean isHeatMap();

    ChartSummary calculateSummaryInfo(DataRequest request) throws Exception;

    Page getData( DataRequest dataRequest ) throws Exception;
    Page getData2D( DataRequest dataRequest ) throws Exception;
}
