package csi.client.gwt.viz.chart.overview;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import csi.client.gwt.viz.chart.overview.range.Range;
import csi.client.gwt.viz.chart.overview.range.RangeCalculator;

/**
 * @author Centrifuge Systems, Inc.
 */
public class RangeUtilCalculateRangeTest {

    @Test(expected = RuntimeException.class)
    public void testNotEnoughSpace(){
        RangeCalculator.calculateRange(new OverviewState(0), 5);
    }

    @Test
    public void testZeroCategories(){
        OverviewState overviewState = new OverviewState(100);
        Range range = RangeCalculator.calculateRange(overviewState, 0);

        assertEquals(-1, range.getStartIndex());
        assertEquals(-1, range.getEndIndex());
    }

    @Test
    public void testCalculateRange(){
        OverviewState overviewState = new OverviewState(12);
        Range range = RangeCalculator.calculateRange(overviewState, 4);

        assertEquals(0, range.getStartIndex());
        assertEquals(3, range.getEndIndex());
    }

    @Test
    public void testCalculateRangeZoomedIn(){
        OverviewState overviewState = new OverviewState(12);
        overviewState.zoomIn();

        assertEquals(2, overviewState.getStartPosition());
        assertEquals(10, overviewState.getEndPosition());

        Range range = RangeCalculator.calculateRange(overviewState, 4);

        assertEquals(0, range.getStartIndex());
        assertEquals(3, range.getEndIndex());
    }

    @Test
    public void testCalculateRangeOnStartBoundary(){
        OverviewState overviewState = new OverviewState(12);
        overviewState.zoomIn();
        overviewState.pan(1);

        assertEquals(3, overviewState.getStartPosition());
        assertEquals(11, overviewState.getEndPosition());

        Range range = RangeCalculator.calculateRange(overviewState, 4);

        assertEquals(1, range.getStartIndex());
        assertEquals(3, range.getEndIndex());
    }

    @Test
    public void testCalculateRangeOnEndBoundary(){
        OverviewState overviewState = new OverviewState(12);
        overviewState.zoomIn();
        overviewState.pan(-1);

        assertEquals(1, overviewState.getStartPosition());
        assertEquals(9, overviewState.getEndPosition());

        Range range = RangeCalculator.calculateRange(overviewState, 4);

        assertEquals(0, range.getStartIndex());
        assertEquals(2, range.getEndIndex());
    }


    @Test
    public void testCalculateRangeZoomedInTwice(){
        OverviewState overviewState = new OverviewState(12);
        overviewState.zoomIn();
        overviewState.zoomIn();

        assertEquals(4, overviewState.getStartPosition());
        assertEquals(8, overviewState.getEndPosition());

        Range range = RangeCalculator.calculateRange(overviewState, 4);

        assertEquals(1, range.getStartIndex());
        assertEquals(2, range.getEndIndex());
    }

    @Test
    public void testOddValueResultsInOverflow(){
        OverviewState overviewState = new OverviewState(9);

        Range range = RangeCalculator.calculateRange(overviewState, 4);

        assertEquals(0, range.getStartIndex());
        assertEquals(4, range.getEndIndex());
    }
}
