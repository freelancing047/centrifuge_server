package csi.client.gwt.viz.chart.overview;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * @author Centrifuge Systems, Inc.
 */
public class OverviewStateTest {

    @Test
    public void initializedTest(){
        OverviewState overviewState = new OverviewState(16);

        assertEquals(0, overviewState.getStartPosition());
        assertEquals(16, overviewState.getEndPosition());
        assertEquals(16, overviewState.getWidth());
    }

    @Test
    public void zoomInTest() {
        OverviewState overviewState = new OverviewState(16);

        overviewState.zoomIn();

        assertEquals(3, overviewState.getStartPosition());
        assertEquals(13, overviewState.getEndPosition());
        assertEquals(16, overviewState.getWidth());
    }

    @Test
    public void zoomAllTheWayIn() {
        OverviewState overviewState = new OverviewState(16);

        for(int i = 0 ; i < 10; i++)
            overviewState.zoomIn();

        assertEquals(8, overviewState.getStartPosition());
        assertEquals(9, overviewState.getEndPosition());
        assertEquals(16, overviewState.getWidth());
    }

    @Test
    public void zoomOutTest() {
        OverviewState overviewState = new OverviewState(16);

        overviewState.zoomOut();

        assertEquals(0, overviewState.getStartPosition());
        assertEquals(16, overviewState.getEndPosition());
        assertEquals(16, overviewState.getWidth());
    }

    @Test
    public void zoomOutWhenZoomedIn() {
        OverviewState overviewState = new OverviewState(16);

        for(int i = 0 ; i < 10; i++)
            overviewState.zoomIn();

        overviewState.zoomOut();
        assertEquals(7, overviewState.getStartPosition());
        assertEquals(9, overviewState.getEndPosition());
        assertEquals(16, overviewState.getWidth());
    }

    @Test
    public void scaleWidthTest() {
        OverviewState overviewState = new OverviewState(16);

        overviewState.scaleToNewWidth(32);

        assertEquals(0, overviewState.getStartPosition());
        assertEquals(32, overviewState.getEndPosition());
        assertEquals(32, overviewState.getWidth());
    }

    @Test
    public void panWhenUnableTest() {
        OverviewState overviewState = new OverviewState(16);

        overviewState.pan(1);
        assertEquals(0, overviewState.getStartPosition());
        assertEquals(16, overviewState.getEndPosition());

        overviewState.pan(-1);
        assertEquals(0, overviewState.getStartPosition());
        assertEquals(16, overviewState.getEndPosition());

    }

    @Test
    public void panTest() {
        OverviewState overviewState = new OverviewState(16);

        //Zoom in a few times so we can pan
        for(int i = 0 ; i < 3; i++){
            overviewState.zoomIn();
        }

        assertEquals(6, overviewState.getStartPosition());
        assertEquals(10, overviewState.getEndPosition());

        overviewState.pan(4);
        assertEquals(10, overviewState.getStartPosition());
        assertEquals(14, overviewState.getEndPosition());

        overviewState.pan(-8);
        assertEquals(2, overviewState.getStartPosition());
        assertEquals(6, overviewState.getEndPosition());

        overviewState.pan(11);
        assertEquals(12, overviewState.getStartPosition());
        assertEquals(16, overviewState.getEndPosition());

        overviewState.pan(-13);
        assertEquals(0, overviewState.getStartPosition());
        assertEquals(4, overviewState.getEndPosition());
    }

    @Test
    public void centerTest(){
        OverviewState overviewState = new OverviewState(16);

        overviewState.center(12);

        assertEquals(4, overviewState.getStartPosition());
        assertEquals(16, overviewState.getEndPosition());

        overviewState.center(5);

        assertEquals(0, overviewState.getStartPosition());
        assertEquals(11, overviewState.getEndPosition());

    }

    @Test
    public void moveStartTest(){
        OverviewState overviewState = new OverviewState(16);

        overviewState.moveStart(-5);
        assertEquals(0, overviewState.getStartPosition());

        overviewState.moveStart(4);
        assertEquals(4, overviewState.getStartPosition());

        overviewState.moveStart(-5);
        assertEquals(0, overviewState.getStartPosition());
    }

    @Test
    public void moveEndTest(){
        OverviewState overviewState = new OverviewState(16);

        overviewState.moveEnd(4);
        assertEquals(16, overviewState.getEndPosition());

        overviewState.moveEnd(-5);
        assertEquals(11, overviewState.getEndPosition());

        overviewState.moveEnd(6);
        assertEquals(16, overviewState.getEndPosition());

    }

    @Test
    public void resetTest(){
        OverviewState overviewState = new OverviewState(16);

        overviewState.setStartPosition(5);
        overviewState.setEndPosition(6);
        overviewState.reset();

        assertEquals(0, overviewState.getStartPosition());
        assertEquals(16, overviewState.getEndPosition());
        assertEquals(16, overviewState.getWidth());
    }

    @Test
    public void setPositionTest(){
        OverviewState overviewState = new OverviewState(16);

        overviewState.setStartPosition(5);
        overviewState.setEndPosition(6);
        assertEquals(5, overviewState.getStartPosition());
        assertEquals(6, overviewState.getEndPosition());

        overviewState.setStartPosition(-5);
        overviewState.setEndPosition(20);

        assertEquals(0, overviewState.getStartPosition());
        assertEquals(16, overviewState.getEndPosition());
    }
}
