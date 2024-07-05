package csi.client.gwt.dataview.directed;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.sencha.gxt.widget.core.client.Window;

import csi.client.gwt.dataview.directed.visualization.DirectedWindow;
import csi.client.gwt.mainapp.MessagesDialog;
import csi.client.gwt.validation.multi.MultiValidatorCollectingErrors;
import csi.client.gwt.viz.Visualization;
import csi.client.gwt.viz.shared.chrome.panel.VizPanel;
import csi.client.gwt.widget.ui.FullSizeLayoutPanel;
import csi.client.gwt.widget.ui.ResizeableAbsolutePanel;


public class FitView extends VizLayout{

	private DirectedPresenter presenter;
	private List<DirectedWindow> windows = new ArrayList<DirectedWindow>();

    protected MultiValidatorCollectingErrors validators = new MultiValidatorCollectingErrors();

	@UiField
	FullSizeLayoutPanel mainContainer;

    @UiField
    ResizeableAbsolutePanel panel;

    MessagesDialog messagesDialog;


    interface SpecificUiBinder extends UiBinder<FullSizeLayoutPanel, FitView> {
    }

    private static SpecificUiBinder uiBinder = GWT.create(SpecificUiBinder.class);
    
    public FitView(DirectedPresenter presenter){
    	mainContainer = uiBinder.createAndBindUi(this);
        initWidget(mainContainer);
        this.presenter = presenter;
        messagesDialog = new MessagesDialog(presenter.getDataView(), presenter);

    }
    
    @Override
    protected void onAttach() {
        super.onAttach();
        Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
            @Override
            public void execute() {
            	presenter.loadVisualizations();
            }
        });
    }
    
    @Override
    public void addVisualization(Visualization visualization){
    	vizCount++;
    	DirectedWindow window = createWindow(visualization);
    	windows.add(window);
    	panel.add(window);
        
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
    	tile();
    	
    }
    

	public void tile() {
		
		int sq = (int) Math.ceil(Math.sqrt(windows.size()));
        int rem = windows.size() % sq;
        int windowsPerRow = rem == 1 ? sq + 1 : sq;

        int rows = (int) Math.ceil(windows.size() / (double) windowsPerRow);
        int elementsInLastRow = windows.size() % windowsPerRow;
        int width = mainContainer.getOffsetWidth() / windowsPerRow;
        int lastRowWidth = elementsInLastRow == 0 ? 0 : mainContainer.getOffsetWidth() / elementsInLastRow;
        int height = mainContainer.getOffsetHeight() / rows;
        int x = 0, y = 0, w = 0;
        int counter = 0;
        for (int i = windows.size() - 1; i >= 0; i--) {
            if (i < elementsInLastRow) {
                x = (counter % windowsPerRow) * lastRowWidth;
                w = lastRowWidth;
            } else {
                x = (counter % windowsPerRow) * width;
                w = width;
            }
            y = counter / windowsPerRow * height;

            DirectedWindow window = windows.get(i);
            window.getElement().getStyle().setPosition(Position.ABSOLUTE);
            window.restore(false);
            window.setPosition(x, y);
            window.setPixelSize(w, height);

            counter++;
        }
		
    }

	@Override
	public void add(Window floatingTabWindow) {
		panel.add(floatingTabWindow);
	}
    
    
	
}
