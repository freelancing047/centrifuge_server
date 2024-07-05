package csi.client.gwt.widget.ui;

import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;

import csi.client.gwt.widget.boot.Dialog;

/**
 * Created by centrifuge on 11/26/2014.
 */
public class CsiBanner extends HorizontalPanel {


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                      GUI Objects                                       //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    Label _label;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public CsiBanner(String textIn, String backgroundIn, String foregroundIn) {

        _label = (null != textIn) ? new Label(textIn) : new Label();
        if (null != foregroundIn) {

            _label.getElement().getStyle().setColor(foregroundIn);
        }
        if (null != backgroundIn) {

            getElement().getStyle().setBackgroundColor(backgroundIn);
        }
        setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
        setHeight(Integer.toString(Dialog.intLabelHeight) + "px"); //$NON-NLS-1$
        setWidth("100%"); //$NON-NLS-1$
        add(_label);
    }

    public CsiBanner(String textIn, String backgroundIn) {

        this(textIn, backgroundIn, null);
    }

    public CsiBanner(String textIn) {

        this(textIn, null, null);
    }

    public CsiBanner() {

        this(null, null, null);
    }

    public void updateBanner(String textIn, String backgroundIn, String foregroundIn) {

        setText(textIn);
        setBackground(backgroundIn);
        setForeground(foregroundIn);
    }

    public void updateBanner(String textIn, String backgroundIn) {

        setText(textIn);
        setBackground(backgroundIn);
    }

    public void updateBanner(String textIn) {

        setText(textIn);
    }

    public void setText(String textIn) {

        _label.setText(textIn);
    }

    public void setBackground(String colorIn) {

        getElement().getStyle().setBackgroundColor((null != colorIn) ? colorIn : "white"); //$NON-NLS-1$
    }

    public void setForeground(String colorIn) {

        _label.getElement().getStyle().setColor((null != colorIn) ? colorIn : "black"); //$NON-NLS-1$
    }
}
