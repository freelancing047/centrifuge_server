package csi.server.business.visualization.graph.player;

import csi.server.business.visualization.graph.GraphContext;
import csi.server.business.visualization.graph.player.GraphPlayer.GraphData;

public interface PlaybackHandler {

    public void initialize(GraphPlayer player, GraphContext context);

    public void step(GraphData previous, GraphData current);

    public void destroy();

    public void resetVisuals();

    public void seek(GraphData current);

}
