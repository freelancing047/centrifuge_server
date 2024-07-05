package csi.shared.gwt.viz.graph.tab.pattern.settings;

import com.google.gwt.user.client.rpc.IsSerializable;

public interface PatternCriterion extends IsSerializable {
    String getName();

    void setName(String name);

    String getValue();

    void setValue(String value);

    String getType();

    boolean isShowInResults();

    void setShowInResults(boolean value);
}
