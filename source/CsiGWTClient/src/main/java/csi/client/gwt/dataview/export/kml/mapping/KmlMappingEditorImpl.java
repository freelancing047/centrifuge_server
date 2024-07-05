package csi.client.gwt.dataview.export.kml.mapping;

import java.util.Collection;
import java.util.List;

import com.google.common.collect.Lists;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.SimpleEventBus;

import csi.client.gwt.dataview.export.kml.KmlExport;
import csi.client.gwt.util.CSIActivityManager;
import csi.client.gwt.viz.graph.node.settings.NodeSettingsActivityMapper;
import csi.server.common.model.FieldDef;
import csi.server.common.model.kml.KmlMapping;

/**
 * Created by Patrick on 10/21/2014.
 */
public class KmlMappingEditorImpl implements KmlMappingEditor {
    private final KmlMappingDialog view;
    private final KmlMapping model;
    private final EventBus eventBus;
    private final CSIActivityManager activityManager;
    private KmlExport kmlExport;
    private List<KmlMappingSaveHandler> saveHandlers = Lists.newArrayList();

    public KmlMappingEditorImpl(KmlExport kmlExport) {
        this(kmlExport,new KmlMapping());
    }

    public KmlMappingEditorImpl(KmlExport kmlExport, KmlMapping kmlMapping) {
        this.kmlExport = kmlExport;

        view = new KmlMappingDialog(this);
        model = kmlMapping;

        eventBus = new SimpleEventBus();
        activityManager = new CSIActivityManager(new NodeSettingsActivityMapper(), eventBus);
        activityManager.setActivity(new KmlMappingPresenterImpl(this));
    }

    @Override
    public Collection<FieldDef> getDataviewFieldDefs() {
        return kmlExport.getDataviewFieldDefs();
    }

    @Override
    public void show() {
        view.show();
    }

    @Override
    public void save() {
        for (KmlMappingSaveHandler saveHandler : saveHandlers) {
            saveHandler.onSave(getModel());
        }
    }

    @Override
    public void cancel() {

    }

    @Override
    public void addSaveHandler(KmlMappingSaveHandler handler) {
        saveHandlers.add(handler);
    }

    public KmlMapping getModel() {
        return model;
    }

    public KmlMappingDialog getView() {
        return view;
    }

}
