package com.csi.chart.data;

import java.io.FileNotFoundException;
import java.util.Arrays;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.xy.MatrixSeries;
import org.jfree.data.xy.MatrixSeriesCollection;
import org.jfree.data.xy.XYDataset;

import test.data.LoadData;
import test.data.Tuple;
import test.data.Wrapper;

import com.csi.chart.dto.*;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

public class JsonDataService implements DataService
{
    boolean sample = false;
    boolean sortByValue = false;

    @Override
    public CategoryDataset getCategoryData(BaseRequest request) throws Exception
    {
        Wrapper wrapper = loadData();

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        Tuple[] jsonData = wrapper.data;
        if (sample) {
            System.arraycopy(wrapper.data, 0, jsonData, 0, 30);
        }
        
        if( sortByValue ) {
            Arrays.sort(jsonData, Tuple.ValueAscending );
        }

        for (Tuple t : jsonData) {
            int i = t.categories.size();
            String series = (String) t.categories.get(0);
            String name;
            int value = t.value;

            if (i == 1) {
                name = series;
                series = "unknown";
            } else {
                name = (String) t.categories.get(1);
            }

            dataset.addValue(value, series, name);
        }

        return dataset;
    }

    private Wrapper loadData() throws FileNotFoundException
    {
        LoadData loader = new LoadData();
        Wrapper wrapper = loader.load("data.json");
        return wrapper;
    }

    @Override
    public XYDataset getXYData(BaseRequest request) throws Exception
    {
        Wrapper wrapper = loadData();
        
        Tuple[] data = wrapper.data;
        BiMap<Object, Number> xMap = HashBiMap.create();
        BiMap<Object, Number> yMap = HashBiMap.create();
        
        for( Tuple t : data ) {
            Object x = t.categories.get(0);
            Object y = t.categories.get(1);
            
            if( !xMap.containsKey(x)) {
                xMap.put(x, xMap.size());
            }
            
            if( !yMap.containsKey(y)) {
                yMap.put(y, yMap.size() );
            }
        }
        
        MatrixSeriesCollection dataset = new MatrixSeriesCollection();
        MatrixSeries series = new MatrixSeries("", xMap.size(), yMap.size());
        for( Tuple t: data ) {
            Object ox = t.categories.get(0);
            Object oy = t.categories.get(1);
            
            Number x = xMap.get(ox);
            Number y = yMap.get(oy);
            
            series.update(x.intValue(), y.intValue(), t.value);
        }
        
        dataset.addSeries(series);
        return dataset;
    }

    @Override
    public boolean isCategoryChart( )
    {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public ChartSummary calculateSummaryInfo(DataRequest request) throws Exception
    {
        try {
            Wrapper wrapper = loadData();
            SummaryStatistics stats = new SummaryStatistics();

            for (Tuple t : wrapper.data) {
                stats.addValue(t.value);
            }

            ChartSummary summary = new ChartSummary();
            
//            summary.min = stats.getMin();
//            summary.max = stats.getMax();
//            summary.count = stats.getN();
//            summary.stddev = stats.getStandardDeviation();
            return summary;
            
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return new ChartSummary();
    }

    @Override
    public void sortByValue( boolean flag )
    {
        this.sortByValue = flag;
    }

    @Override
    public boolean isHeatMap()
    {
        // TODO Auto-generated method stub
        return false;
    }

    public Page getData( int page, int pageSize ) throws Exception
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Page getData( DataRequest dataRequest ) throws Exception
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Page getData2D( DataRequest dataRequest ) throws Exception
    {
        // TODO Auto-generated method stub
        return null;
    }

}
