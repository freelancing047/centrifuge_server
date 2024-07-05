package csi.client.gwt.viz.table;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.sencha.gxt.core.client.IdentityValueProvider;
import com.sencha.gxt.data.shared.loader.BeforeLoadEvent;
import com.sencha.gxt.data.shared.loader.FilterPagingLoadConfig;
import com.sencha.gxt.data.shared.loader.LoadEvent;
import com.sencha.gxt.data.shared.loader.LoadHandler;
import com.sencha.gxt.data.shared.loader.PagingLoadResult;
import com.sencha.gxt.data.shared.loader.PagingLoader;

import csi.client.gwt.widget.gxt.grid.CsiCheckboxSelectionModel;

/**
 * Contains selection across pages.
 * @author Centrifuge Systems, Inc.
 */
public class TableCheckboxSelectionModel extends CsiCheckboxSelectionModel<Map<String, String>> {

    private static final String INTERNAL_ID = "internalID";

    private Set<String> possibleIdsOnThisPage = new HashSet<String>();
    private Set<String> allSelectedIds = new HashSet<String>();

    private PagingLoader<FilterPagingLoadConfig, PagingLoadResult<Map<String, String>>> pagingLoader;

    public TableCheckboxSelectionModel(IdentityValueProvider<Map<String, String>> identity, PagingLoader<FilterPagingLoadConfig, PagingLoadResult<Map<String, String>>> loader) {
        super(identity);
        this.pagingLoader = loader;

        addPagingHandler();
    }

    private void addPagingHandler() {
        pagingLoader.addBeforeLoadHandler(new BeforeLoadEvent.BeforeLoadHandler<FilterPagingLoadConfig>() {
            @Override
            public void onBeforeLoad(BeforeLoadEvent<FilterPagingLoadConfig> event) {
                getAllSelectedIds();
            }
        });

        pagingLoader.addLoadHandler(new LoadHandler<FilterPagingLoadConfig, PagingLoadResult<Map<String, String>>>() {
            @Override
            public void onLoad(LoadEvent<FilterPagingLoadConfig, PagingLoadResult<Map<String, String>>> event) {
                updatePossibleIds(event);
                applySelectionOnThisPage(event);
            }
        });
    }

    private void updatePossibleIds(LoadEvent<FilterPagingLoadConfig, PagingLoadResult<Map<String, String>>> event) {
        possibleIdsOnThisPage.clear();
        possibleIdsOnThisPage.addAll(getIdsFromData(event.getLoadResult().getData()));
    }

    private void applySelectionOnThisPage(LoadEvent<FilterPagingLoadConfig, PagingLoadResult<Map<String, String>>> event) {
        List<Map<String,String>> selectedRows = getSelectedRows(event.getLoadResult().getData());
        setSelection(selectedRows);
    }

    private List<Map<String, String>> getSelectedRows(List<Map<String, String>> data) {
        List<Map<String, String>> selectedRows = new ArrayList<Map<String, String>>();
        for (Map<String, String> row : data) {
            if(allSelectedIds.contains(row.get(INTERNAL_ID))){
                selectedRows.add(row);
            }
        }
        return selectedRows;
    }

    public void setSelectionByIds(Set<Integer> newSelection) {
        allSelectedIds.clear();
        for(Integer selected: newSelection){
            allSelectedIds.add(selected.toString());
        }
        List<Map<String,String>> selectedRows = getSelectedRows(getGrid().getStore().getAll());
        setSelection(selectedRows);
    }

    public Set<String> getAllSelectedIds() {
        allSelectedIds.removeAll(possibleIdsOnThisPage);
        List<String> selectedIdsOnThisPage = getIdsFromData(getSelection());
        allSelectedIds.addAll(selectedIdsOnThisPage);
        return allSelectedIds;
    }

    private static List<String> getIdsFromData(List<Map<String,String>> data){
        List<String> ids = new ArrayList<String>();
        for(Map<String,String> row : data){
            ids.add(row.get(INTERNAL_ID));
        }
        return ids;
    }

    public boolean hasSelection() {
        return allSelectedIds.size() > 0;
    }
}
