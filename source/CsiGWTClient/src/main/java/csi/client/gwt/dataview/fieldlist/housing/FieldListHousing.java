package csi.client.gwt.dataview.fieldlist.housing;

import java.util.Collection;
import java.util.List;

import csi.client.gwt.dataview.fieldlist.FieldList;
import csi.client.gwt.dataview.fieldlist.editor.FieldEditor;
import csi.client.gwt.dataview.fieldlist.editor.FieldModel;
import csi.client.gwt.dataview.fieldlist.grid.FieldGrid;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.server.common.model.FieldDef;

/**
 * @author Centrifuge Systems, Inc.
 * Contains logic for the container that houses the field list
 */
public class FieldListHousing {
    private static final CentrifugeConstants i18n = CentrifugeConstantsLocator.get();
    private final FieldListHousingView view;
    private final FieldGrid fieldGrid;
    private final FieldList fieldList;
//    private final ValidationAndFeedbackPair duplicateNameValidator;
    private FieldEditor fieldEditor;

    public FieldListHousing(FieldList fieldList){
        this.fieldList = fieldList;
        this.fieldGrid = new FieldGrid(fieldList);
        this.view = new FieldListHousingView(fieldGrid);
//        this.duplicateNameValidator = new ValidationAndFeedbackPair(fieldGrid.getDuplicateNameInGridValidator(), new ErrorLabelValidationFeedback(view.getDuplicateError(), i18n.validator_DuplicateName()));
    }

    public FieldListHousingView getView() {
        return view;
    }

    public String getGridSelection() {

        return fieldGrid.getSelection();
    }

    public void addOrUpdateFieldGrid(FieldDef fieldDef) {
        fieldGrid.addOrUpdateRow(fieldDef);
    }

    public void selectAndEnsureRowVisible(String keyIn) {

        fieldGrid.selectAndEnsureRowVisible(keyIn);
    }

    public void deleteFieldDef(String uuidIn) {
        fieldGrid.deleteFieldDef(uuidIn);
    }

    public void showFieldEditor(FieldModel fieldModel, String newTitle, boolean isNewIn) {
        this.fieldEditor = new FieldEditor(fieldList, fieldModel, isNewIn);
        view.editorMode(fieldEditor.getView(), newTitle);
    }

    public void showFieldGrid() {
        view.gridMode();
    }

    public void deleteCurrentFieldDef() {
        if(fieldEditor == null)
            return;

        fieldEditor.delete();
    }

    public void saveCurrentFieldDef() {
        if(fieldEditor == null)
            return;

        fieldEditor.save();
    }

    public boolean validate() {
        return fieldEditor.validate();
    }

    public void refreshGrid(List<FieldDef> listIn) {

        if (null != fieldGrid) {

            fieldGrid.refresh(listIn);
        }
    }

    public List<String> finalizeUpdates() {

        return fieldGrid.sychonizeFieldOrder();
    }
/*
    public void saveGrid() {
        if(isValid()) {
            updateModifiedFieldsFromModel(fieldList.getModel().getFieldDefs());
            fieldList.setExistingFieldEdited(true);
        }
    }

    private void updateModifiedFieldsFromModel(List<FieldDef> fieldDefs) {
        List<FieldGridModel> modelList = fieldGrid.saveModel();

        for (FieldGridModel clientModel : modelList) {
            for (FieldDef def : fieldDefs) {
                if (def.getUuid().equals(clientModel.getUuid())) {
                    saveMatchedFields(clientModel, def);
                }
            }
        }
    }

    private void saveMatchedFields(FieldGridModel clientModel, FieldDef def) {
        setFieldDefFromClientModel(clientModel, def);
        fieldList.getModel().addOrUpdateField(def);
    }

    private void setFieldDefFromClientModel(FieldGridModel clientModel, FieldDef def) {
        if (clientModel.getName() != null && !clientModel.getName().equals(""))
            def.setFieldName(clientModel.getName().trim());

        if (clientModel.getDataType() != null && !clientModel.getDataType().equals(""))
            def.setValueType(clientModel.getDataType());
    }

    private boolean isValid() {

        return true;
    }
*/
}
