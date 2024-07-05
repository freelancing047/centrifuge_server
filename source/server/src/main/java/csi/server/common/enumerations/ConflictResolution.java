package csi.server.common.enumerations;

import csi.server.common.interfaces.TrippleDisplay;

import java.io.Serializable;

/**
 * Created by centrifuge on 3/20/2019.
 */
public enum ConflictResolution implements Serializable, TrippleDisplay {

    NO_CHANGE(      "Do not import",
                    "Keep the current item as it is.",
                    ""),
    IMPORT_NEW(     "Import as new",
                    "Keep both the current item and the import.",
                    "Import the item as a new item without ovewriting the current item."),
    SAVE_CURRENT(   "Backup then import",
                    "Keep both the current item and the import.",
                    "Import the item as a new item without ovewriting the current item."),
    REPLACE(        "Replace with import",
                    "Replace the current item with the imported item.",
                    "The current item is discarded, and the imported item takes its place."),
    MERGE_KEEP(     "Add only new data",
                    "Merge without replacing any existing components.",
                    "Add to the current item only non-conflicting components of the imported item."),
    MERGE_REPLACE(  "Import and augment",
                    "Merge, retaining only non-conflicting current components.",
                    "Replace the current item with the imported item plus any non-conflicting current components."),
    PROMPT(         "Prompt",
                    "",
                    "");

    private String _label;
    private String _title;
    private String _description;

    public static int[] getLimits(AclResourceType typeIn) {

        switch (typeIn) {

            case TEMPLATE:
            case MAP_BASEMAP:

                return new int[]{IMPORT_NEW.ordinal(), MERGE_KEEP.ordinal()};

            case GRAPH_THEME:
            case MAP_THEME:

                return new int[]{IMPORT_NEW.ordinal(), PROMPT.ordinal()};
        }
        return null;
    }

    public String getLabel() {

        return _label;
    }

    public String getTitle() {

        return _title;
    }

    public String getDescription() {

        return _description;
    }

    private ConflictResolution(String labelIn, String titleIn, String descriptionIn) {

        _label = labelIn;
        _title = titleIn;
        _description = descriptionIn;
    }
}
