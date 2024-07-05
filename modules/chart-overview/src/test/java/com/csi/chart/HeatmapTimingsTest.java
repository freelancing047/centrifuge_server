    package com.csi.chart;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.nio.charset.Charset;

import javax.imageio.ImageIO;

import org.apache.commons.math3.random.RandomData;
import org.apache.commons.math3.random.RandomDataImpl;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.MatrixSeries;
import org.jfree.data.xy.MatrixSeriesCollection;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.google.common.base.Stopwatch;
import com.google.common.io.Files;

@Ignore
public class HeatmapTimingsTest
{

    int width = 600;
    int height = 600;

    private RandomData random;
    private int Loops = 5;
    
    private Stopwatch timer;

    @Before
    public void initialize()
    {
        timer = new Stopwatch();
        random = new RandomDataImpl();
        ((RandomDataImpl) random).reSeed(1000);

    }

    @Test
    public void test_100x100() throws Exception
    {
        logTestStart("100x100");
        
        MatrixSeriesCollection dataset = new MatrixSeriesCollection();
        dataset = generateDataSet(100,100);
        
        doTestWithTimings(dataset);
    }

    
    @Test
    public void test_400x400() throws Exception
    {
        logTestStart("400x400");
        MatrixSeriesCollection dataset = new MatrixSeriesCollection();
        dataset = generateDataSet(400,400);
        
        doTestWithTimings(dataset);


    }

    @Test
    public void test_1000x1000() throws Exception
    {
        logTestStart("1000x1000");
        Loops = 1;
        MatrixSeriesCollection dataset = new MatrixSeriesCollection();
        dataset = generateDataSet(1000,1000);
        
        doTestWithTimings(dataset);
    }

    @Test
    public void test_1250x1250() throws Exception
    {
        logTestStart("1250x1250");
        Loops = 1;
        MatrixSeriesCollection dataset = new MatrixSeriesCollection();
        dataset = generateDataSet(1250,1250);
        
        doTestWithTimings(dataset);
    }
    
    @Test
    public void test_5000x5000() throws Exception
    {
        logTestStart("5000x5000");
        Loops = 1;
        MatrixSeriesCollection dataset = new MatrixSeriesCollection();
        dataset = generateDataSet(5000,5000);
        
        doTestWithTimings(dataset);
        
    }
    
    private MatrixSeriesCollection generateDataSet( int rows, int cols )
    {
        MatrixSeriesCollection dataset = new MatrixSeriesCollection();
        MatrixSeries series = new MatrixSeries("empty", rows, cols);
        
        int samples = (int) (rows*cols*0.1);
        for( int s=0; s < samples; s++ ) {
            double val = random.nextUniform(0, 100, true);
            int x = random.nextInt(0, rows-1);
            int y = random.nextInt(0, cols-1);
            series.update(x,y,val);
        }
        
//        for( int i=0; i < rows; i++ ){
//            for( int j = 0; j < cols; j++ ) {
//                double val = random.nextUniform(0, 50, true);
//                series.update(i, j, val);
//            }
//        }
        
        dataset.addSeries(series);
        return dataset;
    }

    
    private void logTestStart( String name )
    {
        System.out.println();
        System.out.println("\t" + name);
        System.out.println();
    }

    
    protected void doTestWithTimings(MatrixSeriesCollection dataset) throws Exception {
        SummaryStatistics createData = new SummaryStatistics();
        SummaryStatistics createChart = new SummaryStatistics();
        SummaryStatistics renderImage = new SummaryStatistics();
        SummaryStatistics total = new SummaryStatistics();
        long sum = 0;
        
        BufferedImage lastImage = null;
        
        for( int i=0; i < Loops; i++ ) {
            
            sum = 0;
            
            timer.start();
            JFreeChart chart = createChart(dataset);
            timer.stop();

            sum += timer.elapsedMillis();
            createChart.addValue(timer.elapsedMillis());

            timer.start();
            Image image = TestUtils.renderImage(chart, width, height);
            lastImage = (BufferedImage) image;
            timer.stop();

            sum += timer.elapsedMillis();
            renderImage.addValue(timer.elapsedMillis());

            total.addValue(sum);

            timer.reset();
            
        }
        
        MatrixSeries series = dataset.getSeries(0);
        int rows = series.getRowCount();
        int cols = series.getColumnsCount();
        String fileName = String.format("heatmap-%1dx%1d", rows, cols);
        
        ImageIO.write(lastImage, "png", new File(fileName+".png"));
        
        
        StringBuffer report = new StringBuffer();
        
        
        report.append("Statistics for " + Loops + " runs:\n");
        report.append("=====================\n");
        report.append("   Create Data \n");
        report.append("=====================\n");
        report.append(createData);
        report.append("\n");
        report.append("=====================\n");
        report.append("   Create Chart\n");
        report.append("=====================\n");
        report.append(createChart);
        report.append("\n");
        report.append("=====================\n");
        report.append("   Render Image\n");
        report.append("====================\n");
        report.append(renderImage);
        report.append("\n");
        report.append("=====================\n");
        report.append("   Totals\n");
        report.append("=====================\n");
        report.append(total);
        
        System.out.println(report.toString());
        BufferedWriter writer = Files.newWriter(new File(fileName+".txt"), Charset.defaultCharset());
        writer.write(report.toString());
        writer.close();

    }

    private JFreeChart createChart( MatrixSeriesCollection dataset ) throws Exception
    {
        ChartBuilder builder = new ChartBuilder();
        builder.withChartType("bubble").withDataset(dataset);
        JFreeChart chart = builder.build();
        return chart;
    }

}
