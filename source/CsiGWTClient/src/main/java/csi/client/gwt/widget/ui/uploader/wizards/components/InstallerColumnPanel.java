package csi.client.gwt.widget.ui.uploader.wizards.components;

import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;

import csi.client.gwt.csiwizard.panels.SingleEntryWizardPanel;
import csi.client.gwt.events.CarriageReturnEvent;
import csi.client.gwt.events.EscapeKeyEvent;
import csi.client.gwt.widget.boot.CanBeShownParent;
import csi.client.gwt.widget.ui.uploader.wizards.support.FormatValue;
import csi.server.common.enumerations.CsiFileType;
import csi.server.common.exception.CentrifugeException;

/**
 * Created by centrifuge on 8/19/2015.
 */
public class InstallerColumnPanel extends SingleEntryWizardPanel {


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Event Handlers                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public InstallerColumnPanel(CanBeShownParent parentDialogIn, String nameIn, CsiFileType fileTypeIn,
                                String tableNameIn, List<InstallerColumnDisplay> listIn, FormatValue formatterIn,
                                boolean forceIn)
            throws CentrifugeException {

        super (parentDialogIn, nameIn, new InstallerColumnGridWidget(new ColumnGridInfo(), fileTypeIn,
                                                        tableNameIn, listIn, formatterIn, forceIn));

        parameterInput.setPanel(this);

        DeferredCommand.add(new Command() {
            public void execute() {

                ((InstallerColumnGridWidget)parameterInput).refresh();
            }
        });
    }

    public List<InstallerColumnDisplay> getDisplayList() {

        return ((InstallerColumnGridWidget)parameterInput).getGridData();
    }

    @Override
    public void handleCarriageReturn() {

        if (inOverlayMode()) {

            parameterInput.handleCarriageReturn();

        } else {

            fireEvent(new CarriageReturnEvent(isOkToLeave()));
        }
    }

    @Override
    public void handleEscapeKey() {

        if (inOverlayMode()) {

            parameterInput.handleEscapeKey();

        } else {

            fireEvent(new EscapeKeyEvent());
        }
    }
}
