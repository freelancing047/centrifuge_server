package csi.client.gwt.widget.display_list_widgets;


import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.ui.HTML;

/**
 * Created by centrifuge on 3/2/2015.
 */
public abstract class ComponentLabel extends HTML implements CanBeSelected {

    protected boolean _enabled;
    protected boolean _selected;
    protected String _text = "";

    protected abstract void setColor();

    public ComponentLabel(String textIn) {

        this(textIn, true, false);
    }

    public ComponentLabel(String textIn, boolean selectedIn) {

        this(textIn, true, selectedIn);
    }

    public ComponentLabel(String textIn, boolean enabledIn, boolean selectedIn) {

        _text = textIn;
        _enabled = enabledIn;
        _selected = selectedIn;
        setHeight("25px");
        if ('(' == textIn.charAt(0)) {

            setHTML(encode("  " + textIn));

        } else {

            setHTML(encode(textIn));
        }
        this.getElement().getStyle().setCursor(Style.Cursor.POINTER);
        setColor();
    }

    public void setEnabled(boolean enabledIn) {

        _enabled = enabledIn;
        setColor();
    }

    public void setSelected(boolean selectedIn) {

        _selected = selectedIn;
        setColor();
    }

    public boolean isEnabled() {

        return _enabled;
    }

    public boolean isSelected() {

        return _selected;
    }

    public boolean isValid() {

        return true;
    }

    @Override
    public String getText() {

        return _text;
    }

    private String encode(String textIn) {

        StringBuilder myBuffer = new StringBuilder();

        for (int i = 0; textIn.length() > i; i++ ) {

            char myCharacter = textIn.charAt(i);

            switch (myCharacter) {

                case ' ' :

                    myBuffer.append("&nbsp;");
                    break;

                case '\"' :

                    myBuffer.append("&#34;");
                    break;

                case '&' :

                    myBuffer.append("&amp;");
                    break;

                case '\'' :

                    myBuffer.append("&#39;");
                    break;

                case '-' :

                    myBuffer.append("&#8209;");
                    break;

                case '<' :

                    myBuffer.append("&lt;");
                    break;

                case '>' :

                    myBuffer.append("&gt;");
                    break;

                default :

                    myBuffer.append(myCharacter);
                    break;
            }
        }

        return myBuffer.toString();
    }

    private void encode(String textIn, StringBuilder myBuffer) {
    }
}
