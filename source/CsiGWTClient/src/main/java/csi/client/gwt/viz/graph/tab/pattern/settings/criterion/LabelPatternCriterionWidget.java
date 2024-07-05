package csi.client.gwt.viz.graph.tab.pattern.settings.criterion;

import com.github.gwtbootstrap.client.ui.FluidContainer;
import com.github.gwtbootstrap.client.ui.FluidRow;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.InlineLabel;

import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.shared.gwt.viz.graph.tab.pattern.settings.LabelNodePatternCriterion;
import csi.shared.gwt.viz.graph.tab.pattern.settings.PatternCriterion;

public class LabelPatternCriterionWidget extends Composite implements PatternCriterionWidget {
    private final TextBox valueBox;
    private PatternCriterion criterion;

    public LabelPatternCriterionWidget() {
        FluidContainer con = new FluidContainer();
        {
            FluidRow row = new FluidRow();
            row.add(new InlineLabel(CentrifugeConstantsLocator.get().labelCriteria_mustEqualLabel()));
            valueBox = new TextBox();
            row.add(valueBox);
            con.add(row);
            {
                valueBox.addChangeHandler(new ChangeHandler() {
                    @Override
                    public void onChange(ChangeEvent event) {
                        criterion.setValue(valueBox.getValue());
                    }
                });
            }
        }
//        {
//            FluidRow row = new FluidRow();
//            row.add(new InlineLabel("Tooltip Value"));
//            TextBox ValueBox = new TextBox();
//            row.add(ValueBox);
//            con.add(row);
//        }
        initWidget(con);
    }

    @Override
    public PatternCriterion getCriterion() {
        return criterion;
    }

    @Override
    public void setCriterion(PatternCriterion criterion) {
        if (criterion instanceof LabelNodePatternCriterion) {

            this.criterion = criterion;
        }
        else {
            this.criterion = new LabelNodePatternCriterion();
            this.criterion.setName(criterion.getName());
            this.criterion.setShowInResults(criterion.isShowInResults());
        }
        valueBox.setValue(criterion.getValue());
    }
}
