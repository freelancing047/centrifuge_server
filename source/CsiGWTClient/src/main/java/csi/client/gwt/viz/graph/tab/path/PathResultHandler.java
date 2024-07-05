package csi.client.gwt.viz.graph.tab.path;

import csi.server.common.dto.graph.path.FindPathResponse;


public interface PathResultHandler {

    void onPathsLoaded(FindPathResponse response);
}
