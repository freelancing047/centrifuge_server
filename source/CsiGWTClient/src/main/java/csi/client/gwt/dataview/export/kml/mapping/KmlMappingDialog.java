package csi.client.gwt.dataview.export.kml.mapping;

import java.util.List;

import com.github.gwtbootstrap.client.ui.ControlGroup;
import com.github.gwtbootstrap.client.ui.Image;
import com.github.gwtbootstrap.client.ui.base.TextBox;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.Widget;

import csi.client.gwt.WebMain;
import csi.client.gwt.widget.boot.Dialog;
import csi.client.gwt.widget.buttons.Button;
import csi.client.gwt.widget.combo_boxes.FieldDefComboBox;
import csi.client.gwt.widget.combo_boxes.StringComboBox;
import csi.config.advanced.KmlExportAdvConfig;
import csi.server.common.dto.ClientStartupInfo;
import csi.server.common.model.FieldDef;
import csi.server.common.model.kml.KmlMapping;
import csi.shared.gwt.dataview.export.kml.mapping.KmlIcon;

/**
 * Created by Patrick on 10/21/2014.
 */
public class KmlMappingDialog {
    private static KmlMappingDialogUiBinder ourUiBinder = GWT.create(KmlMappingDialogUiBinder.class);
    @UiField
    Dialog dialog;
    @UiField
    RadioButton LatLongRadioButton;
    @UiField
    RadioButton AddressRadioButton;
    @UiField
    ControlGroup LatitudeControlGroup;
    @UiField
    ControlGroup LongitudeControlGroup;
    @UiField
    ControlGroup AddressControlGroup;
    @UiField
    Button multiDetails;
    @UiField
    RadioButton iconFieldRadioButton;
    @UiField
    RadioButton iconFixedRadioButton;
    @UiField
    FieldDefComboBox startTimeFDCB;
    @UiField
    FieldDefComboBox endTimeFDCB;
    @UiField
    FieldDefComboBox durationFDCB;
    @UiField
    FieldDefComboBox iconFDCB;
    @UiField
    StringComboBox iconListBox;
    @UiField
    FieldDefComboBox detailsFDCB;
    @UiField
    FieldDefComboBox addressFDCB;
    @UiField
    FieldDefComboBox labelFDCB;
    @UiField
    FieldDefComboBox longFDCB;
    @UiField
    FieldDefComboBox latFDCB;
    @UiField
    Button multiLabel;
    @UiField
    Button multiAddress;
    @UiField
    TextBox nameTextBox;
    @UiField
    Image iconImage;
    private KmlMappingPresenter presenter;
    private KmlMappingEditor kmlMappingEditor;
    private MultiFieldDef multipleFieldDef;
    private BiMap<String, KmlIcon> listToKmlIconMap = HashBiMap.create();

    public KmlMappingDialog(KmlMappingEditor kmlMappingEditor) {
        this.kmlMappingEditor = kmlMappingEditor;
        ourUiBinder.createAndBindUi(this);
        initializeMultiFieldDef();
        initializeFDCBs();
        initializeDialog();
        initializeIconList();
    }

    @UiHandler("iconListBox")
    public void onIconListBox(SelectionEvent<String> event) {
        presenter.setIcon(listToKmlIconMap.get(event.getSelectedItem()));
    }

    private void initializeIconList() {
        iconListBox.setWidth(200);
        ClientStartupInfo clientStartupInfo = WebMain.getClientStartupInfo();
        KmlExportAdvConfig kmlExportAdvConfig = clientStartupInfo.getKmlExportAdvConfig();
        List<KmlIcon> availableIcons = kmlExportAdvConfig.getAvailableIcons();
        iconListBox.getStore().add(""); //$NON-NLS-1$
        for (KmlIcon availableIcon : availableIcons) {
            listToKmlIconMap.put(availableIcon.getName(), availableIcon);
            iconListBox.getStore().add(availableIcon.getName());
        }
    }

    private void initializeDialog() {
        Button cancelButton = dialog.getCancelButton();
        cancelButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                getPresenter().cancel();
            }
        });
        cancelButton.setVisible(false);
        dialog.getActionButton().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                getPresenter().save();
            }
        });
    }

    private void initializeFDCBs() {
        latFDCB.getStore().addAll(kmlMappingEditor.getDataviewFieldDefs());
        latFDCB.setSelectedIndex(0);

        longFDCB.getStore().addAll(kmlMappingEditor.getDataviewFieldDefs());
        longFDCB.setSelectedIndex(0);

        addressFDCB.getStore().addAll(kmlMappingEditor.getDataviewFieldDefs());
        addressFDCB.setSelectedIndex(0);

        detailsFDCB.getStore().addAll(kmlMappingEditor.getDataviewFieldDefs());
        detailsFDCB.setSelectedIndex(0);

        durationFDCB.getStore().addAll(kmlMappingEditor.getDataviewFieldDefs());
        durationFDCB.setSelectedIndex(0);

        iconFDCB.getStore().addAll(kmlMappingEditor.getDataviewFieldDefs());
        iconFDCB.setSelectedIndex(0);

        startTimeFDCB.getStore().addAll(kmlMappingEditor.getDataviewFieldDefs());
        startTimeFDCB.setSelectedIndex(0);

        endTimeFDCB.getStore().addAll(kmlMappingEditor.getDataviewFieldDefs());
        endTimeFDCB.setSelectedIndex(0);

        labelFDCB.getStore().addAll(kmlMappingEditor.getDataviewFieldDefs());
        labelFDCB.setSelectedIndex(0);


    }

    @UiHandler("latFDCB")
    public void onlatFieldDef(ValueChangeEvent<FieldDef> event) {
        presenter.setLatFieldDef(latFDCB.getValue());
    }

    @UiHandler("longFDCB")
    public void onlongFieldDef(ValueChangeEvent<FieldDef> event) {
        presenter.setLongFieldDef(longFDCB.getValue());
    }

    @UiHandler("addressFDCB")
    public void onaddressFieldDef(ValueChangeEvent<FieldDef> event) {
        presenter.setAddressFieldDef(addressFDCB.getValue());
    }

    @UiHandler("durationFDCB")
    public void ondurationFieldDef(ValueChangeEvent<FieldDef> event) {
        presenter.setDurationFieldDef(durationFDCB.getValue());
    }

    @UiHandler("detailsFDCB")
    public void onDetailsFieldDef(ValueChangeEvent<FieldDef> event) {
        presenter.setDetailsFieldDef(detailsFDCB.getValue());
    }

    @UiHandler("iconFDCB")
    public void oniconFieldDef(ValueChangeEvent<FieldDef> event) {
        presenter.setIconFieldDef(iconFDCB.getValue());
    }

    @UiHandler("startTimeFDCB")
    public void onstartTimeFieldDef(ValueChangeEvent<FieldDef> event) {
        presenter.setStartTimeFieldDef(startTimeFDCB.getValue());
    }

    @UiHandler("endTimeFDCB")
    public void onendTimeFieldDef(ValueChangeEvent<FieldDef> event) {
        presenter.setEndTimeFieldDef(endTimeFDCB.getValue());
    }

    @UiHandler("labelFDCB")
    public void onlabelFieldDef(ValueChangeEvent<FieldDef> event) {
        presenter.setLabelFieldDef(labelFDCB.getValue());
    }

    private void initializeMultiFieldDef() {
        multipleFieldDef = new MultiFieldDef();
        multipleFieldDef.setFieldName("Multiple"); //$NON-NLS-1$
    }

    public void show() {
        dialog.show();
    }

    @UiHandler("multiDetails")
    public void onMultiDetails(ClickEvent event) {
        presenter.useMultipleDetailFields();
    }

    @UiHandler("multiAddress")
    public void onMultiAddress(ClickEvent event) {
        presenter.useMultipleAddressFields();
    }

    @UiHandler("multiLabel")
    public void onMultiLabel(ClickEvent event) {
        presenter.useMultipleLabelFields();
    }

    @UiHandler("AddressRadioButton")
    public void onAddressRadioButton(ClickEvent event) {
        presenter.setLocationType(KmlMapping.LocationType.Address);
    }

    @UiHandler("LatLongRadioButton")
    public void onLatLongRadioButton(ClickEvent event) {
        presenter.setLocationType(KmlMapping.LocationType.LatLong);

    }

    public void setLocationType(KmlMapping.LocationType type) {
        switch (type) {
            case Address:
                AddressRadioButton.setValue(true);
                AddressControlGroup.setVisible(true);
                LatitudeControlGroup.setVisible(false);
                LongitudeControlGroup.setVisible(false);
                break;
            case LatLong:
                LatLongRadioButton.setValue(true);
                AddressControlGroup.setVisible(false);
                LatitudeControlGroup.setVisible(true);
                LongitudeControlGroup.setVisible(true);
                break;
        }
    }

    public void setPresenter(KmlMappingPresenterImpl presenter) {
        this.presenter = presenter;
    }

    public KmlMappingPresenter getPresenter() {
        return presenter;
    }

    public void setPresenter(KmlMappingPresenter presenter) {
        this.presenter = presenter;
    }

    public void setIconMode(KmlMapping.IconMode iconMode) {
        switch (iconMode) {
            case FIXED:
                iconFixedRadioButton.setValue(true);
                iconFDCB.setVisible(false);
                iconListBox.setVisible(true);
                iconImage.setVisible(true);
                break;
            case FIELD:
                iconFieldRadioButton.setValue(true);
                iconFDCB.setVisible(true);
                iconListBox.setVisible(false);
                iconImage.setVisible(false);
                break;
        }
    }

    public FieldDef getMultipleFieldDef() {
        return multipleFieldDef;
    }

    public void setDetailsFieldDef(FieldDef fieldDef) {
        detailsFDCB.setValue(fieldDef);
    }

    public void setLatFieldDef(FieldDef latFieldDef) {
        latFDCB.setValue(latFieldDef);
    }

    public void setLongFieldDef(FieldDef longFieldDef) {
        longFDCB.setValue(longFieldDef);
    }

    public void setDurationFieldDef(FieldDef durationFieldDef) {
        durationFDCB.setValue(durationFieldDef);
    }

    public void setAddressFieldDef(FieldDef addressFieldDef) {
        addressFDCB.setValue(addressFieldDef);
    }

    public void setIconFieldDef(FieldDef iconFieldDef) {
        iconFDCB.setValue(iconFieldDef);
    }


    public void setStartTimeFieldDef(FieldDef startFieldDef) {
        startTimeFDCB.setValue(startFieldDef);
    }

    public void setEndTimeFieldDef(FieldDef endFieldDef) {
        endTimeFDCB.setValue(endFieldDef);
    }

    public void setLabelFieldDef(FieldDef labelFieldDef) {
        labelFDCB.setValue(labelFieldDef);
    }

    public void hide() {
        dialog.hide();
    }

    public void setName(String name) {
        nameTextBox.setValue(name);
    }

    @UiHandler("nameTextBox")
    public void onNameTextBox(ValueChangeEvent<String> event) {
        presenter.setName(event.getValue());
    }

    @UiHandler("iconFixedRadioButton")
    public void onIconFixed(ClickEvent event) {
        presenter.setIconMode(KmlMapping.IconMode.FIXED);
    }

    @UiHandler("iconFieldRadioButton")
    public void onIconField(ClickEvent event) {
        presenter.setIconMode(KmlMapping.IconMode.FIELD);
    }


    public void setIcon(KmlIcon icon) {
        if (icon != null && icon.getURL() != null) {
            String name = listToKmlIconMap.inverse().get(icon);
            if (name != null) {
                iconListBox.setValue(name, false);
                iconImage.setUrl(icon.getURL());
                iconImage.setVisible(true);
            }
        }
        else {
            iconImage.setVisible(false);
            iconListBox.setSelectedIndex(0);
        }
    }

    interface KmlMappingDialogUiBinder extends UiBinder<Widget, KmlMappingDialog> {
    }

    private class MultiFieldDef extends FieldDef {

    }
}
