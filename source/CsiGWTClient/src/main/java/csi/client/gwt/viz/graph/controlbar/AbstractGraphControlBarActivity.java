package csi.client.gwt.viz.graph.controlbar;

import java.util.Date;

import csi.server.common.model.visualization.graph.TimePlayerConstants.TimePlayerStepMode;

public abstract class AbstractGraphControlBarActivity implements GraphControlBarPresenter {
    protected final GraphControlBar controlBar;

    AbstractGraphControlBarActivity(GraphControlBar controlBar) {

        this.controlBar = controlBar;
    }

    @Override
    public void togglePlaying() {
        controlBar.play();
    }

    @Override
    public void stopPlayer() {
        controlBar.stop();
    }

    @Override
    public String mayStop() {
        return null;
    }

    @Override
    public void onCancel() {

    }

    @Override
    public void onStop() {

    }

    @Override
    public void scrubDragStart() {
        controlBar.getView().setPlaying(false);
        controlBar.pause();
    }

    @Override
    public void scrubMoved(double newRelativePosition) {
    	if(controlBar.getGraph().getTimePlayer().getSettings().getStepMode() == TimePlayerStepMode.PERCENTAGE){
	        controlBar.getGraph().getTimePlayer().seekTo(newRelativePosition, controlBar.getModel());
    	} else {
        	Date currentTime = controlBar.getModel().getCurrentTime();
        	if(currentTime == null){
        		controlBar.getModel().setCurrentPercent(newRelativePosition);
        		currentTime = controlBar.getModel().getCurrentTime();
        	}
	        controlBar.getGraph().getTimePlayer().seekTo(currentTime);
	        controlBar.getModel().setCurrentPercent(newRelativePosition);
    	}
    }
}
