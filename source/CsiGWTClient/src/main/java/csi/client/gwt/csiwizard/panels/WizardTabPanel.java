package csi.client.gwt.csiwizard.panels;

import java.util.List;

import com.github.gwtbootstrap.client.ui.RadioButton;
import com.github.gwtbootstrap.client.ui.TabLink;
import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.ui.*;

import csi.client.gwt.csiwizard.widgets.AbstractInputWidget;
import csi.client.gwt.csiwizard.widgets.CsiTabWidget;
import csi.client.gwt.events.ChoiceMadeEventHandler;
import csi.client.gwt.viz.timeline.events.SelectionChangeEventHandler;
import csi.client.gwt.widget.boot.CsiTabPanel;
import csi.client.gwt.widget.boot.Dialog;
import csi.client.gwt.widget.boot.CanBeShownParent;
import csi.client.gwt.widget.display_list_widgets.SelectionCallback;
import csi.server.common.exception.CentrifugeException;

/**
 * Created by centrifuge on 7/27/2015.
 */
public class WizardTabPanel extends AbstractWizardPanel {

    private CsiTabPanel _tabCollection;
    private AbsolutePanel _greyPanel;
    private AbsolutePanel _whitePanel;
    private Widget _auxPanel;

    private int _widgetWidth;
    private int _widgetHeight;
    private boolean _isNested;

    public WizardTabPanel(CanBeShownParent parentDialogIn, boolean isNestedIn, boolean isRequired) {

        super(parentDialogIn, isRequired);

        try {

            _isNested = isNestedIn;
            initializeObject();

        } catch (Exception myException) {

            Dialog.showException("WizardTabPanel", myException);
        }
    }

    public WizardTabPanel(CanBeShownParent parentDialogIn, boolean isNestedIn) {

        this(parentDialogIn, isNestedIn, true);
    }

    public WizardTabPanel(CanBeShownParent parentDialogIn) {

        this(parentDialogIn, false);
    }

    public WizardTabPanel() {

        this(null, false);
    }

    public void addAuxPanel(Widget auxPanelIn) {

        if ((null != _auxPanel) && (null != _greyPanel)) {

            try {

                _greyPanel.remove(_auxPanel);

            } catch (Exception IGNORE) {}
        }
        if (null != auxPanelIn) {

            _auxPanel = auxPanelIn;
            _greyPanel.add(_auxPanel);
            placeAuxPanel();
        }
    }

    public void addTab(CsiTabWidget tabIn) {

        try {

            tabIn.setPixelSize(_widgetWidth, _widgetHeight);
            _tabCollection.add(tabIn);

        } catch (Exception myException) {

            Dialog.showException("WizardTabPanel", myException);
        }
    }

    public void setDefaultSelectedTab(int defaultSelectedTab) {
        _tabCollection.setDefaultSelectedTab(defaultSelectedTab);
    }

    @Override
    public String getText() throws CentrifugeException {
        return null;
    }

    @Override
    public void grabFocus() {

    }

    @Override
    public void destroy() {

    }

    @Override
    public void enableInput() {

    }

    @Override
    protected void createWidgets(String descriptionIn, AbstractInputWidget inputCellIn) {

        _greyPanel = new AbsolutePanel();
        _whitePanel = new AbsolutePanel();
        _tabCollection = new CsiTabPanel();

        _greyPanel.getElement().getStyle().setBackgroundColor(Dialog.txtBorderColor);
        _whitePanel.getElement().getStyle().setBackgroundColor("#ffffff");

        add(_greyPanel);
        _greyPanel.add(_whitePanel);
        _greyPanel.add(_tabCollection);
        if (null != _auxPanel) {

            _greyPanel.add(_auxPanel);
        }
    }

    @Override
    protected void layoutDisplay() throws CentrifugeException {

        List<TabLink> myList = _tabCollection.getLinks();

        _widgetWidth = _width - 10;
        _widgetHeight = _height - (Dialog.intTextBoxHeight + 5);

        _greyPanel.setPixelSize((_isNested ? (_width - Dialog.intIconSize) : _width), _height);
        _whitePanel.setPixelSize(_widgetWidth, _widgetHeight + 5);
        _tabCollection.setPixelSize(_widgetWidth, _height - 5);

        _greyPanel.setWidgetPosition(_whitePanel, 4, Dialog.intTextBoxHeight - 4);
        _greyPanel.setWidgetPosition(_tabCollection, 4, 0);
        if (null != myList) {

            for (TabLink myWidget : myList) {

                myWidget.setPixelSize(_widgetWidth, _widgetHeight);
            }
        }
        placeAuxPanel();
    }

    @Override
    protected void wireInHandlers() {

    }

    private void placeAuxPanel() {

        if (null != _auxPanel) {

            int myWidth = 0;

            _greyPanel.setWidgetPosition(_auxPanel, 0, 0);
            myWidth = _auxPanel.getOffsetWidth();
            _greyPanel.setWidgetPosition(_auxPanel, _widgetWidth - myWidth, 0);
        }
    }
}
