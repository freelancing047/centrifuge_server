package csi.server.business.visualization.graph.pattern.engine;

import csi.server.business.visualization.graph.pattern.model.Pattern;

public interface PatternEngine {
    void setMatchLimit(int limit);

    void setTimeout(int milliseconds);

    void setPattern(Pattern patttern);

}
