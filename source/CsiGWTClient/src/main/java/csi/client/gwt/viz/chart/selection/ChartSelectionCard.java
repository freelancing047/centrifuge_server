package csi.client.gwt.viz.chart.selection;

import java.util.ArrayList;
import java.util.List;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.Column;
import com.github.gwtbootstrap.client.ui.FluidContainer;
import com.github.gwtbootstrap.client.ui.FluidRow;
import com.github.gwtbootstrap.client.ui.constants.ButtonType;
import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.github.gwtbootstrap.client.ui.resources.ButtonSize;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.ui.Composite;
import com.sencha.gxt.data.shared.LabelProvider;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;

import com.sencha.gxt.widget.core.client.info.Info;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.viz.chart.selection.criterion.ChartCriterionPresenter;
import csi.client.gwt.viz.graph.tab.pattern.settings.InputEvent;
import csi.client.gwt.viz.graph.tab.pattern.settings.InputHandler;
import csi.client.gwt.widget.combo_boxes.StringComboBox;
import csi.server.common.model.visualization.chart.ChartCriterion;

public class ChartSelectionCard extends Composite {
	private static CentrifugeConstants i18n = CentrifugeConstantsLocator.get();
	private static LabelProvider<String> labelProvider;
    private int cardWidth = 162;

    private static LabelProvider<String> getLabelProvider() {
		if (labelProvider == null) {
			labelProvider = new LabelProvider<String>() {
				@Override
				public String getLabel(String item) {
					switch (item) {
					case "<":
						return i18n.chartSelectDialog_lt();
					case "<=":
						return i18n.chartSelectDialog_le();
					case "==":
						return i18n.chartSelectDialog_eq();
					case ">=":
						return i18n.chartSelectDialog_ge();
					case ">":
						return i18n.chartSelectDialog_gt();
					case "!=":
						return i18n.chartSelectDialog_ne();
					case "<<":
						return i18n.chartSelectDialog_btw();
					case "Top":
						return i18n.chartSelectDialog_top();
					case "Top%":
						return i18n.chartSelectDialog_topPercent();
					case "Bottom":
						return i18n.chartSelectDialog_bottom();
					case "Bottom%":
						return i18n.chartSelectDialog_bottomPercent();
					default:
						return "";
					}
				}
			};
		}
		return labelProvider;
	}

	private ValidationListener validationListener;
	private DeleteListener deleteListener;
	private int ordinal;
	private FluidContainer container = new FluidContainer();
	private Column cell02;
	private FluidRow row1;
	private StringComboBox measureList;
	private Button button;
	private StringComboBox operatorList;
	private ListStore<String> listStore;
	private ChartCriterionPresenter criterionPresenter;
	private MeasuresSource measuresSource;
	
	public ChartSelectionCard(ValidationListener validationListener, DeleteListener deleteListener, MeasuresSource measuresSource, int ordinal) {
		this.validationListener = validationListener;
		this.deleteListener = deleteListener;
		this.measuresSource = measuresSource;
		this.ordinal = ordinal;
		criterionPresenter = new ChartCriterionPresenter(validationListener);
		setupStructure();
		addHandlers();
		initWidget(container);
	}

	public ChartSelectionCard(ValidationListener validationListener, DeleteListener deleteListener, MeasuresSource measuresSource, int ordinal, int width) {
		this.validationListener = validationListener;
		this.deleteListener = deleteListener;
		this.measuresSource = measuresSource;
		this.ordinal = ordinal;

		if(width > 0 && width > 200){
            this._width = width;
            this.cardWidth = width/2 - 75;
        }


        criterionPresenter = new ChartCriterionPresenter(validationListener);
        setupStructure();
        addHandlers();
        initWidget(container);
	}

	private int _width = 374;

	
	public void setMeasuresSource(MeasuresSource measuresSource) {
		this.measuresSource = measuresSource;
	}

	private void setupStructure() {
		container.setWidth(100+"%");
		container.addStyleName("selectCardContainer");
        FluidRow wrapper = new FluidRow();
        Column filter = new Column(11);


        FluidRow row0 = new FluidRow();
        row0.addStyleName("selectCardRow0");
        row1=row0;

        createMeasureList();
        row0.add(measureList);


        setupOperatorsIfNull();
        row0.add(operatorList);



        // i wanna change this too. - must be right aligned.

        button = new Button();
        button.setSize(ButtonSize.MINI);
        button.setIcon(IconType.REMOVE);
        button.setType(ButtonType.LINK);
        button.addStyleName("selectCardRemove");
//        row0.add(button);
        filter.add(row0);
        Column closeButton = new Column(1);
        closeButton.add(button);
        wrapper.add(filter);
        wrapper.add(closeButton);
        container.add(wrapper);
    }

	private void createMeasureList() {
		measureList = new StringComboBox();
        {
            Style style = measureList.getElement().getStyle();
            style.setMargin(2, Style.Unit.PX);
        }
		measureList.setForceSelection(true);
		measureList.setEmptyText(i18n.chartSelectDialog_selectMeasure());
		measureList.setWidth(150);
		populateMeasureList(true);
		handleMeasureListEvents();
	}

	private void populateMeasureList(boolean setDefault) {
		ListStore<String> measureStore = measureList.getStore();
		measureStore.clear();
		List<String> measures = getHeaders();

		measureStore.add("");
		if (measures.size() == 2) {
			String selectedText = measures.get(1);
			measureStore.add(selectedText);
			if (setDefault) {
				measureList.setText(selectedText);
				measureList.select(1);
				handleMeasureSelectionList();
			}
		} else {
			int i = 1;
			while (i < measures.size()) {
				measureStore.add(measures.get(i));
				i++;
			}
		}
	}
	
	private List<String> getHeaders() {
		List<String> headers = new ArrayList<String>();
		headers.add("");
		measuresSource.fillHeaders(headers);
		return headers;
	}

	private void handleMeasureSelectionList() {
		setupOperatorsIfNull();
		if (measureList.getSelectedIndex() > 0) {
			int columnIndex = measureList.getSelectedIndex();
			criterionPresenter.setColumnIndex(columnIndex);
			criterionPresenter.setColumnHeader(measureList.getStore().get(columnIndex));
		}
		validationListener.checkCriteriaValidity();
	}

	private void setupOperatorsIfNull() {
		if (operatorList == null) {
			createOperators();
		}
	}

	private void createOperators() {
		operatorList = new StringComboBox(getListStore(), getLabelProvider());
        {
            Style style = operatorList.getElement().getStyle();
            style.setMargin(2, Style.Unit.PX);
        }
		operatorList.setForceSelection(true);
		operatorList.setEmptyText(i18n.chartSelectDialog_selectOperator());
		operatorList.setWidth(150);
		handleOperatorListEvents();
	}

	private ListStore<String> getListStore() {
		if (listStore == null) {
			listStore = new ListStore<String>(new ModelKeyProvider<String>(){
				@Override
				public String getKey(String item) {
					return item;
				}
			});

			listStore.add("");
			listStore.add("<");
			listStore.add("<=");
			listStore.add("==");
			listStore.add(">=");
			listStore.add(">");
			listStore.add("!=");
			listStore.add("<<");
			listStore.add("Top");
			listStore.add("Top%");
			listStore.add("Bottom");
			listStore.add("Bottom%");
		}
		return listStore;
	}

	private void handleOperatorListEvents() {
		operatorList.addSelectionHandler(new SelectionHandler<String>() {
			@Override
			public void onSelection(SelectionEvent<String> event) {
				String operatorString = event.getSelectedItem();
				assignOperator(operatorString);
			}
		});
		operatorList.addBitlessDomHandler(new InputHandler() {
            @Override
            public void onInput(InputEvent inputEvent) {
            	StringComboBox stringComboBox = (StringComboBox) inputEvent.getSource();
				String operatorString = stringComboBox.getText();
				assignOperator(operatorString);
            }

        }, InputEvent.getType());
	}

	private void assignOperator(String operatorString) {
		criterionPresenter.assignOperator(operatorString);
        if(row1.getWidgetCount() > 2) {
            while (row1.getWidgetCount() != 2) {
                row1.remove(2);
            }
        }
		criterionPresenter.setup(row1);
		validationListener.checkCriteriaValidity();
	}

	private void handleMeasureListEvents() {
		measureList.addSelectionHandler(new SelectionHandler<String>() {
			@Override
			public void onSelection(SelectionEvent<String> event) {
				handleMeasureSelectionList();
			}
		});
	}

	private void addHandlers() {
		button.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				deleteListener.notifyDelete(ordinal);
			}
		});
	}
	
	public ChartCriterion getCriterion() {
	    updateMeasureList();
		return criterionPresenter.getCriterion();
	}

	public boolean isValid() {
		List<String> measures = getHeaders();
		if (measures.contains(criterionPresenter.getColumnHeader())) {
            return criterionPresenter.isValid();
		} else {
			return false;
		}
	}

	public void updateMeasureList() {
		populateMeasureList(false);

		List<String> measures = getHeaders();
		String columnHeader = criterionPresenter.getColumnHeader();
		if (measures.contains(columnHeader)) {
			measureList.select(measureList.getStore().indexOf(columnHeader));
			criterionPresenter.setColumnIndex(measures.indexOf(columnHeader));
		} else {
			measureList.select(0);
		}
		measureList.setText(criterionPresenter.getColumnHeader());
	}

	public void setOrdinal(int ordinal) {
		this.ordinal = ordinal;
	}
	
	public void setCriterion(ChartCriterion criterion) {
		criterionPresenter.setCriterion(criterion);
		criterionPresenter.setColumnHeader(criterion.getColumnHeader());
		updateMeasureList();
		setupOperatorsIfNull();
		String operatorString = criterion.getOperatorString();
		operatorList.select(operatorList.getStore().indexOf(operatorString));
		operatorList.setText(getLabelProvider().getLabel(operatorString));
		criterionPresenter.setup(row1);
	}
}
