package csi.client.gwt.viz.graph.plunk.edit.widgets;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.Column;
import com.github.gwtbootstrap.client.ui.Row;
import com.google.gwt.dom.client.Style.BorderStyle;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.*;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.icon.IconSelectionEvent;
import csi.client.gwt.icon.IconSelectionHandler;
import csi.client.gwt.icon.IconUtils;
import csi.client.gwt.icon.ui.IconManager;
import csi.client.gwt.vortex.AbstractVortexEventHandler;

/**
 * @author Centrifuge Systems, Inc.
 */
public class IconPicker extends Composite implements TakesValue<String> {

    private static CentrifugeConstants i18n = CentrifugeConstantsLocator.get();
    private final Row mainPanel = new Row();
    private final SimplePanel imageContainer =  new SimplePanel();
    private final SimplePanel iconButtonPanel = new SimplePanel();
    private final Image imagePreview = new Image();
    private final CheckBox iconEnabled = new CheckBox();
    private String iconId = null;

    private static final String CSI_ICON_PREVIEW_STYLE = "csi-icon-preview";
    private static final String CSI_ICON_BORDER_STYLE = "csi-icon-border";

    public IconPicker(){

        initWidget(mainPanel);

        buildUI();
        addHandlers();
        styleComponents();
        showIcon();
    }

    private void styleComponents() {
        imageContainer.addStyleName(CSI_ICON_PREVIEW_STYLE);
        imagePreview.addStyleName(CSI_ICON_BORDER_STYLE);
        imagePreview.addStyleName(CSI_ICON_PREVIEW_STYLE);
        imagePreview.getElement().getStyle().setPadding(0, Unit.PX);
        imagePreview.getElement().getStyle().setMargin(0, Unit.PX);
    }

    @Override
    public void setValue(String value) {
        iconId = value;
        showIcon();
    }

    @Override
    public String getValue() {
        return iconId;
    }

    private void buildUI() {
        Column pickerColumn = new Column(2);
        Column checkBoxColumn = new Column(1);

        mainPanel.add(checkBoxColumn);
        mainPanel.add(pickerColumn);
        
        checkBoxColumn.add(iconEnabled);

        imageContainer.add(imagePreview);
        HorizontalPanel hPanel = new HorizontalPanel();
        hPanel.add(imageContainer);
        hPanel.add(iconButtonPanel);
        pickerColumn.add(hPanel);
        
    }

    private void addHandlers() {
        
        iconButtonPanel.setHeight("24px");
        iconButtonPanel.setWidth("56px");
        iconButtonPanel.getElement().getStyle().setDisplay(Display.BLOCK);
            
        Button iconDialogButton = new Button();
        iconDialogButton.setText(i18n.iconPicker_browseButton());
        iconDialogButton.setHeight("22px");
        iconDialogButton.setWidth("56px");

        iconDialogButton.addClickHandler(new ClickHandler(){

            @Override
            public void onClick(ClickEvent event) {
                
                
                IconManager iconManager = new IconManager(new IconSelectionHandler(){

                    @Override
                    public void onSelect(IconSelectionEvent iconSelectionEvent) {
                        iconId = iconSelectionEvent.getIconUuid();
                        imagePreview.setUrl(iconSelectionEvent.getIconUrl());
                        //nodeStyle.setIconId(iconSelectionEvent.getIconUuid());
                        iconEnabled.setValue(true, true);
                        //updateCurrent();
                    }});
                
                iconManager.show();
            }});
        
        iconButtonPanel.add(iconDialogButton);
        
        imagePreview.setHeight("100%");
        imagePreview.setWidth("100%");
        imagePreview.getElement().getStyle().setBorderStyle(BorderStyle.NONE);

            
        iconEnabled.addValueChangeHandler(new ValueChangeHandler<Boolean>(){

            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                
                if(event.getValue()){
                    imagePreview.getElement().getStyle().setBackgroundColor("#efefef");
                    imagePreview.getElement().getStyle().setOpacity(1);
                } else {
                    imagePreview.getElement().getStyle().setBackgroundColor("#cccccc");
                    imagePreview.getElement().getStyle().setOpacity(.5);
                }
            }});
    }

    private void showIcon() {
        if(iconId != null){
            iconEnabled.setValue(true, false);
            IconUtils.getBase64(iconId, new AbstractVortexEventHandler<String>(){

            @Override
            public void onSuccess(String result) {
                if(result == null || result.isEmpty()){
                    imagePreview.setUrl("");
                } else {
                    imagePreview.setUrl(result);
                }
                
            }});
        }
    }
}
