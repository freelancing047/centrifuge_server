package csi.client.gwt.viz.chart.overview;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import csi.client.gwt.viz.chart.overview.range.RangeCalculator;

/**
 * @author Centrifuge Systems, Inc.
 */
public class RangeUtilCalculateWidthTest {

    @Test(expected = RuntimeException.class)
    public void testNotEnoughSpace(){
        RangeCalculator.calculateWidthOfOverviewContent(51, 50);
    }

    @Test
    public void testZeroCategories(){
        assertEquals(50, RangeCalculator.calculateWidthOfOverviewContent(0, 50));
    }

    @Test
    public void testCalculateWidthWithOddMaxWidth(){
        assertEquals(399, RangeCalculator.calculateWidthOfOverviewContent(1, 399));
        assertEquals(398, RangeCalculator.calculateWidthOfOverviewContent(2, 399));
        assertEquals(390, RangeCalculator.calculateWidthOfOverviewContent(10, 399));
        assertEquals(300, RangeCalculator.calculateWidthOfOverviewContent(100, 399));
    }

    @Test
    public void testCalculateWidthWithDefaultMaxWidth(){
        assertEquals(400, RangeCalculator.calculateWidthOfOverviewContent(1, 400));
        assertEquals(400, RangeCalculator.calculateWidthOfOverviewContent(2, 400));
        assertEquals(400, RangeCalculator.calculateWidthOfOverviewContent(10, 400));
        assertEquals(400, RangeCalculator.calculateWidthOfOverviewContent(100, 400));
    }

    @Test
    public void testCalculateWidthForAnyValue(){
        for(int i = 1; i < 4000; i++){
            int width = RangeCalculator.calculateWidthOfOverviewContent(i, 5000);
            assertEquals(0, width % i);
        }
    }

}
