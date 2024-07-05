package csi.client.gwt.widget.input_boxes;

import java.util.Map;

import com.google.gwt.dom.client.Element;

public class FilteredTrimmedTextBox extends FilteredTextBox implements ValidityCheckCapable {


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public FilteredTrimmedTextBox() {

        super();
    }

    public FilteredTrimmedTextBox(Map<String, ? extends Object> rejectionMapIn) {

        super(rejectionMapIn);
    }

    public FilteredTrimmedTextBox(Map<String, ? extends Object> rejectionMapIn, boolean ignoreExceptionIn) {

        super(rejectionMapIn, ignoreExceptionIn);
    }

    public FilteredTrimmedTextBox(Element elementIn, String styleNameIn) {

        super(elementIn, styleNameIn);
    }

    public FilteredTrimmedTextBox(Element elementIn, String styleNameIn, Map<String, ? extends Object> rejectionMapIn, boolean ignoreExceptionIn) {

        super(elementIn, styleNameIn, rejectionMapIn, ignoreExceptionIn);
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                   Protected Methods                                    //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    //
    //
    //
    @Override
    protected boolean checkValue(String stringIn) {

        if (null != stringIn) {

            String myString = stringIn.trim();

            if (0 < myString.length()) {

                return ((null == _rejectionMap)
                        || ((!_ignoreException) && (null != _exception) && (_exception.equals(myString)))
                        || (!_rejectionMap.containsKey(myString)));
            }
        }
        return false;
    }
}
