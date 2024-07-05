package csi.client.gwt.viz.matrix.settings;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.SimpleLayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import csi.client.gwt.WebMain;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.viz.chart.selection.MeasuresSource;
import csi.client.gwt.viz.shared.filter.selection.MeasureSourceProvider;
import csi.client.gwt.viz.shared.filter.selection.criteria.CriteriaView;
import csi.server.common.model.visualization.chart.ChartCriterion;
import csi.server.common.model.visualization.matrix.MatrixMeasureDefinition;
import csi.server.common.model.visualization.matrix.MatrixSettings;
import csi.server.common.model.visualization.matrix.MatrixViewDef;

import java.util.List;

public class MatrixFilterTab extends MatrixSettingsComposite {

    private MatrixMeasuresTab measuresTab;


    public void setMeasuresTab(MatrixMeasuresTab tabMeasure) {
        measuresTab = tabMeasure;
    }

    interface SpecificUiBinder extends UiBinder<Widget, MatrixFilterTab> {
    }

    private static MatrixFilterTab.SpecificUiBinder uiBinder = GWT.create(MatrixFilterTab.SpecificUiBinder.class);
    @UiField
    SimpleLayoutPanel layoutPanel;

    CriteriaView view;

    public MatrixFilterTab() {
        super();
        initWidget(uiBinder.createAndBindUi(this));
    }

    private MeasuresSource measuresSource = new MeasuresSource() {
        @Override
        public void fillHeaders(List<String> headers) {
            MatrixSettings matrixSettings = measuresTab.getMatrixSettings();
            MatrixViewDef def = getVisualizationSettings().getVisualizationDefinition();
            MatrixSettings settings = def.getMatrixSettings();
            if (matrixSettings.isUseCountForMeasure() || measuresTab.getCurrentMeasure(settings) == null) {
                headers.add(CentrifugeConstantsLocator.get().matrixMeasure());
            } else {
                MatrixMeasureDefinition measureDefinition = measuresTab.getCurrentMeasure(settings);
                headers.add(measureDefinition.getComposedName());
            }
        }
    };

    public void init(){
        measuresTab.updateModelWithView();
        MeasuresSource measure = measuresSource;
        view = new CriteriaView(measure, 700, 290);
        view.setValidationListener(() -> {
//            setButtonsEnabled(criteriaList.isCriteriaValid());
        });
        layoutPanel.add(view);

    }

    @Override
    public void updateViewFromModel() {
        if(view == null){
            init();
        }

        List<ChartCriterion> filterCriteria = getMatrixSettings().getFilterCriteria();
        if (filterCriteria == null) return;
//        view.
        filterCriteria.forEach(chartCriterion -> view.addCriterionCard(chartCriterion));
    }

    @Override
    public void updateModelWithView() {
        if(getMatrixSettings().getFilterCriteria() != null) {
            getMatrixSettings().getFilterCriteria().clear();
        }

        List<ChartCriterion> criteria = view.getCriteria();
        if(criteria.size() > 0){
            getMatrixSettings().setFilterCriteria(criteria);
        }

    }



}
