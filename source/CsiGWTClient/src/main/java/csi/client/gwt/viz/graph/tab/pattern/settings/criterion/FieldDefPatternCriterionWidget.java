package csi.client.gwt.viz.graph.tab.pattern.settings.criterion;

import com.github.gwtbootstrap.client.ui.FluidContainer;
import com.github.gwtbootstrap.client.ui.FluidRow;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.Composite;

import csi.client.gwt.WebMain;
import csi.client.gwt.dataview.AbstractDataViewPresenter;
import csi.client.gwt.util.FieldDefUtils;
import csi.client.gwt.widget.combo_boxes.FieldDefComboBox;
import csi.server.common.enumerations.CsiDataType;
import csi.server.common.model.FieldDef;
import csi.server.common.model.FieldType;
import csi.shared.gwt.viz.graph.tab.pattern.settings.FieldDefNodePatternCriterion;
import csi.shared.gwt.viz.graph.tab.pattern.settings.PatternCriterion;

public class FieldDefPatternCriterionWidget extends Composite implements PatternCriterionWidget {

    private final TextBox valueBox;
    private final FieldDefComboBox fieldDefComboBox;
    FieldDefNodePatternCriterion criterion;

    public FieldDefPatternCriterionWidget() {
        FluidContainer con = new FluidContainer();
        {
            FluidRow row = new FluidRow();
            fieldDefComboBox = new FieldDefComboBox();
            AbstractDataViewPresenter dataview = WebMain.injector.getMainPresenter().getDataViewPresenter(true);

            fieldDefComboBox.getStore().addAll(FieldDefUtils.getSortedNonStaticFields(dataview.getDataView().getMeta().getModelDef(),
                    FieldDefUtils.SortOrder.ALPHABETIC));
            fieldDefComboBox.addValueChangeHandler(new ValueChangeHandler<FieldDef>() {
                @Override
                public void onValueChange(ValueChangeEvent<FieldDef> event) {
                    criterion.setFieldName(fieldDefComboBox.getValue().getFieldName());
                }
            });
            row.add(fieldDefComboBox);
            con.add(row);
        }
        {
            FluidRow row = new FluidRow();

            valueBox = new TextBox();
            row.add(valueBox);
            valueBox.addChangeHandler(new ChangeHandler() {
                @Override
                public void onChange(ChangeEvent event) {
                    criterion.setValue(valueBox.getValue());
                }
            });

            con.add(row);
        }
        initWidget(con);
    }

    @Override
    public PatternCriterion getCriterion() {
        return criterion;
    }

    public void setCriterion(PatternCriterion criterion) {

        if (criterion instanceof FieldDefNodePatternCriterion) {
            this.criterion = (FieldDefNodePatternCriterion) criterion;
        } else {
            this.criterion = new FieldDefNodePatternCriterion();
            this.criterion.setName(criterion.getName());
            this.criterion.setShowInResults(criterion.isShowInResults());
        }
        if (this.criterion.getValue()!= null) {
            valueBox.setValue(criterion.getValue());
        }
        if(this.criterion.getFieldName() != null) {
            FieldDef fieldDef = new FieldDef(this.criterion.getFieldName(), FieldType.STATIC, CsiDataType.String);//NON-NLS
            fieldDefComboBox.setValue(fieldDef);
        }
    }
}
