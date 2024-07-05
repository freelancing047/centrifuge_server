/** 
 *  Copyright (c) 2008 Centrifuge Systems, Inc. 
 *  All rights reserved. 
 *   
 *  This software is the confidential and proprietary information of 
 *  Centrifuge Systems, Inc. ("Confidential Information").  You shall 
 *  not disclose such Confidential Information and shall use it only
 *  in accordance with the terms of the license agreement you entered 
 *  into with Centrifuge Systems.
 *
 **/
package csi.client.gwt.widget.ui.surface;
import com.google.gwt.i18n.client.NumberFormat;
import java.util.Locale;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class TitleDefinition {

    private String text = ""; //$NON-NLS-1$
    private int fontSize = 15;
    private String fontColor = "gray"; //$NON-NLS-1$

    private String delimOpen= "(", delimClose= ")";
    private Integer measureValue = 0;

    /***
     * Overwrites default delimeter ( what the measure value, or whatever is after the measure name)
     * @param delimOpen
     */
    public void setDelim(String delimOpen, String delimClose) {
        this.delimOpen = delimOpen;
        this.delimClose = delimClose;
    }


    public String getDelimOpen() {
        return delimOpen;
    }


    public String getDelimClose() {
        return delimClose;
    }

    public Integer getMeasureValue() {
        return measureValue;
    }

    public void setMeasureValue(Integer measureValue) {
        this.measureValue = measureValue;
    }

    public String getText() {
        StringBuilder b = new StringBuilder();

        if(measureValue != 0){
            b.append(text).append(" ").append(delimOpen).append(NumberFormat.getFormat("#,##0").format(measureValue)).append(delimClose);
        }else{
            b.append(text);
        }

        return b.toString();
    }


    /***
     * Default method to update the text( what is the title ) and the value that follows it.
     */
    public void setTitleAndValue(String text, Integer measureValue){
            setText(text);
            setMeasureValue(measureValue);
    }


    public void setText(String text) {
        this.text = text;
    }

    public int getFontSize() {
        return fontSize;
    }

    public void setFontSize(int fontSize) {
        this.fontSize = fontSize;
    }

    public String getFontColor() {
        return fontColor;
    }

    public void setFontColor(String fontColor) {
        this.fontColor = fontColor;
    }

}
