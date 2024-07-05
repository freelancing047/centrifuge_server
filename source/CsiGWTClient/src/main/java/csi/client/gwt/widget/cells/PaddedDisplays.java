package csi.client.gwt.widget.cells;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.sencha.gxt.core.client.XTemplates;


public class PaddedDisplays {
    
    
    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                   Embedded Classes                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    class PaddedData {
        
        String data;
        byte[] left;
        byte[] right;
        
        public PaddedData(int leftIn, int rightIn) {
            
            left = new byte[leftIn];
            right = new byte[rightIn];
        }
        
        public PaddedData padData(String dataIn) {
            
            data = dataIn;
            return this;
        }
        
        public String getData() {
            
            return data;
        }
        
        public byte[] getLeft() {
            
            return left;
        }
        
        public byte[] getRight() {
            
            return right;
        }
    }
    
    
    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                  Embedded Interfaces                                   //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    interface paddedDisplay extends XTemplates {
        
        SafeHtml display(PaddedData dataIn);
    }

    interface leftPadding extends paddedDisplay {

        @XTemplate("<div><tpl for='left'>&nbsp;</tpl>{dataIn.data}</div>")
        SafeHtml display(PaddedData dataIn);
    }

    interface rightPadding extends paddedDisplay {

        @XTemplate("<div>{dataIn.data}<tpl for='right'>&nbsp;</tpl></div>")
        SafeHtml display(PaddedData dataIn);
    }

    interface fullPadding extends paddedDisplay {

        @XTemplate("<div><tpl for='left'>&nbsp;</tpl>{dataIn.data}<tpl for='right'>&nbsp;</tpl></div>")
        SafeHtml display(PaddedData dataIn);
    }

    interface noPadding extends paddedDisplay {

        @XTemplate("<div>{dataIn.data}</div>")
        SafeHtml display(PaddedData dataIn);
    }
    
    
    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private static leftPadding _leftPadding = GWT.create(leftPadding.class);
    private static rightPadding _rightPadding = GWT.create(rightPadding.class);
    private static fullPadding _fullPadding = GWT.create(fullPadding.class);
    private static noPadding _noPadding = GWT.create(noPadding.class);
    
    private PaddedData _paddedData;
    private paddedDisplay _format;

    
    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////
    
    public PaddedDisplays(int leftPaddingIn, int rightPaddingIn) {
        
        _paddedData = new PaddedData(leftPaddingIn, rightPaddingIn);
        _format = (0 < leftPaddingIn) ? ((0 < rightPaddingIn) ? _fullPadding : _leftPadding)
                                        : ((0 < rightPaddingIn) ? _rightPadding : _noPadding);
    }

    public SafeHtml display(String stringIn) {
        
        return _format.display(_paddedData.padData(stringIn));
    }
}
