package csi.server.business.broadcast;

import static junit.framework.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import csi.server.business.selection.cache.BroadcastResult;
import csi.server.business.selection.cache.SelectionBroadcastCache;
import csi.server.business.selection.storage.AbstractBroadcastStorageService;
import csi.server.common.model.visualization.selection.IntegerRowsSelection;
import csi.server.common.model.visualization.selection.Selection;

/**
 * @author Centrifuge Systems, Inc.
 */
public class SelectionBroadcastCacheTest {

    private SelectionBroadcastCache selectionBroadcastCache;

    @Test
    public void testAddBroadcast(){
        selectionBroadcastCache = new SelectionBroadcastCache(1000, 1, TimeUnit.DAYS);

        IntegerRowsSelection rowsSelection = new IntegerRowsSelection();
        rowsSelection.setSelectedItems(new ArrayList<Integer>(Arrays.asList(5, 10, 15)));

        AbstractBroadcastStorageService.instance().addBroadcast("2", rowsSelection, false);
        BroadcastResult cachedSelectionState = AbstractBroadcastStorageService.instance().getBroadcast("2");

        assertEquals(cachedSelectionState.getBroadcastFilter(), rowsSelection);
    }

    @Test
    public void testAddTwoBroadcasts_SecondOneReplaces(){
        selectionBroadcastCache = new SelectionBroadcastCache(1000, 1, TimeUnit.DAYS);

        AbstractBroadcastStorageService.instance().addBroadcast("2", new IntegerRowsSelection(), false);

        IntegerRowsSelection rowsSelection = new IntegerRowsSelection();
        rowsSelection.setSelectedItems(new ArrayList<Integer>(Arrays.asList(5, 10, 15)));
        AbstractBroadcastStorageService.instance().addBroadcast("2", rowsSelection, true);

        BroadcastResult cachedSelectionState = AbstractBroadcastStorageService.instance().getBroadcast("2");

        assertEquals(cachedSelectionState.getBroadcastFilter(), rowsSelection);
        assertEquals(cachedSelectionState.isExcludeRows(), true);
    }

    @Test
    public void testAddSelection(){
        selectionBroadcastCache = new SelectionBroadcastCache(1000, 1, TimeUnit.DAYS);

        IntegerRowsSelection rowsSelection = new IntegerRowsSelection();
        rowsSelection.setSelectedItems(new ArrayList<Integer>(Arrays.asList(5, 10, 15)));
        selectionBroadcastCache.addSelection("2", rowsSelection);

        Selection selection = selectionBroadcastCache.getSelection("2");

        assertEquals(selection, rowsSelection);
    }
}
