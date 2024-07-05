package csi.client.gwt.viz.graph.tab.player;

import java.util.Date;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

import csi.client.gwt.WebMain;
import csi.client.gwt.vortex.AbstractVortexEventHandler;
import csi.client.gwt.vortex.VortexFuture;
import csi.server.common.dto.CsiMap;
import csi.server.common.service.api.GraphTimePlayerServiceProtocol;

public class ResetTimePlayerRange extends AbstractTimePlayerAction {

    private Date start;
    private Date end;
    
    public ResetTimePlayerRange(TimePlayer timePlayer, Date start, Date end) {
        super(timePlayer);
    }

    @Override
    public void start(AcceptsOneWidget panel, EventBus eventBus) {
        // init();
        TimePlayerView view = timePlayer.getView();
        view.enablePlay(true);
        view.enableStop(false);
        view.enableReset(false);
        timePlayer.setActive(false);
        timePlayer.setPlaying(false);
        final TimePlayerSettings settings = timePlayer.getSettings();
        timePlayer.getGraphSurface().refresh(settings.stopPlayer());
        
        VortexFuture<CsiMap<String, String>> validVF = WebMain.injector.getVortex().createFuture();
        String endField = (settings.getEndField() != null) ? settings.getEndField().getLocalId() : null;
        String startField = (settings.getStartField() != null) ? settings.getStartField().getLocalId() : null;
        validVF.execute(GraphTimePlayerServiceProtocol.class).getEndPoints(timePlayer.getDataviewUuid(),
                timePlayer.getGraphUuid(), startField, endField, settings.getDuration(), settings.getDurationUnit(), settings.getStartTime(), settings.getEndTime());
        validVF.addEventHandler(new AbstractVortexEventHandler<CsiMap<String, String>>() {

            @Override
            public void onSuccess(CsiMap<String, String> result) {
                final TimePlayerView view = timePlayer.getView();
                if (result.size() > 1) {

                    //Date startTime = new Date(Long.parseLong((String) result.get("min")));//NON-NLS
                    //Date endTime = new Date(Long.parseLong((String) result.get("max")));//NON-NLS
                    int totalSteps = Integer.parseInt(result.get("count"));

                    //view.setPlaybackMin(startTime);
                    //view.setPlaybackMax(endTime);
                    settings.setTotalSteps(totalSteps);
                }
            }
        });
    }
}
