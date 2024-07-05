package csi.client.gwt.dataview.directed;

import java.util.ArrayList;
import java.util.List;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.Column;
import com.github.gwtbootstrap.client.ui.FluidContainer;
import com.github.gwtbootstrap.client.ui.FluidRow;
import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.github.gwtbootstrap.client.ui.resources.ButtonSize;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.sencha.gxt.cell.core.client.form.ComboBoxCell;
import com.sencha.gxt.cell.core.client.form.ComboBoxCell.TriggerAction;
import com.sencha.gxt.data.shared.LabelProvider;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.widget.core.client.Window;
import com.sencha.gxt.widget.core.client.form.ComboBox;

import csi.client.gwt.dataview.DataViewPresenter;
import csi.client.gwt.dataview.directed.visualization.DirectedWindow;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.mainapp.MessagesDialog;
import csi.client.gwt.validation.multi.MultiValidatorCollectingErrors;
import csi.client.gwt.validation.multi.ValidationAndFeedbackPair;
import csi.client.gwt.validation.validator.ComboBoxBlankValidator;
import csi.client.gwt.viz.Visualization;
import csi.client.gwt.viz.chart.view.ChartValidationFeedback;
import csi.client.gwt.viz.map.presenter.MapPresenter;
import csi.client.gwt.viz.shared.chrome.panel.VizPanel;
import csi.client.gwt.widget.boot.MaskDialog;
import csi.client.gwt.widget.ui.FullSizeLayoutPanel;
import csi.client.gwt.widget.ui.ResizeableAbsolutePanel;


public class SelectView extends VizLayout{

	private DirectedPresenter presenter;
	private List<DirectedWindow> windows = new ArrayList<DirectedWindow>();

	private static CentrifugeConstants i18n = CentrifugeConstantsLocator.get();

	protected MultiValidatorCollectingErrors validators = new MultiValidatorCollectingErrors();

	@UiField
	FullSizeLayoutPanel mainContainer;

	@UiField
	ResizeableAbsolutePanel panel;

	HorizontalPanel container;

	private ComboBox<Visualization> availableVisualizations;
	private ListStore<Visualization> listStore ;
	private Visualization lastSelected = null;
    private MaskDialog mask = null;
    private Button tMessengerButton;
    private MessagesDialog messagesDialog;


	public Visualization getVizualization() {
		return availableVisualizations.getCurrentValue();
	}

	interface SpecificUiBinder extends UiBinder<FullSizeLayoutPanel, SelectView> {
	}

	private static SpecificUiBinder uiBinder = GWT.create(SpecificUiBinder.class);

	public SelectView(DirectedPresenter presenter){
		mainContainer = uiBinder.createAndBindUi(this);
		initWidget(mainContainer);
		this.presenter = presenter;
		listStore = new ListStore<Visualization>(new ModelKeyProvider<Visualization>(){
			@Override
			public String getKey(Visualization viz) {
				return viz.getName() + ":" + viz.getType().toString(); //$NON-NLS-1$
			}});

		final List<Visualization> visualizations = presenter.getVisualizations();

		//reverse order of the list
		if(!visualizations.isEmpty()){
			for(int ii=visualizations.size()-1; ii>=0; ii--){
				listStore.add(visualizations.get(ii));
			}
		}
		container = new HorizontalPanel();
		ComboBoxCell<Visualization> vizList = new ComboBoxCell<Visualization>(listStore, new StringLabelProvider());

		availableVisualizations = new ComboBox<Visualization>(vizList);
		availableVisualizations.getElement().setAttribute("spellcheck", "false"); //$NON-NLS-1$ //$NON-NLS-2$
		availableVisualizations.setTypeAhead(true);
		availableVisualizations.setForceSelection(true);
		availableVisualizations.setTriggerAction(TriggerAction.ALL);
		availableVisualizations.setWidth(400);
		availableVisualizations.getElement().getStyle().setPaddingTop(4, Unit.PX);
		availableVisualizations.getElement().getStyle().setPaddingLeft(4, Unit.PX);
		availableVisualizations.getElement().getStyle().setPaddingBottom(4, Unit.PX);

		availableVisualizations.addStyleName("remove-margin"); //$NON-NLS-1$

		availableVisualizations.addSelectionHandler(new SelectionHandler<Visualization>(){

			@Override
			public void onSelection(SelectionEvent<Visualization> event) {
				isValid();

				Visualization visualization = event.getSelectedItem();
				if(event.getSelectedItem() == null){

					availableVisualizations.setValue(lastSelected);
				} else {

					lastSelected = visualization;
					availableVisualizations.setValue(visualization);
					onResize();
				}
				if (visualization instanceof MapPresenter) {
					MapPresenter visualization2 = (MapPresenter) visualization;
//					if (visualization2.isMapNeedsLoad()) {
						visualization2.reload();
//					}
				}

			}});

		container.getElement().addClassName("FUCKTEST");


		tMessengerButton = new Button();
		tMessengerButton.addStyleName("TEsTBOY");
		tMessengerButton.setIcon(IconType.COMMENT_ALT);
		tMessengerButton.getElement().setAttribute("font-size", "20px");
		tMessengerButton.getElement().getStyle().setProperty("background", "none");
		tMessengerButton.getElement().getStyle().setProperty("border", "none");
		tMessengerButton.getElement().getStyle().setProperty("color", "black");
		tMessengerButton.getElement().getStyle().setProperty("boxShadow", "none");
		tMessengerButton.getElement().getStyle().setProperty("textShadow", "none");
		tMessengerButton.getElement().getStyle().setPaddingTop(7, Unit.PX);
		tMessengerButton.getElement().getStyle().setMarginLeft(25, Unit.PX);
		tMessengerButton.setSize(ButtonSize.LARGE);
		SpanElement caret = Document.get().createSpanElement();
		tMessengerButton.getElement().appendChild(caret);
		tMessengerButton.setVisible(true);


		container.add(availableVisualizations);
		container.setCellWidth(availableVisualizations, "400");
		container.add(tMessengerButton);

		mainContainer.add(container);

		messagesDialog = new MessagesDialog(presenter.getDataView(), presenter);

		tMessengerButton.addClickHandler(event -> {
			messagesDialog.show();
		});
		ComboBoxBlankValidator notBlankValidator = new ComboBoxBlankValidator(availableVisualizations);
		validators.addValidationAndFeedback(new ValidationAndFeedbackPair(notBlankValidator, ChartValidationFeedback.EMPTY_CATEGORY_FEEDBACK));

	}

	@Override
	protected void onAttach() {
		super.onAttach();
		Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
			@Override
			public void execute() {
				presenter.loadVisualizations();
				if(mask != null){
                    mask.hide();
                }
			}
		});
	}

	@Override
	public void addVisualization(Visualization visualization){

	    vizCount++;
		DirectedWindow window = createWindow(visualization);
		windows.add(window);
		panel.add(window);
		window.show();

		if(availableVisualizations != null){
			listStore.add(visualization);
			lastSelected=visualization;


			if(listStore.size() > 1){
				availableVisualizations.setValue(listStore.get(0));
			} else {
				availableVisualizations.setValue(visualization);
			}
			onResize();
		}


	}

	private DirectedWindow createWindow(Visualization visualization) {
		final DirectedWindow window = new DirectedWindow(panel);

		VizPanel vizPanel = new VizPanel(presenter);
		vizPanel.setFrameProvider(window, visualization.getName());
		vizPanel.setVisualization(visualization);
		window.setVisualizationPanel(vizPanel);

		return window;
	}

	@Override
	public void onResize() {
		super.onResize();
		fullScreen();

	}


	private void fullScreen() {
		if(availableVisualizations != null){

			String uuid = availableVisualizations.getValue().getUuid();
			for(DirectedWindow window: windows){
			    window.setPixelSize(mainContainer.getOffsetWidth(), mainContainer.getOffsetHeight() - availableVisualizations.getOffsetHeight());
            
				if(window.getVisualizationPanel().getVisualization().getUuid().equals(uuid)){

					window.setPosition(0, 35);
					//Not sure why, but sometimes padding is randomly added
					if(mainContainer.getOffsetHeight() - panel.getOffsetHeight() > 30){
						window.setPosition(0,0);
					}
				} else {
					window.setPosition(-20000, -20000);
				}
			}

		}
	}

	protected class StringLabelProvider implements LabelProvider<Visualization>{

		@Override
		public String getLabel(Visualization viz) {
		    String label;
		    
		    switch(viz.getType()){
		    case CHART: label = i18n.selectViewChart(); //$NON-NLS-1$
		    break;
		    case GEOSPATIAL: label = i18n.selectViewMap(); //$NON-NLS-1$
            break;
		    case RELGRAPH: label = i18n.selectViewGraph(); //$NON-NLS-1$
            break;
		    case TABLE: label = i18n.selectViewTable(); //$NON-NLS-1$
            break;
		    case TIMELINE: label = i18n.selectViewTimeline(); //$NON-NLS-1$
            break;
		    case MAP_CHART: label = i18n.selectViewMap(); //$NON-NLS-1$
            break;
		    case BAR_CHART: label = i18n.selectViewBarChart(); //$NON-NLS-1$
            break;
		    case RELGRAPH_V2: label = i18n.selectViewGraph(); //$NON-NLS-1$
            break;
		    case SKETCH: label = i18n.selectViewSketch(); //$NON-NLS-1$
            break;
		    case GOOGLE_MAPS: label = i18n.selectViewMap(); //$NON-NLS-1$
            break;
		    case DRILL_CHART: label = i18n.selectViewChart();; //$NON-NLS-1$
            break;
		    case MATRIX: label = i18n.selectViewMatrix(); //$NON-NLS-1$
            break;
		    case CHRONOS: label = i18n.selectViewTimeline(); //$NON-NLS-1$
            break;
		    case GEOSPATIAL_V2: label = i18n.selectViewMap(); //$NON-NLS-1$
            break;
		    default: label = viz.getType().toString();
		    break;
		    }
		    
			return viz.getName() + "  ("+label+")"; //$NON-NLS-1$ //$NON-NLS-2$
		}}

	protected class StringModelKeyProvider implements ModelKeyProvider<String>{
		@Override
		public String getKey(String item) {
			return item.toString();
		}
	}

	public boolean isValid() {
		if(validators.validate()){
			availableVisualizations.clearInvalid();
			return true;
		} else {
			String error = validators.getErrors().get(0);
			availableVisualizations.forceInvalid(error);
			return false;
		}
	}

	@Override
	public void add(Window floatingTabWindow) {
		panel.add(floatingTabWindow);
	}

}
