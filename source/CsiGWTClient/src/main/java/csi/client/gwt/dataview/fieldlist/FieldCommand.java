package csi.client.gwt.dataview.fieldlist;

/**
 * @author Centrifuge Systems, Inc.
 * Implementations invoke commands on FieldDefs by it's uuid
 */
public interface FieldCommand {

    public abstract void execute(String uuid);

}
