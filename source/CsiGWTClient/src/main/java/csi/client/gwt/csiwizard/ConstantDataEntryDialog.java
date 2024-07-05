package csi.client.gwt.csiwizard;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

import csi.client.gwt.csiwizard.panels.SingleEntryWizardPanel;
import csi.client.gwt.csiwizard.widgets.AbstractInputWidget;
import csi.client.gwt.csiwizard.widgets.BooleanInputWidget;
import csi.client.gwt.csiwizard.widgets.DateInputWidget;
import csi.client.gwt.csiwizard.widgets.DateTimeInputWidget;
import csi.client.gwt.csiwizard.widgets.IntegerInputWidget;
import csi.client.gwt.csiwizard.widgets.TextInputWidget;
import csi.client.gwt.csiwizard.widgets.TimeInputWidget;
import csi.client.gwt.csiwizard.widgets.ValueInputWidget;
import csi.client.gwt.events.UserInputEvent;
import csi.client.gwt.events.UserInputEventHandler;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.util.Display;
import csi.client.gwt.widget.boot.Dialog;
import csi.client.gwt.widget.boot.WatchingParent;
import csi.server.common.enumerations.CsiDataType;

/**
 * Created by centrifuge on 3/24/2015.
 */
public class ConstantDataEntryDialog extends WatchingParent {

    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                      GUI Objects                                       //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private PanelDialog dialog;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private static final Map<CsiDataType, String> _promptMap;
    private static final Map<CsiDataType, String> _infoMap;

    protected static CentrifugeConstants _constants = CentrifugeConstantsLocator.get();

    private static final String _txtDialogTitle = _constants.sqlFunctionConstantDialog_Title();
    private static final String _txtHelpTarget = _constants.sqlFunctionConstantDialog_HelpTarget();

    private static String _txtDefaultName = "constant value";
    private static String _txtDescription = _constants.sqlFunction_ConstantDescription();
    private static String _txtUnsupportedParameterType = _constants.parameters_UnsupportedType();

    static {
        _promptMap = new HashMap<CsiDataType, String>();
        _promptMap.put(CsiDataType.String,  _constants.parameterPrompt_String());
        _promptMap.put(CsiDataType.Boolean,  _constants.parameterPrompt_Boolean());
        _promptMap.put(CsiDataType.Integer,  _constants.parameterPrompt_Integer());
        _promptMap.put(CsiDataType.Number,  _constants.parameterPrompt_Number());
        _promptMap.put(CsiDataType.DateTime,  _constants.parameterPrompt_DateTime());
        _promptMap.put(CsiDataType.Date,  _constants.parameterPrompt_Date());
        _promptMap.put(CsiDataType.Time,  _constants.parameterPrompt_Time());
    }

    static {
        _infoMap = new HashMap<CsiDataType, String>();
        _infoMap.put(CsiDataType.String,  _constants.parameterInfo_String());
        _infoMap.put(CsiDataType.Boolean,  _constants.parameterInfo_Boolean());
        _infoMap.put(CsiDataType.Integer,  _constants.parameterInfo_Integer());
        _infoMap.put(CsiDataType.Number,  _constants.parameterInfo_Number());
        _infoMap.put(CsiDataType.DateTime,  _constants.parameterInfo_DateTime());
        _infoMap.put(CsiDataType.Date,  _constants.parameterInfo_Date());
        _infoMap.put(CsiDataType.Time,  _constants.parameterInfo_Time());
    }

    private String _results = null;
    private String _initialValue = null;
    UserInputEventHandler<Integer> _exitHandler = null;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Event Handlers                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private ClickHandler handleApplyClick = new ClickHandler() {

        @Override
        public void onClick(ClickEvent event) {

            _results = dialog.getText();
            dialog.destroy();

            if (null != _exitHandler) {

                _exitHandler.onUserInput(new UserInputEvent<Integer>(_results));
            }
        }
    };

    private ClickHandler handleCancelClick = new ClickHandler() {

        @Override
        public void onClick(ClickEvent event) {

            _results = _initialValue;
            dialog.destroy();

            if (null != _exitHandler) {

                _exitHandler.onUserInput(new UserInputEvent<Integer>(true));
            }
        }
    };


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////


    public ConstantDataEntryDialog(CsiDataType dataTypeIn, String initialValueIn, UserInputEventHandler<Integer> exitHandlerIn) {

        _initialValue = initialValueIn;
        _exitHandler = exitHandlerIn;

        SingleEntryWizardPanel myPanel = buildParameterPanel(dataTypeIn, initialValueIn);

        if (null != myPanel) {

            dialog = new PanelDialog(myPanel, _txtDialogTitle, _txtHelpTarget,
                                    _infoMap.get(dataTypeIn), handleApplyClick, handleCancelClick);

            if (null != dialog) {

                dialog.getActionButton().setText(Dialog.txtApplyButton);
                dialog.show(60);
            }
        }
    }

    public void show() {

    }

    public void hide() {

    }

    public String getText() {

        return _results;
    }

    private SingleEntryWizardPanel buildParameterPanel(CsiDataType dataTypeIn, String initialValueIn) {

        SingleEntryWizardPanel myPanel = null;

        if (null != dataTypeIn) {

            try {
                AbstractInputWidget myInputWidget = null;

                String myPrompt = _promptMap.get(dataTypeIn);
                String myName = _txtDefaultName;
                String myDescription = _txtDescription;

                switch (dataTypeIn) {

                    case String :

                        myInputWidget = new TextInputWidget(myPrompt, initialValueIn, true);
                        break;

                    case Boolean :

                        myInputWidget = new BooleanInputWidget(myPrompt, initialValueIn, true);
                        break;

                    case Integer :

                        myInputWidget = new IntegerInputWidget(myPrompt, initialValueIn, true);
                        break;

                    case Number :

                        myInputWidget = new ValueInputWidget(myPrompt, initialValueIn, true);
                        break;

                    case DateTime :

                        myInputWidget = new DateTimeInputWidget(myPrompt, initialValueIn, true);
                        break;

                    case Date :

                        myInputWidget = new DateInputWidget(myPrompt, initialValueIn, true);
                        break;

                    case Time :

                        myInputWidget = new TimeInputWidget(myPrompt, initialValueIn, true);
                        break;

                    case Unsupported :

                        Display.error(_txtUnsupportedParameterType);
                        break;
                }
                if (null != myInputWidget) {

                    myPanel = new SingleEntryWizardPanel(this, myName, myInputWidget, myDescription, initialValueIn, initialValueIn);
                }

            } catch (Exception myException) {

                Dialog.showException(myException);
            }
        }

        return myPanel;
    }
}
