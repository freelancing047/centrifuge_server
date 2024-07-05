/**
 * 
 */
package csi.server.common.model.visualization;



import java.io.Serializable;

import com.google.gwt.user.client.rpc.IsSerializable;


public enum VisualizationType implements Serializable, IsSerializable {
    /**
     * This Type is no longer used.
     */
    CHART,                  // This one is gone.
    GEOSPATIAL,             //
    RELGRAPH,               // might be gone
    TABLE(true),            //
    TIMELINE,               //
    MAP_CHART,              //
    BAR_CHART,              //
    RELGRAPH_V2(true),      //
    SKETCH,                 // gone.
    GOOGLE_MAPS,            // gone
    /**
     * Current Chart Visualization is using this type
     */
    DRILL_CHART(true),      // New drillable 1D charts.
    MATRIX(true),           // 2d charts (bubble, heat-map and co-occurrence)
    CHRONOS(true),          // New implementation of timeline using timeflow (chronos = greek for sequence of events)
    GEOSPATIAL_V2(true),    //
    CHRONOS_HOVER(),   // EXISTS are icons of visualization types that have at least one viz created
    DRILL_CHART_HOVER(),
    MAP_CHART_HOVER(),
    GEOSPATIAL_V2_HOVER(),
    MATRIX_HOVER(),
    RELGRAPH_V2_HOVER(),
    TABLE_HOVER(),
    ;

    private boolean h5;

    private VisualizationType() {
        // h5 is false.
    }

    private VisualizationType(boolean h5) {
        this.h5 = h5;
    }

    public boolean isH5() {
        return h5;
    }
    

}