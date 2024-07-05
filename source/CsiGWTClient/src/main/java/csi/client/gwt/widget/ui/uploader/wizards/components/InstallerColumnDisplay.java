package csi.client.gwt.widget.ui.uploader.wizards.components;

import csi.server.common.enumerations.DisplayMode;
import csi.server.common.util.ValuePair;

/**
 * Created by centrifuge on 8/17/2015.
 */
public class InstallerColumnDisplay {


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private int columnNumber;
    private String firstValue;
    private ValuePair<String, DisplayMode> display;
    private String dataType;
    private boolean include;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public InstallerColumnDisplay() {

    }

    public InstallerColumnDisplay(int columnNumberIn, String firstValueIn, String nameIn, DisplayMode modeIn, String dataTypeIn, boolean includeIn) {

        columnNumber = columnNumberIn;
        firstValue = firstValueIn;
        display = new ValuePair<String, DisplayMode>(nameIn, modeIn);
        dataType = dataTypeIn;
        include = includeIn;
    }

    public void setColumnNumber(int columnNumberIn) {

        columnNumber = columnNumberIn;
    }

    public int getColumnNumber() {

        return columnNumber;
    }

    public void setFirstValue(String firstValueIn) {

        firstValue = firstValueIn;
    }

    public String getFirstValue() {

        return firstValue;
    }

    public void setDisplay(ValuePair<String, DisplayMode> displayIn) {

        display = displayIn;
    }

    public ValuePair<String, DisplayMode> getDisplay() {

        return display;
    }

    public void setName(String nameIn) {

        if (null != display) {

            display.setValue1(nameIn);

        } else {

            display = new ValuePair<String, DisplayMode>(nameIn, null);
        }
    }

    public String getName() {

        return (null != display) ? display.getValue1() : null;
    }

    public void setMode(DisplayMode modeIn) {

        if (null != display) {

            display.setValue2(modeIn);

        } else {

            display = new ValuePair<String, DisplayMode>(null, modeIn);
        }
    }

    public DisplayMode getMode() {

        return (null != display) ? display.getValue2() : null;
    }

    public void setDataType(String dataTypeIn) {

        dataType = dataTypeIn;
    }

    public String getDataType() {

        return dataType;
    }

    public void setInclude(boolean includeIn) {

        include = includeIn;
    }

    public boolean getInclude() {

        return include;
    }
}
