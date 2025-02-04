package csi.client.gwt.viz.graph.tab.pattern.settings;

import com.google.gwt.event.dom.client.DomEvent;

public class InputEvent extends DomEvent<InputHandler> {
    private static final Type<InputHandler> TYPE = new Type<>("input",//NON-NLS
            new InputEvent());

    public static Type<InputHandler> getType() {
        return TYPE;
    }

    protected InputEvent() {
    }

    @Override
    public final Type<InputHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(InputHandler handler) {
        handler.onInput(this);
    }
}
