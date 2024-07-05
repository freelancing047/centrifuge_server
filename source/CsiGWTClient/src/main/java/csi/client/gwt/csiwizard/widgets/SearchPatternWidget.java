package csi.client.gwt.csiwizard.widgets;

import com.github.gwtbootstrap.client.ui.CheckBox;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.ui.Label;
import csi.client.gwt.csiwizard.support.ParameterValidator;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.widget.boot.Dialog;
import csi.server.common.enumerations.CsiDataType;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.util.StringUtil;

/**
 * Created by centrifuge on 3/19/2018.
 */
public class SearchPatternWidget extends AbstractInputWidget {



    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                      GUI Objects                                       //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    Label matchLabel = null;
    Label rejectLabel = null;
    TextBox matchTextBox = null;
    TextBox rejectTextBox = null;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    protected static final CentrifugeConstants _constants = CentrifugeConstantsLocator.get();
    protected static final String _defaultMatchLable = _constants.doMatchPattern();
    protected static final String _defaultRejectLable = _constants.doNotMatchPattern();

    private boolean _monitoring = false;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Event Handlers                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public SearchPatternWidget(String matchLabelIn, String rejectLabelIn, String matchPatternIn, String rejectPatternIn) {

        super(false);
        initializeObject(matchLabelIn, rejectLabelIn, matchPatternIn, rejectPatternIn);
    }

    public SearchPatternWidget() {

        this(_defaultMatchLable, _defaultRejectLable, null, null);
    }

    public SearchPatternWidget(String matchPatternIn, String rejectPatternIn) {

        this(_defaultMatchLable, _defaultRejectLable, matchPatternIn, rejectPatternIn);
    }

    public void displayValues(String[] valuesIn) {

        if ((null != valuesIn) && (0 < valuesIn.length)) {

            matchTextBox.setText(valuesIn[0]);

            if (1 < valuesIn.length) {

                rejectTextBox.setText(valuesIn[1]);
            }
        }
    }

    public String[] extractValues() {

        return new String[] {matchTextBox.getText(), rejectTextBox.getText()};
    }

    @Override
    public String getText() throws CentrifugeException {
        return null;
    }

    @Override
    public void resetValue() {

        matchTextBox.setText(null);
        rejectTextBox.setText(null);
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public boolean atReset() {
        return ((null == matchTextBox.getText()) || (0 == matchTextBox.getText().length()))
                && ((null == rejectTextBox.getText()) || (0 == rejectTextBox.getText().length()));
    }

    @Override
    public void grabFocus() {

        matchTextBox.setFocus(true);
    }

    @Override
    public int getRequiredHeight() {
        return 50;
    }

    @Override
    protected void layoutDisplay() {

        int  myRightMargin = getRightMargin();
        int myWidth = (getWidth() - myRightMargin - Dialog.intMargin) / 2;

        setWidgetTopHeight(matchLabel, 0, Style.Unit.PX, Dialog.intLabelHeight, Style.Unit.PX);
        setWidgetLeftWidth(matchLabel, 0, Style.Unit.PX, myWidth, Style.Unit.PX);
        setWidgetTopHeight(rejectLabel, 0, Style.Unit.PX, Dialog.intLabelHeight, Style.Unit.PX);
        setWidgetRightWidth(rejectLabel, myRightMargin, Style.Unit.PX, myWidth, Style.Unit.PX);

        setWidgetTopHeight(matchTextBox, Dialog.intLabelHeight, Style.Unit.PX, Dialog.intTextBoxHeight, Style.Unit.PX);
        setWidgetLeftWidth(matchTextBox, 0, Style.Unit.PX, myWidth, Style.Unit.PX);
        setWidgetTopHeight(rejectTextBox, Dialog.intLabelHeight, Style.Unit.PX, Dialog.intTextBoxHeight, Style.Unit.PX);
        setWidgetRightWidth(rejectTextBox, myRightMargin, Style.Unit.PX, myWidth, Style.Unit.PX);
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                   Protected Methods                                    //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    protected void initializeObject(String matchLabelIn, String rejectLabelIn, String matchPatternIn, String rejectPatternIn) {

        matchLabel = new Label(matchLabelIn);
        rejectLabel = new Label(rejectLabelIn);
        matchTextBox = new TextBox();
        matchTextBox.setText(matchPatternIn);
        rejectTextBox = new TextBox();
        rejectTextBox.setText(rejectPatternIn);
    }
}
