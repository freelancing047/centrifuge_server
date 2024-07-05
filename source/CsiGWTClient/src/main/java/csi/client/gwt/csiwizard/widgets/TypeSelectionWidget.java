package csi.client.gwt.csiwizard.widgets;

import java.util.ArrayList;
import java.util.List;

import com.github.gwtbootstrap.client.ui.RadioButton;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Label;

import csi.client.gwt.widget.boot.Dialog;
import csi.server.common.enumerations.CsiDataType;


public class TypeSelectionWidget extends AbstractInputWidget {
    
    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                      GUI Objects                                       //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////
    
    List<RadioButton> _buttonList = null;
    

    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private CsiDataType _value = CsiDataType.String;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////
    
    public TypeSelectionWidget(String promptIn, CsiDataType defaultIn, List<CsiDataType> listUnusedIn) {
        
        //
        // Initialize the display objects
        //
        initializeObject(promptIn, defaultIn, listUnusedIn);
    }
    
    public TypeSelectionWidget(String promptIn, List<CsiDataType> listUnusedIn) {
        
        this(promptIn, CsiDataType.String, listUnusedIn);
    }
    
    public TypeSelectionWidget(String promptIn, CsiDataType defaultIn) {
        
        this(promptIn, defaultIn, null);
    }
    
    public TypeSelectionWidget(String promptIn) {
        
        this(promptIn, CsiDataType.String, null);
    }

    @Override
    public String getText() {
        return _value.getLabel();
    }
    
    @Override
    public void resetValue() {
        
        _value = CsiDataType.String;
    }
    
    public void grabFocus() {
        
    }
    
    public int getRequiredHeight() {
        
        return (((null != parameterPrompt) ? 3 : 2) * Dialog.intLabelHeight);
    }

    public boolean atReset() {
        
        return true;
    }

    
    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                   Protected Methods                                    //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////
    
    //
    //
    //
    protected void initializeObject(String promptIn, CsiDataType defaultIn, List<CsiDataType> listUnusedIn) {
        
        //
        // Identify default value
        //
        if (null != defaultIn) {
                
            _value = defaultIn;
        }
        //
        // Create the widgets which are part of this selection widget
        //
        createWidgets(promptIn, listUnusedIn);
        
        //
        // Wire in the handlers
        //
        wireInHandlers();
    }
    
    protected void wireInHandlers() {

    }
    
    protected void layoutDisplay() {
        
        int myWidth = getWidth();
        int myMod = (_buttonList.size() + 1) / 2;
        int mySubWidth = myWidth / myMod;
        int myTop = (getHeight() - (3 * Dialog.intLabelHeight)) / 2;
         
        if (null != parameterPrompt) {
            
            myTop -= _margin;
            setWidgetTopHeight(parameterPrompt, myTop, Unit.PX, Dialog.intLabelHeight, Unit.PX);
            setWidgetLeftWidth(parameterPrompt, 0, Unit.PX, myWidth, Unit.PX);
            myTop += Dialog.intLabelHeight;
        }
        
        for (int i = 0; _buttonList.size() > i; i++) {
            
            RadioButton myWidget = _buttonList.get(i);
            int myX = (i % myMod) * mySubWidth;
            int myY = ((i / myMod) * Dialog.intLabelHeight) + myTop;
            
            setWidgetTopHeight(myWidget, myY, Unit.PX, Dialog.intLabelHeight, Unit.PX);
            setWidgetLeftWidth(myWidget, myX, Unit.PX, mySubWidth, Unit.PX);
        }
        
        if (null != addButton) {
        
            centerAddButton();
        }
    }
    

    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Private Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////
    
    private void createWidgets(String promptIn, List<CsiDataType> listUnusedIn) {
        
        if (null != promptIn) {
            
            String myPrompt = promptIn.trim();

            if (0 < myPrompt.length()) {
                
                parameterPrompt = new Label();
                parameterPrompt.setText(myPrompt);
                add(parameterPrompt);
            }
        }
        
        _buttonList = new ArrayList<RadioButton>();

        for (CsiDataType myEnum : CsiDataType.values()) {
            
            if ((CsiDataType.Unsupported != myEnum)
                    && (!((null != listUnusedIn) && (listUnusedIn.contains(myEnum))))) {
                
                RadioButton myWidget = new RadioButton("type", myEnum.getLabel());
                
                myWidget.setValue(myEnum == _value);
                myWidget.addClickHandler(wireInButton(myEnum));
                _buttonList.add(myWidget);
                add(myWidget);
            }
        }
    }
    
    ClickHandler wireInButton(final CsiDataType enumIn) {
        
        return new ClickHandler() {
            
            public void onClick(ClickEvent eventIn) {
                
                _value = enumIn;
            }
        };
    }

    @Override
    public boolean isValid() {

        return true;
    }
}
