package csi.client.gwt.i18n;

import csi.server.common.enumerations.ServerMessage;

/**
 * Created by centrifuge on 5/4/2015.
 */
public class ServerMessageStrings {

    private static final CentrifugeConstants _constants = CentrifugeConstantsLocator.get();

    private static String[] _list = new String[ServerMessage.values().length];

    static {

        _list[ServerMessage.LICENSE_LIMITATION.ordinal()] = _constants.serverMessage_LicenseLimitation();
        _list[ServerMessage.ADMIN_REQUIRED.ordinal()] = _constants.serverMessage_AdminRequired();
        _list[ServerMessage.LICENSE_EXCEEDED.ordinal()] = _constants.serverMessage_LicenseCountExceeded();
        _list[ServerMessage.LICENSE_USER_DISABLED.ordinal()] = _constants.serverMessage_LicenseUserDisabled();
        _list[ServerMessage.CORRUPT_USER_DATA.ordinal()] = _constants.serverMessage_CorruptUserData();

        _list[ServerMessage.USER_NOT_AUTHORIZED.ordinal()] = _constants.serverMessage_UserNotAuthorized();
        _list[ServerMessage.CSO_REQUIRED.ordinal()] = _constants.serverMessage_CsoRequired();
        _list[ServerMessage.GROUP_CREATE_FAILED.ordinal()] = _constants.serverMessage_GroupCreateFailed();
        _list[ServerMessage.CORRUPT_GROUP.ordinal()] = _constants.serverMessage_CorruptGroup();
        _list[ServerMessage.ADMIN_OR_CSO_REQUIRED.ordinal()] = _constants.serverMessage_AdminOrCsoRequired();

        _list[ServerMessage.CAUGHT_EXCEPTION.ordinal()] = _constants.serverMessage_CaughtException();
        _list[ServerMessage.CREDENTIALS_EXCEPTION.ordinal()] = _constants.serverMessage_CredentialsException();
        _list[ServerMessage.LINKUP_DATAVIEW_EXCEPTION.ordinal()] = _constants.serverMessage_LinkupDataViewException();
        _list[ServerMessage.LINKUP_DATASOURCE_EMPTY.ordinal()] = _constants.serverMessage_LinkupDataSourceEmpty();
        _list[ServerMessage.INVALID_STATE.ordinal()] = _constants.serverMessage_InvalidState();

        _list[ServerMessage.SAVE_CANCELED.ordinal()] = _constants.serverMessage_SaveCanceled();
        _list[ServerMessage.TEMPLATE_ACCESS_ERROR.ordinal()] = _constants.serverMessage_TemplateAccessError();
        _list[ServerMessage.DATAVIEW_CREATE_EXCEPTION.ordinal()] = _constants.serverMessage_DataViewCreateException();
        _list[ServerMessage.DATAVIEW_SAVE_ERROR.ordinal()] = _constants.serverMessage_DataViewSaveError();
        _list[ServerMessage.DATAVIEW_LOCATE_ERROR.ordinal()] = _constants.serverMessage_DataViewLocateError();

        _list[ServerMessage.DATAVIEW_EDIT_ERROR.ordinal()] = _constants.serverMessage_DataViewEditError();
        _list[ServerMessage.DATAVIEW_VALIDATION_ERROR.ordinal()] = _constants.serverMessage_DataViewValidationError();
        _list[ServerMessage.DATAVIEW_DELETE_EXCEPTION.ordinal()] = _constants.serverMessage_DataViewDeleteException();
        _list[ServerMessage.DATAVIEW_LOAD_EXCEPTION.ordinal()] = _constants.serverMessage_DataViewLoadException();
        _list[ServerMessage.DATAVIEW_CREATE_ERROR.ordinal()] = _constants.serverMessage_DataViewCreateError();

        _list[ServerMessage.DATAVIEW_UUID_ERROR.ordinal()] = _constants.serverMessage_DataViewUuidError();
        _list[ServerMessage.DATAVIEW_DATASOURCE_EMPTY.ordinal()] = _constants.serverMessage_DataViewDataSourceEmpty();
        _list[ServerMessage.TEMPLATE_NOT_FOUND.ordinal()] = _constants.serverMessage_TemplateNotFound();
        _list[ServerMessage.DRIVER_LOCATE_ERROR.ordinal()] = _constants.serverMessage_DriverLocateError();
        _list[ServerMessage.CONNECTOR_CONFIG_ERROR.ordinal()] = _constants.serverMessage_ConnectorConfigError();

        _list[ServerMessage.MISSING_DRIVER_KEY.ordinal()] = _constants.serverMessage_MissingDriverKey();
        _list[ServerMessage.TEMPLATE_ACCESS_EXCEPTION.ordinal()] = _constants.serverMessage_TemplateAccessException();
        _list[ServerMessage.DATAVIEW_SAVE_EXCEPTION.ordinal()] = _constants.serverMessage_DataViewSaveException();
        _list[ServerMessage.TEMPLATE_XFER_EXCEPTION.ordinal()] = _constants.serverMessage_TemplateXferException();
        _list[ServerMessage.DATAVIEW_PERSIST_EXCEPTION.ordinal()] = _constants.serverMessage_DataViewPersistException();

        _list[ServerMessage.FILE_UPLOAD_ERROR.ordinal()] = _constants.serverMessage_FileUploadError();
        _list[ServerMessage.BAD_ARGUMENTS.ordinal()] = _constants.serverMessage_BadArguments();
        _list[ServerMessage.TEMPLATE_SAVE_ERROR.ordinal()] = _constants.serverMessage_TemplateSaveError();
        _list[ServerMessage.TEMPLATE_LOCATE_ERROR.ordinal()] = _constants.serverMessage_TemplateLocateError();
        _list[ServerMessage.TEMPLATE_EDIT_ERROR.ordinal()] = _constants.serverMessage_TemplateEditError();

        _list[ServerMessage.TEMPLATE_VALIDATION_ERROR.ordinal()] = _constants.serverMessage_TemplateValidationError();
        _list[ServerMessage.TEMPLATE_DELETE_EXCEPTION.ordinal()] = _constants.serverMessage_TemplateDeleteException();
        _list[ServerMessage.TEMPLATE_LOAD_EXCEPTION.ordinal()] = _constants.serverMessage_TemplateLoadException();
        _list[ServerMessage.TEMPLATE_UUID_ERROR.ordinal()] = _constants.serverMessage_TemplateUuidError();
        _list[ServerMessage.RESOURCE_NOT_IMPORTABLE.ordinal()] = _constants.serverMessage_ResourceNotImportable();

        _list[ServerMessage.RESOURCE_NOT_FOUND.ordinal()] = _constants.serverMessage_ResourceNotFound();
        _list[ServerMessage.FAILED_SERVER_REQUEST.ordinal()] = _constants.serverMessage_ResourceNotFound();
        _list[ServerMessage.MISSING_DATA_KEY.ordinal()] = _constants.serverMessage_MissingDataKey();
        _list[ServerMessage.MISSING_DIALOG_KEY.ordinal()] = _constants.serverMessage_MissingDialogKey();
        _list[ServerMessage.RESOURCE_NOT_SUPPORTED.ordinal()] = _constants.serverMessage_ResourceNotSupported();

        _list[ServerMessage.TEMPLATE_CREATE_ERROR.ordinal()] = _constants.serverMessage_TemplateCreateError();
        _list[ServerMessage.INSTALLED_TABLE_EXISTS.ordinal()] = _constants.serverMessage_InstalledTableExists();
        _list[ServerMessage.RESOURCE_EXPORT_ERROR.ordinal()] = _constants.serverMessage_ResourceExportError();
    }

    public static boolean exists(ServerMessage messageEnumIn) {

        return ((null != messageEnumIn) && (0 < _list[messageEnumIn.ordinal()].length()));
    }

    public static String get(ServerMessage messageEnumIn) {

        return (null != messageEnumIn) ? _list[messageEnumIn.ordinal()] : "";
    }
}
