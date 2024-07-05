package csi.client.gwt.viz.graph.tab.pattern.settings.criterion;

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
import com.google.gwt.user.client.ui.Composite;

import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.shared.gwt.viz.graph.tab.pattern.settings.OccurrencePatternCriterion;
import csi.shared.gwt.viz.graph.tab.pattern.settings.PatternCriterion;

public class OccurrencePatternCriterionWidget extends Composite implements PatternCriterionWidget {
    private TextBox valueTextBox;
    private OccurrencePatternCriterion criterion;
    private TextBox minValueTextBox;
    private TextBox maxValueTextBox;
    private CentrifugeConstants _constants = CentrifugeConstantsLocator.get();


    public OccurrencePatternCriterionWidget() {
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
            ControlLabel controlLabel = new ControlLabel(_constants.pattern_newCriterion_type_occurrence_greaterThan());
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

    private ControlGroup createMaxValueRow() {
        ControlGroup controlGroup = new ControlGroup();
        {
            ControlLabel controlLabel = new ControlLabel(_constants.pattern_newCriterion_type_occurrence_lessThan());
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

    protected ControlGroup createValueControlGroup() {
        ControlGroup controlGroup = new ControlGroup();
        {
            ControlLabel label = new ControlLabel(_constants.pattern_newCriterion_type_occurrence_equalTo());
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
        if (criterion instanceof OccurrencePatternCriterion) {

            this.criterion = (OccurrencePatternCriterion) criterion;
        } else {
            this.criterion = new OccurrencePatternCriterion();
            this.criterion.setName(criterion.getName());
            this.criterion.setShowInResults(criterion.isShowInResults());
        }
        updateUXwithCurrentValues();
    }

    protected void updateUXwithCurrentValues() {
        valueTextBox.setValue(criterion.getValue());
        minValueTextBox.setValue(criterion.getMinValue());
        maxValueTextBox.setValue(criterion.getMaxValue());
    }
}
