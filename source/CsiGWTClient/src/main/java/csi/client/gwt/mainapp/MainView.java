package csi.client.gwt.mainapp;

import java.util.Map;
import java.util.Stack;
import java.util.TreeMap;

import com.google.common.base.Strings;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ContextMenuEvent;
import com.google.gwt.event.dom.client.ContextMenuHandler;
import com.google.gwt.user.client.ui.*;
import com.sencha.gxt.widget.core.client.container.CardLayoutContainer;

import csi.client.gwt.WebMain;
import csi.client.gwt.dataview.AbstractDataViewPresenter;
import csi.client.gwt.widget.boot.Dialog;
import csi.client.gwt.widget.boot.TitleBar;
import csi.server.common.dto.user.UserSecurityInfo;
import javafx.application.Application;

public class MainView extends ResizeComposite {

    private static final int APPLICATION_TOOLBAR_HEIGHT = 30;

    public enum Mode {
        GET_STARTED, DATA_SOURCE_EDITOR, ADMIN, SHARING, ANALYSIS, VIZ
    }
    public enum Status {
        SUCCESS, NEED_MODE, NEED_DISPLAY, COLLISION, MIS_MATCH, EXCEPTION
    }

    private MainPresenter presenter = null;
    private CsiLandingPage getStartedView = null;
    private CardLayoutContainer contentArea = new CardLayoutContainer();
    private Map<Mode, CsiDisplay> displayMap = new TreeMap<Mode, CsiDisplay>();
    private Stack<Mode> displayStack = new Stack<Mode>();
    private DockLayoutPanel applicationWrapper = new DockLayoutPanel(Unit.PX);

    public ContextMenuHandler contextMenuCancel = new ContextMenuHandler() {

        public void onContextMenu(ContextMenuEvent eventIn) {

            // stop the browser from opening the context menu
            eventIn.preventDefault();
            eventIn.stopPropagation();
        }
    };

    public MainView(MainPresenter presenterIn) {

        try {

            CsiLandingPage blankPage = new BlankPage();

            presenter = presenterIn;
            addDomHandler(contextMenuCancel, ContextMenuEvent.getType());

            if (WebMain.getClientStartupInfo().isProvideBanners()) {
                applicationWrapper.addNorth(SecurityBanner.getTopBanner(), SecurityBanner.getHeight());
                applicationWrapper.addSouth(SecurityBanner.getBottomBanner(), SecurityBanner.getHeight());
                SecurityBanner.displayBanner(null);
            }
            if (WebMain.getClientStartupInfo().getDisplayApplicationBanner()) {

                applicationWrapper.addNorth(ApplicationBanner.getBanner(), ApplicationBanner.getHeight());
            }
            applicationWrapper.addNorth(ApplicationToolbarLocator.getInstance(), APPLICATION_TOOLBAR_HEIGHT);
            applicationWrapper.addNorth(TitleBar.getInstance(), APPLICATION_TOOLBAR_HEIGHT);
            showApplicationToolbar();
            hideApplicationToolbar();
            if(WebMain.getClientStartupInfo().getFeatureConfigGWT().isUseNewLogoutPage()) {
                getStartedView = new GetStartedViewNew(null);
            } else {
                getStartedView = new GetStartedView(null);
            }
            displayMap.put(Mode.GET_STARTED, getStartedView);
            displayStack.push(Mode.GET_STARTED);
            contentArea.clear();
            contentArea.add(blankPage.asWidget());
            contentArea.setActiveWidget(blankPage.asWidget());

            // Center widget
            applicationWrapper.add(contentArea);
            initWidget(applicationWrapper);

        } catch (Exception myException) {

            Dialog.showException("MainView", 1, myException);
        }
    }

    public void initializeDisplay(UserSecurityInfo userSecurityInfoIn) {
        showApplicationToolbar().finalizeMenus(userSecurityInfoIn);
        getStartedView.finalizeWidget(userSecurityInfoIn);
    }

    public void refreshDataViewList() {

        if (presenter.displayLandingPage()) {

            getStartedView.reloadData();
        }
    }

    public void hideApplicationToolbar(){

        try {

            applicationWrapper.setWidgetSize(ApplicationToolbarLocator.getInstance(), 0);
            ApplicationToolbarLocator.getInstance().setVisible(false);

        } catch (Exception myException) {

            Dialog.showException("MainView", 2, myException);
        }
    }

    public AbstractApplicationToolbar showApplicationToolbar(){

        try {

            applicationWrapper.setWidgetSize(TitleBar.getInstance(), 0);
            TitleBar.getInstance().setVisible(false);
            applicationWrapper.setWidgetSize(ApplicationToolbarLocator.getInstance(), APPLICATION_TOOLBAR_HEIGHT);

            ApplicationToolbarLocator.getInstance().setVisible(true);

        } catch (Exception myException) {

            Dialog.showException("MainView", 3, myException);
        }
        return ApplicationToolbarLocator.getInstance();
    }

    public void replaceMenuBar(String textIn){

        TitleBar.getInstance().setText(textIn);
        hideApplicationToolbar();
        applicationWrapper.setWidgetSize(TitleBar.getInstance(), APPLICATION_TOOLBAR_HEIGHT);
        TitleBar.getInstance().setVisible(true);
    }

    public Status enterMode(Mode modeIn, CsiDisplay displayIn) {

        Status myStatus = Status.EXCEPTION;

        try {

            myStatus = Status.NEED_MODE;

            if (null != modeIn) {

                CsiDisplay myDisplay = Mode.GET_STARTED.equals(modeIn)
                                                ? getStartedView
                                                : ((null != displayIn) ? displayIn : displayMap.get(modeIn));

                myStatus = Status.NEED_DISPLAY;
                if (null != myDisplay) {

                    myStatus = Status.COLLISION;
                    if (Mode.SHARING.equals(modeIn) || Mode.GET_STARTED.equals(modeIn) || (null == displayMap.get(modeIn))) {

                        Mode myMode = displayStack.empty() ? null : displayStack.peek();

                        displayStack.push(modeIn);
                        displayMap.put(modeIn, myDisplay);
                        contentArea.clear();
                        contentArea.add(myDisplay.asWidget());
                        contentArea.setActiveWidget(myDisplay.asWidget());

                        if (null != myMode) {

                            myDisplay = displayMap.get(myMode);

                            if (null != myDisplay) {

                                if (!myMode.equals(modeIn)) {

                                    myDisplay.saveState();
                                    myDisplay.asWidget().removeFromParent();
                                }
                                if (Mode.ANALYSIS.equals(myMode)) {

                                    myDisplay.restoreState();
                                }
                            }
                        }
                        if (Mode.GET_STARTED.equals(getMode())) {

                            if (presenter.displayLandingPage()) {

                                refreshDataViewList();
                            }
                        }
                        SecurityBanner.displayBanner(null);

                    }
                }
            }

        } catch (Exception myException) {

            Dialog.showException("MainView", 4, myException);
        }
        ApplicationToolbarLocator.getInstance().toggleMessagesMenu(modeIn);
        return myStatus;
    }

    public Status exitMode(Mode modeIn) {

        Status myStatus = Status.EXCEPTION;
        Mode newMode = null;

        try {

            myStatus = Status.MIS_MATCH;

            if ((!displayStack.empty()) && ((null == modeIn) || modeIn.equals(displayStack.peek()))) {

                Mode myMode = displayStack.pop();
                CsiDisplay myDisplay = (null != myMode) ? displayMap.get(myMode) : null;

                if ((null != myDisplay) && ((!Mode.SHARING.equals(myMode)) || (!displayStack.contains(myMode)))) {

                    myDisplay.forceExit();
                    displayMap.remove(myMode);
                    myDisplay.asWidget().removeFromParent();
                }
                myMode = displayStack.peek();
                myDisplay = (null != myMode) ? displayMap.get(myMode) : null;
                if (null != myDisplay) {

                    myDisplay.restoreState();
                    contentArea.clear();
                    contentArea.add(myDisplay.asWidget());
                    contentArea.setActiveWidget(myDisplay.asWidget());

                } else {

                    enterMode(Mode.GET_STARTED, getStartedView);
                }
                if (Mode.GET_STARTED.equals(getMode())) {

                    refreshDataViewList();
                }
                newMode = myMode;
                myStatus = Status.SUCCESS;
            }
            if (Mode.ANALYSIS.equals(getMode())) {

                AbstractDataViewPresenter myPresenter = presenter.getDataViewPresenter(true);

                if (null != myPresenter) {

                    myPresenter.checkStatus();
                }
            }

        } catch (Exception myException) {

            Dialog.showException("MainView", 5, myException);
        }
        ApplicationToolbarLocator.getInstance().toggleMessagesMenu(newMode);
        return myStatus;
    }

    public void removeDataViewDisplay() {

        CsiDisplay mySharingDisplay = displayMap.get(Mode.SHARING);
        CsiDisplay myAnalysisDisplay = displayMap.get(Mode.ANALYSIS);
        boolean mySharingActive = Mode.SHARING.equals(getMode());

        if (null != myAnalysisDisplay) {

            if (mySharingActive) {

                mySharingDisplay.saveState();
                displayStack.pop();

            } else {

                myAnalysisDisplay.asWidget().removeFromParent();
            }
            displayStack.pop();
            displayMap.remove(Mode.ANALYSIS);
            if (mySharingActive) {

                if (!displayStack.contains(Mode.SHARING)) {

                    displayStack.push(Mode.SHARING);
                }

            } else {

                Mode myActiveMode = displayStack.peek();
                CsiDisplay myActiveDisplay = (null != myActiveMode) ? displayMap.get(myActiveMode) : null;

                contentArea.clear();
                contentArea.add(myActiveDisplay);
                contentArea.setActiveWidget(myActiveDisplay);
            }
        }
    }

    @Override
    public void onResize() {

        contentArea.onResize();
    }
    
    public Mode getMode() {
        return displayStack.empty() ? null : displayStack.peek();
    }

    public Widget getWidget() {
        return displayMap.get(getMode()).asWidget();
    }

    public IsWidget getDisplayObject() {
        return displayMap.get(getMode());
    }
}
