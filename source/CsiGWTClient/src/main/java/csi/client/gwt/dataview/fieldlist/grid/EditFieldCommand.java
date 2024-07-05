package csi.client.gwt.dataview.fieldlist.grid;

import csi.client.gwt.dataview.fieldlist.FieldCommand;
import csi.client.gwt.dataview.fieldlist.FieldList;
import csi.client.gwt.dataview.fieldlist.editor.FieldModel;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.server.common.model.FieldDef;

/**
 * @author Centrifuge Systems, Inc.
 * Launches a new FieldEditor to edit a field
 */
public class EditFieldCommand implements FieldCommand {

    private static final CentrifugeConstants i18n = CentrifugeConstantsLocator.get();

    private final FieldList fieldList;

    public EditFieldCommand(FieldList fieldList){
        this.fieldList = fieldList;
    }

    @Override
    public void execute(String uuidIn) {

        FieldDef myField = fieldList.getModelProxy().getFieldDefByUuid(uuidIn);
        FieldModel myFieldModel = new FieldModel(myField);
        fieldList.editorMode(myFieldModel, false, i18n.fieldList_EditExisting(), false);
    }
}
