package csi.client.gwt.dataview.fieldlist;


/**
 * @author Centrifuge Systems, Inc.
 * Deletes a FieldDef from the FieldList
 */
public class DeleteCommand implements FieldCommand {

    private final FieldList fieldList;

    public DeleteCommand(FieldList fieldList){
        this.fieldList = fieldList;
    }

    @Override
    public void execute(String uuidIn) {

        fieldList.deleteFieldDef(uuidIn);
    }
}
