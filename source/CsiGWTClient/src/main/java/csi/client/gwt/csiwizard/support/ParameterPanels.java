package csi.client.gwt.csiwizard.support;

import java.util.List;

import com.google.gwt.event.dom.client.ClickHandler;

import csi.client.gwt.csiwizard.WizardInterface;
import csi.client.gwt.csiwizard.panels.ParameterControlPanel;
import csi.server.common.dto.LaunchParam;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.model.query.QueryParameterDef;


public class ParameterPanels {

    
    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private List<ParameterPanelSet> _panelList = null;
    private ParameterControlPanel _controlPanel = null;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public ParameterPanels(WizardInterface parentDialogIn, List<QueryParameterDef> parameterListIn, ClickHandler handleRadioButtonClickIn) throws CentrifugeException {
        
        _controlPanel = new ParameterControlPanel(parentDialogIn, parameterListIn, handleRadioButtonClickIn);
    }

    public int getPanelCount() {
        
        int myCount = _controlPanel.getPanelCount();
        
        if  ((null != _panelList) && (0  < _panelList.size())) {
            
            _panelList.get(0).setCount(myCount);
        }
        
        return myCount;
    }

    public List<LaunchParam> gatherParameterData() throws CentrifugeException {
        
        return ParameterPanelSupport.gatherParameterData(_panelList);
    }

    public ParameterPanelSet getPanel(int indexIn) {
        
        ParameterPanelSet myParameterPanelSet = null;
        
        if (0 == indexIn) {
            
            return new ParameterPanelSet(_controlPanel, _controlPanel.getInstructions(), _controlPanel.getPanelCount());
            
        } else if (1 == indexIn) {
                
            // Process panel 0 to determine how many panels to  display
            _panelList = ParameterPanelSupport.createPanelList(_controlPanel);
        }

        if ((null != _panelList) && (0 <= indexIn) && (_panelList.size() > indexIn)) {
                
            myParameterPanelSet = _panelList.get(indexIn);
        }
        return myParameterPanelSet;
    }
}
