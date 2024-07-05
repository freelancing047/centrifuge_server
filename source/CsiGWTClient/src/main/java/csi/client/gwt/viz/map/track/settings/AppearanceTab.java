package csi.client.gwt.viz.map.track.settings;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.CheckBox;
import com.github.gwtbootstrap.client.ui.ControlGroup;
import com.github.gwtbootstrap.client.ui.RadioButton;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
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

import com.sencha.gxt.widget.core.client.event.BlurEvent;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.validation.feedback.StringValidationFeedback;
import csi.client.gwt.validation.feedback.ValidationFeedback;
import csi.client.gwt.validation.multi.MultiValidatorCollectingErrors;
import csi.client.gwt.validation.multi.ValidationAndFeedbackPair;
import csi.client.gwt.validation.validator.Validator;
import csi.client.gwt.viz.map.settings.MapSettingsPresenter;
import csi.client.gwt.widget.combo_boxes.FieldDefComboBox;
import csi.client.gwt.widget.combo_boxes.StringComboBox;
import csi.client.gwt.widget.misc.ModelAwareView;
import csi.client.gwt.widget.ui.form.SortOrderButton;
import csi.server.common.enumerations.CsiDataType;
import csi.server.common.model.FieldDef;
import csi.server.common.model.SortOrder;
import csi.server.common.model.visualization.map.LineStyle;
import csi.server.common.model.visualization.map.MapPlace;
import csi.server.common.model.visualization.map.MapSettings;
import csi.server.common.model.visualization.map.MapTrack;
import csi.shared.core.color.ClientColorHelper;
import csi.shared.core.color.ClientColorHelper.Color;

public class AppearanceTab extends Composite implements ModelAwareView {
	private static final String[] nodeColors = { "660000", "990000", "CC0000", "CC3333", "EA4C88", "D10553", "823CC8",
			"663399", "333399", "0066CC", "0099CC", "7AD9F9", "66CCCC", "74E618", "77CC33", "336600", "666600",
			"999900", "CCCC33", "EAEA26", "FFFF00", "FFCC33", "FF9900", "CE7C00", "FF6600", "CC6633", "996633",
			"AA6117", "663300", "000000", "999999", "CCCCCC" };
    private static AppearanceTabUiBinder uiBinder = GWT.create(AppearanceTabUiBinder.class);

    @UiField
    ControlGroup identityControlGroup;
    @UiField
    RadioButton identityFixedRB;
    @UiField
    RadioButton identityDynamicRB;
    @UiField(provided = true)
    FieldDefComboBox identityFDCB;
    @UiField
    TextBox identityTextBox;
    @UiField
    ControlGroup sequenceControlGroup;
    @UiField
    FieldDefComboBox sequenceFDCB;
    @UiField
    SortOrderButton sequenceSortOrder;
    @UiField
    StringComboBox placeField;
    @UiField(provided = true)
    StringComboBox styleField;
    @UiField
    CheckBox widthCheckBox;
    @UiField
    TextBox widthTextBox;
    @UiField
    CheckBox colorCheckBox;
    @UiField
    Button colorButton;
    @UiField
    TextBox colorTextBox;
    @UiField(provided = true)
    ColorPalette colorPalette;
    @UiField
    RadioButton identityPlaceRB;
    @UiField
    CheckBox placeOpacityCheckBox;
    @UiField
    TextBox placeOpacityTextBox;

    private Color color;
    private List<String> placeNames;
    private FieldDef identityField;	private MapTrack mapTrack = new MapTrack();
    private FieldDef sequenceField;
    private SortOrder sequenceSortOrder1;
    private String place;
    private String identityName;
    private String colorString;
    private String styleTypeString;

    private Integer width;
    private int listPosition;

    private float nodeTransparency;

    private boolean identityFixed;
	private boolean identityDynamic;
	private boolean identityPlace;
    private boolean useDefaultColor;
    private boolean useDefaultWidth;
	private boolean usedefaultOpacity;


	public void updatePlaces(Collection<MapPlace> places) {
		placeField.getStore().clear();
		placeField.getStore().addAll(places.stream().map(MapPlace::getName).collect(Collectors.toList()));
		if (!placeField.getStore().getAll().contains(place)) {
			place = placeField.getStore().get(0);

		}
		placeField.setValue(place);
	}

	interface AppearanceTabUiBinder extends UiBinder<Widget, AppearanceTab> {
	}

    @UiHandler("identityPlaceRB")
    void onIdentityPlace(ClickEvent event) {
        identityFDCB.setVisible(false);
        identityTextBox.setVisible(false);
		identityFixed = false;
		identityDynamic = false;
		identityPlace = true;

	}

    @UiHandler("identityFixedRB")
    void onIdentityFixed(ClickEvent event) {
        identityFDCB.setVisible(false);
        identityTextBox.setVisible(true);
		identityFixed = true;
		identityDynamic = false;
		identityPlace = false;

    }

    @UiHandler("identityDynamicRB")
    void onIdentityDynamic(ClickEvent event) {
        identityFDCB.setVisible(true);
        identityTextBox.setVisible(false);
		identityFixed = false;
		identityDynamic = true;
		identityPlace = false;
    }

	@UiHandler("widthCheckBox")
	void onWidthCheckBox(ClickEvent event) {
		widthTextBox.setEnabled(widthCheckBox.getValue());
		useDefaultWidth = !widthCheckBox.getValue();
	}

	@UiHandler("placeOpacityCheckBox")
	void onOpacityCheckbox(ClickEvent event) {
		placeOpacityTextBox.setEnabled(placeOpacityCheckBox.getValue());
		usedefaultOpacity = !placeOpacityCheckBox.getValue();
	}

	@UiHandler({ "colorCheckBox" })
	void onColorCheckBox(ClickEvent event) {
		Boolean value = colorCheckBox.getValue();
		colorButton.setEnabled(value);
		colorTextBox.setEnabled(value);
		useDefaultColor = !colorCheckBox.getValue();
	}

	@UiHandler("identityTextBox")
	void onIdentityTBChange(ValueChangeEvent<String> e){
		identityName = identityTextBox.getValue();
		identityName = identityName.trim();
		if(identityName.length()>250) {
            identityName = identityName.substring(0, 250);
        }
        identityTextBox.setValue(identityName);
	}

	@UiHandler("identityFDCB")
	void onIdentityFdcbBlur(BlurEvent event) {
		identityField = identityFDCB.getValue();
	}

	@UiHandler("placeOpacityTextBox")
	void onOpacityTBChange(ValueChangeEvent<String> e) {
		String value = placeOpacityTextBox.getValue();
		try {
			float f = Float.parseFloat(value);
			if (f < 0.0) {
				f = 0;
			}
			if (f > 100) {
				f = 100;
			}
			nodeTransparency = f;
		} catch (Exception ignored) {
		}
			placeOpacityTextBox.setValue(nodeTransparency + "");
	}

	private void setColor(Color color) {
		if (color == null) {
			color = new Color(0);
		}
		this.color = color;
		colorPalette.setValue(color.toString().substring(1).toUpperCase());
		colorTextBox.setValue(color.toString().toUpperCase());
		colorButton.getElement().getStyle().setColor(color.toString());
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
			}
		} catch (NumberFormatException nfe) {
			setColor(color);
		}
		colorPalette.setVisible(false);
	}

	@UiHandler("colorPalette")
	public void onSelection(SelectionEvent<String> event) {
		String colorString = event.getSelectedItem();
		color = ClientColorHelper.get().makeFromHex(colorString);
		setColor(color);
		colorPalette.setVisible(false);
	}

	public AppearanceTab() {
		styleField = new StringComboBox();
		ListStore<String> lineStyleListStore = styleField.getStore();
		List<String> lineStyles = new ArrayList<String>();
		for (LineStyle lineStyle : LineStyle.lineStyleWheel) {
			lineStyles.add(lineStyle.toString());
		}
		lineStyleListStore.addAll(lineStyles);
		colorPalette = new ColorPalette(nodeColors, nodeColors);
		colorPalette.setVisible(false);

		identityFDCB = new FieldDefComboBox();
		identityFDCB.addSelectionHandler(event -> {
			identityField = event.getSelectedItem();
			identityFDCB.setEnabled(true);
		});
		if (identityFDCB.getValue() == null) {
			FieldDef value;
			if (identityField != null) {
				value = identityField;
			} else {
				value = identityFDCB.getStore().get(0);
			}
			identityField = value;
			identityFDCB.setValue(value, true);
		}

		initWidget(uiBinder.createAndBindUi(this));
		placeField.setForceSelection(true);
		placeField.addBlurHandler(new BlurEvent.BlurHandler() {
			@Override
			public void onBlur(BlurEvent blurEvent) {
				if (placeField.getValue() == null) {
					if (placeNames.isEmpty()) {
						placeField.setValue(null);
					} else {
						placeField.setValue(place);
					}
				}
			}
		});

		identityFDCB.setForceSelection(true);

		sequenceFDCB.setForceSelection(true);
		sequenceFDCB.addBlurHandler(new BlurEvent.BlurHandler() {
			@Override
			public void onBlur(BlurEvent blurEvent) {
				FieldDef value = sequenceFDCB.getValue();
				if (value == null) {
					setDefaultSequenceValue();
				}
				sequenceField = sequenceFDCB.getValue();
			}
		});
		Style style = sequenceSortOrder.getElement().getStyle();
		style.setProperty("display", "inline");

		styleField.addBlurHandler(blurEvent -> {
			if (styleField.getValue() == null) {
				styleField.setValue(LineStyle.SOLID.toString());
			}
		});
		widthTextBox.addBlurHandler(blurEvent -> {
			String value = widthTextBox.getValue();
			try {
				width = Integer.parseInt(value);
			}catch (Exception ignored){
			}
			if (width>10) {
				width = 10;
			} else if (width < 1) {
				width = 1;
			}
			widthTextBox.setValue(width.toString());
		});

	}

	private MapSettingsPresenter presenter;

	public void setPresenter(MapSettingsPresenter presenter) {
		this.presenter = presenter;
		populate();
	}


	private void populate() {

		populatePlaces();

		identityFDCB.getStore().addAll(getFieldList());

		sequenceFDCB.getStore().addAll(getFieldList());


	}

	private void populatePlaces() {
		List<MapPlace> mapPlaces = presenter.visualizationDef.getMapSettings().getMapPlaces();
		placeNames = mapPlaces.stream().map(MapPlace::getName).collect(Collectors.toList());
		placeField.getStore().addAll(placeNames);
        identityTextBox.setMaxLength(250);
	}

	private CentrifugeConstants _constants = CentrifugeConstantsLocator.get();

	protected void initializeValidation(MultiValidatorCollectingErrors validator) {
		Validator identityColumnNullValidator = new Validator() {
			@Override
			public boolean isValid() {
				if (identityFixedRB.getValue()) {
					return true;
				} else {
					return identityFDCB.getCurrentValue() != null;
				}
			}
		};
		ValidationFeedback identityColumnNullFeedback = new StringValidationFeedback(
				_constants.mapSettingsView_identityColumnNullFeedbackText()); // $NON-NLS-1$
		validator.addValidationAndFeedback(
				new ValidationAndFeedbackPair(identityColumnNullValidator, identityColumnNullFeedback));

		Validator placeFieldNullValidator = new Validator() {
			@Override
			public boolean isValid() {
				String placeFieldValue = placeField.getValue();
				return placeFieldValue != null && placeFieldValue.length() != 0;
			}
		};
		ValidationFeedback placeFieldNullFeedback = new StringValidationFeedback(
				_constants.mapSettingsView_placeFieldNullFeedbackText()); // $NON-NLS-1$
		validator.addValidationAndFeedback(
				new ValidationAndFeedbackPair(placeFieldNullValidator, placeFieldNullFeedback));

		Validator placeFieldExistenceValidator = new Validator() {
			@Override
			public boolean isValid() {
				String placeFieldValue = placeField.getValue();
				if (placeFieldValue != null && placeFieldValue.length() > 0) {
					return placeNames.contains(placeFieldValue);
				}
				return true;
			}
		};
		ValidationFeedback placeFieldExistenceValidatorFeedback = new StringValidationFeedback(
				_constants.mapSettingsView_placeFieldExistenceValidatorFeedbackText()); // $NON-NLS-1$
		validator.addValidationAndFeedback(
				new ValidationAndFeedbackPair(placeFieldExistenceValidator, placeFieldExistenceValidatorFeedback));
	}

	public String getPlaceValue() {
		return placeField.getValue();
	}

    public void updateViewFromModel() {
        MapSettings mapSettings = presenter.getVisualizationDef().getMapSettings();
        List<MapTrack> tracks = mapSettings.getMapTracks();
        if (tracks == null) {
            tracks = new ArrayList<MapTrack>();
            mapSettings.setMapTracks(tracks);
        }
        if (!tracks.isEmpty()) {

            mapTrack = tracks.get(0);
        } else {
            mapTrack = new MapTrack();
        }
		extractModelValues(mapSettings);

		{
            if (placeNames.size() == 0) {
                placeField.clear();
            }else {
                int i = placeNames.indexOf(place);
                i = Math.max(i, 0);
                placeField.setSelectedIndex(i);
            }
        }
        {
        	identityFDCB.setValue(identityField);

			if (Strings.isNullOrEmpty(identityName)) {
				identityName = CentrifugeConstantsLocator.get().map_track_name();
			}
			identityTextBox.setValue(identityName);


            identityPlaceRB.setValue(identityPlace);
            identityDynamicRB.setValue(identityDynamic);
			identityFDCB.setVisible(identityDynamic);
            identityFixedRB.setValue(identityFixed);
			identityTextBox.setVisible(identityFixed);
        }
        {
			setDefaultSequenceValue();
        }
		{
			if (Strings.isNullOrEmpty(styleTypeString)) {
				styleTypeString = LineStyle.SOLID.toString();
			}
			styleField.setValue(styleTypeString);
		}
		{
			widthCheckBox.setValue(!useDefaultWidth);
			widthTextBox.setEnabled(!useDefaultWidth);
			if(width<1){
				width = 1;
			}
			widthTextBox.setValue(width.toString());
		}
		{
			colorCheckBox.setValue(!useDefaultColor);
			colorTextBox.setEnabled(!useDefaultColor);
			color = new Color(0);
			colorTextBox.setValue(colorString,true);
		}
		{
			placeOpacityCheckBox.setValue(!usedefaultOpacity);
			placeOpacityTextBox.setEnabled(!usedefaultOpacity);
			placeOpacityTextBox.setValue(nodeTransparency+"");
		}
    }

	public void extractModelValues(MapSettings mapSettings) {
		place = mapTrack.getPlace();
		identityName = mapTrack.getIdentityName();
		identityField = mapTrack.getIdentityField();
		sequenceField = mapTrack.getSequenceField();
		colorString = mapTrack.getColorString();
		sequenceSortOrder1 = mapTrack.getSequenceSortOrder();
		styleTypeString = mapTrack.getStyleTypeString();
		width = mapTrack.getWidth();
		listPosition = mapTrack.getListPosition();
		identityFixed = mapTrack.isIdentityFixed();
		identityDynamic = mapTrack.isIdentityDynamic();
		identityPlace = mapTrack.isIdentityPlace();
		useDefaultColor = mapTrack.isUseDefaultColorSetting();
		useDefaultWidth = mapTrack.isUseDefaultWidthSetting();
		usedefaultOpacity = mapTrack.isUseDefaultOpacity();
		nodeTransparency = mapSettings.getNodeTransparency();
	}

	private void setDefaultSequenceValue() {
		final List<FieldDef> fieldDefs = getFieldList();
		if (sequenceField == null) {
			Stream<FieldDef> temporalFieldDefs = fieldDefs.stream().filter(fieldDef -> fieldDef.getDataType() == CsiDataType.DateTime || fieldDef.getDataType() == CsiDataType.Date);
			sequenceField = temporalFieldDefs.min(Comparator.comparing(FieldDef::getName)).orElseGet(() -> fieldDefs.stream().min(Comparator.comparing(FieldDef::getName)).orElse(null));
		}
		sequenceFDCB.setValue(sequenceField);
	}

	public List<FieldDef> getFieldList() {
		return presenter.getDataViewDef().getFieldList();
	}

	@Override
    public void updateModelWithView() {
        MapSettings mapSettings = presenter.getVisualizationDef().getMapSettings();
        ArrayList<MapTrack> mapTracks = Lists.newArrayList(mapTrack);
        mapSettings.setMapTracks(mapTracks);

        mapTrack.setListPosition(listPosition);

        mapTrack.setPlace(placeField.getValue());

		{

			//If field mode for identity chosen, but no field chosen, goto place mode
			if (identityField == null) {
				identityDynamic = false;
			}

			//Enforce only single mode, default to place.
			{
				if (identityPlace) {
					identityDynamic = false;
					identityFixed = false;
				} else if (identityDynamic) {
					identityFixed = false;
				} else if (!identityFixed) {
					identityPlace = true;
				}

				mapTrack.setIdentityPlace(identityPlace);
				mapTrack.setIdentityDynamic(identityDynamic);
				mapTrack.setIdentityFixed(identityFixed);
			}
			if (identityFixed) {
				if (Strings.isNullOrEmpty(identityName.trim())) {
					identityName = CentrifugeConstantsLocator.get().map_track_name();;
				}
				mapTrack.setIdentityName(identityName);
			}
			if (identityDynamic) {
				mapTrack.setIdentityField(identityField);
			}
		}
		{
			mapTrack.setSequenceField(sequenceField);
			mapTrack.setSequenceSortOrder(sequenceSortOrder1);
			mapTrack.setStyleTypeString(styleField.getValue());
		}

        mapTrack.setUseDefaultColorSetting(useDefaultColor);
        mapTrack.setColorString(color.toString());

        mapTrack.setUseDefaultWidthSetting(useDefaultWidth);
        mapTrack.setWidth(width);

        mapTrack.setUseDefaultOpacity(usedefaultOpacity);
        mapSettings.setNodeTransparency(nodeTransparency);
    }
}