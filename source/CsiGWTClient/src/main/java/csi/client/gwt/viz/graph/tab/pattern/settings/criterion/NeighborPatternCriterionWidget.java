package csi.client.gwt.viz.graph.tab.pattern.settings.criterion;

import com.github.gwtbootstrap.client.ui.CheckBox;
import com.github.gwtbootstrap.client.ui.ControlGroup;
import com.github.gwtbootstrap.client.ui.ControlLabel;
import com.github.gwtbootstrap.client.ui.Controls;
import com.github.gwtbootstrap.client.ui.Fieldset;
import com.github.gwtbootstrap.client.ui.FluidContainer;
import com.github.gwtbootstrap.client.ui.Form;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.github.gwtbootstrap.client.ui.constants.FormType;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Composite;

import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.shared.gwt.viz.graph.tab.pattern.settings.NeighborNodePatternCriterion;
import csi.shared.gwt.viz.graph.tab.pattern.settings.PatternCriterion;

public class NeighborPatternCriterionWidget extends Composite implements PatternCriterionWidget {
    private TextBox valueTextBox;
    private NeighborNodePatternCriterion criterion;
    private TextBox minValueTextBox;
    private TextBox maxValueTextBox;
    private TextBox typeTextBox;
    private CheckBox includeHiddenCheckBox;
    private CentrifugeConstants _constants = CentrifugeConstantsLocator.get();


    public NeighborPatternCriterionWidget() {
        FluidContainer fluidContainer = new FluidContainer();
        Form form = createForm();
        fluidContainer.add(form);
        initWidget(fluidContainer);
    }

    protected Fieldset createFieldSet() {
        Fieldset fieldset = new Fieldset();
        {
            ControlGroup controlGroup = createValueControlGroup();
            fieldset.add(controlGroup);
        }
        {
            ControlGroup row = createMinValueControlGroup();
            fieldset.add(row);
        }
        {
            ControlGroup row = createMaxValueRow();
            fieldset.add(row);
        }
        {
            ControlGroup row = createTypeRow();
            fieldset.add(row);
        }
        {
            ControlGroup controlGroup = createIncludeHiddenControlGroup();
            fieldset.add(controlGroup);
        }
        return fieldset;
    }

    protected Form createForm() {
        Form form = new Form();
        form.setType(FormType.HORIZONTAL);
        form.getElement().getStyle().setMarginLeft(-60, Style.Unit.PX);
        Fieldset fieldset = createFieldSet();
        form.add(fieldset);
        return form;
    }

    private ControlGroup createMinValueControlGroup() {
        ControlGroup controlGroup = new ControlGroup();
        {
            ControlLabel controlLabel = new ControlLabel(_constants.pattern_newCriterion_type_numberOfNeighbors_greaterThan());
            controlGroup.add(controlLabel);
        }
        Controls controls = new Controls();
        controlGroup.add(controls);
        {
            minValueTextBox = new TextBox();
            minValueTextBox.setSize(8);
            controls.add(minValueTextBox);
            minValueTextBox.addChangeHandler(new ChangeHandler() {
                @Override
                public void onChange(ChangeEvent event) {
                    criterion.setMinValue(minValueTextBox.getValue());
                }
            });
        }
        return controlGroup;
    }

    private ControlGroup createTypeRow() {
        ControlGroup controlGroup = new ControlGroup();
        {
            ControlLabel controlLabel = new ControlLabel(_constants.pattern_newCriterion_type_numberOfNeighbors_neighborType());
            controlGroup.add(controlLabel);
        }
        Controls controls = new Controls();
        controlGroup.add(controls);
        typeTextBox = new TextBox();
        typeTextBox.setSize(8);
        controls.add(typeTextBox);
        typeTextBox.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                criterion.setTypeOfNeighbor(typeTextBox.getValue());
            }
        });
        return controlGroup;
    }

    private ControlGroup createMaxValueRow() {
        ControlGroup controlGroup = new ControlGroup();
        {
            ControlLabel controlLabel = new ControlLabel(_constants.pattern_newCriterion_type_numberOfNeighbors_lessThan());
            controlGroup.add(controlLabel);
        }
        Controls controls = new Controls();
        controlGroup.add(controls);
        maxValueTextBox = new TextBox();
        maxValueTextBox.setSize(8);
        controls.add(maxValueTextBox);
        maxValueTextBox.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                criterion.setMaxValue(maxValueTextBox.getValue());
            }
        });
        return controlGroup;
    }

    private ControlGroup createIncludeHiddenControlGroup() {
        ControlGroup controlGroup = new ControlGroup();
        Controls controls = new Controls();
        controlGroup.add(controls);
        includeHiddenCheckBox = new CheckBox(_constants.pattern_newCriterion_type_numberOfNeighbors_includeHiddenNeighbors());
        controls.add(includeHiddenCheckBox);
        includeHiddenCheckBox.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                criterion.setIncludeHidden(Boolean.toString(includeHiddenCheckBox.getValue()));
            }
        });
        return controlGroup;
    }

    protected ControlGroup createValueControlGroup() {
        ControlGroup controlGroup = new ControlGroup();
        {
            ControlLabel label = new ControlLabel(_constants.pattern_newCriterion_type_numberOfNeighbors_equalTo());
            controlGroup.add(label);
        }
        Controls controls = new Controls();
        controlGroup.add(controls);
        valueTextBox = new TextBox();
        valueTextBox.setSize(8);
        controls.add(valueTextBox);
        valueTextBox.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                criterion.setValue(valueTextBox.getValue());
            }
        });
        return controlGroup;
    }

    @Override
    public PatternCriterion getCriterion() {
        return criterion;
    }

    @Override
    public void setCriterion(PatternCriterion criterion) {
        if (criterion instanceof NeighborNodePatternCriterion) {

            this.criterion = (NeighborNodePatternCriterion) criterion;
        } else {
            this.criterion = new NeighborNodePatternCriterion();
            this.criterion.setName(criterion.getName());
            this.criterion.setShowInResults(criterion.isShowInResults());
        }
        updateUXwithCurrentValues();
    }

    protected void updateUXwithCurrentValues() {
        valueTextBox.setValue(criterion.getValue());
        minValueTextBox.setValue(criterion.getMinValue());
        maxValueTextBox.setValue(criterion.getMaxValue());
        typeTextBox.setValue(criterion.getTypeOfNeighbor());
        includeHiddenCheckBox.setValue(Boolean.valueOf(criterion.getIncludeHidden()));
    }
}
