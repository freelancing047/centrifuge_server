package csi.client.gwt.mainapp;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.LIElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.*;

import csi.client.gwt.WebMain;
import csi.client.gwt.csi_resource.filters.ResourceFilterListDialog;
import csi.client.gwt.csiwizard.AdHocEditLauncher;
import csi.client.gwt.csiwizard.dialogs.DataViewFromTemplateDialog;
import csi.client.gwt.dataview.DataViewPresenter;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.icon.ui.IconManager;
import csi.client.gwt.theme.editor.ThemeEditorManager;
import csi.client.gwt.theme.editor.ThemeEditorPresenter;
import csi.client.gwt.widget.boot.Dialog;
import csi.client.gwt.widget.buttons.Button;
import csi.client.gwt.widget.mapper.MapperWidget;
import csi.server.common.dto.user.UserSecurityInfo;
import csi.server.common.enumerations.AclResourceType;

public class GetStartedViewNew extends Composite implements CsiLandingPage {

    @UiField
    LIElement openContainer;
    @UiField
    HTMLPanel openDataview;

    @UiField
    LIElement createContainer;
    @UiField
    HTMLPanel newDataViewOption;

    @UiField
    LIElement launchContainer;
    @UiField
    HTMLPanel newDataViewFromTemplateOption;

    @UiField
    LIElement resourceContainer;
    @UiField
    HTMLPanel manageResource;

    @UiField
    LIElement filterContainer;
    @UiField
    HTMLPanel manageResourceFilters;

    @UiField
    LIElement iconContainer;
    @UiField
    HTMLPanel manageIcons;

    @UiField
    LIElement themeContainer;
    @UiField
    HTMLPanel manageThemes;

    @UiField
    LIElement samplesContainer;
    @UiField
    HTMLPanel samplesOption;

    @UiField
    LIElement usersContainer;
    @UiField
    HTMLPanel manageUsers;

    @UiField
    HTMLPanel scrollingPanel;
    @UiField
    HTMLPanel recentDataViewsPanel;

    @UiField
    LayoutPanel recentDataviewLayoutPanel;

    private RecentDataviewsGrid _recentGrid;


    interface SpecificUiBinder extends UiBinder<Widget, GetStartedViewNew> {
    }

    private static SpecificUiBinder uiBinder = GWT.create(SpecificUiBinder.class);

    private DataViewPresenter _presenter;
    private int nameColumnWidth = 0;

    public GetStartedViewNew(DataViewPresenter dataViewPresenter) {
        this._presenter = dataViewPresenter;

        try {

            initWidget(uiBinder.createAndBindUi(this));

            showMapperOnMainPage();
            Scheduler.get().scheduleFixedDelay(() -> {
                nameColumnWidth = recentDataViewsPanel.getElement().getClientWidth();
                _recentGrid = new RecentDataviewsGrid();
                recentDataviewLayoutPanel.add(_recentGrid.asWidget());

                return false;
            }, 500);

            openDataview.addDomHandler(event -> new OpenDataviewDialog(_presenter).show(), ClickEvent.getType());


            samplesOption.addDomHandler(event -> (new DataViewFromTemplateDialog(true)).show(), ClickEvent.getType());


            newDataViewOption.addDomHandler(event -> AdHocEditLauncher.open(AclResourceType.DATAVIEW), ClickEvent.getType());


            newDataViewFromTemplateOption.addDomHandler(event -> (new DataViewFromTemplateDialog(false)).show(), ClickEvent.getType());

            manageUsers.addDomHandler(event -> WebMain.injector.getEventBus().fireEvent(new csi.client.gwt.events.EnterAdminModeEvent()), ClickEvent.getType());

            manageResource.addDomHandler(event ->  WebMain.injector.getEventBus().fireEvent(new csi.client.gwt.events.EnterSharingModeEvent()), ClickEvent.getType());

            manageIcons.addDomHandler(event -> {
                IconManager iconManager = new IconManager();
                iconManager.show();
            }, ClickEvent.getType());

            manageResourceFilters.addDomHandler(event -> (new ResourceFilterListDialog()).show(), ClickEvent.getType());

            manageThemes.addDomHandler(event -> {
                ThemeEditorManager themeManager = new ThemeEditorManager(new ThemeEditorPresenter(), dataViewPresenter);
                themeManager.show();
            }, ClickEvent.getType());

        } catch (Exception myException) {

            Dialog.showException("GetStartedView", myException);
        }
    }

    public void saveState() {}

    public void restoreState() {
        reloadData();
    }

    public void forceExit() {}

    public void finalizeWidget(UserSecurityInfo userInfoIn) {
        try {

            scrollingPanel.setVisible(!userInfoIn.isRestricted());
            if (!userInfoIn.getIconAdmin()) {

                iconContainer.removeFromParent();
            }
            if (userInfoIn.isRestricted()) {

                openContainer.removeFromParent();
                createContainer.removeFromParent();
                launchContainer.removeFromParent();
                iconContainer.removeFromParent();
                themeContainer.removeFromParent();
                samplesContainer.removeFromParent();
            }
            if ((!userInfoIn.isAdmin()) && (!userInfoIn.isSecurity())) {

                usersContainer.removeFromParent();
            }
            if(!WebMain.getClientStartupInfo().isShowSamples()){

                samplesContainer.removeFromParent();
            }
            if(WebMain.getClientStartupInfo().getFeatureConfigGWT().isUseNewLogoutPage() && themeContainer != null) {
                resourceContainer.removeFromParent();
                themeContainer.removeFromParent();
                filterContainer.removeFromParent();
            }

        } catch (Exception myException) {

            Dialog.showException("GetStartedView", myException);
        }
    }

    public void reloadData() {
        try {

            if (null != _recentGrid) {

                _recentGrid.reloadData();
            }

        } catch (Exception myException) {

            Dialog.showException("GetStartedView", myException);
        }
    }

    private void showMapperOnMainPage() {
        if(WebMain.getClientStartupInfo().getFeatureConfigGWT().isShowMapper()){
            Button button = new Button(CentrifugeConstantsLocator.get().getStartedView_launchMappterDialog());
            button.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    Dialog dialog = new Dialog();
                    dialog.setBodyWidth("300px");
                    dialog.setBodyHeight("535px");
                    dialog.setWidth("330px");
                    dialog.setHeight("600px");
                    dialog.hideOnCancel();
                    dialog.hideOnAction();
                    dialog.add(new MapperWidget());
                    dialog.show();
                }
            });
            HTMLPanel htmlPanel = (HTMLPanel)getWidget();
            htmlPanel.add(button);
        }
    }
}
