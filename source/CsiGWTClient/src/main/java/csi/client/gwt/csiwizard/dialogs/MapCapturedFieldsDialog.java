package csi.client.gwt.csiwizard.dialogs;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import csi.client.gwt.csiwizard.WizardControl;
import csi.client.gwt.csiwizard.WizardDialog;
import csi.client.gwt.csiwizard.panels.ColumnMappingPanel;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.server.common.model.FieldDef;
import csi.server.common.model.column.InstalledColumn;
import csi.server.common.util.ValuePair;

import java.util.List;

/**
 * Created by centrifuge on 9/20/2018.
 */
public class MapCapturedFieldsDialog extends WizardDialog {


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private static CentrifugeConstants _constants = CentrifugeConstantsLocator.get();


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                      GUI Objects                                       //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private WizardControl wizardControl;
    ColumnMappingPanel panel;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public MapCapturedFieldsDialog(WizardControl wizardControlIn, String titleIn, String helpIn, String instructionsIn,
                                   List<InstalledColumn> columnListIn, List<FieldDef> fieldListIn) {

        super(wizardControlIn,
                new ColumnMappingPanel(null, 460,340, null, columnListIn, fieldListIn),
                titleIn, helpIn, instructionsIn);

        setAsFinal();
        wizardControl = wizardControlIn;
        panel = (ColumnMappingPanel)getCurrentPanel();
        DeferredCommand.add(new Command() {
            public void execute() {
                panel.beginMonitoring();
            }
        });
    }

    public List<ValuePair<InstalledColumn, FieldDef>> getResults() {

        return panel.getResults();
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Protected Methods                                   //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected void createPanel() {

    }

    @Override
    protected void execute() {

        wizardControl.clickComplete();
    }
}
