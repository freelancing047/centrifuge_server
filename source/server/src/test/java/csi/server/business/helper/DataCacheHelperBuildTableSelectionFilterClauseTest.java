package csi.server.business.helper;

import static org.junit.Assert.assertEquals;

import java.util.Set;

import org.junit.Test;

import com.google.common.collect.Lists;
import com.google.inject.Provider;

import csi.server.business.cachedb.DataSyncListener;
import csi.server.common.model.dataview.DataView;
import csi.server.common.model.visualization.selection.IntegerRowsSelection;

/**
 * @author Centrifuge Systems, Inc.
 */
public class DataCacheHelperBuildTableSelectionFilterClauseTest {

    @Test(expected = NullPointerException.class)
    public void testNull(){
        DataCacheHelper dataCacheHelper = createDataCacheHelper();
        //dataCacheHelper.buildTableSelectionFilterClause(null, false, 1);
    }

    @Test
    public void testEmptySelection(){
        DataCacheHelper dataCacheHelper = createDataCacheHelper();
        String filterClause = dataCacheHelper.buildTableSelectionFilterClause(new IntegerRowsSelection(), false, new DataView());

        assertEquals(filterClause, "TRUE = TRUE");
    }

    @Test
    public void testSingleSelection(){
        DataCacheHelper dataCacheHelper = createDataCacheHelper();
        IntegerRowsSelection rowsSelection = new IntegerRowsSelection();
        rowsSelection.setSelectedItems(Lists.newArrayList(1));

        String filterClause = dataCacheHelper.buildTableSelectionFilterClause(rowsSelection, false, new DataView());

        assertEquals(filterClause, "\"internal_id\" IN (1)");
    }

    @Test
    public void testSingleSelectionExclude(){
        DataCacheHelper dataCacheHelper = createDataCacheHelper();
        IntegerRowsSelection rowsSelection = new IntegerRowsSelection();
        rowsSelection.setSelectedItems(Lists.newArrayList(1));

        String filterClause = dataCacheHelper.buildTableSelectionFilterClause(rowsSelection, true, new DataView());

        assertEquals(filterClause, "\"internal_id\" NOT IN (1)");
    }

    @Test
    public void testMultipleSelection(){
        DataCacheHelper dataCacheHelper = createDataCacheHelper();
        IntegerRowsSelection rowsSelection = new IntegerRowsSelection();
        rowsSelection.setSelectedItems(Lists.newArrayList(1, 5, 10));

        String filterClause = dataCacheHelper.buildTableSelectionFilterClause(rowsSelection, false, new DataView());

        assertEquals(filterClause, "\"internal_id\" IN (1,5,10)");
    }

    private DataCacheHelper createDataCacheHelper() {
        DataCacheHelper.ListenerProvider = new Provider<Set<DataSyncListener>>() {
            @Override
            public Set<DataSyncListener> get() {
                return null;
            }
        };
        return new DataCacheHelper();
    }

}
