package csi.client.gwt.viz.graph.tab.pattern.settings;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

import csi.shared.gwt.viz.graph.tab.pattern.settings.HasPatternCriteria;
import csi.shared.gwt.viz.graph.tab.pattern.settings.PatternLink;
import csi.shared.gwt.viz.graph.tab.pattern.settings.PatternNode;

public class RemoveItem extends AbstractPatternSettingsActivity {
    private final HasPatternCriteria item;

    public RemoveItem(PatternSettings settings, HasPatternCriteria item) {
        super(settings);
        this.item = item;
    }

    @Override
    public void start(AcceptsOneWidget panel, EventBus eventBus) {
        Set<PatternLink> patternLinks = getModel().getEditPattern().getPatternLinks();
        if (item instanceof PatternNode) {
            List<PatternLink> links = Lists.newArrayList(patternLinks);
            for (PatternLink patternLink : links) {
                if (patternLink.getNode1().getUuid().equals(((PatternNode) item).getUuid())) {
                    patternLinks.remove(patternLink);
                }
                if (patternLink.getNode2().getUuid().equals(((PatternNode) item).getUuid())) {
                    patternLinks.remove(patternLink);
                }
            }
            getModel().getEditPattern().getPatternNodes().remove(item);
        } else if (item instanceof PatternLink) {
            patternLinks.remove(item);
        }
        patternSettings.editPattern(getModel().getEditPattern());
    }
}
