package csi.client.gwt.widget.boot;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.ui.ResizeComposite;
import com.google.gwt.user.client.ui.Widget;

import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;

/**
 * Created by centrifuge on 3/21/2016.
 */
public class Instructions extends ResizeComposite {

    interface SpecificUiBinder extends UiBinder<Widget, Instructions> {
    }

    private static final CentrifugeConstants _constants = CentrifugeConstantsLocator.get();

    private static SpecificUiBinder uiBinder = GWT.create(SpecificUiBinder.class);

    @UiField
    DialogInfoTextArea instructionText;

    public Instructions() {

        super();
        initWidget(uiBinder.createAndBindUi(this));
        instructionText = new DialogInfoTextArea();
    }

    public Instructions(String instructionsIn) {

        this();
        instructionText = new DialogInfoTextArea(instructionsIn);
    }

    public void setText(String instructionsIn) {

        instructionText.setText(instructionsIn);
    }

    @Override
    public void onResize() {
        super.onResize();

        DeferredCommand.add(new Command() {
            public void execute() {

                int myWidth = Math.max(0, getOffsetWidth() - 10);
                int myHeight = getOffsetHeight();

                instructionText.setPixelSize(myWidth, myHeight);
            }
        });
    }

}
