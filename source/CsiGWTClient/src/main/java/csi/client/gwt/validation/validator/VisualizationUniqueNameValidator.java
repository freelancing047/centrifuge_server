package csi.client.gwt.validation.validator;

import java.util.List;

import com.google.gwt.user.client.TakesValue;

import csi.server.common.model.visualization.VisualizationDef;

/**
 * @author Centrifuge Systems, Inc.
 */
public class VisualizationUniqueNameValidator implements Validator {

    private final List<VisualizationDef> visualizations;
    private final TakesValue<String> valueBox;
    private final String currentUuid;

    public VisualizationUniqueNameValidator(List<VisualizationDef> visualizations, TakesValue<String> valueBox, String uuid){
        this.visualizations = visualizations;
        this.valueBox = valueBox;
        this.currentUuid = uuid;
    }

    @Override
    public boolean isValid() {
        String proposedName = valueBox.getValue();
        for (VisualizationDef visDef : visualizations) {
            if(visDef.getUuid().equals(currentUuid)){
                continue;
            }
            if(visDef.getName().trim().equalsIgnoreCase(proposedName.trim())){
                return false;
            }
        }
        return true;
    }
}
