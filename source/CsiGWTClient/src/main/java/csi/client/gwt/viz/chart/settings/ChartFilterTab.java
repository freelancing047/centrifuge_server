package csi.client.gwt.viz.chart.settings;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.FluidContainer;
import com.github.gwtbootstrap.client.ui.FluidRow;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.viz.chart.selection.ChartSelectionCard;
import csi.client.gwt.viz.chart.selection.DeleteListener;
import csi.client.gwt.viz.chart.selection.MeasuresSource;
import csi.client.gwt.viz.chart.selection.ValidationListener;
import csi.server.common.model.visualization.chart.ChartCriterion;
import csi.server.common.model.visualization.chart.ChartSettings;
import csi.server.common.model.visualization.chart.DrillChartViewDef;
import csi.server.common.model.visualization.chart.MeasureDefinition;

import java.util.ArrayList;
import java.util.List;

public class ChartFilterTab extends ChartSettingsComposite {
	private static CentrifugeConstants i18n = CentrifugeConstantsLocator.get();

	private static ChartFilterTabUiBinder uiBinder = GWT.create(ChartFilterTabUiBinder.class);

	interface ChartFilterTabUiBinder extends UiBinder<Widget, ChartFilterTab> {
	}

	@UiField
	ScrollPanel scrollPanel;

	private FluidContainer cards = new FluidContainer();
	private Button buttonAddCriterion = new Button(i18n.chartSelectDialog_addCriterion());
    private ChartMeasuresTab measuresTab;

	public ChartFilterTab() {
		initWidget(uiBinder.createAndBindUi(this));
		setupStructure();
		addHandlers();
	}

	private void setupStructure() {
        cards.setWidth("650px");
        cards.addStyleName("selectDialogContent");
        buttonAddCriterion.setWidth("650px");
        cards.add(buttonAddCriterion);
        scrollPanel.add(cards);
    }
	private void addHandlers() {
		buttonAddCriterion.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				addCriterion();
			}
		});
	}

	private ValidationListener validationListener = new ValidationListener() {
		@Override
		public void checkCriteriaValidity() {
			// Do Nothing
		}
	};

	private DeleteListener deleteListener = new DeleteListener() {
		@Override
		public void notifyDelete(int ordinal) {
			cards.remove(ordinal);
			int index = 0;
			while (index < cards.getWidgetCount() - 1) {
				FluidRow contentRow = (FluidRow) cards.getWidget(index);
				ChartSelectionCard card = (ChartSelectionCard) contentRow.getWidget(0);
				card.setOrdinal(index);
				index++;
			}
		}
	};

	private MeasuresSource measuresSource = new MeasuresSource() {
		@Override
		public void fillHeaders(List<String> headers) {
			ChartSettings chartSettings = measuresTab.getDrillChartSettings();
			if (chartSettings.isUseCountStarForMeasure() || measuresTab.getCurrentMeasures().size() == 0) {
				headers.add("Count (*)");
			} else {
				for (MeasureDefinition measureDefinition : measuresTab.getCurrentMeasures()) {
					headers.add(measureDefinition.getComposedName());
				}
			}
		}
	};

	private void addCriterion() {
		ChartSelectionCard card = new ChartSelectionCard(validationListener, deleteListener, measuresSource, cards.getWidgetCount() - 1, 700);
		FluidRow contentRow = new FluidRow();
		contentRow.addStyleName("selectDialogContentItem");
		contentRow.add(card);
		cards.remove(buttonAddCriterion);
		cards.add(contentRow);
		cards.add(buttonAddCriterion);
	}

	@Override
	public void updateViewFromModel() {
		DrillChartViewDef vizDef = (DrillChartViewDef)getVisualizationSettings().getVisualizationDefinition();
		List<ChartCriterion> criteria = vizDef.getChartSettings().getFilterCriteria();
		if (criteria == null) return;
		for (ChartCriterion criterion : criteria) {
			ChartSelectionCard card = new ChartSelectionCard(validationListener, deleteListener, measuresSource, cards.getWidgetCount() - 1);
			card.setCriterion(criterion);
			FluidRow contentRow = new FluidRow();
			contentRow.addStyleName("selectDialogContentItem");
			contentRow.add(card);
			cards.remove(buttonAddCriterion);
			cards.add(contentRow);
			cards.add(buttonAddCriterion);
		}
	}

	@Override
	public void updateModelWithView() {
		DrillChartViewDef vizDef = (DrillChartViewDef)getVisualizationSettings().getVisualizationDefinition();
		List<ChartCriterion> criteria = new ArrayList<ChartCriterion>();
		int index = 0;
		while (index < cards.getWidgetCount() - 1) {
			FluidRow contentRow = (FluidRow) cards.getWidget(index);
			ChartSelectionCard card = (ChartSelectionCard) contentRow.getWidget(0);
			criteria.add(card.getCriterion());
			index++;
		}
		vizDef.getChartSettings().setFilterCriteria(criteria);
	}

	public boolean isCriteriaValid() {
		int numCards = cards.getWidgetCount() - 1;
		if (numCards == 0) {
			return true;
		} else {
			List<String> headers = new ArrayList<String>();
			measuresSource.fillHeaders(headers);
			int index = 0;
			while (index < numCards) {
				FluidRow contentRow = (FluidRow) cards.getWidget(index);
				ChartSelectionCard card = (ChartSelectionCard) contentRow.getWidget(0);
				if (!card.isValid()) return false;
				if (!headers.contains(card.getCriterion().getColumnHeader())) return false;
				index++;
			}
			return true;
		}
	}

	public void setMeasuresTab(ChartMeasuresTab measuresTab) {
		this.measuresTab = measuresTab;
	}

	public void updateHeaders() {
		int numCards = cards.getWidgetCount() - 1;
		int index = 0;
		while (index < numCards) {
			FluidRow contentRow = (FluidRow) cards.getWidget(index);
			ChartSelectionCard card = (ChartSelectionCard) contentRow.getWidget(0);
			card.updateMeasureList();
			index++;
		}
	}
}
