package csi.client.gwt.widget.boot;

import com.github.gwtbootstrap.client.ui.Heading;

/**
 * Created by centrifuge on 12/29/2017.
 */
public class CsiHeading extends Heading {

    public CsiHeading() {

        super(3);
    }

    public CsiHeading(int sizeIn) {

        super(sizeIn);
    }

    public CsiHeading(java.lang.String textIn) {

        super(3, encode(textIn));
    }

    public CsiHeading(int sizeIn, java.lang.String textIn) {

        super(sizeIn, encode(textIn));
    }

    public CsiHeading(java.lang.String textIn, java.lang.String subtextIn) {

        super(3, encode(textIn), encode(subtextIn));
    }

    public CsiHeading(int sizeIn, java.lang.String textIn, java.lang.String subtextIn) {

        super(sizeIn, encode(textIn), encode(subtextIn));
    }

    public void setSubtext(java.lang.String subtextIn) {

        setSubtext(encode(subtextIn));
    }

    public void setText(java.lang.String textIn) {

        super.setText(encode(textIn));
    }

    public java.lang.String getText() {

        return decode(super.getText());
    }

    private static String encode(String stringIn) {

        StringBuilder myBuffer = new StringBuilder();
        char[] myData = stringIn.toCharArray();

        for (int i = 0; myData.length > i; i++) {

            char myChar = myData[i];

            switch (myChar) {

                case '<':

                    myBuffer.append("&lt;");
                    break;

                case '>':

                    myBuffer.append("&gt;");
                    break;

                default:

                    myBuffer.append(myChar);
                    break;
            }
        }
        return myBuffer.toString();
    }

    private static String decode(String stringIn) {

        StringBuilder myBuffer = new StringBuilder();
        char[] myData = stringIn.toCharArray();

        for (int i = 0; myData.length > i; i++) {

            char myChar = myData[i];

            if (('&' == myChar) && (3 < (myData.length - i))
                    && ('t' == myData[i + 2]) && (';' == myData[i + 3])) {

                if ('l' == myData[i + 1]) {

                    i += 3;
                    myBuffer.append('<');

                } else if ('g' == myData[i + 1]) {

                    i += 3;
                    myBuffer.append('>');

                } else {

                    myBuffer.append(myChar);
                }

            } else {

                myBuffer.append(myChar);
            }
        }
        return myBuffer.toString();
    }
}
