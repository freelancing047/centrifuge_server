package csi.client.gwt.viz.viewer;

import com.google.common.collect.Lists;
import csi.client.gwt.WebMain;
import csi.client.gwt.viz.viewer.settings.ViewerSettings;
import csi.server.common.dto.CsiMap;
import csi.server.common.model.dataview.DataView;
import csi.server.common.model.visualization.viewer.LensDef;

import java.util.Collections;

public class ViewerModelImpl implements ViewerImpl.ViewerModel {
    private final ViewerSettings settings;

    public ViewerModelImpl(ViewerSettings settings) {
        this.settings = settings;
    }

    public ViewerSettings getSettings() {
        return settings;
    }


    @Override
    public Iterable<LensDef> getLenses() {
        //FIXME:
        return Collections.unmodifiableList(Lists.newArrayList());
    }

    @Override
    public void save() {
        //TODO:
/*        DataView dataView = WebMain.injector.getMainPresenter().getDataViewPresenter(true).getDataView();

        CsiMap<String, String> dvprops = dataView.getMeta().getModelDef().getClientProperties();

        dvprops.put("viewer", settings.saveString());*/


    }


}
