package csi.client.gwt.viz.viewer;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.activity.shared.Activity;
import com.google.gwt.activity.shared.ActivityMapper;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.EventBus;
import csi.client.gwt.WebMain;
import csi.client.gwt.util.CSIActivityManager;
import csi.client.gwt.viz.viewer.lens.LensImageViewer;
import csi.client.gwt.viz.viewer.settings.ViewerSettings;
import csi.server.common.dto.CsiMap;
import csi.server.common.model.dataview.DataView;
import csi.server.common.model.visualization.viewer.LensDef;
import csi.server.common.model.visualization.viewer.Objective;

public class ViewerImpl implements Viewer {
    private ViewerPresenter presenter;
    private ViewerContainer container;

    @Override
    public void view(Objective objective) {
        //FIXME:business logic here?
//        if (presenter instanceof ViewerReady) {
        activityManager.setActivity(new ChangeObjective(this, objective));
//        }
    }

    @Override
    public Widget asWidget() {
        return view.asWidget();
    }

    interface ViewerModel {
        Iterable<LensDef> getLenses();

        void save();

        ViewerSettings getSettings();
    }

    interface ViewerView extends IsWidget {
        void setObjective(Objective objective);

        void addLensImageViewer(LensImageViewer lens);

        void removeAllLensImageViewers();
    }

    interface ViewerPresenter extends Activity {
    }

    private CSIActivityManager activityManager;
    private ViewerModel model;
    private ViewerView view;

    public ViewerModel getModel() {
        return model;
    }

    @Override
    public void ready() {
        activityManager.setActivity(new ViewerReady(this));
    }

    @Override
    public void setPresenter(ViewerPresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void loading() {
        activityManager.setActivity(new LoadingViewer(this));
    }

    @Override
    public ViewerContainer getContainer() {
        return container;
    }

    @Override
    public void save() {
        model.save();
    }

    public ViewerView getView() {
        return view;
    }

    public ViewerImpl(ViewerContainer container) {
        this.container = container;

        EventBus eventBus = new SimpleEventBus();
        ActivityMapper activityMapper = new ViewerActivityMapper(this);
        activityManager = new CSIActivityManager(activityMapper, eventBus);
        activityManager.setActivity(new InitializeViewer(this));

        //FIXME: Need to pass in settings?
        DataView dataView = WebMain.injector.getMainPresenter().getDataViewPresenter(true).getDataView();
//        CsiMap<String, String> dvprops = dataView.getMeta().getModelDef().getClientProperties();
//        String viewerString = dvprops.get("viewer");
        ViewerSettings settings;
            settings = new ViewerSettings();
/*
        if (viewerString == null) {
            dvprops.put("viewer", settings.saveString());
        }
        else {
            settings = new ViewerSettings(viewerString);
        }
*/

        model = new ViewerModelImpl(settings);
        view = new ViewerViewImpl(this);

    }

    private class ViewerReady extends AbstractActivity implements ViewerPresenter {
        private ViewerImpl viewer;

        public ViewerReady(ViewerImpl viewer) {
            this.viewer = viewer;
        }

        @Override
        public void start(AcceptsOneWidget panel, com.google.gwt.event.shared.EventBus eventBus) {
            viewer.setPresenter(this);
        }
    }
}
