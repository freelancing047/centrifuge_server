package csi.client.gwt.widget.ui.uploader.wizards;

import java.util.List;

import com.google.gwt.event.dom.client.ClickEvent;

import csi.client.gwt.csiwizard.WizardDialog;
import csi.client.gwt.widget.ui.uploader.UploaderControl;
import csi.client.gwt.widget.ui.uploader.wizards.components.InstallerColumnDisplay;
import csi.server.common.dto.installed_tables.ColumnParameters;
import csi.server.common.dto.installed_tables.TableInstallRequest;
import csi.server.common.enumerations.CsiDataType;
import csi.server.common.enumerations.CsiFileType;
import csi.server.common.model.tables.InstalledTable;

/**
 * Created by centrifuge on 10/19/2015.
 */
public class JsonInstallWizard extends FileInstallWizard {


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public JsonInstallWizard(WizardDialog priorDialogIn, UploaderControl controlIn,
                             InstalledTable tableIn, String titleIn, String helpIn) {

        super(CsiFileType.JSON, priorDialogIn, controlIn, tableIn, titleIn, helpIn);

    }

    public String onDataTypeChange(int dataIdIn, CsiDataType dataTypeIn) {

        return "";
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                   Protected Methods                                    //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected String getColumnName(int indexIn) {

        return "COL_" + Integer.toString(indexIn);
    }

    @Override
    protected TableInstallRequest buildInstallRequest() {
        return null;
    }

    @Override
    protected TableInstallRequest buildUpdateRequest() {

        return null;
    }

    @Override
    protected void prepareFileParser() {

    }

    @Override
    protected void initializeColumnDefinitions(ColumnParameters[] parametersIn) {

    }

    @Override
    protected List<InstallerColumnDisplay> buildColumnDisplayList(ColumnParameters[] parametersIn) {
        return null;
    }

    @Override
    protected void determineDataFormat() {

    }

    @Override
    protected void resetDataFormat() {

    }

    @Override
    protected void displayNewPanel(int indexIn, ClickEvent eventIn) {

    }
}
