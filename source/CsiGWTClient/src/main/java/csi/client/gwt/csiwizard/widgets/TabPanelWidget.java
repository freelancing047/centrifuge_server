package csi.client.gwt.csiwizard.widgets;

import com.github.gwtbootstrap.client.ui.TabLink;
import com.google.gwt.user.client.ui.AbsolutePanel;
import csi.client.gwt.widget.boot.CsiTabPanel;
import csi.client.gwt.widget.boot.Dialog;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by centrifuge on 3/19/2018.
 */
public class TabPanelWidget extends AbstractInputWidget {


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                      GUI Objects                                       //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private List<CsiTabWidget> widgetList;
    private CsiTabPanel tabCollection;
    private AbsolutePanel greyPanel;
    private AbsolutePanel whitePanel;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private int _topMargin = 5;
    private int _widgetWidth;
    private int _widgetHeight;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public TabPanelWidget() {

        greyPanel = new AbsolutePanel();
        whitePanel = new AbsolutePanel();
        tabCollection = new CsiTabPanel();
        widgetList = new ArrayList<>();

        greyPanel.getElement().getStyle().setBackgroundColor(Dialog.txtBorderColor);
        whitePanel.getElement().getStyle().setBackgroundColor("#ffffff");

        add(greyPanel);
        greyPanel.add(whitePanel);
        greyPanel.add(tabCollection);
    }

    public void addTab(CsiTabWidget tabIn) {

        try {

            tabIn.setPixelSize(_widgetWidth, _widgetHeight);
            tabCollection.add(tabIn);
            widgetList.add(tabIn);

        } catch (Exception myException) {

            Dialog.showException("WizardTabPanel", myException);
        }
    }

    public void setDefaultSelectedTab(int defaultSelectedTab) {
        tabCollection.setDefaultSelectedTab(defaultSelectedTab);
    }

    @Override
    public String getText() throws CentrifugeException {

        List<String> myText = new ArrayList<String>();

        for (CsiTabWidget myWidget : widgetList) {

            myText.add(myWidget.getText());
        }
        return StringUtil.concatInput(myText);
    }

    @Override
    public void resetValue() {

        for (CsiTabWidget myWidget : widgetList) {

            myWidget.resetValue();
        }
    }

    @Override
    public boolean isValid() {

        boolean myValidFlag = true;

        for (CsiTabWidget myWidget : widgetList) {

            if (!myWidget.isValid()) {

                myValidFlag = false;
                break;
            }
        }
        return myValidFlag;
    }

    @Override
    public boolean atReset() {

        boolean myResetFlag = true;

        for (CsiTabWidget myWidget : widgetList) {

            if (!myWidget.atReset()) {

                myResetFlag = false;
                break;
            }
        }
        return myResetFlag;
    }

    @Override
    public void grabFocus() {

        int myIndex = tabCollection.getSelectedTab();
        CsiTabWidget myWidget = widgetList.get(myIndex);

        if (null != myWidget) {

            myWidget.grabFocus();
        }
    }

    @Override
    public int getRequiredHeight() {

        int myMaxChildHeight = 0;

        for (CsiTabWidget myWidget : widgetList) {

            myMaxChildHeight = Math.max(myMaxChildHeight, myWidget.getRequiredHeight());
        }
        return Dialog.intTextBoxHeight + 5 + myMaxChildHeight;
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                   Protected Methods                                    //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected void layoutDisplay() {

        List<TabLink> myList = tabCollection.getLinks();
        int myWidth = getWidth();
        int myHeight = getHeight();

        _widgetWidth = myWidth - 10;
        _widgetHeight = myHeight - (Dialog.intTextBoxHeight + 5);

        greyPanel.setPixelSize(myWidth, myHeight);
        whitePanel.setPixelSize(_widgetWidth, _widgetHeight + 5);
        tabCollection.setPixelSize(_widgetWidth, myHeight - 5);

        greyPanel.setWidgetPosition(whitePanel, 4, Dialog.intTextBoxHeight - 4);
        greyPanel.setWidgetPosition(tabCollection, 4, 0);

        if (null != myList) {

            for (TabLink myWidget : myList) {

                myWidget.setPixelSize(_widgetWidth, _widgetHeight);
            }
        }
    }
}
