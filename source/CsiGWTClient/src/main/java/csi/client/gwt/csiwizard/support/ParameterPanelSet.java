package csi.client.gwt.csiwizard.support;

import csi.client.gwt.csiwizard.panels.AbstractWizardPanel;


public class ParameterPanelSet {
    
    private AbstractWizardPanel _panel;
    private String _instructions;
    private int _count;
    
    public ParameterPanelSet(AbstractWizardPanel panelIn, String instructionsIn, int countIn) {
        
        _panel = panelIn;
        _instructions = instructionsIn;
        _count = countIn;
    }
    
    public AbstractWizardPanel getPanel() {
        
        return _panel;
    }
    
    public String getInstructions() {
        
        return _instructions;
    }
    
    public void setCount(int countIn) {
        
        _count = countIn;
    }
    
    public int getCount() {
        
        return _count;
    }
}
