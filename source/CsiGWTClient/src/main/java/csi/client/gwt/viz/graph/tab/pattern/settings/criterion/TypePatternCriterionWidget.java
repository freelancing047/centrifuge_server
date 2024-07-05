package csi.client.gwt.viz.graph.tab.pattern.settings.criterion;

import com.github.gwtbootstrap.client.ui.FluidRow;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.InlineLabel;

import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.shared.gwt.viz.graph.tab.pattern.settings.PatternCriterion;
import csi.shared.gwt.viz.graph.tab.pattern.settings.TypeNodePatternCriterion;

public class TypePatternCriterionWidget extends Composite implements PatternCriterionWidget {
    private final TextBox box;
    private PatternCriterion criterion;

    public TypePatternCriterionWidget() {
        FluidRow row = new FluidRow();
        InlineLabel label = new InlineLabel(CentrifugeConstantsLocator.get().typeCriteria_valueLabel());
        label.getElement().getStyle().setPaddingRight(10.0D, Style.Unit.PX);
        row.add(label);
        box = new TextBox();
        box.addChangeHandler(new ChangeHandler() {
            public void onChange(ChangeEvent event) {
                criterion.setValue(box.getValue());
            }
        });

        row.add(box);
        initWidget(row);
    }

    @Override
    public PatternCriterion getCriterion() {
        return criterion;
    }

    @Override
    public void setCriterion(PatternCriterion criterion) {
        if (criterion instanceof TypeNodePatternCriterion) {
            this.criterion = criterion;
        } else {
            this.criterion = new TypeNodePatternCriterion();
            this.criterion.setName(criterion.getName());
            this.criterion.setShowInResults(criterion.isShowInResults());
        }
        box.setValue(this.criterion.getValue());
    }
}
