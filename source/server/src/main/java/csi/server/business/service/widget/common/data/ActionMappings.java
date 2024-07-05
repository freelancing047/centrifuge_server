package csi.server.business.service.widget.common.data;

import java.util.List;

/**
 * Data object used at action - class mappings data loader
 */
public class ActionMappings {

    /**
     * Collection of action mappings
     */
    private List<ActionMapping> actionMappings;

    public List<ActionMapping> getActionMappings() {
        return actionMappings;
    }

    public void setActionMappings(List<ActionMapping> actionMappings) {
        this.actionMappings = actionMappings;
    }

    @Override
    public String toString() {
        return "ActionMappings{" + "actionMappings=" + actionMappings + '}';
    }
}
