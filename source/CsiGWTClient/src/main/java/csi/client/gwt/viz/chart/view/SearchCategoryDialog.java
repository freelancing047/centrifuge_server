package csi.client.gwt.viz.chart.view;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.sencha.gxt.cell.core.client.form.ComboBoxCell.TriggerAction;
import com.sencha.gxt.data.shared.LabelProvider;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.widget.core.client.event.BeforeQueryEvent.BeforeQueryHandler;
import com.sencha.gxt.widget.core.client.form.ComboBox;

import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.validation.multi.MultiValidatorCollectingErrors;
import csi.client.gwt.validation.multi.ValidationAndFeedbackPair;
import csi.client.gwt.validation.validator.ComboBoxBlankValidator;
import csi.client.gwt.widget.boot.Dialog;
import csi.client.gwt.widget.combo_boxes.StringComboBox;

public class SearchCategoryDialog {
    private Dialog dialog;

    @UiField(provided = true)
    String title = CentrifugeConstantsLocator.get().searchCategoryDialog_title();
    
    @UiField
    HorizontalPanel container;
    
    private ComboBox<String> comboBox;
    
    protected MultiValidatorCollectingErrors validators = new MultiValidatorCollectingErrors();

    interface SpecificUiBinder extends UiBinder<Dialog, SearchCategoryDialog> {
    }

    private static SpecificUiBinder uiBinder = GWT.create(SpecificUiBinder.class);

    public SearchCategoryDialog(List<String> categories) {
        super();
        dialog = uiBinder.createAndBindUi(this);
		ListStore<String> listStore = new ListStore<String>(item -> item);


        listStore.addAll(categories);
        comboBox = new StringComboBox(listStore, new StringLabelProvider());
        comboBox.setTriggerAction(TriggerAction.QUERY);

        comboBox.getElement().getStyle().setPaddingRight(4, Unit.PX);
		comboBox.addSelectionHandler(event -> isValid());
		
        container.add(comboBox);

        dialog.getActionButton().setText(CentrifugeConstantsLocator.get().searchCategoryDialog_actionButton());
        dialog.hideOnCancel();
        
        ComboBoxBlankValidator notBlankValidator = new ComboBoxBlankValidator(comboBox);
        validators.addValidationAndFeedback(new ValidationAndFeedbackPair(notBlankValidator, ChartValidationFeedback.EMPTY_CATEGORY_FEEDBACK));
        
    }
    
    

    public String getName() {
        return comboBox.getValue();
    }

    public void show() {
        dialog.show();
    }

    public HandlerRegistration addClickHandler(ClickHandler handler) {
        return dialog.getActionButton().addClickHandler(handler);
    }

    public HandlerRegistration addQueryHandler(BeforeQueryHandler<String> handler){
        return comboBox.addBeforeQueryHandler(handler);
    }
    
    public void hide() {
        dialog.hide();
    }

    protected class StringLabelProvider implements LabelProvider<String>{
        @Override
        public String getLabel(String item) {
            return item;
    }
    }

    public boolean isValid() {
        if(validators.validate()){
            comboBox.clearInvalid();
            return true;
        } else {
            String error = validators.getErrors().get(0);
            comboBox.forceInvalid(error);
            return false;
        }
    }



    public boolean isStoreEmpty(){
        return comboBox.getStore().size() == 0;
    }

    public void updateStore(List<String> results) {
        comboBox.getStore().replaceAll(results);
    }
}
