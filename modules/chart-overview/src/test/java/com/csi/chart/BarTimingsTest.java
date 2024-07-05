package com.csi.chart;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.RGBImageFilter;
import java.io.BufferedWriter;
import java.io.File;
import java.nio.charset.Charset;
import java.util.Random;

import javax.imageio.ImageIO;

import org.apache.commons.math3.random.RandomData;
import org.apache.commons.math3.random.RandomDataImpl;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.google.common.base.Stopwatch;
import com.google.common.io.Files;
import com.jhlabs.image.ImageUtils;

@Ignore
public class BarTimingsTest
{
    int Loops = 5;
    RandomData random;
    Random jdkRandom;
    Stopwatch timer;

    int width = 800;
    int height = 100;

    double[] generatedData;

    @Before
    public void buildData()
    {
        timer = new Stopwatch();
        Loops = 5;

        jdkRandom = new Random();
        random = new RandomDataImpl();
        // ensure we get repeated data in 1k blocks
        ((RandomDataImpl) random).reSeed(1000);
        generatedData = null;

    }

    @Test
    public void test_10k() throws Exception
    {
        String name = "Test 10k";
        logTestStart(name);
        int dataSize = 10000;
        doTestWithTimings(dataSize);

    }

    @Test
    public void test_100k() throws Exception
    {
        String name = "Test 100k";
        logTestStart(name);

        int dataSize = 100000;
        doTestWithTimings(dataSize);
    }

    @Test
    public void test_1m() throws Exception
    {
        String name = "Test 1M";
        logTestStart(name);

        // flipping timing to single pass since this is large.
        Loops = 2;

        int dataSize = 1000000;
        doTestWithTimings(dataSize);
    }

    @Test
    @Ignore
    public void test_10m() throws Exception
    {
        String name = "Test 10M";
        Loops = 1;
        logTestStart(name);
        int dataSize = 10000000;
        doTestWithTimings(dataSize);
    }

    private void logTestStart( String name )
    {
        System.out.println();
        System.out.println("\t" + name);
        System.out.println();
    }

    private void doTestWithTimings( int dataSize ) throws Exception
    {
        SummaryStatistics createData = new SummaryStatistics();
        SummaryStatistics createChart = new SummaryStatistics();
        SummaryStatistics renderImage = new SummaryStatistics();
        SummaryStatistics total = new SummaryStatistics();

        long sum = 0;

        double[] snap = new double[Loops];

        BufferedImage lastImage = null;
        for (int i = 0; i < Loops; i++, sum = 0) {

            long start = System.currentTimeMillis();
            timer.start();
            CategoryDataset dataset = generateDataSet(timer, dataSize);
            timer.stop();

            sum += timer.elapsedMillis();
            createData.addValue(timer.elapsedMillis());

            timer.start();
            JFreeChart chart = createChart(dataset);
            timer.stop();

            sum += timer.elapsedMillis();
            createChart.addValue(timer.elapsedMillis());

            timer.start();
            Image image = TestUtils.renderImage(chart, width, height);
            timer.stop();
            lastImage = (BufferedImage) image;

            sum += timer.elapsedMillis();
            renderImage.addValue(timer.elapsedMillis());

            total.addValue(sum);

            timer.reset();
        }

        String fileName = String.format("bar-%1d", dataSize);
        ImageIO.write(lastImage, "png", new File(fileName + ".png"));
        
        BufferedImage withClip = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        RGBImageFilter filter = new RGBImageFilter() {
            
            @Override
            public int filterRGB( int x, int y, int rgb )
            {
                if( rgb == 0 ) {
                    return rgb;
                } 
                
                int update = (0xFF << 24) | rgb;
                return update;
            }
        };
        
        FilteredImageSource source = new FilteredImageSource(lastImage.getSource(), filter);
        BufferedImage noAlpha = ImageUtils.createImage(source);
        
        
        
//        Graphics2D wcg2d = withClip.createGraphics();
//        int[] data = lastImage.getRGB(0, 0, width, height, null, 0, width);
//        for( int i=0; i < data.length; i++ ) {
//            if( data[i] != 0 ) {
//                data[i] = data[i] | 0xff000000;
//            }
//        }
        
        ImageIO.write(noAlpha, "png", new File(fileName+"-clip.png"));
        

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
        BufferedWriter writer = Files.newWriter(new File(fileName + ".txt"), Charset.defaultCharset());
        writer.write(report.toString());
        writer.close();

    }

    private CategoryDataset generateDataSet( Stopwatch timer, int n )
    {
        DefaultCategoryDataset ds = new DefaultCategoryDataset();

        if (generatedData == null) {
            timer.stop();
            generatedData = new double[n];
            int y = 0;
            double factor = 1.0d;
            for (int i = 0; i < n; i++, y++) {
                if (y >= 5000) {
                    y = 0;
                    factor = 3.0d;
                }
                // generatedData[i] = random.nextUniform(0, 100);
                double val;
                val = generatedData[i] = random.nextUniform(0, 100);
                // double val = Math.floor(jdkRandom.nextDouble() * 100.d);
                if (factor > 2.0d) {
                    while (val < 180.d) {
                        val = val * factor;
                    }
                    generatedData[i] = val;
                } else {
                    generatedData[i] = val;
                    // generatedData[i] = 60.0d;
                }
                factor = 1.0d;
            }
            timer.start();
        }

        String series = "1d";

        int y = 0;
        for (int i = 0; i < n; i++, y++) {
            // double value = random.nextUniform(0, 100);
            ds.setValue(generatedData[i], series, Integer.toString(i));
        }

        return ds;
    }

    private JFreeChart createChart( CategoryDataset dataset ) throws Exception
    {
        ChartBuilder builder = new ChartBuilder();
        builder.withChartType("bar").withDataset(dataset);
        JFreeChart chart = builder.build();
        return chart;
    }

}
