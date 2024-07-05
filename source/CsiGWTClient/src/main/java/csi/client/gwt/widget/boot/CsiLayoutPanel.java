package csi.client.gwt.widget.boot;

import com.google.gwt.user.client.ui.LayoutPanel;

/**
 * Created by centrifuge on 9/13/2016.
 */
public abstract class CsiLayoutPanel extends LayoutPanel {

    protected abstract void layoutDisplay();

    private int _height = 0;
    private int _width = 0;
    private boolean _processingRequest = false;

    public CsiLayoutPanel() {

        super();
    }

    public CsiLayoutPanel(int widthIn, int heightIn) {

        super();

        setPixelSize(widthIn, heightIn);
    }

    public int getWidth() {

        return _width;
    }

    public int getHeight() {

        return _height;
    }

    @Override
    public void setHeight(String stringIn) {

        if (_processingRequest) {

            super.setHeight(stringIn);

        } else {

            forceDimensions(_width, decode(stringIn));
        }
    }

    @Override
    public void setWidth(String stringIn) {

        if (_processingRequest) {

            super.setWidth(stringIn);

        } else {

            forceDimensions(decode(stringIn), _height);
        }
    }

    @Override
    public void setPixelSize(int widthIn, int heightIn) {

        if (_processingRequest) {

            super.setPixelSize(widthIn, heightIn);
        } else {

            forceDimensions(widthIn, heightIn);
        }
    }

    protected int decode(String valueIn) {

        int myValue = 0;

        for (int i = 0; valueIn.length() > i; i++) {

            int myDigit = (int)valueIn.charAt(i) - (int)('0');

            if ((0 <= myDigit) && (9 >= myDigit)) {

                myValue = (myValue * 10) + myDigit;

            } else {

                break;
            }
        }
        return myValue;
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Private Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private void forceDimensions(int widthIn, int heightIn) {

        _processingRequest = true;
        _width = widthIn;
        _height = heightIn;
        super.setPixelSize(_width, _height);
        _processingRequest = false;

        if ((0 < _width) && (0 < _height)) {

            layoutDisplay();
        }
    }
}
