package csi.client.gwt.dataview.fieldlist.editor.scripted.widget;

import java.util.ArrayList;
import java.util.List;

import com.github.gwtbootstrap.client.ui.ControlLabel;
import com.github.gwtbootstrap.client.ui.RadioButton;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

import csi.client.gwt.dataview.fieldlist.FieldList;
import csi.client.gwt.dataview.fieldlist.editor.scripted.ScriptedFunctions;
import csi.client.gwt.dataview.fieldlist.editor.scripted.ScriptedFunctionsEditorModel;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.util.Display;
import csi.client.gwt.validation.multi.ValidationAndFeedbackPair;
import csi.client.gwt.widget.boot.Dialog;
import csi.client.gwt.widget.list_boxes.CsiStringListBox;
import csi.server.common.model.DurationUnit;
import csi.server.common.model.FieldDef;
import csi.server.common.model.FieldType;
import csi.server.common.model.functions.DurationFunction;

/**
 * @author Centrifuge Systems, Inc.
 * Calculate time between two dates.
 */
public class CalculateDurationWidget extends Composite implements ScriptedFunctionsWidget {

    private static final CentrifugeConstants i18n = CentrifugeConstantsLocator.get();

    private final FieldList fieldList;

    private final Grid grid = new Grid(2, 3);
    private final CsiStringListBox field1ListBox = new CsiStringListBox();
    private final CsiStringListBox field2ListBox = new CsiStringListBox();
    private final ControlLabel nowLabel = new ControlLabel(i18n.fieldList_CalculationDuration_Now());
    private final CsiStringListBox timeUnitsListBox = new CsiStringListBox();

    private final RadioButton fieldRadio = new RadioButton("duration", i18n.fieldList_CalculationDuration_DurationField()); //$NON-NLS-1$
    private final RadioButton nowRadio = new RadioButton("duration", i18n.fieldList_CalculationDuration_DurationNow()); //$NON-NLS-1$

    public CalculateDurationWidget(FieldList fieldListIn, String currentModelName) {
        this.fieldList = fieldListIn;

        buildGrid();

        VerticalPanel mainPanel = new VerticalPanel();
        mainPanel.add(grid);
        mainPanel.add(createRadioButtonPanel());

        initWidget(mainPanel);

        setSpacing();
        populateDropdowns(currentModelName);
    }

    @Override
    public ScriptedFunctionsEditorModel getValue() {
        ScriptedFunctionsEditorModel model = new ScriptedFunctionsEditorModel();
        DurationFunction durationFunction = createDurationFunction();

        model.setFunctionType(ScriptedFunctions.CALCULATE_DURATION);
        model.setScriptFunction(durationFunction);
        return model;
    }

    @Override
    public List<ValidationAndFeedbackPair> getValidators() {
        return new ArrayList<ValidationAndFeedbackPair>();
    }

    @Override
    public void clear() {

    }

    @Override
    public void setUIFromModel(ScriptedFunctionsEditorModel dynamicFunctionsModel) {
        DurationFunction calculateDuration = (DurationFunction) dynamicFunctionsModel.getScriptFunction();

        if (null != calculateDuration) {

            setSelectedValue(field1ListBox, getFieldName(calculateDuration.getStartField(fieldList.getModelProxy())));
            setToFieldUI(calculateDuration);
            timeUnitsListBox.setSelectedValue(new DurationUnitLabelProvider().getLabel(calculateDuration.getUnit()));

        } else {

            Display.error(i18n.calculateDurationWidgetErrorTitle(), //$NON-NLS-1$
                    i18n.calculateDurationWidgetErrorMessage()); //$NON-NLS-1$

            dynamicFunctionsModel.setScriptFunction(createDurationFunction());
        }

    }

    @Override
    public void handleNameChange(String currentModelNameIn) {

        populateDropdowns(currentModelNameIn);
    }

    private void setSelectedValue(CsiStringListBox listBoxIn, String valueIn) {
        if (null != valueIn) {
            listBoxIn.setSelectedValue(valueIn);
        } else {
            listBoxIn.setSelectedIndex(-1);
        }
    }

    private String getFieldName(FieldDef fieldIn) {
        return (null != fieldIn) ? fieldIn.getFieldName() : null;
    }

    private void setToFieldUI(DurationFunction calculateDuration) {
        FieldDef endFieldDef = calculateDuration.getEndField(fieldList.getModelProxy());
        if (endFieldDef != null) {
            fieldRadio.setValue(Boolean.TRUE);
            field2ListBox.setSelectedValue(endFieldDef.getFieldName());
            grid.setWidget(1, 1, field2ListBox);
        }
        else {
            nowRadio.setValue(Boolean.TRUE);
            grid.setWidget(1,1,nowLabel);
        }
    }

    private DurationFunction createDurationFunction() {
        DurationFunction durationFunction = new DurationFunction();
        durationFunction.setOrdinal(0);

        durationFunction.setStartField(getStartFieldDef());
        durationFunction.setEndField(getEndFieldDef());
        durationFunction.setUnit(DurationUnit.valueOf(timeUnitsListBox.getSelectedValue()));

        return durationFunction;
    }

    private void buildGrid() {
        grid.setWidget(0, 0, new ControlLabel(i18n.fieldList_Field()));
        grid.setWidget(0, 1, new ControlLabel(i18n.fieldList_CalculationDuration_To()));
        grid.setWidget(0, 2, new ControlLabel(i18n.fieldList_CalculationDuration_TimeUnits()));
        grid.setWidget(1, 0, field1ListBox);
        grid.setWidget(1, 1, field2ListBox);
        grid.setWidget(1, 2, timeUnitsListBox);
    }

    private FieldDef getStartFieldDef() {
        return fieldList.getModelProxy().findFieldDefByName(field1ListBox.getSelectedValue());
    }

    private FieldDef getEndFieldDef() {
        if(nowRadio.getValue()){
            return null;
        }
        return fieldList.getModelProxy().findFieldDefByName(field2ListBox.getSelectedValue());
    }

    private HorizontalPanel createRadioButtonPanel() {
        HorizontalPanel panel = new HorizontalPanel();
        panel.add(fieldRadio);
        panel.add(nowRadio);
        panel.setCellWidth(fieldRadio, "165px"); //$NON-NLS-1$
        fieldRadio.setValue(Boolean.TRUE);

        fieldRadio.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                grid.setWidget(1, 1, field2ListBox);
            }
        });

        nowRadio.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                grid.setWidget(1, 1, nowLabel);
            }
        });

        return panel;
    }

    private void setSpacing() {
        field1ListBox.setWidth("140px"); //$NON-NLS-1$
        field2ListBox.setWidth("140px"); //$NON-NLS-1$
        timeUnitsListBox.setWidth("140px"); //$NON-NLS-1$
        nowLabel.setWidth("140px"); //$NON-NLS-1$

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 2; j++) {
                grid.getCellFormatter().setHorizontalAlignment(j, i, HasAlignment.ALIGN_CENTER);
                grid.getCellFormatter().setVerticalAlignment(j, i, HasAlignment.ALIGN_MIDDLE);
            }
        }
    }

    private void populateDropdowns(String currentModelNameIn) {
        List<FieldDef> myFieldList = fieldList.getModelProxy().getAlphaOrderedNonDependentFieldDefs();

        field1ListBox.clear();
        field2ListBox.clear();
        myFieldList.addAll(fieldList.getModelProxy().getAlphaOrderedScriptedFieldDefs());
        for (FieldDef myField : myFieldList) {
            String myFieldName = myField.getFieldName();
            if ((null == currentModelNameIn) || (0 == currentModelNameIn.length())
                    || ((!myFieldName.equals(currentModelNameIn))
                        && ((!FieldType.SCRIPTED.equals(myField.getFieldType()))
                            || (0 > myFieldName.compareTo(currentModelNameIn))))) {
                field1ListBox.addItem(myFieldName);
                field2ListBox.addItem(myFieldName);
            }
        }
        field1ListBox.setSelectedIndex(0);
        field2ListBox.setSelectedIndex(0);

        for (DurationUnit unit : DurationUnit.values()) {
            timeUnitsListBox.addItem(unit.name());
        }
    }

}
