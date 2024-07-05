package csi.client.gwt.viz.shared.filter.selection.criteria;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.FluidContainer;
import com.github.gwtbootstrap.client.ui.FluidRow;
import com.google.gwt.user.client.ui.ScrollPanel;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.viz.chart.selection.ChartSelectionCard;
import csi.client.gwt.viz.chart.selection.DeleteListener;
import csi.client.gwt.viz.chart.selection.MeasuresSource;
import csi.client.gwt.viz.chart.selection.ValidationListener;
import csi.server.common.model.visualization.chart.ChartCriterion;

import java.util.ArrayList;
import java.util.List;

public class CriteriaView extends ScrollPanel {

    protected int _width = 670;
    protected int _height = 200;

    private static CentrifugeConstants i18n = CentrifugeConstantsLocator.get();
    // maybe i'll pull this one out to a sep class.
    protected FluidContainer criteriaContainer= new FluidContainer();
    // add new criteria to filter.
    protected Button addCriteria = new Button(i18n.chartSelectDialog_addCriterion());
    protected CriteriaValidator validator;
    private ValidationListener validationListener;
    protected MeasuresSource availableMeasures;


    // handles
    private DeleteListener deleteListener = ordinal -> {
        criteriaContainer.remove(ordinal);

        // re-index all cards in view
        int index = 0;
        while (index < criteriaContainer.getWidgetCount() - 1) {
            FluidRow contentRow = (FluidRow) criteriaContainer.getWidget(index);
            ChartSelectionCard card = (ChartSelectionCard) contentRow.getWidget(0);
            card.setOrdinal(index);
            index++;
        }

        // check if the view is valid to enable/disable buttons
        this.validationListener.checkCriteriaValidity();
    };


    public void setValidationListener(ValidationListener validationListener){
        this.validationListener = validationListener;
    }


    // we might need presenter
    public CriteriaView(MeasuresSource measures) {
        super();
        this.availableMeasures = measures;
        initView();
    }

    public CriteriaView(MeasuresSource measures, int w, int h) {
        super();
        if((w > 0 && h > 0)){
            this._height = h;
            this._width = w;
        }
        this.availableMeasures = measures;
        initView();
    }

    private void initView(){
        this.setSize(_width +"px", _height +"px");
        criteriaContainer.setWidth(_width - 30 + "px");
        criteriaContainer.addStyleName("selectDialogContent");
        // size the button
        addCriteria.setWidth(_width - 35 +"px");
        criteriaContainer.add(addCriteria);
        this.add(criteriaContainer);

        addCriteria.addClickHandler(event -> addCriterionCard());
    }

    public void addCriterionCard() {
        ChartSelectionCard card = new ChartSelectionCard(validationListener, deleteListener, availableMeasures, criteriaContainer.getWidgetCount() - 1, _width-30);
        FluidRow contentRow = new FluidRow();
        contentRow.addStyleName("selectDialogContentItem");
        contentRow.add(card);

        criteriaContainer.remove(addCriteria);
        criteriaContainer.add(contentRow);
        criteriaContainer.add(addCriteria);

        validationListener.checkCriteriaValidity();
    }

    public void updateMeasureLists() {
        int index = 0;
        while (index < criteriaContainer.getWidgetCount() - 1) {
            FluidRow contentRow = (FluidRow) criteriaContainer.getWidget(index);
            ChartSelectionCard card = (ChartSelectionCard) contentRow.getWidget(0);
            card.updateMeasureList();
            index++;
        }
    }

    public boolean isCriteriaValid() {
        int numCards = criteriaContainer.getWidgetCount() - 1;
        if (numCards == 0) {
            return true;
        } else {
            int index = 0;
            while (index < numCards) {
                FluidRow contentRow = (FluidRow) criteriaContainer.getWidget(index);
                ChartSelectionCard card = (ChartSelectionCard) contentRow.getWidget(0);
                if (!card.isValid()) return false;
                index++;
            }
            return true;
        }
    }

    public void addCriterionCard(ChartCriterion crit) {
        ChartSelectionCard card = new ChartSelectionCard(this::isCriteriaValid, deleteListener, availableMeasures, criteriaContainer.getWidgetCount() - 1, _width-30);
        card.setCriterion(crit);

        FluidRow contentRow = new FluidRow();
        contentRow.addStyleName("selectDialogContentItem");
        contentRow.add(card);

        criteriaContainer.remove(addCriteria);
        criteriaContainer.add(contentRow);
        criteriaContainer.add(addCriteria);
    }

    public List<ChartCriterion> getCriteria() {
        List<ChartCriterion> criteria = new ArrayList<ChartCriterion>();
        int index = 0;
        while (index < criteriaContainer.getWidgetCount() - 1) {
            FluidRow contentRow = (FluidRow) criteriaContainer.getWidget(index);
            ChartSelectionCard card = (ChartSelectionCard) contentRow.getWidget(0);
            criteria.add(card.getCriterion());
            index++;
        }

        return criteria;
    }
}
