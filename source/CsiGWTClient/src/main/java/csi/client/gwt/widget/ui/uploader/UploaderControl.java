package csi.client.gwt.widget.ui.uploader;

import com.google.gwt.typedarrays.shared.Int8Array;

import csi.client.gwt.csiwizard.WizardDialog;
import csi.client.gwt.widget.ui.uploader.wizards.support.ReadBlock;
import csi.server.common.dto.installed_tables.TableInstallRequest;
import csi.server.common.enumerations.CsiFileType;
import csi.server.common.model.tables.InstalledTable;

/**
 * Created by centrifuge on 10/26/2015.
 */
public interface UploaderControl {

    public void cancel();
    public void beginInstall(TableInstallRequest requestIn);
    public void launchWizard(CsiFileType fileTypeIn, WizardDialog parentIn);
    public void onError(Exception exceptionIn);
    public Int8Array getTestBlock();
    public Object getBlockAccess();
    public String getFileName();
    public ReadBlock getDataReader();
    public void doUpdateRequest(TableInstallRequest requestIn);
}
