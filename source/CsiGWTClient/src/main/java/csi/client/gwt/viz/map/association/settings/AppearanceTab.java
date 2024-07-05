package csi.client.gwt.viz.map.association.settings;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.List;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.CheckBox;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.widget.core.client.ColorPalette;

import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.validation.feedback.StringValidationFeedback;
import csi.client.gwt.validation.feedback.ValidationFeedback;
import csi.client.gwt.validation.multi.MultiValidatorCollectingErrors;
import csi.client.gwt.validation.multi.ValidationAndFeedbackPair;
import csi.client.gwt.validation.validator.Validator;
import csi.client.gwt.widget.combo_boxes.StringComboBox;
import csi.server.common.model.themes.map.AssociationStyle;
import csi.server.common.model.visualization.map.LineStyle;
import csi.server.common.model.visualization.map.MapAssociation;
import csi.server.common.model.visualization.map.MapPlace;
import csi.shared.core.color.ClientColorHelper;
import csi.shared.core.color.ClientColorHelper.Color;

public class AppearanceTab extends Composite {

	private static final String[] nodeColors = { "660000", "990000", "CC0000", "CC3333", "EA4C88", "D10553", "823CC8",
			"663399", "333399", "0066CC", "0099CC", "7AD9F9", "66CCCC", "74E618", "77CC33", "336600", "666600",
			"999900", "CCCC33", "EAEA26", "FFFF00", "FFCC33", "FF9900", "CE7C00", "FF6600", "CC6633", "996633",
			"AA6117", "663300", "000000", "999999", "CCCCCC" };

	private static AppearanceTabUiBinder uiBinder = GWT.create(AppearanceTabUiBinder.class);

	interface AppearanceTabUiBinder extends UiBinder<Widget, AppearanceTab> {
	}

	@UiField
	TextBox associationNameTextBox;
	@UiField
	StringComboBox sourceField;
	@UiField
	StringComboBox destinationField;

	@UiHandler("associationNameTextBox")
	public void handleAssociationeNameTextBoxChange(ChangeEvent e) {
		presenter.setCurrentAssociationName(associationNameTextBox.getValue());
	}

	private List<String> placeNames;
	private List<MapAssociation> mapAssociations;

	private CentrifugeConstants _constants = CentrifugeConstantsLocator.get();

	protected void initializeValidation(MultiValidatorCollectingErrors validator) {
		Validator associationNameNullValidator = new Validator() {
			@Override
			public boolean isValid() {
				return associationNameTextBox.getText() != null && associationNameTextBox.getText().length() != 0;
			}
		};
		ValidationFeedback associationNameNullFeedback = new StringValidationFeedback(
				_constants.mapSettingsView_associationNameNullFeedbackText()); // $NON-NLS-1$
		validator.addValidationAndFeedback(
				new ValidationAndFeedbackPair(associationNameNullValidator, associationNameNullFeedback));

		Validator associationNameAlreadyExistingValidator = new Validator() {
			@Override
			public boolean isValid() {
				String associationNameText = associationNameTextBox.getText();
				for (MapAssociation mapAssociation : mapAssociations) {
					if (!mapAssociation.getUuid().equals(presenter.getMapAssociationUuid())
							&& mapAssociation.getName().equals(associationNameText)) {
						return false;
					}
				}
				return true;
			}
		};
		ValidationFeedback associationNameAlreadyExistingFeedback = new StringValidationFeedback(
				_constants.mapSettingsView_associationNameAlreadyExistingFeedbackText()); // $NON-NLS-1$
		validator.addValidationAndFeedback(new ValidationAndFeedbackPair(associationNameAlreadyExistingValidator,
				associationNameAlreadyExistingFeedback));

		Validator sourceFieldNullValidator = new Validator() {
			@Override
			public boolean isValid() {
				String sourceFieldValue = sourceField.getValue();
				return sourceFieldValue != null && sourceFieldValue.length() != 0;
			}
		};
		ValidationFeedback sourceFieldNullFeedback = new StringValidationFeedback(
				_constants.mapSettingsView_sourceFieldNullFeedbackText()); // $NON-NLS-1$
		validator.addValidationAndFeedback(
				new ValidationAndFeedbackPair(sourceFieldNullValidator, sourceFieldNullFeedback));

		Validator destinationFieldNullValidator = new Validator() {
			@Override
			public boolean isValid() {
				String desintationFieldValue = destinationField.getValue();
				return desintationFieldValue != null && desintationFieldValue.length() != 0;
			}
		};
		ValidationFeedback destinationFieldNullFeedback = new StringValidationFeedback(
				_constants.mapSettingsView_destinationFieldNullFeedbackText()); // $NON-NLS-1$
		validator.addValidationAndFeedback(
				new ValidationAndFeedbackPair(destinationFieldNullValidator, destinationFieldNullFeedback));

		Validator sourceFieldExistenceValidator = new Validator() {
			@Override
			public boolean isValid() {
				String sourceFieldValue = sourceField.getValue();
				if (sourceFieldValue != null && sourceFieldValue.length() > 0) {
					return placeNames.contains(sourceFieldValue);
				}
				return true;
			}
		};
		ValidationFeedback sourceFieldExistenceValidatorFeedback = new StringValidationFeedback(
				_constants.mapSettingsView_sourceFieldExistenceValidatorFeedbackText()); // $NON-NLS-1$
		validator.addValidationAndFeedback(
				new ValidationAndFeedbackPair(sourceFieldExistenceValidator, sourceFieldExistenceValidatorFeedback));

		Validator destinationFieldExistenceValidator = new Validator() {
			@Override
			public boolean isValid() {
				String destinationFieldValue = destinationField.getValue();
				if (destinationFieldValue != null && destinationFieldValue.length() > 0) {
					return placeNames.contains(destinationFieldValue);
				}
				return true;
			}
		};
		ValidationFeedback destinationFieldExistenceValidatorFeedback = new StringValidationFeedback(
				_constants.mapSettingsView_destinationFieldExistenceValidatorFeedbackText()); // $NON-NLS-1$
		validator.addValidationAndFeedback(new ValidationAndFeedbackPair(destinationFieldExistenceValidator,
				destinationFieldExistenceValidatorFeedback));

		Validator sourceAndDestinationSameValidator = new Validator() {
			@Override
			public boolean isValid() {
				String sourceFieldValue = sourceField.getValue();
				String destinationFieldValue = destinationField.getValue();
				if (sourceFieldValue != null && sourceFieldValue.length() > 0 && destinationFieldValue != null
						&& destinationFieldValue.length() > 0) {
					return !sourceFieldValue.equals(destinationFieldValue);
				}
				return true;
			}
		};
		ValidationFeedback sourceAndDestinationSameValidatorFeedback = new StringValidationFeedback(
				_constants.mapSettingsView_sourceAndDestinationSameValidatorFeedbackText()); // $NON-NLS-1$
		validator.addValidationAndFeedback(new ValidationAndFeedbackPair(sourceAndDestinationSameValidator,
				sourceAndDestinationSameValidatorFeedback));
	}

	protected void clearSelection() {
		associationNameTextBox.setText("");
		sourceField.setValue("");
		sourceField.setText("");
		sourceField.select("");
		destinationField.setValue("");
		destinationField.setText("");
		destinationField.select("");
	}

	@UiField(provided = true)
	StringComboBox styleField;

	@UiField
	TextBox widthTextBox;

	@UiField
	Button colorButton;

	@UiField
	TextBox colorTextBox;

	@UiField(provided = true)
	ColorPalette colorPalette;

	private Color color;

	@UiField
	CheckBox showDirection;

	@UiField
	CheckBox widthCheckBox;

	@UiHandler({ "widthCheckBox" })
	void onWidthCheckBox(ClickEvent event) {
		enableWidthControls(!widthCheckBox.getValue());
	}

	public void enableWidthControls(boolean useDefault) {
		widthTextBox.setEnabled(!useDefault);
		updateWidthControls();
	}

	private void updateWidthControls() {
		MapAssociation mapAssociation = presenter.getMapAssociation();
		AssociationStyle associationStyle = presenter.getAssociationStyle();

		Integer width = null;
		if (!widthCheckBox.getValue()) {
			if (associationStyle != null) {
				width = associationStyle.getWidth().intValue();
			} else {
				width = useOverrideWidthSettings(mapAssociation);
			}
		} else {
			width = useOverrideWidthSettings(mapAssociation);
		}
		if (width != null) {
			widthTextBox.setValue(width.toString());
		}
	}

	private Integer useOverrideWidthSettings(MapAssociation mapAssociation) {
		Integer width = mapAssociation.getWidth();
		if (width == null) {
			width = 1;
			mapAssociation.setWidth(width);
		}
		return width;
	}

	@UiField
	CheckBox colorCheckBox;

	@UiHandler({ "colorCheckBox" })
	void onColorCheckBox(ClickEvent event) {
		enableColorControls(!colorCheckBox.getValue());
	}

	private void enableColorControls(boolean useDefault) {
		enableColorPicker(!useDefault);
		updateColorControls();
	}

	public void enableColorPicker(boolean value) {
		colorButton.setEnabled(value);
		colorTextBox.setEnabled(value);
	}

	private void updateColorControls() {
		MapAssociation mapAssociation = presenter.getMapAssociation();
		AssociationStyle associationStyle = presenter.getAssociationStyle();

		if (!colorCheckBox.getValue()) {
			if (associationStyle != null) {
				color = ClientColorHelper.get().make(associationStyle.getColor());
			} else {
				useOverrideColorSettings(mapAssociation);
			}
		} else {
			useOverrideColorSettings(mapAssociation);
		}
		setColor(color);
	}

	private void useOverrideColorSettings(MapAssociation mapAssociation) {
		String colorString = mapAssociation.getColorString();
		if (colorString == null || colorString.equals("")) {
			colorString = "333333";
			mapAssociation.setColorString(colorString);
		}
		if (colorString.charAt(0) == '#') {
			colorString = colorString.substring(1);
		}
		color = ClientColorHelper.get().makeFromHex(colorString);
	}

	public AppearanceTab() {
		styleField = new StringComboBox();
		ListStore<String> lineStyleListStore = styleField.getStore();
		List<String> lineStyles = new ArrayList<String>();
		for (LineStyle lineStyle : LineStyle.lineStyleWheel) {
			lineStyles.add(lineStyle.getLabel());
		}
		lineStyleListStore.addAll(lineStyles);
		colorPalette = new ColorPalette(nodeColors, nodeColors);
		colorPalette.setVisible(false);
		initWidget(uiBinder.createAndBindUi(this));
	}

	@UiHandler("colorButton")
	void colorButtonClickHandler(ClickEvent event) {
		if (colorPalette.isVisible()) {
			colorPalette.setVisible(false);
		} else {
			colorPalette.setVisible(true);
			colorPalette.focus();
		}
	}

	@UiHandler("colorTextBox")
	void onColorTextInput(ValueChangeEvent<String> event) {
		checkNotNull(event);
		String value = checkNotNull(event.getValue());
		if (value.charAt(0) == '#') {
			value = value.substring(1);
		}
		try {
			Color testColor = ClientColorHelper.get().makeFromHex(value);
			if (testColor != null) {
				color = testColor;
				setColor(color);
				colorPalette.setVisible(false);
			} else {
				setColor(color);
			}
		} catch (NumberFormatException nfe) {
			setColor(color);
		}
	}

	public void setColor(Color color) {
		colorPalette.setValue(color.toString().substring(1).toUpperCase());
		colorTextBox.setValue(color.toString().toUpperCase());
		colorButton.getElement().getStyle().setColor(color.toString());
	}

	@UiHandler("colorPalette")
	public void onSelection(SelectionEvent<String> event) {
		String colorString = event.getSelectedItem();
		color = ClientColorHelper.get().makeFromHex(colorString);
		setColor(color);
		colorPalette.setVisible(false);
	}

	protected void updateMapAssociation() {
		MapAssociation mapAssociation = presenter.getMapAssociation();
		mapAssociation.setName(associationNameTextBox.getText());
		mapAssociation.setSource(sourceField.getValue());
		mapAssociation.setDestination(destinationField.getValue());
		AssociationStyle associationStyle = presenter.getAssociationStyle();
		String styleFieldValue = styleField.getValue();
		if (styleFieldValue == null) {
			mapAssociation.setStyleTypeString(LineStyle.NONE.getLabel());
		} else {
			mapAssociation.setStyleTypeString(styleFieldValue);
		}
		mapAssociation.setUseDefaultWidthSetting(!widthCheckBox.getValue());
		if (!mapAssociation.isUseDefaultWidthSetting() || associationStyle == null) {
			String widthString = widthTextBox.getValue();
			try {
				int widthValue = Integer.parseInt(widthString);
				mapAssociation.setWidth(widthValue);
			} catch (NumberFormatException nfe) {
				widthTextBox.setValue("1");
				mapAssociation.setWidth(1);
			}
		}
		mapAssociation.setUseDefaultColorSetting(!colorCheckBox.getValue());
		if (!mapAssociation.isUseDefaultColorSetting() || associationStyle == null) {
			mapAssociation.setColorString(colorPalette.getValue());
		}
		mapAssociation.setShowDirection(showDirection.getValue());
	}

	protected void setSelection() {
		MapAssociation mapAssociation = presenter.getMapAssociation();
		String associationName = mapAssociation.getName();
		String source = mapAssociation.getSource();
		String destination = mapAssociation.getDestination();

		setSelection(associationName, source, destination);

		String styleTypeString = mapAssociation.getStyleTypeString();
		if (styleTypeString == null) {
			styleField.setValue(LineStyle.NONE.getLabel());
			styleField.setText(LineStyle.NONE.getLabel());
		} else {
			styleField.setValue(styleTypeString);
			styleField.setText(styleTypeString);
		}

		setWidthCheckBox(mapAssociation.isUseDefaultWidthSetting());

		setColorCheckBox(mapAssociation.isUseDefaultColorSetting());

		showDirection.setValue(mapAssociation.isShowDirection());
	}

	private void setSelection(String associationName, String source, String destination) {
		associationNameTextBox.setText(associationName);
		sourceField.setValue(source);
		sourceField.setText(source);
		sourceField.select(placeNames.indexOf(source));
		destinationField.setValue(destination);
		destinationField.setText(destination);
		destinationField.select(placeNames.indexOf(destination));
	}

	public void setWidthCheckBox(boolean useDefault) {
		widthCheckBox.setValue(!useDefault);
		enableWidthControls(useDefault);
	}

	public void setColorCheckBox(boolean useDefault) {
		colorCheckBox.setValue(!useDefault);
		enableColorControls(useDefault);
	}

	private MapAssociationSettingsPresenter presenter;

	public void setPresenter(MapAssociationSettingsPresenter presenter) {
		this.presenter = presenter;
		populate();
		hookupListener();
	}

	public void populate() {
		placeNames = new ArrayList<String>();
		placeNames.add("");
		for (MapPlace mapPlace : presenter.getMapPlaces()) {
			placeNames.add(mapPlace.getName());
		}
		mapAssociations = presenter.getMapAssociations();
		sourceField.clear();
		sourceField.getStore().clear();
		sourceField.getStore().addAll(placeNames);
		destinationField.clear();
		destinationField.getStore().clear();
		destinationField.getStore().addAll(placeNames);
	}

	private void hookupListener() {
		presenter.registerCurrentNameListener((String currentPlaceName) -> {
			AssociationStyle associationStyle = presenter.getAssociationStyle(currentPlaceName);
			if (associationStyle != null) {
				if (!widthCheckBox.getValue()) {
					Integer width = associationStyle.getWidth().intValue();
					widthTextBox.setValue(width.toString());
				}
				if (!colorCheckBox.getValue()) {
					color = ClientColorHelper.get().make(associationStyle.getColor());
					setColor(color);
				}
			}
		});
	}

	public String getSourceValue() {
		return sourceField.getValue();
	}

	public String getDestinationValue() {
		return destinationField.getValue();
	}
}
