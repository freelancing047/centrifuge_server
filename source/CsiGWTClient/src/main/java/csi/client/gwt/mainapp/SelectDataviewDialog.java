package csi.client.gwt.mainapp;

import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.sencha.gxt.cell.core.client.form.ComboBoxCell;
import com.sencha.gxt.cell.core.client.form.ComboBoxCell.TriggerAction;
import com.sencha.gxt.data.shared.LabelProvider;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.widget.core.client.form.ComboBox;

import csi.client.gwt.WebMain;
import csi.client.gwt.events.LayoutType;
import csi.client.gwt.events.OpenDataViewEvent;
import csi.client.gwt.events.OpenDirectedViewEvent;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.validation.multi.MultiValidatorCollectingErrors;
import csi.client.gwt.validation.multi.ValidationAndFeedbackPair;
import csi.client.gwt.validation.validator.ComboBoxBlankValidator;
import csi.client.gwt.viz.chart.view.ChartValidationFeedback;
import csi.client.gwt.widget.boot.Dialog;
import csi.client.gwt.widget.combo_boxes.StringComboBox;
import csi.server.common.model.dataview.DataView;

public class SelectDataviewDialog {
	
	private CentrifugeConstants _constants = CentrifugeConstantsLocator.get();

	private Dialog dialog;

	@UiField(provided = true)
	String title = _constants.selectDataviewDialog_title();

	@UiField
	HorizontalPanel container;

	private ComboBox<String> comboBox;

	private ComboBoxCell<String> worksheetList;

	protected MultiValidatorCollectingErrors validators = new MultiValidatorCollectingErrors();

	interface SpecificUiBinder extends UiBinder<Dialog, SelectDataviewDialog> {
	}

	private static SpecificUiBinder uiBinder = GWT.create(SpecificUiBinder.class);

	public SelectDataviewDialog(final Map<String, DataView> dataviewMap, final Map<String, Integer> vizParams, final String layout, final MainView mainView) {
		super();

        try {


        } catch (Exception myException) {

            Dialog.showException("SelectDataviewDialog", myException);
        }
		dialog = uiBinder.createAndBindUi(this);
		ListStore<String> listStore = new ListStore<String>(new ModelKeyProvider<String>(){

			@Override
			public String getKey(String item) {
				return item.toString();
			}});

		listStore.addAll(dataviewMap.keySet());

        comboBox = new StringComboBox(listStore, new StringLabelProvider());
		comboBox.setTypeAhead(true);
		comboBox.setTriggerAction(TriggerAction.ALL);
		comboBox.setWidth(200);
		comboBox.getElement().getStyle().setPaddingRight(4, Unit.PX);
		comboBox.addSelectionHandler(new SelectionHandler<String>(){

			@Override
			public void onSelection(SelectionEvent<String> event) {
				isValid();
			}});

		container.add(comboBox);


		dialog.getActionButton().setText(_constants.dialog_ContinueButton());
		dialog.hideOnCancel();

		ComboBoxBlankValidator notBlankValidator = new ComboBoxBlankValidator(comboBox);
		validators.addValidationAndFeedback(new ValidationAndFeedbackPair(notBlankValidator, ChartValidationFeedback.EMPTY_CATEGORY_FEEDBACK));

		addClickHandler(new ClickHandler(){

			@Override
			public void onClick(ClickEvent event) {
				if(isValid()){
					if(layout == null){
						mainView.showApplicationToolbar();
                        WebMain.injector.getMainPresenter().beginOpenDataView(dataviewMap.get(getName()));
        			} else {
        				WebMain.injector.getEventBus().fireEvent(new OpenDirectedViewEvent(dataviewMap.get(getName()).getUuid(), LayoutType.valueOf(layout.toUpperCase()), vizParams));
        			}
					hide();
				}
			}

		});
	}

	public String getName() {

        try {

            return comboBox.getValue();

        } catch (Exception myException) {

            Dialog.showException("SelectDataviewDialog", myException);
        }
        return null;
	}

	public void show() {

        try {

            dialog.show();

        } catch (Exception myException) {

            Dialog.showException("SelectDataviewDialog", myException);
        }
	}

	public HandlerRegistration addClickHandler(ClickHandler handler) {

        try {

            return dialog.getActionButton().addClickHandler(handler);

        } catch (Exception myException) {

            Dialog.showException("SelectDataviewDialog", myException);
        }
        return null;
	}

	public void hide() {

        try {

            dialog.hide();

        } catch (Exception myException) {

            Dialog.showException("SelectDataviewDialog", myException);
        }
	}

	protected class StringLabelProvider implements LabelProvider<String>{

		@Override
		public String getLabel(String item) {

            try {

                return item.toString();

            } catch (Exception myException) {

                Dialog.showException("SelectDataviewDialog", myException);
            }
            return null;
		}}

	protected class StringModelKeyProvider implements ModelKeyProvider<String>{
		@Override
		public String getKey(String item) {

            try {

                return item.toString();

            } catch (Exception myException) {

                Dialog.showException("SelectDataviewDialog", myException);
            }
            return null;
		}
	}

	public boolean isValid() {

        try {

            if(validators.validate()){
                comboBox.clearInvalid();
                return true;
            } else {
                String error = validators.getErrors().get(0);

                comboBox.forceInvalid(error);
            }

        } catch (Exception myException) {

            Dialog.showException("SelectDataviewDialog", myException);
        }
        return false;
	}

}