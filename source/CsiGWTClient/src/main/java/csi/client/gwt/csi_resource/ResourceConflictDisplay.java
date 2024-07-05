package csi.client.gwt.csi_resource;

import csi.server.common.enumerations.ConflictResolution;

/**
 * Created by centrifuge on 4/22/2019.
 */
public class ResourceConflictDisplay implements OptionControl<ConflictResolution> {

    private int key;
    private String uuid;
    private String type;
    private String name;
    private String owner;
    private String remarks;
    private ConflictResolution resolution;
    private Boolean conflicts;
    private Boolean selected;

    public ResourceConflictDisplay(int keyIn, String uuidIn, String typeIn, String nameIn,
                                   String ownerIn, String remarksIn, ConflictResolution resolutionIn,
                                   Boolean conflictsIn, Boolean selectedIn) {
        
        key = keyIn;
        uuid = uuidIn;
        type = typeIn;
        name = nameIn;
        owner = ownerIn;
        remarks = remarksIn;
        resolution = resolutionIn;
        conflicts = conflictsIn;
        selected = selectedIn;
    }

    public ConflictResolution getOption() {

        return resolution;
    }

    public int getKey() {

        return key;
    }

    public void setKey(int keyIn) {

        key = keyIn;
    }

    public String getUuid() {

        return uuid;
    }

    public void setUuid(String uuidIn) {

        uuid = uuidIn;
    }

    public String getType() {

        return type;
    }

    public void setType(String typeIn) {

        type = typeIn;
    }

    public String getName() {

        return name;
    }

    public void setName(String nameIn) {

        name = nameIn;
    }

    public String getOwner() {

        return owner;
    }

    public void setOwner(String ownerIn) {

        owner = ownerIn;
    }

    public String getRemarks() {

        return remarks;
    }

    public void setRemarks(String remarksIn) {

        remarks = remarksIn;
    }

    public ConflictResolution getResolution() {

        return resolution;
    }

    public void setResolution(ConflictResolution resolutionIn) {

        resolution = resolutionIn;
    }

    public Boolean getConflicts() {

        return conflicts;
    }

    public void setConflicts(Boolean conflictsIn) {

        conflicts = conflictsIn;
    }

    public Boolean getSelected() {

        return selected;
    }

    public void setSelected(Boolean selectedIn) {

        selected = selectedIn;
    }
}
