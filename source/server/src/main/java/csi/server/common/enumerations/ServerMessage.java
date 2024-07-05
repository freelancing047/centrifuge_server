package csi.server.common.enumerations;

import java.io.Serializable;

/**
 * Created by centrifuge on 5/4/2015.
 */
public enum ServerMessage implements Serializable {

    LICENSE_LIMITATION,
    ADMIN_REQUIRED,
    LICENSE_EXCEEDED,
    LICENSE_USER_DISABLED,
    CORRUPT_USER_DATA,

    USER_NOT_AUTHORIZED,
    CSO_REQUIRED,
    GROUP_CREATE_FAILED,
    CORRUPT_GROUP,
    ADMIN_OR_CSO_REQUIRED,

    CAUGHT_EXCEPTION,
    CREDENTIALS_EXCEPTION,
    LINKUP_DATAVIEW_EXCEPTION,
    LINKUP_DATASOURCE_EMPTY,
    INVALID_STATE,

    SAVE_CANCELED,
    TEMPLATE_ACCESS_ERROR,
    DATAVIEW_CREATE_EXCEPTION,
    DATAVIEW_SAVE_ERROR,
    DATAVIEW_LOCATE_ERROR,

    DATAVIEW_EDIT_ERROR,
    DATAVIEW_VALIDATION_ERROR,
    DATAVIEW_DELETE_EXCEPTION,
    DATAVIEW_LOAD_EXCEPTION,
    DATAVIEW_CREATE_ERROR,

    DATAVIEW_UUID_ERROR,
    DATAVIEW_DATASOURCE_EMPTY,
    TEMPLATE_NOT_FOUND,
    DRIVER_LOCATE_ERROR,
    CONNECTOR_CONFIG_ERROR,

    MISSING_DRIVER_KEY,
    TEMPLATE_ACCESS_EXCEPTION,
    DATAVIEW_SAVE_EXCEPTION,
    TEMPLATE_XFER_EXCEPTION,
    DATAVIEW_PERSIST_EXCEPTION,

    FILE_UPLOAD_ERROR,
    BAD_ARGUMENTS,
    TEMPLATE_SAVE_ERROR,
    TEMPLATE_LOCATE_ERROR,
    TEMPLATE_EDIT_ERROR,

    TEMPLATE_VALIDATION_ERROR,
    TEMPLATE_DELETE_EXCEPTION,
    TEMPLATE_LOAD_EXCEPTION,
    TEMPLATE_UUID_ERROR,
    RESOURCE_NOT_IMPORTABLE,

    RESOURCE_NOT_FOUND,
    FAILED_SERVER_REQUEST,
    MISSING_DATA_KEY,
    MISSING_DIALOG_KEY,
    RESOURCE_NOT_SUPPORTED,

    TEMPLATE_CREATE_ERROR,
    INSTALLED_TABLE_EXISTS,
    RESOURCE_EXPORT_ERROR;
}
