package csi.client.gwt.widget.boot;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

import csi.client.gwt.widget.input_boxes.FilteredTextBox;

/**
 * Created by centrifuge on 10/5/2016.
 */
public class TextInputDialog extends ValidatingDialog {

    private FilteredTextBox _textInput = new FilteredTextBox();

    public TextInputDialog(String titleIn, String promptIn, String defaultIn,
                           ClickHandler executeHandlerIn, ClickHandler cancelHandlerIn) {
        super();
        initializeDisplay(titleIn, promptIn, defaultIn);
        wireInHandlers(executeHandlerIn, cancelHandlerIn);
    }

    public void setFocus() {

        setFocus(_textInput);
    }

    public String getResult() {

        return _textInput.getText();
    }

    protected void initializeDisplay(String titleIn, String promptIn, String defaultIn) {

        VerticalPanel myBasePanel = new VerticalPanel();
        Label myPrompt = new Label(promptIn);

        setTitle(titleIn);

        getActionButton().setText(Dialog.txtContinueButton);
        getActionButton().setVisible(true);

        getCancelButton().setText(Dialog.txtCancelButton);
        getCancelButton().setEnabled(true);
        getCancelButton().setVisible(true);

        _textInput.setWidth(Integer.toString(360) + "px");
        _textInput.setText(defaultIn);
        _textInput.setEnabled(true);

        myBasePanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
        myBasePanel.add(myPrompt);
        myBasePanel.add(_textInput);

        add(myBasePanel);

        setWidth("400px");
        myBasePanel.setWidth("380px");
        hideTitleCloseButton();
    }

    protected void wireInHandlers(ClickHandler executeHandlerIn, ClickHandler cancelHandlerIn) {

        getActionButton().addClickHandler(executeHandlerIn);
        getCancelButton().addClickHandler(cancelHandlerIn);
    }

    @Override
    protected void checkValidity() {

        String myTextInput = _textInput.getText();

        getActionButton().setEnabled((null != myTextInput) && (0 < myTextInput.length()));

        if (isMonitoring()) {

            DeferredCommand.add(new Command() {
                public void execute() {
                    checkValidity();
                }
            });
        }
    }
}
