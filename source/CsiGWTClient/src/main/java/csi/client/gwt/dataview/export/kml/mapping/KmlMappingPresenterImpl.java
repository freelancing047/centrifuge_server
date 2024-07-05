package csi.client.gwt.dataview.export.kml.mapping;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

import csi.client.gwt.dataview.export.kml.mapping.multiselector.DualListBoxDialog;
import csi.server.common.model.FieldDef;
import csi.server.common.model.kml.KmlMapping;
import csi.shared.gwt.dataview.export.kml.mapping.KmlIcon;

/**
 * Created by Patrick on 10/21/2014.
 */
public class KmlMappingPresenterImpl implements KmlMappingPresenter {
    private KmlMappingEditorImpl kmlMapping;

    public KmlMappingPresenterImpl(KmlMappingEditorImpl kmlMapping) {
        this.kmlMapping = kmlMapping;
        initView();

    }

    private void initView() {
        KmlMappingDialog view = kmlMapping.getView();
        KmlMapping model = kmlMapping.getModel();
        view.setName(model.getName());
        view.setLocationType(model.getLocationType());
        view.setLatFieldDef(resolveFieldDefFromList(model.getLatFields()));
        view.setLongFieldDef(resolveFieldDefFromList(model.getLongFields()));
        view.setAddressFieldDef(resolveFieldDefFromList(model.getAddressFields()));
        view.setLabelFieldDef(resolveFieldDefFromList(model.getLabelFields()));
        view.setDetailsFieldDef(resolveFieldDefFromList(model.getDetailFields()));
        view.setStartTimeFieldDef(resolveFieldDefFromList(model.getStartTimeFields()));
        view.setEndTimeFieldDef(resolveFieldDefFromList(model.getEndTimeFields()));
        view.setDurationFieldDef(resolveFieldDefFromList(model.getDurationFields()));
        view.setIconMode(model.getIconMode());
        view.setIcon(model.getIcon());
        view.setIconFieldDef(resolveFieldDefFromList(model.getIconFields()));
    }

    private FieldDef resolveFieldDefFromList(List<FieldDef> fields) {
        if (fields == null) {
            return null;
        }
        if (fields.isEmpty()) {
            return null;
        }
        if (fields.size() > 1) {
            return kmlMapping.getView().getMultipleFieldDef();
        }
        return fields.get(0);
    }

    @Override
    public void setLocationType(KmlMapping.LocationType type) {
        KmlMapping model = kmlMapping.getModel();
        model.setLocationType(type);
        KmlMappingDialog view = kmlMapping.getView();
        view.setLocationType(model.getLocationType());
    }

    @Override
    public void setIconMode(KmlMapping.IconMode mode) {
        KmlMapping model = kmlMapping.getModel();
        model.setIconMode(mode);
        KmlMappingDialog view = kmlMapping.getView();
        view.setIconMode(model.getIconMode());
    }

    @Override
    public void useMultipleDetailFields() {
        final DualListBoxDialog dualListBoxDialog = new DualListBoxDialog();
        dualListBoxDialog.show();
        KmlMapping model = kmlMapping.getModel();
        List<FieldDef> detailFields = model.getDetailFields();
        if (detailFields == null) {
            detailFields = Lists.newArrayList();
        }
        for (FieldDef detailField : detailFields) {
            dualListBoxDialog.addSelectedField(detailField);
        }
        for (FieldDef fieldDef : kmlMapping.getDataviewFieldDefs()) {
            if (!detailFields.contains(fieldDef)) {
                dualListBoxDialog.addAvailableField(fieldDef);
            }
        }
        dualListBoxDialog.getActionButton().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                ArrayList<FieldDef> selectedFieldDefs = Lists.newArrayList(dualListBoxDialog.getSelectedFieldDefs());
                kmlMapping.getModel().setDetailFields(selectedFieldDefs);
                dualListBoxDialog.close();
                KmlMappingDialog view = kmlMapping.getView();
                view.setDetailsFieldDef(resolveFieldDefFromList(kmlMapping.getModel().getDetailFields()));
            }
        });
        dualListBoxDialog.getCancelButton().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {

                dualListBoxDialog.close();
            }
        });
    }

    @Override
    public void setDetailsFieldDef(FieldDef fieldDef) {
        KmlMapping model = kmlMapping.getModel();
        model.setDetailFields(Lists.newArrayList(fieldDef));
        KmlMappingDialog view = kmlMapping.getView();
        List<FieldDef> fields = model.getDetailFields();
        FieldDef modelFieldDef = null;
        if (fields != null && !fields.isEmpty()) {
            modelFieldDef = fields.get(0);
        }
        view.setDetailsFieldDef(modelFieldDef);
    }

    @Override
    public void setLatFieldDef(FieldDef fieldDef) {
        KmlMapping model = kmlMapping.getModel();
        model.setLatFields(Lists.newArrayList(fieldDef));
        KmlMappingDialog view = kmlMapping.getView();
        List<FieldDef> fields = model.getLatFields();
        FieldDef modelFieldDef = null;
        if (fields != null && !fields.isEmpty()) {
            modelFieldDef = fields.get(0);
        }
        view.setLatFieldDef(modelFieldDef);
    }

    @Override
    public void setLongFieldDef(FieldDef fieldDef) {
        KmlMapping model = kmlMapping.getModel();
        model.setLongFields(Lists.newArrayList(fieldDef));
        KmlMappingDialog view = kmlMapping.getView();
        List<FieldDef> fields = model.getLongFields();
        FieldDef modelFieldDef = null;
        if (fields != null && !fields.isEmpty()) {
            modelFieldDef = fields.get(0);
        }
        view.setLongFieldDef(modelFieldDef);

    }

    @Override
    public void setDurationFieldDef(FieldDef fieldDef) {
        KmlMapping model = kmlMapping.getModel();
        model.setDurationFields(Lists.newArrayList(fieldDef));
        KmlMappingDialog view = kmlMapping.getView();
        List<FieldDef> fields = model.getDurationFields();
        FieldDef modelFieldDef = null;
        if (fields != null && !fields.isEmpty()) {
            modelFieldDef = fields.get(0);
        }
        view.setDurationFieldDef(modelFieldDef);
    }

    @Override
    public void setAddressFieldDef(FieldDef fieldDef) {
        KmlMapping model = kmlMapping.getModel();
        model.setAddressFields(Lists.newArrayList(fieldDef));
        KmlMappingDialog view = kmlMapping.getView();
        List<FieldDef> fields = model.getAddressFields();
        FieldDef modelFieldDef = null;
        if (fields != null && !fields.isEmpty()) {
            modelFieldDef = fields.get(0);
        }
        view.setAddressFieldDef(modelFieldDef);
    }

    @Override
    public void setIconFieldDef(FieldDef fieldDef) {
        KmlMapping model = kmlMapping.getModel();
        model.setIconFields(Lists.newArrayList(fieldDef));
        KmlMappingDialog view = kmlMapping.getView();
        List<FieldDef> fields = model.getIconFields();
        FieldDef modelFieldDef = null;
        if (fields != null && !fields.isEmpty()) {
            modelFieldDef = fields.get(0);
        }
        view.setIconFieldDef(modelFieldDef);
    }

    @Override
    public void setStartTimeFieldDef(FieldDef fieldDef) {
        KmlMapping model = kmlMapping.getModel();
        model.setStartTimeFields(Lists.newArrayList(fieldDef));
        KmlMappingDialog view = kmlMapping.getView();
        List<FieldDef> fields = model.getStartTimeFields();
        FieldDef modelFieldDef = null;
        if (fields != null && !fields.isEmpty()) {
            modelFieldDef = fields.get(0);
        }
        view.setStartTimeFieldDef(modelFieldDef);
    }

    @Override
    public void setEndTimeFieldDef(FieldDef fieldDef) {
        KmlMapping model = kmlMapping.getModel();
        model.setEndTimeFields(Lists.newArrayList(fieldDef));
        KmlMappingDialog view = kmlMapping.getView();
        List<FieldDef> fields = model.getEndTimeFields();
        FieldDef modelFieldDef = null;
        if (fields != null && !fields.isEmpty()) {
            modelFieldDef = fields.get(0);
        }
        view.setEndTimeFieldDef(modelFieldDef);
    }

    @Override
    public void setLabelFieldDef(FieldDef fieldDef) {
        KmlMapping model = kmlMapping.getModel();
        model.setLabelFields(Lists.newArrayList(fieldDef));
        KmlMappingDialog view = kmlMapping.getView();
        List<FieldDef> fields = model.getLabelFields();
        FieldDef modelFieldDef = null;
        if (fields != null && !fields.isEmpty()) {
            modelFieldDef = fields.get(0);
        }
        view.setLabelFieldDef(modelFieldDef);
    }

    @Override
    public void useMultipleAddressFields() {
        final DualListBoxDialog dualListBoxDialog = new DualListBoxDialog();
        dualListBoxDialog.show();
        KmlMapping model = kmlMapping.getModel();
        List<FieldDef> addressFields = model.getAddressFields();
        if (addressFields == null) {
            addressFields = Lists.newArrayList();
        }
        for (FieldDef addressField : addressFields) {
            dualListBoxDialog.addSelectedField(addressField);
        }
        for (FieldDef fieldDef : kmlMapping.getDataviewFieldDefs()) {
            if (!addressFields.contains(fieldDef)) {
                dualListBoxDialog.addAvailableField(fieldDef);
            }
        }
        dualListBoxDialog.getActionButton().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                ArrayList<FieldDef> selectedFieldDefs = Lists.newArrayList(dualListBoxDialog.getSelectedFieldDefs());
                kmlMapping.getModel().setAddressFields(selectedFieldDefs);
                dualListBoxDialog.close();
                KmlMappingDialog view = kmlMapping.getView();
                view.setAddressFieldDef(resolveFieldDefFromList(kmlMapping.getModel().getAddressFields()));
            }
        });
        dualListBoxDialog.getCancelButton().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {

                dualListBoxDialog.close();
            }
        });
    }

    @Override
    public void useMultipleLabelFields() {
        final DualListBoxDialog dualListBoxDialog = new DualListBoxDialog();
        dualListBoxDialog.show();
        KmlMapping model = kmlMapping.getModel();
        List<FieldDef> labelFields = model.getLabelFields();
        if (labelFields == null) {
            labelFields = Lists.newArrayList();
        }
        for (FieldDef labelField : labelFields) {
            dualListBoxDialog.addSelectedField(labelField);
        }
        for (FieldDef fieldDef : kmlMapping.getDataviewFieldDefs()) {
            if (!labelFields.contains(fieldDef)) {
                dualListBoxDialog.addAvailableField(fieldDef);
            }
        }
        dualListBoxDialog.getActionButton().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                ArrayList<FieldDef> selectedFieldDefs = Lists.newArrayList(dualListBoxDialog.getSelectedFieldDefs());
                kmlMapping.getModel().setLabelFields(selectedFieldDefs);
                dualListBoxDialog.close();
                KmlMappingDialog view = kmlMapping.getView();
                view.setLabelFieldDef(resolveFieldDefFromList(kmlMapping.getModel().getLabelFields()));
            }
        });
        dualListBoxDialog.getCancelButton().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {

                dualListBoxDialog.close();
            }
        });
    }

    @Override
    public void cancel() {
        kmlMapping.getView().hide();
        kmlMapping.cancel();
    }

    @Override
    public void setName(String name) {
        KmlMapping model = kmlMapping.getModel();
        model.setName(name);
        KmlMappingDialog view = kmlMapping.getView();
        view.setName(model.getName());
    }

    @Override
    public void setIcon(KmlIcon icon) {
        KmlMapping model = kmlMapping.getModel();
        model.setIcon(icon);
        KmlMappingDialog view = kmlMapping.getView();
        view.setIcon(model.getIcon());
    }

    @Override
    public void save() {
        kmlMapping.getView().hide();
        kmlMapping.save();
    }

    @Override
    public String mayStop() {
        return null;
    }

    @Override
    public void onCancel() {

    }

    @Override
    public void onStop() {

    }

    @Override
    public void start(AcceptsOneWidget panel, EventBus eventBus) {
        KmlMappingDialog view = kmlMapping.getView();
        view.setPresenter(this);
    }
}
