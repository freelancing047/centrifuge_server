package csi.client.gwt.csiwizard.dialogs;

import csi.client.gwt.csiwizard.WizardControl;
import csi.client.gwt.csiwizard.WizardDialog;
import csi.client.gwt.csiwizard.panels.PairedListPanel;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.server.common.dto.SelectionListData.SelectorBasics;
import csi.server.common.model.FieldDef;

import java.util.*;

/**
 * Created by centrifuge on 9/20/2018.
 */
public class IdentifyCaptureFieldsDialog extends WizardDialog {


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private static CentrifugeConstants _constants = CentrifugeConstantsLocator.get();

    private List<FieldDef> _fullFieldList;
    private List<FieldDef> _finalFieldList;
    private Collection<SelectorBasics> _sourceDisplayList;
    private Collection<SelectorBasics> _selectedDisplayList;
    private Map<String, FieldDef> _fieldMap;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                      GUI Objects                                       //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private WizardControl wizardControl;
    private PairedListPanel<SelectorBasics> panel;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public IdentifyCaptureFieldsDialog(WizardControl controlDialogIn, String titleIn, String helpIn,
                                       String instructionsIn, List<FieldDef> fieldListIn) {

        super(controlDialogIn, new PairedListPanel<SelectorBasics>(true), titleIn, helpIn, instructionsIn);

        wizardControl = controlDialogIn;
        panel = (PairedListPanel)getCurrentPanel();
        _fullFieldList = fieldListIn;
        _finalFieldList = new ArrayList<FieldDef>();
        _sourceDisplayList = new ArrayList<SelectorBasics>();
        _selectedDisplayList = new ArrayList<SelectorBasics>();
        _fieldMap = new TreeMap<String, FieldDef>();
        for (int i = 0; _fullFieldList.size() > i; i++) {

            FieldDef myField = _fullFieldList.get(i);
            String myKey = myField.getLocalId();
            String myName = myField.getFieldName();
            String myRemarks = "Data Type: " + myField.getValueType().getLabel();
            int myOrdinal = myField.getOrdinal();

            _sourceDisplayList.add(new SelectorBasics(myKey, myName, myRemarks, myOrdinal));
            _fieldMap.put(myField.getLocalId(), myField);
        }
        panel.loadData(_sourceDisplayList, _selectedDisplayList);
    }

    public List<FieldDef> getResults() {

        _finalFieldList = new ArrayList<FieldDef>();
        _selectedDisplayList = panel.getListOnRight();

        if ((null != _selectedDisplayList) && (0 < _selectedDisplayList.size())) {

            for (SelectorBasics myItem : _selectedDisplayList) {

                if ((null != myItem) && (null != myItem.getKey())) {

                    FieldDef myField = _fieldMap.get(myItem.getKey());

                    if (null != myField) {

                        _finalFieldList.add(myField);
                    }
                }
            }
        }

        return _finalFieldList;
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
