package csi.client.gwt.viz.timeline.settings;

import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Widget;

import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.validation.feedback.StringValidationFeedback;
import csi.client.gwt.validation.feedback.ValidationFeedback;
import csi.client.gwt.validation.multi.MultiValidatorCollectingErrors;
import csi.client.gwt.validation.multi.ValidationAndFeedbackPair;
import csi.client.gwt.validation.validator.Validator;
import csi.client.gwt.widget.boot.CsiHeading;
import csi.client.gwt.widget.boot.Dialog;
import csi.client.gwt.widget.boot.Modal;
import csi.client.gwt.widget.boot.MultiNotificationPopup;
import csi.client.gwt.widget.buttons.Button;
import csi.client.gwt.widget.combo_boxes.FieldDefComboBox;
import csi.server.common.enumerations.CsiDataType;
import csi.server.common.model.FieldDef;
import csi.server.common.model.visualization.timeline.TimelineEventDefinition;

public class EventDialog {
    interface SpecificUiBinder extends UiBinder<Widget, EventDialog> {
    }

    private static SpecificUiBinder uiBinder = GWT.create(SpecificUiBinder.class);
    
    @UiField
    Dialog dialog;
    
    @UiField
    FieldDefComboBox startEventField;    
    @UiField
    FieldDefComboBox endEventField;    
    @UiField
    FieldDefComboBox labelField;



    protected static final CentrifugeConstants i18n = CentrifugeConstantsLocator.get();

    protected MultiValidatorCollectingErrors validator = new MultiValidatorCollectingErrors();
    
    private TimelineSettingsPresenter presenter;

    private TimelineEventDefinition eventDefinition;
    

    public EventDialog(TimelineSettingsPresenter presenter) {
        this.presenter = presenter;
        uiBinder.createAndBindUi(this);
        initializeDialog();
        initializeValidation();
    }

    private void initializeValidation() {
        Validator dateFieldValidator = new Validator() {
            @Override
            public boolean isValid() {
                return endEventField.getValue() != null || startEventField.getValue() != null;
            }
        };
        
        //Determines at least one field is a Date
        Validator timeFieldValidator = new Validator() {
            @Override
            public boolean isValid() {
                
              FieldDef endField = endEventField.getValue();
              FieldDef startField = startEventField.getValue();
              
              if(endField != null && endField.getValueType() == CsiDataType.Time){
                  if(startField == null || startField.getValueType() == CsiDataType.Time){
                      return false;
                  }
              }
              
              if(startField != null && startField.getValueType() == CsiDataType.Time){
                  if(endField == null || endField.getValueType() == CsiDataType.Time){
                      return false;
                  }
              }
                
                return true;
            }
        };
        
        ValidationFeedback dateFieldFeedback = new StringValidationFeedback(i18n.timelineValidationMinimum()); //$NON-NLS-1$
        ValidationFeedback timeFieldFeedback = new StringValidationFeedback(i18n.timelineValidationTime()); //$NON-NLS-1$

        validator.addValidationAndFeedback(new ValidationAndFeedbackPair(dateFieldValidator, dateFieldFeedback));
        validator.addValidationAndFeedback(new ValidationAndFeedbackPair(timeFieldValidator, timeFieldFeedback));
    }

    private void initializeDialog() {
        CsiHeading myHeading = Modal.createHeading(i18n.timelineSettings_newDefinition_eventDefinition());
        myHeading.getElement().getStyle().setDisplay(Style.Display.INLINE);
        dialog.addToHeader(myHeading);
        Button cancelButton = dialog.getCancelButton();
        cancelButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                getPresenter().cancelEventDefinition();
                eventDefinition = null;
                clearSelections();
            }
        });

        dialog.getActionButton().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if(isValid()){
                    if(eventDefinition == null){
                        getPresenter().saveEventDefinition(startEventField.getCurrentValue(), endEventField.getCurrentValue(), labelField.getCurrentValue());
                    } else {
                        getPresenter().saveEventDefinition(startEventField.getCurrentValue(), endEventField.getCurrentValue(), labelField.getCurrentValue(), eventDefinition);
                        eventDefinition = null;
                    }
                    clearSelections();
                }
            }

           
        });
        
        populate();
    }
    
    private void clearSelections() {
        startEventField.clear();
        endEventField.clear();
        labelField.clear();
    }
    
    protected boolean isValid() {
        if (validator.validate()) {
            return true;
        } else {
            String errors = "Please fix the following errors";
            MultiNotificationPopup dialog = new MultiNotificationPopup("Settings Error", errors, validator.getErrors());
            dialog.show();
            return false;
        }
    }

    public void setSelection(FieldDef start, FieldDef end, FieldDef label){
        startEventField.setValue(start);
        endEventField.setValue(end);
        labelField.setValue(label);
    }

    private void populate() {
        
        List<FieldDef> fieldDefs = getPresenter().getDataViewDef().getModelDef().getFieldDefs();

        for(FieldDef fieldDef: fieldDefs){
            CsiDataType type = fieldDef.getValueType();
            if(type == CsiDataType.Date || type == CsiDataType.Time || type == CsiDataType.DateTime){
                startEventField.getStore().add(fieldDef);
                endEventField.getStore().add(fieldDef);
            } else {
//              groupingField.getStore().add(fieldDef);
//          
//              if(type == CsiDataType.Number || type == CsiDataType.Integer){
//                  dotSizeField.getStore().add(fieldDef);
//              }
            }
        }

        labelField.getStore().addAll(fieldDefs);

    }

    public void show() {
        dialog.show();
    }

    
    public void hide() {
        dialog.hide();
    }

    public void setPresenter(TimelineSettingsPresenter presenter) {
        this.presenter = presenter;
    }

    public TimelineSettingsPresenter getPresenter() {
        return presenter;
    }

    @UiHandler("clearLabel")
    public void handleLabelClear(ClickEvent e) {
        
        labelField.setValue(null);
        
    }
    
    @UiHandler("clearEnd")
    public void handleEndClear(ClickEvent e) {
        
        endEventField.setValue(null);
        
    }
    
    @UiHandler("clearStart")
    public void handleStartClear(ClickEvent e) {
        
        startEventField.setValue(null);
        
    }
    
    public void setSelection(TimelineEventDefinition eventDefinition) {
        FieldDef start = null;
        FieldDef end = null;
        
        if(eventDefinition.getStartField() != null){
            start = eventDefinition.getStartField().getFieldDef();
        }
        
        if(eventDefinition.getEndField() != null){
            end = eventDefinition.getEndField().getFieldDef();
        }

        this.eventDefinition = eventDefinition;
        setSelection(start, end, eventDefinition.getLabelField());
        
    }
}
