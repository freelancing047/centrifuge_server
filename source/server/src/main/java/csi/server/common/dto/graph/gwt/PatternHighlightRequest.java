package csi.server.common.dto.graph.gwt;

import java.util.Collection;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.gwt.user.client.rpc.IsSerializable;

import csi.server.business.visualization.graph.pattern.model.PatternMeta;

public class PatternHighlightRequest implements IsSerializable {
    List<PatternMeta> selectedPatterns = Lists.newArrayList();
    String color;

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public void addPatterns(Collection<PatternMeta> patterns) {
        selectedPatterns.addAll(patterns);
    }

    public List<PatternMeta> getSelectedPatterns() {
        return ImmutableList.copyOf(selectedPatterns);
    }
}
