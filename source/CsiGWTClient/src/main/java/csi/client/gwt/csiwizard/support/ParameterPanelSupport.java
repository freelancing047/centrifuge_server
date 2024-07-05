package csi.client.gwt.csiwizard.support;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import csi.client.gwt.csiwizard.panels.AbstractWizardPanel;
import csi.client.gwt.csiwizard.panels.MultipleEntryWizardPanel;
import csi.client.gwt.csiwizard.panels.ParameterControlPanel;
import csi.client.gwt.csiwizard.panels.SingleEntryWizardPanel;
import csi.client.gwt.csiwizard.widgets.AbstractInputWidget;
import csi.client.gwt.csiwizard.widgets.BooleanInputWidget;
import csi.client.gwt.csiwizard.widgets.DateInputWidget;
import csi.client.gwt.csiwizard.widgets.DateTimeInputWidget;
import csi.client.gwt.csiwizard.widgets.IntegerInputWidget;
import csi.client.gwt.csiwizard.widgets.TextInputWidget;
import csi.client.gwt.csiwizard.widgets.TimeInputWidget;
import csi.client.gwt.csiwizard.widgets.ValueInputWidget;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.util.Display;
import csi.client.gwt.widget.boot.CanBeShownParent;
import csi.client.gwt.widget.boot.Dialog;
import csi.server.common.dto.LaunchParam;
import csi.server.common.enumerations.CsiDataType;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.model.query.QueryParameterDef;
import csi.server.common.util.Format;

/**
 * Created by centrifuge on 1/15/2015.
 */
public class ParameterPanelSupport {


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private static final Map<CsiDataType, String> _infoMap;
    private static final CentrifugeConstants i18n = CentrifugeConstantsLocator.get();

    protected static CentrifugeConstants _constants = CentrifugeConstantsLocator.get();

    private static String _txtUnsupportedParameterType = _constants.parameters_UnsupportedType();
    private static String _txtListInfo = _constants.parameterInfo_MultiValue();
    private static String _txtDefaultPrompt = _constants.fieldList_FieldEditor_Value();
    private static String _txtDefaultName = _constants.parameters_UnnamedParameter();

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


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public static SingleEntryWizardPanel buildParameterPanel(CanBeShownParent parentDialogIn, QueryParameterDef parameterIn, boolean requiredIn, boolean creationModeIn) {

        SingleEntryWizardPanel myPanel = null;

        if (null != parameterIn) {

            try {
                AbstractInputWidget myInputWidget = null;
                CsiDataType myType = parameterIn.getType();

                List<String> myDefaultValues = creationModeIn ? null : parameterIn.getDefaultValues();
                List<String> myCurrentValues = creationModeIn ? parameterIn.getDefaultValues() : parameterIn.getValues();

                String myPrompt = selectValue(parameterIn.getPrompt(), parameterIn.getName(), _txtDefaultPrompt);
                String myName = selectValue(parameterIn.getName(), _txtDefaultName);
                String myDescription = _constants.parameters_DefaultDialogDescription(Format.value(myName));

                String myDefault = creationModeIn ? null : parameterIn.getDefaultValue();
                String myCurrent = creationModeIn ? parameterIn.getDefaultValue() : parameterIn.getValue();
                String myInitialValue = parameterIn.getListParameter() ? null : ((null != myCurrent) && (0 < myCurrent.length())) ? myCurrent : myDefault;

                switch (myType) {

                    case String :

                        myInputWidget = new TextInputWidget(myPrompt, myInitialValue, false);
                        break;

                    case Boolean :

                        myInputWidget = new BooleanInputWidget(myPrompt, myInitialValue, false);
                        break;

                    case Integer :

                        myInputWidget = new IntegerInputWidget(myPrompt, myInitialValue, false);
                        break;

                    case Number :

                        myInputWidget = new ValueInputWidget(myPrompt, myInitialValue, false);
                        break;

                    case DateTime :

                        myInputWidget = new DateTimeInputWidget(myPrompt, myInitialValue, false);
                        break;

                    case Date :

                        myInputWidget = new DateInputWidget(myPrompt, myInitialValue, false);
                        break;

                    case Time :

                        myInputWidget = new TimeInputWidget(myPrompt, myInitialValue, false);
                        break;

                    case Unsupported :

                        Display.error(_txtUnsupportedParameterType);
                        break;
                }
                if (null != myInputWidget) {

                    if (parameterIn.getListParameter()) {

                        myPanel = new MultipleEntryWizardPanel(parentDialogIn, myName, myInputWidget, myDescription, myCurrentValues, myDefaultValues, requiredIn);

                    } else {

                        myPanel = new SingleEntryWizardPanel(parentDialogIn, myName, myInputWidget, myDescription, myCurrent, myDefault, requiredIn);
                    }
                }

            } catch (Exception myException) {

                Dialog.showException(myException);
            }
        }

        return myPanel;
    }

    public static List<QueryParameterDef> buildUsedParameterList(List<QueryParameterDef> parameterListIn) {

        List<QueryParameterDef> myList = new ArrayList<QueryParameterDef>();

        if ((null != parameterListIn) && (0 < parameterListIn.size())) {

            int i;

            for (i = 0; parameterListIn.size() > i; i++) {

                QueryParameterDef myParameter = parameterListIn.get(i);

                if (myParameter.getAlwaysFill() || myParameter.isInUse()) {

                    myList.add(myParameter);
                }
            }
        }
        return myList;
    }

    public static List<QueryParameterDef> buildRequiredParameterList(List<QueryParameterDef> parameterListIn) {

        List<QueryParameterDef> myList = new ArrayList<QueryParameterDef>();

        if ((null != parameterListIn) && (0 < parameterListIn.size())) {

            int i;

            for (i = 0; parameterListIn.size() > i; i++) {

                QueryParameterDef myParameter = parameterListIn.get(i);

                if (myParameter.getAlwaysPrompt() || myParameter.needsPrompt()) {

                    myList.add(myParameter);
                }
            }
        }
        return myList;
    }

    public static String getParameterInfo(QueryParameterDef parameterIn) {

        String myInfo = null;

        if (null != parameterIn) {

            CsiDataType myType = parameterIn.getType();
            if (null != myType) {

                myInfo = ((null != parameterIn.getDescription()) ? (parameterIn.getDescription() + "\n\n") : "") //$NON-NLS-1$ //$NON-NLS-2$
                        + _infoMap.get(myType) + (parameterIn.getListParameter() ? ("\n\n" + _txtListInfo) : ""); //$NON-NLS-1$ //$NON-NLS-2$
            }
        }

        return (null != myInfo) ? myInfo : i18n.parameterPanelSupportParameterError(); //$NON-NLS-1$
    }

    public static List<LaunchParam> gatherParameterData(List<ParameterPanelSet> panelListIn) throws CentrifugeException {

        List<LaunchParam> myList = new ArrayList<LaunchParam>();

        if ((null != panelListIn) && (0 < panelListIn.size())) {

            for (int i = 1; panelListIn.size() > i; i++) {

                AbstractWizardPanel myPanel = panelListIn.get(i).getPanel();

                if (myPanel instanceof SingleEntryWizardPanel) {

                    myList.add(new LaunchParam(myPanel.getPanelName(), ((SingleEntryWizardPanel)myPanel).getList()));
                }
            }
        }
        return myList;
    }

    public static List<ParameterPanelSet> createPanelList(ParameterControlPanel controlPanelIn) {

        List<ParameterPanelSet> myPanelList = new ArrayList<ParameterPanelSet>();

        if (null != controlPanelIn) {

            List<QueryParameterDef> myParameterList = controlPanelIn.getParameterList();
            ParameterPanelSet myControlSet = new ParameterPanelSet(controlPanelIn, controlPanelIn.getInstructions(), controlPanelIn.getPanelCount());

            myPanelList.add(myControlSet);

            for (int i = 0; myParameterList.size() > i; i++) {

                myPanelList.add(createParameterPanelSet(controlPanelIn.getParentDialog(), myParameterList, i));
            }
        }
        return myPanelList;
    }

    public static ParameterPanelSet createParameterPanelSet(CanBeShownParent parentDialogIn, List<QueryParameterDef> parameterListIn, int indexIn) {

        ParameterPanelSet myPanelSet = null;

        if ((0 <= indexIn) && (parameterListIn.size() > indexIn)) {

            QueryParameterDef myParameter = parameterListIn.get(indexIn);
            String myInfo = getParameterInfo(myParameter);

            myPanelSet = new ParameterPanelSet(buildParameterPanel(parentDialogIn, myParameter, true, false),
                    _constants.dataviewFromTemplateWizard_ParameterPanel(myParameter.getName(),
                            Integer.toString(indexIn + 1),
                            Integer.toString(parameterListIn.size()), myInfo,
                            Dialog.txtNextButton), parameterListIn.size() + 1);
        }
        return myPanelSet;
    }

    public static String selectValue(String oneIn, String twoIn, String threeIn) {

        String myValue = oneIn;

        if ((null == myValue) || (0 == myValue.length())) {

            myValue = twoIn;

            if ((null == myValue) || (0 == myValue.length())) {

                myValue = threeIn;
            }
        }
        return myValue;
    }

    public static String selectValue(String oneIn, String twoIn) {

        String myValue = oneIn;

        if ((null == myValue) || (0 == myValue.length())) {

            myValue = twoIn;
        }
        return myValue;
    }
}
