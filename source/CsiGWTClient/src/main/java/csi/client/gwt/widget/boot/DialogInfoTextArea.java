package csi.client.gwt.widget.boot;

import com.github.gwtbootstrap.client.ui.TextArea;
import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.ui.RichTextArea;

/**
 * Created by centrifuge on 4/3/2015.
 */
public class DialogInfoTextArea extends TextArea {

    public DialogInfoTextArea() {

        initialize();
    }

    public DialogInfoTextArea(String textIn) {

        this();
        setText(textIn);
    }

    public void setFont(String fontFamilyIn) {

        getElement().getStyle().setProperty("fontFamily", fontFamilyIn);
    }

    public void setFontStyle(Style.FontStyle styleIn) {

        getElement().getStyle().setFontStyle(styleIn);
    }

    public void setFontWeight(Style.FontWeight weightIn) {

        getElement().getStyle().setFontWeight(weightIn);
    }

    public void setFontSize(int sizeIn) {

        getElement().getStyle().setFontSize(sizeIn, Style.Unit.PT);
    }

    public void setFontSize(int sizeIn, Style.Unit unitsIn) {

        getElement().getStyle().setFontSize(sizeIn, unitsIn);
    }

    public void setFontColor(String colorIn) {

        getElement().getStyle().setColor(colorIn);
    }

    public void setLineHeight(int sizeIn) {

        getElement().getStyle().setLineHeight(sizeIn, Style.Unit.PT);
    }

    public void setLineHeight(int sizeIn, Style.Unit unitsIn) {

        getElement().getStyle().setLineHeight(sizeIn, unitsIn);
    }

    public void setTextAlign(Style.TextAlign alignmentIn) {

        getElement().getStyle().setTextAlign(alignmentIn);
    }

    public void setMarginTop(int sizeIn) {

        getElement().getStyle().setMarginTop(sizeIn, Style.Unit.PT);
    }

    public void setMarginTop(int sizeIn, Style.Unit unitsIn) {

        getElement().getStyle().setMarginTop(sizeIn, unitsIn);
    }

    private void initialize() {

        setReadOnly(true);
        setEnabled(false);
        getElement().getStyle().setBorderStyle(Style.BorderStyle.NONE);
        getElement().getStyle().setProperty("resize", "none");
        getElement().getStyle().setBackgroundColor("white");
        getElement().getStyle().setBorderColor("white");
        getElement().getStyle().setColor(Dialog.txtInfoColor);
    }
}
