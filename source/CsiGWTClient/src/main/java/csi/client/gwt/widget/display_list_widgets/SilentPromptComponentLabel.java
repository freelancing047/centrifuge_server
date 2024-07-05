package csi.client.gwt.widget.display_list_widgets;


/**
 * Created by centrifuge on 3/18/2015.
 */
public class SilentPromptComponentLabel extends ObjectComponentLabel {

    private final static String _text = " . . . ";

    public SilentPromptComponentLabel() {

        super(_text);
    }

    public SilentPromptComponentLabel(boolean selectedIn) {

        super(_text, selectedIn);
    }

    public SilentPromptComponentLabel(boolean enabledIn, boolean selectedIn) {

        super(_text, enabledIn, selectedIn);
    }
}
