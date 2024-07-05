package csi.client.gwt.worksheet;

import java.util.List;

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

import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.validation.multi.MultiValidatorCollectingErrors;
import csi.client.gwt.validation.multi.ValidationAndFeedbackPair;
import csi.client.gwt.validation.validator.ComboBoxBlankValidator;
import csi.client.gwt.viz.chart.view.ChartValidationFeedback;
import csi.client.gwt.widget.boot.Dialog;
import csi.client.gwt.widget.combo_boxes.StringComboBox;

public class WorksheetCopyDialog {

    static CentrifugeConstants i18n = CentrifugeConstantsLocator.get();
	private final String ACTION_LABEL = i18n.worksheetCopyDialogActionButton(); //$NON-NLS-1$

	private Dialog dialog;

	@UiField(provided = true)
	String title = i18n.worksheetCopyDialogTitle(); //$NON-NLS-1$

	@UiField
	HorizontalPanel container;

	private ComboBox<String> comboBox;

	private ComboBoxCell<String> worksheetList;

	private WorksheetPresenter presenter;
	private String vizUuid;

	protected MultiValidatorCollectingErrors validators = new MultiValidatorCollectingErrors();

	interface SpecificUiBinder extends UiBinder<Dialog, WorksheetCopyDialog> {
	}

	private static SpecificUiBinder uiBinder = GWT.create(SpecificUiBinder.class);

	public WorksheetCopyDialog(WorksheetPresenter worksheetPresenter, String uuid) {
		super();
		presenter = worksheetPresenter;
		vizUuid = uuid;
		dialog = uiBinder.createAndBindUi(this);
		ListStore<String> listStore = new ListStore<String>(new ModelKeyProvider<String>(){

			@Override
			public String getKey(String item) {
				return item.toString();
			}});

		List<String> names = worksheetPresenter.getWorksheetNames();

		listStore.addAll(names);

        comboBox = new StringComboBox(listStore, new StringLabelProvider());
		comboBox.setTypeAhead(true);
		comboBox.setTriggerAction(TriggerAction.ALL);
		comboBox.setWidth(180);
		comboBox.getElement().getStyle().setPaddingRight(4, Unit.PX);
		comboBox.addSelectionHandler(new SelectionHandler<String>(){

			@Override
			public void onSelection(SelectionEvent<String> event) {
				isValid();
			}});

		container.add(comboBox);


		dialog.getActionButton().setText(ACTION_LABEL);
		dialog.hideOnCancel();

		ComboBoxBlankValidator notBlankValidator = new ComboBoxBlankValidator(comboBox);
		validators.addValidationAndFeedback(new ValidationAndFeedbackPair(notBlankValidator, ChartValidationFeedback.EMPTY_CATEGORY_FEEDBACK));

		addClickHandler(new ClickHandler(){

			@Override
			public void onClick(ClickEvent event) {
				if(isValid()){
					presenter.copyVisualization(vizUuid, getName());
					hide();
				}
			}

		});
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

	public void hide() {
		dialog.hide();
	}

	protected class StringLabelProvider implements LabelProvider<String>{

		@Override
		public String getLabel(String item) {
			return item.toString();
		}}

	protected class StringModelKeyProvider implements ModelKeyProvider<String>{
		@Override
		public String getKey(String item) {
			return item.toString();
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

}