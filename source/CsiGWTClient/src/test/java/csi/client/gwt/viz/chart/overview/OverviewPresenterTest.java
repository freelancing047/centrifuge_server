package csi.client.gwt.viz.chart.overview;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;

import csi.client.gwt.viz.chart.overview.range.Range;
import csi.client.gwt.viz.chart.overview.range.RangeChangedEvent;
import csi.client.gwt.viz.chart.overview.range.RangeChangedEventHandler;

/**
 * @author Centrifuge Systems, Inc.
 */
public class OverviewPresenterTest {

    private static final int OVERVIEW_WIDTH = 36;
    private OverviewPresenter overviewPresenter;
    private boolean rangeChangedFired;

    @Before
    public void setup(){
        overviewPresenter = new OverviewPresenter(new FakeOverviewView());
        rangeChangedFired = false;
        overviewPresenter.addRangeChangedEventHandler(new RangeChangedEventHandler() {
            @Override
            public void onRangeChanged(RangeChangedEvent event) {
                rangeChangedFired = true;
            }
        });
    }

    @Test
    public void setRangeWithNoCategories(){
        assertTrue(overviewPresenter.getCategoryRange().isEmpty());
        overviewPresenter.setRange(new Range(1,5));
        assertTrue(overviewPresenter.getCategoryRange().isEmpty());
        assertFalse(rangeChangedFired);
    }

    @Test
    public void setCategoryData(){
        setSixCategoriesOnPresenter();

        assertEquals(OVERVIEW_WIDTH, overviewPresenter.getWidth());
        assertEquals(0, overviewPresenter.getCategoryRange().getStartIndex());
        assertEquals(5, overviewPresenter.getCategoryRange().getEndIndex());
    }

    @Test
    public void setInvalidRange() {
        setSixCategoriesOnPresenter();
        overviewPresenter.setRange(new Range(-1, 8));
        assertEquals(0, overviewPresenter.getCategoryRange().getStartIndex());
        assertEquals(5, overviewPresenter.getCategoryRange().getEndIndex());
        assertFalse(rangeChangedFired);
    }

    @Test
    public void setValidRange(){
        setSixCategoriesOnPresenter();

        overviewPresenter.setRange(new Range(2,3));
        assertEquals(2, overviewPresenter.getCategoryRange().getStartIndex());
        assertEquals(3, overviewPresenter.getCategoryRange().getEndIndex());
        assertTrue(rangeChangedFired);
    }

    @Test
    public void reset(){
        setSixCategoriesOnPresenter();

        overviewPresenter.setRange(new Range(2,3));
        overviewPresenter.reset();

        assertEquals(0, overviewPresenter.getCategoryRange().getStartIndex());
        assertEquals(5, overviewPresenter.getCategoryRange().getEndIndex());
        assertTrue(rangeChangedFired);
    }

    @Test
    public void resetWithNoRangeChange(){
        setSixCategoriesOnPresenter();
        overviewPresenter.reset();

        assertEquals(0, overviewPresenter.getCategoryRange().getStartIndex());
        assertEquals(5, overviewPresenter.getCategoryRange().getEndIndex());
        assertFalse(rangeChangedFired);
    }

    @Test
    public void updateCategoryData(){
        setSixCategoriesOnPresenter();
        overviewPresenter.setRange(new Range(2,3));

        List<String> values = Lists.newArrayList("1", "2");
        //overviewPresenter.setCategoryData(values, OVERVIEW_WIDTH, rangeChangedFired);

        assertEquals(0, overviewPresenter.getCategoryRange().getStartIndex());
        assertEquals(1, overviewPresenter.getCategoryRange().getEndIndex());

    }

    @Test
    public void resizeWidth(){
        setSixCategoriesOnPresenter();
        overviewPresenter.resizeWidth(OVERVIEW_WIDTH*2, rangeChangedFired);

        assertEquals(0, overviewPresenter.getCategoryRange().getStartIndex());
        assertEquals(5, overviewPresenter.getCategoryRange().getEndIndex());
        assertFalse(rangeChangedFired);
    }

    @Test
    public void resizeWidthWithRange(){
        setSixCategoriesOnPresenter();
        overviewPresenter.setRange(new Range(2,3));
        overviewPresenter.resizeWidth(OVERVIEW_WIDTH*2, rangeChangedFired);

        assertEquals(2, overviewPresenter.getCategoryRange().getStartIndex());
        assertEquals(3, overviewPresenter.getCategoryRange().getEndIndex());
        assertTrue(rangeChangedFired);
    }

    @Test
    public void setIndividualBinCount(){
        setSixCategoriesOnPresenter();
        overviewPresenter.setRange(new Range(2,3));
        rangeChangedFired = false;
        overviewPresenter.setIndividualBinCount(2);

        assertEquals(0, overviewPresenter.getCategoryRange().getStartIndex());
        assertEquals(5, overviewPresenter.getCategoryRange().getEndIndex());
        assertTrue(rangeChangedFired);
    }

    private void setSixCategoriesOnPresenter() {
        List<String> values = Lists.newArrayList("1", "2", "3", "4", "5", "6");
        //overviewPresenter.setCategoryData(values, OVERVIEW_WIDTH, rangeChangedFired);
        rangeChangedFired = false;
    }

}
