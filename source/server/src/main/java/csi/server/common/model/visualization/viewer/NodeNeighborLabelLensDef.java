package csi.server.common.model.visualization.viewer;

import com.google.common.collect.Lists;
import csi.server.common.model.CsiUUID;
import csi.shared.gwt.viz.viewer.settings.editor.LensDefSettings;
import csi.shared.gwt.viz.viewer.settings.editor.LensListSetting;
import csi.shared.gwt.viz.viewer.settings.editor.LensMultiListSetting;
import csi.shared.gwt.viz.viewer.settings.editor.LensSettingsControl;

import java.util.ArrayList;
import java.util.Collection;

public class NodeNeighborLabelLensDef implements LensDef {
    private boolean includeBundles = true;
    private boolean includeBundled = false;
    private boolean includeHidden = false;
    private boolean includeVisible = true;
    private boolean includeSources = true;//neighbors via links with arrows pointing toward objective
    private boolean includeIncoming = true;//neighbors via links with arrows pointing away from objective
    private boolean includeUserNodes = true;
    private boolean includeDataNodes = true;
    private boolean asDrawn = false;//Neighbors via links that correspond to rows backing the current display style of objective
    private boolean includeBidirectional = true;
    private boolean includeNoDirection = true;

    public void setId(String id) {
        this.id = id;
    }

    private String id = CsiUUID.randomUUID();

    public LensDefSettings getSettings() {
        LensDefSettings lds = new LensDefSettings();
        ArrayList<LensSettingsControl> controls = Lists.newArrayList();
        {
        LensListSetting control = new LensListSetting();
            control.setLabel("Bundle");
            control.setType("list");
            if(includeBundles && !includeBundled) {
                control.setSelectedValue("Bundles");
            }
            else if(!includeBundles && includeBundled) {
                control.setSelectedValue("Bundled");
            }else{
                control.setSelectedValue("Bundles & Bundled");
            }
            ArrayList<String> values = Lists.newArrayList();
            values.add("Bundles");
            values.add("Bundled");
            values.add("Bundles & Bundled");
            control.setValues(values);
            controls.add(control);
        }{
        LensListSetting control = new LensListSetting();
            control.setLabel("Hidden");
            control.setType("list");
            control.setSelectedValue("Visible");
            ArrayList<String> values = Lists.newArrayList();
            values.add("Visible");
            values.add("Hidden");
            values.add("Visible & Hidden");
            control.setValues(values);
            controls.add(control);
        }{
        LensMultiListSetting control = new LensMultiListSetting();
            control.setLabel("Direction");
            control.setType("multilist");
            ArrayList<String> selectedValues = Lists.newArrayList();
            selectedValues.add("Source");
            selectedValues.add("Target");
            selectedValues.add("Both");
            selectedValues.add("Neither");
            control.setSelectedValues(selectedValues);
            ArrayList<String> values = Lists.newArrayList();
            values.add("Source");
            values.add("Target");
            values.add("Both");
            values.add("Neither");
            control.setValues(values);
            controls.add(control);
        }{
        LensListSetting control = new LensListSetting();
            control.setLabel("Data/User");
            control.setType("list");
            control.setSelectedValue("Data & User");
            ArrayList<String> values = Lists.newArrayList();
            values.add("Data");
            values.add("User");
            values.add("Data & User");
            control.setValues(values);
            controls.add(control);
        }
        lds.setControls(controls);
        lds.setName("Neighbors");
        lds.setLensType("NeighborLabels");
        return lds;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public boolean handles(Objective objective) {
        return objective instanceof NodeObjective;
    }

    @Override
    public String getDisplayName() {
        return null;
    }

    @Override
    public void setDisplayName(String displayName) {

    }

    public Collection<String> getGroups() {
        ArrayList<String> strings = Lists.newArrayList();
        strings.add("Graph");
        return strings;
    }

    public boolean isIncludeBundles() {
        return includeBundles;
    }

    public void setIncludeBundles(boolean includeBundles) {
        this.includeBundles = includeBundles;
    }

    public boolean isIncludeBundled() {
        return includeBundled;
    }

    public void setIncludeBundled(boolean includeBundled) {
        this.includeBundled = includeBundled;
    }

    public boolean isIncludeHidden() {
        return includeHidden;
    }

    public void setIncludeHidden(boolean includeHidden) {
        this.includeHidden = includeHidden;
    }

    public boolean isIncludeVisible() {
        return includeVisible;
    }

    public void setIncludeVisible(boolean includeVisible) {
        this.includeVisible = includeVisible;
    }

    public boolean isIncludeSources() {
        return includeSources;
    }

    public void setIncludeSources(boolean includeSources) {
        this.includeSources = includeSources;
    }

    public boolean isIncludeIncoming() {
        return includeIncoming;
    }

    public void setIncludeIncoming(boolean includeIncoming) {
        this.includeIncoming = includeIncoming;
    }

    public boolean isIncludeUserNodes() {
        return includeUserNodes;
    }

    public void setIncludeUserNodes(boolean includeUserNodes) {
        this.includeUserNodes = includeUserNodes;
    }

    public boolean isIncludeDataNodes() {
        return includeDataNodes;
    }

    public void setIncludeDataNodes(boolean includeDataNodes) {
        this.includeDataNodes = includeDataNodes;
    }

    public boolean isAsDrawn() {
        return asDrawn;
    }

    public void setAsDrawn(boolean asDrawn) {
        this.asDrawn = asDrawn;
    }

    public boolean isIncludeBidirectional() {
        return includeBidirectional;
    }

    public void setIncludeBidirectional(boolean includeBidirectional) {
        this.includeBidirectional = includeBidirectional;
    }

    public boolean isIncludeNoDirection() {
        return includeNoDirection;
    }

    public void setIncludeNoDirection(boolean includeNoDirection) {
        this.includeNoDirection = includeNoDirection;
    }
}
