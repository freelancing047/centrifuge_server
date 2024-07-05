package csi.client.gwt.csiwizard.panels;

        import com.github.gwtbootstrap.client.ui.RadioButton;
        import com.google.gwt.dom.client.Style;
        import com.google.gwt.event.dom.client.ClickHandler;
        import com.google.gwt.user.client.ui.Label;

        import csi.client.gwt.csiwizard.panels.AbstractWizardPanel;
        import csi.client.gwt.csiwizard.widgets.AbstractInputWidget;
        import csi.client.gwt.i18n.CentrifugeConstants;
        import csi.client.gwt.i18n.CentrifugeConstantsLocator;
        import csi.client.gwt.widget.boot.Dialog;
        import csi.client.gwt.widget.boot.CanBeShownParent;
        import csi.server.common.exception.CentrifugeException;
        import csi.server.common.model.tables.InstalledTable;

/**
 * Created by centrifuge on 11/3/2014.
 */
public class TableUpdatePanel extends AbstractWizardPanel {


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                      GUI Objects                                       //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    Label infoLabel;
    RadioButton useWizardRadioButton;
    RadioButton useDseRadioButton;
    RadioButton refreshRadioButton;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    protected static CentrifugeConstants _constants = CentrifugeConstantsLocator.get();

    private static String _txtInstructions = _constants.fromScratchWizard_ModeSelectionPanel(Dialog.txtNextButton);
    private static String _txtTitle = _constants.dataviewFromScratchWizard_IntroTitle();
    private static String _txtInfo = _constants.dataviewFromScratchWizard_IntroInfo();
    private static String _txtUseWizard = _constants.dataviewFromScratchWizard_UseWizard();
    private static String _txtUseDSE = _constants.dataviewFromScratchWizard_UseDSE();

    private ClickHandler _handleRadioButtonClick;
    private InstalledTable _table = null;
    private boolean _refreshOk = false;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public TableUpdatePanel(CanBeShownParent parentDialogIn, ClickHandler handleRadioButtonClickIn)
            throws CentrifugeException {

        super(parentDialogIn, _txtTitle, true);

        _handleRadioButtonClick = handleRadioButtonClickIn;

        initializeObject();
    }

    public void attachTable(InstalledTable tableIn) {

        _table = tableIn;
        _refreshOk = (null != _table) && (null != _table.getSourceDefinition());
        useWizardRadioButton.setValue(!_refreshOk, false);
        refreshRadioButton.setValue(_refreshOk, false);
        refreshRadioButton.setVisible(_refreshOk);
        refreshRadioButton.setEnabled(_refreshOk);
    }

    public boolean useWizard() {

        return useWizardRadioButton.getValue();
    }

    public boolean defineDataSource() {

        return useDseRadioButton.getValue();
    }

    public boolean refreshDataSource() {

        return refreshRadioButton.getValue();
    }

    public static String getInstructions() {

        return _txtInstructions;
    }

    @Override
    public String getText() {

        return useWizardRadioButton.getValue() ? "true" : "false"; //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Override
    public void grabFocus() {

        useWizardRadioButton.setFocus(true);
    }

    @Override
    public void destroy() {

        removeFromParent();
    }

    @Override
    public void enableInput() {

        useWizardRadioButton.setEnabled(true);
        useDseRadioButton.setEnabled(true);
        refreshRadioButton.setEnabled(_refreshOk);
    }

    @Override
    public boolean isOkToLeave() {

        return true;
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                   Protected Methods                                    //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected void createWidgets(String descriptionIn, AbstractInputWidget inputCellIn) {

        boolean myRefreshOk = (null != _table) && (null != _table.getSourceDefinition());

        infoLabel = new Label(_txtInfo);

        useWizardRadioButton = new RadioButton("CreationMode", _txtUseWizard); //$NON-NLS-1$
        useWizardRadioButton.setValue(!myRefreshOk, false);
        useDseRadioButton = new RadioButton("CreationMode", "Edit data source(s)."); //$NON-NLS-1$
        useDseRadioButton.setValue(false, false);
        refreshRadioButton = new RadioButton("CreationMode", "Reload using defined data source(s)."); //$NON-NLS-1$
        refreshRadioButton.setValue(myRefreshOk, false);
        refreshRadioButton.setVisible(myRefreshOk);

        add(infoLabel);
        add(useWizardRadioButton);
        add(useDseRadioButton);
        add(refreshRadioButton);
    }

    @Override
    protected void layoutDisplay() {

        int myRequestedHeight = (5 * Dialog.intLabelHeight) + (2 * Dialog.intMargin);
        int myHeight = (myRequestedHeight <= _height) ? myRequestedHeight : _height;
        int mySpacing = (myHeight == myRequestedHeight) ? Dialog.intMargin : (_height - (5 * Dialog.intLabelHeight)) / 2;
        int myTop = (_height - myHeight) / 2;
        int mySmallStep = Dialog.intLabelHeight;
        int myBigStep = (0 < mySpacing) ? (mySmallStep + mySpacing) : mySmallStep;

        setWidgetTopHeight(infoLabel, myTop, Style.Unit.PX, Dialog.intLabelHeight, Style.Unit.PX);
        setWidgetLeftRight(infoLabel, Dialog.intMargin, Style.Unit.PX, Dialog.intMargin, Style.Unit.PX);

        myTop += myBigStep;
        setWidgetTopHeight(useWizardRadioButton, myTop, Style.Unit.PX, Dialog.intLabelHeight, Style.Unit.PX);
        setWidgetLeftRight(useWizardRadioButton, Dialog.intMargin, Style.Unit.PX, Dialog.intMargin, Style.Unit.PX);

        myTop += mySmallStep;
        setWidgetTopHeight(useDseRadioButton, myTop, Style.Unit.PX, Dialog.intLabelHeight, Style.Unit.PX);
        setWidgetLeftRight(useDseRadioButton, Dialog.intMargin, Style.Unit.PX, Dialog.intMargin, Style.Unit.PX);

        myTop += mySmallStep;
        setWidgetTopHeight(refreshRadioButton, myTop, Style.Unit.PX, Dialog.intLabelHeight, Style.Unit.PX);
        setWidgetLeftRight(refreshRadioButton, Dialog.intMargin, Style.Unit.PX, Dialog.intMargin, Style.Unit.PX);
    }

    @Override
    protected void wireInHandlers() {

        if (null != _handleRadioButtonClick) {

            useWizardRadioButton.addClickHandler(_handleRadioButtonClick);
            useDseRadioButton.addClickHandler(_handleRadioButtonClick);
            refreshRadioButton.addClickHandler(_handleRadioButtonClick);
        }
    }
}
