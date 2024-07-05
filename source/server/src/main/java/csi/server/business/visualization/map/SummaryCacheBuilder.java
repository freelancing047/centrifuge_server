package csi.server.business.visualization.map;

import csi.shared.core.visualization.map.UBox;

public interface SummaryCacheBuilder {
    UBox getUBox();

    void build();

    String getStatus();
}
