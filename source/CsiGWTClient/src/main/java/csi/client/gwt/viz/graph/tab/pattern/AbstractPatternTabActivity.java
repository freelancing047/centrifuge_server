package csi.client.gwt.viz.graph.tab.pattern;

import csi.client.gwt.viz.graph.tab.pattern.PatternTab.PatternTabActivity;

abstract class AbstractPatternTabActivity implements PatternTabActivity {
    AbstractPatternTabActivity() {
    }

    @Override
    public void search() {
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
}
