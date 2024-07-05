package csi.client.gwt.widget.ui.uploader.wizards.components;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import csi.client.gwt.csiwizard.panels.SingleEntryWizardPanel;
import csi.client.gwt.widget.boot.CanBeShownParent;
import csi.client.gwt.widget.ui.uploader.wizards.support.FormatValue;
import csi.server.common.enumerations.CsiFileType;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.model.column.InstalledColumn;

import java.util.List;

/**
 * Created by centrifuge on 2/19/2019.
 */
public class InstallerColumnMapPanel extends SingleEntryWizardPanel {


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

    public InstallerColumnMapPanel(CanBeShownParent parentDialogIn, String nameIn,
                                   List<InstalledColumn> targetIn, List<InstallerColumnDisplay> sourceIn)
            throws CentrifugeException {

        super (parentDialogIn, nameIn, new InstallerColumnMapWidget());

        parameterInput.setPanel(this);

        DeferredCommand.add(new Command() {
            public void execute() {

                ((InstallerColumnGridWidget) parameterInput).refresh();
            }
        });
    }

}
