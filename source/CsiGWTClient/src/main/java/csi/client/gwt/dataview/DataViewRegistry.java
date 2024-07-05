package csi.client.gwt.dataview;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;

import csi.server.common.model.dataview.DataView;

public class DataViewRegistry {

    private static DataViewRegistry instance;
    private Map<String, AbstractDataViewPresenter> dataViewPresentersByUuid = new HashMap<String, AbstractDataViewPresenter>();
    private Map<String, AbstractDataViewPresenter> dataViewPresentersByVisualizationUuid = new HashMap<String, AbstractDataViewPresenter>();

    private DataViewRegistry() {
    }

    public static DataViewRegistry getInstance() {
        if (instance == null) {
            instance = new DataViewRegistry();
        }
        return instance;
    }

    public void associatePresenterWithDataView(String dataViewUuid, AbstractDataViewPresenter presenter) {
        dataViewPresentersByUuid.put(dataViewUuid, presenter);
    }

    public void associateVisualizationWithDataView(String visualizationUuid, AbstractDataViewPresenter presenter) {
        dataViewPresentersByVisualizationUuid.put(visualizationUuid, presenter);
    }

    public AbstractDataViewPresenter dataViewPresenterForVisualization(String visualizationUuid) {
        return dataViewPresentersByVisualizationUuid.get(visualizationUuid);
    }
    
    public AbstractDataViewPresenter dataViewPresenterForDataView(String dataViewUuid) {
        return dataViewPresentersByUuid.get(dataViewUuid);
    }

    public DataView getDataViewByUuid(String uuid) {
        return dataViewPresentersByUuid.get(uuid).getDataView();
    }

    public List<AbstractDataViewPresenter> getAllDataviews(){
        return Lists.newArrayList(dataViewPresentersByUuid.values());
    }

}
