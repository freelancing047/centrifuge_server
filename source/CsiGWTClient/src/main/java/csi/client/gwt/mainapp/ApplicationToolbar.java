package csi.client.gwt.mainapp;

import java.util.List;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.NavHeader;
import com.github.gwtbootstrap.client.ui.Divider;
import com.github.gwtbootstrap.client.ui.NavLink;
import com.github.gwtbootstrap.client.ui.Navbar;
import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.user.client.ui.Composite;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.container.HBoxLayoutContainer;
import com.sencha.gxt.widget.core.client.event.HideEvent.HideHandler;
import com.sencha.gxt.widget.core.client.menu.Menu;

import csi.client.gwt.WebMain;
import csi.client.gwt.admin.PasswordPopup;
import csi.client.gwt.csi_resource.ExportDialog;
import csi.client.gwt.csi_resource.ImportDialog;
import csi.client.gwt.csi_resource.filters.ResourceFilterListDialog;
import csi.client.gwt.csi_resource.ResourceSaveAsDialog;
import csi.client.gwt.csi_resource.ResourceSaveCallback;
import csi.client.gwt.dataview.AbstractDataViewPresenter;
import csi.client.gwt.dataview.DataViewInNewTab;
import csi.client.gwt.dataview.DataviewSaveAsTemplateDialog;
import csi.client.gwt.dataview.export.kml.KmlExportImpl;
import csi.client.gwt.dataview.fieldlist.FieldList;
import csi.client.gwt.dataview.fieldlist.editor.derived.InstallFunctionDialog;
import csi.client.gwt.dataview.linkup.LinkupDefinitionDialog;
import csi.client.gwt.csiwizard.dialogs.DataViewFromTemplateDialog;
import csi.client.gwt.csiwizard.AdHocEditLauncher;
import csi.client.gwt.edit_sources.DataSourceEditorView;
import csi.client.gwt.etc.ApplicationInjector;
import csi.client.gwt.events.CloseDataViewEvent;
import csi.client.gwt.events.DataChangeEvent;
import csi.client.gwt.events.DataChangeEventHandler;
import csi.client.gwt.http.Post;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.icon.ui.IconManager;
import csi.client.gwt.sharing.ConfigureReaperDialog;
import csi.client.gwt.theme.editor.ThemeEditorManager;
import csi.client.gwt.theme.editor.ThemeEditorPresenter;
import csi.server.common.util.ConnectorSupport;
import csi.client.gwt.util.Display;
import csi.client.gwt.viz.shared.menu.CsiDropdown;
import csi.client.gwt.vortex.Callback;
import csi.client.gwt.vortex.VortexFuture;
import csi.client.gwt.widget.boot.Dialog;
import csi.client.gwt.widget.boot.DropdownHelper;
import csi.client.gwt.widget.boot.InfoDialog;
import csi.client.gwt.widget.boot.SuccessDialog;
import csi.client.gwt.widget.boot.WarningDialog;
import csi.server.common.dto.UserDisplay;
import csi.server.common.dto.system.ReleaseInfo;
import csi.server.common.dto.user.UserSecurityInfo;
import csi.server.common.enumerations.AclResourceType;
import csi.server.common.model.dataview.DataView;
import csi.server.common.model.dataview.DataViewDef;
import csi.server.common.service.api.DataViewActionServiceProtocol;
import csi.server.common.service.api.UserAdministrationServiceProtocol;

public class ApplicationToolbar extends AbstractApplicationToolbar {
    public static final ApplicationInjector injector = GWT.create(ApplicationInjector.class);
    private static final int Z_INDEX = 200;
    private static CentrifugeConstants i18n = CentrifugeConstantsLocator.get();
    private static ApplicationToolbar at;
    private static ApplicationToolbar.MyUiBinder uiBinder = GWT.create(ApplicationToolbar.MyUiBinder.class);

    @UiField
    protected CsiDropdown dataviewDropdown, centrifugeDropdown, loggedInUserDropDown, helpDropDown, managementDropdown;

    @UiField
    protected Button newMessageAlert;

    @UiField
    protected NavLink shareDataView , manageLinkupDef, displayParameters;

    protected NavLink newDataviewNavLink, newFromTemplateLink, openDataviewNavLink,
                        importNavLink, exportNavLink, shareResourcesNavLink,
                        editTemplateNavLink, installTableNavLink,
                        manageFiltersNavLink, installFunctionNavLink;

    @UiField
    protected NavLink changePassword, logout, openAbout, helpTopics,editDataSources;

    protected NavLink saveNavLink, exportThisNavLink, exportKMLNavLink,  saveAsNavLink, saveAsTemplateNavLink, renameDataView, refreshDataSources = new NavLink();

    protected NavLink closeDataView, deleteDataView;

    protected NavLink systemAdministration, configureReaper;
    DataSourceEditorView dseView = null;
    @UiField
    InlineLabel emptyCentrifuge;
    @UiField
    Navbar navbar;
    @UiField
    NavHeader header;
    @UiField
    NavLink openFieldList;
    NavLink editIconsNavLink, editThemeNavLink;

    TextButton addWorksheetButton;
    TextButton editNodeButton;
    TextButton perspectiveButton;
    Menu perspectiveMenu;
    HBoxLayoutContainer layoutPanel;
    TextButton dataviewTextButton;
    HideHandler hideHandler;
    String currentPerspective = i18n.applicationToolbar_analysisView();
    ClickHandler newDataviewClickHandler = new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
            try {
                AdHocEditLauncher.open(AclResourceType.DATAVIEW);
            } catch (Exception myException) {
                Dialog.showException("ApplicationToolbar", myException);
            }
        }
    };
    ClickHandler newFromTemplateClickHandler = new ClickHandler() {

        @Override
        public void onClick(ClickEvent event) {

            try {

                (new DataViewFromTemplateDialog(false)).show();

            } catch (Exception myException) {

                Dialog.showException("ApplicationToolbar", myException);
            }
        }
    };
    ClickHandler InstallDataSourceClickHandler = new ClickHandler() {

        @Override
        public void onClick(ClickEvent event) {

            try {

                (new AdHocEditLauncher(AclResourceType.DATA_TABLE, null)).show();

            } catch (Exception myException) {

                Dialog.showException("ApplicationToolbar", myException);
            }
        }
    };
    ClickHandler editTemplateClickHandler = new ClickHandler() {

        @Override
        public void onClick(ClickEvent event) {

            try {

                (new EditTemplateDialog()).show();

            } catch (Exception myException) {

                Dialog.showException("ApplicationToolbar", myException);
            }
        }
    };
    ClickHandler installFunctionClickHandler = new ClickHandler() {

        @Override
        public void onClick(ClickEvent event) {

            try {

                (new InstallFunctionDialog()).show();

            } catch (Exception myException) {

                Dialog.showException("ApplicationToolbar", myException);
            }
        }
    };
    ClickHandler shareResourcesClickHandler = new ClickHandler() {

        @Override
        public void onClick(ClickEvent event) {

            try {

                WebMain.injector.getEventBus().fireEvent(new csi.client.gwt.events.EnterSharingModeEvent());

            } catch (Exception myException) {

                Dialog.showException("ApplicationToolbar", myException);
            }
        }
    };
    ClickHandler editFiltersClickHandler = new ClickHandler() {

        @Override
        public void onClick(ClickEvent event) {

            try {

                (new ResourceFilterListDialog()).show();

            } catch (Exception myException) {

                Dialog.showException("ApplicationToolbar", myException);
            }
        }
    };
    ClickHandler importClickHandler = new ClickHandler() {

        @Override
        public void onClick(ClickEvent event) {

            try {

                (new ImportDialog()).show();

            } catch (Exception myException) {

                Dialog.showException("ApplicationToolbar", myException);
            }
        }
    };
    ClickHandler exportClickHandler = new ClickHandler() {

        @Override
        public void onClick(ClickEvent event) {

            try {

                (new ExportDialog()).show();

            } catch (Exception myException) {

                Dialog.showException("ApplicationToolbar", myException);
            }
        }
    };
    ClickHandler systemAdministrationClickHandler = new ClickHandler() {

        @Override
        public void onClick(ClickEvent event) {

            try {

                WebMain.injector.getEventBus().fireEvent(new csi.client.gwt.events.EnterAdminModeEvent());

            } catch (Exception myException) {

                Dialog.showException("ApplicationToolbar", myException);
            }
        }
    };
    ClickHandler configureReaperClickHandler = new ClickHandler() {

        @Override
        public void onClick(ClickEvent event) {

            try {

                (new ConfigureReaperDialog()).show();

            } catch (Exception myException) {

                Dialog.showException("ApplicationToolbar", myException);
            }
        }
    };
    ClickHandler openDataviewClickHandler = new ClickHandler() {

        @Override
        public void onClick(ClickEvent event) {

            try {

                (new OpenDataviewDialog(dataViewPresenter)).show();

            } catch (Exception myException) {

                Dialog.showException("ApplicationToolbar", myException);
            }
        }
    };
    ClickHandler exportThisClickHandler = new ClickHandler() {

        @Override
        public void onClick(ClickEvent event) {

            try {

                if ((null != dataViewPresenter) && (null != dataViewPresenter.getDataView())) {

                    DataViewDef myMeta = dataViewPresenter.getDataView().getMeta();

                    if (ConnectorSupport.getInstance().canExport(myMeta)) {

                        (new ExportDialog(dataViewPresenter.getUuid(), AclResourceType.DATAVIEW)).show();

                    } else {

                        Display.error(i18n.securityBlock_ExportDenied());
                    }
                }

            } catch (Exception myException) {

                Dialog.showException("ApplicationToolbar", myException);
            }
        }
    };
    private ThemeEditorPresenter themeEditorPresenter = new ThemeEditorPresenter();
    ClickHandler editThemeHandler = new ClickHandler() {

        @Override
        public void onClick(ClickEvent event) {

            try {

                ThemeEditorManager themeManager = new ThemeEditorManager(themeEditorPresenter, dataViewPresenter);
                themeManager.show();

            } catch (Exception myException) {

                Dialog.showException("ApplicationToolbar", myException);
            }
        }
    };
    private ImageResource logo;

    public ApplicationToolbar() {
        final Widget component = uiBinder.createAndBindUi(this);
        initWidget(component);

        // Make overflow visible for bootstrap dropdown menus
        Scheduler.get().scheduleDeferred(() -> {
            try {
                    component.getElement().getParentElement().getStyle().setOverflow(Overflow.VISIBLE);
            } catch (Exception myException) {
                Dialog.showException("ApplicationToolbar", myException);
            }
        });

        manageLinkupDef.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {

                try {

                    (new LinkupDefinitionDialog(dataViewPresenter)).show();

                } catch (Exception myException) {

                    Dialog.showException("ApplicationToolbar", myException);
                }
            }
        });

        shareDataView.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {

                try {

                    if (null != dataViewPresenter)   {

                        dataViewPresenter.launchSharingDialog();
                    }

                } catch (Exception myException) {

                    Dialog.showException("ApplicationToolbar", myException);
                }
            }
        });

        displayParameters.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {

                try {

                    if (null != dataViewPresenter) {

                        dataViewPresenter.launchParameterDisplay();
                    }

                } catch (Exception myException) {

                    Dialog.showException("ApplicationToolbar", myException);
                }
            }
        });

        changePassword.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent eventIn) {

                try {

                    PasswordPopup myDialog = new PasswordPopup(null, null);

                    myDialog.addDataChangeEventHandler(new DataChangeEventHandler() {

                        public void onDataChange(DataChangeEvent eventIn) {

                            String myPassword = (String) eventIn.getData();

                            if ((null != myPassword) && (0 < myPassword.length())) {

                                try {
                                    VortexFuture<List<UserDisplay>> vortexFuture = WebMain.injector.getVortex()
                                            .createFuture();
                                    vortexFuture.execute(UserAdministrationServiceProtocol.class).setPassword(myPassword);

                                } catch (Exception myException) {

                                    Dialog.showException(myException);
                                }
                            }
                        }
                    });

                    myDialog.show();

                } catch (Exception myException) {

                    Dialog.showException("ApplicationToolbar", myException);
                }
            }
        });

        openAbout.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent eventIn) {

                try {

                    InfoDialog myDialog = new InfoDialog(i18n.Help_AboutDialog_Title(),
                                                        i18n.Help_AboutDialog_Info(ReleaseInfo.version));

                    myDialog.show();

                } catch (Exception myException) {

                    Dialog.showException("ApplicationToolbar", myException);
                }
            }
        });

        helpTopics.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent eventIn) {

                try {

                    HelpTopicsDialog myDialog = new HelpTopicsDialog();

                    myDialog.show();

                } catch (Exception myException) {

                    Dialog.showException("ApplicationToolbar", myException);
                }
            }
        });

        logout.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {

                try {

                /*
                WebMain.injector.getVortex().execute(new Callback<Void>() {

                    @Override
                    public void onSuccess(Void result) {
                        Window.Location.reload();
                    }
                }, UserActionsServiceProtocol.class).logout();
                */
                    //Window.Location.assign("/Centrifuge/api/logout/post");

                    Post.logout();

                } catch (Exception myException) {

                    Dialog.showException("ApplicationToolbar", myException);
                }
            }
        });


        editDataSources.addClickHandler(event -> {
            try {
                if ((null != dataViewPresenter) && (null != dataViewPresenter.getDataView())) {
                    DataViewDef myMeta = dataViewPresenter.getDataView().getMeta();
                    if (ConnectorSupport.getInstance().canEnterSourceEditor(myMeta)) {
                        dataViewPresenter.launchDataSourceEditor();
                    } else {
                        Display.error(i18n.securityBlock_SourceEditDenied());
                    }
                }
            } catch (Exception myException) {
                Dialog.showException("ApplicationToolbar", myException);
            }
        });


        navbar.getElement().getStyle().setZIndex(Z_INDEX);
        DropdownHelper.setupZ(dataviewDropdown);
        DropdownHelper.setupZ(centrifugeDropdown);
        DropdownHelper.setupZ(managementDropdown);
        DropdownHelper.setupZ(loggedInUserDropDown);
        DropdownHelper.setupZ(helpDropDown);
    }

    public static ApplicationToolbar getInstance() {
        if (at == null) {
            at = new ApplicationToolbar();
        }
        return at;
    }

    private void createDataviewDropdown(DataView dataViewIn) {
        centrifugeDropdown.setVisible(true);
        centrifugeDropdown.setText(i18n.applicationToolbar_dataviewDropdown());
        UserSecurityInfo myInfo = WebMain.injector.getMainPresenter().getUserInfo();
        centrifugeDropdown.clear();

        saveNavLink = new NavLink();
        saveNavLink.setText(i18n.applicationToolbar_saveDataview());
        saveNavLink.addClickHandler(event -> {
            try {
                if (null != dataViewPresenter) {
                    dataViewPresenter.save(new ResourceSaveCallback() {
                        public void onSave() {
                            (new SuccessDialog(i18n.applicationToolbar_SaveSuccess_DialogTitle(), i18n.applicationToolbar_SaveSuccess_InfoString(dataViewPresenter.getDisplayName()))).show();
                        }
                    });
                }
            } catch (Exception myException) {
                Dialog.showException("ApplicationToolbar", myException);
            }
        });

        saveAsNavLink = new NavLink();
        saveAsNavLink.setText(i18n.applicationToolbar_saveDataviewAs());
        saveAsNavLink.addClickHandler(event -> {
            try {
                (new ResourceSaveAsDialog<DataView>(dataViewPresenter)).show();
            } catch (Exception myException) {
                Dialog.showException("ApplicationToolbar", myException);
            }
        });

        saveAsTemplateNavLink = new NavLink();
        saveAsTemplateNavLink.setText(i18n.applicationToolbar_saveAsTemplate());
        saveAsTemplateNavLink.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {

                try {

                    (new DataviewSaveAsTemplateDialog(dataViewPresenter)).show();

                } catch (Exception myException) {

                    Dialog.showException("ApplicationToolbar", myException);
                }
            }
        });

        renameDataView = new NavLink();
        renameDataView.setText(i18n.applicationToolbar_renameDataview());
        renameDataView.addClickHandler(event -> {
            try {
                if (null != dataViewPresenter) {
                    dataViewPresenter.launchRenamingDialog();
                }
            } catch (Exception myException) {
                Dialog.showException("ApplicationToolbar", myException);
            }
        });

        refreshDataSources = new NavLink();
        refreshDataSources.setText(i18n.applicationToolbar_refreshDataSources());
        refreshDataSources.addClickHandler(event -> {
            try {
                if (null != dataViewPresenter) {
                    dataViewPresenter.launchRefreshData();
                }
            } catch (Exception myException) {
                Dialog.showException("ApplicationToolbar", myException);
            }
        });


        closeDataView = new NavLink();
        closeDataView.setText(i18n.applicationToolbar_closeDataview());
        closeDataView.addClickHandler(event -> {
            try {
                messagesDialog.hide();
                WarningDialog dialog = new WarningDialog(i18n.applicationToolbar_ClosingDataview_DialogTitle(), i18n.applicationToolbar_ClosingDataview_InfoString());
                dialog.addClickHandler(event1 -> {
                    try {
                        //close without saving
                        WebMain.injector.getEventBus().fireEvent(new CloseDataViewEvent(dataViewPresenter.getUuid()));
                        clearDataView();
                    } catch (Exception myException) {
                        Dialog.showException("ApplicationToolbar", myException);
                    }
                });
                dialog.show();
            } catch (Exception myException) {
                Dialog.showException("ApplicationToolbar", myException);
            }
        });

        deleteDataView = new NavLink();
        deleteDataView.setText(i18n.applicationToolbar_deleteDataview());
        deleteDataView.addClickHandler(event -> {
            try {
                WarningDialog dialog = new WarningDialog(i18n.applicationToolbar_DeleteDataview_DialogTitle(), i18n.applicationToolbar_DeleteDataview_InfoString());
                dialog.addClickHandler(event12 -> {
                    try {
                        WebMain.injector.getVortex().execute((Callback<String>) result -> {
                            WebMain.injector.getEventBus().fireEvent(new CloseDataViewEvent(dataViewPresenter.getUuid()));
                            clearDataView();
                        }, DataViewActionServiceProtocol.class).deleteDataView(dataViewPresenter.getUuid());
                    } catch (Exception myException) {
                        Dialog.showException("ApplicationToolbar", myException);
                    }
                });
                dialog.show();
            } catch (Exception myException) {
                Dialog.showException("ApplicationToolbar", myException);
            }
        });

        exportKMLNavLink = new NavLink();
        exportKMLNavLink.setText(i18n.applicationToolbar_createKML());
        exportKMLNavLink.addClickHandler(event -> {
            try {
                KmlExportImpl kmlExport = new KmlExportImpl(dataViewPresenter);
                kmlExport.show();
            } catch (Exception myException) {
                Dialog.showException("ApplicationToolbar", myException);
            }
        });

        exportThisNavLink = new NavLink();
        exportThisNavLink.setText(i18n.applicationToolbar_exportDataview());
        exportThisNavLink.addClickHandler(exportThisClickHandler);


        // open, new, new from template
        if (!myInfo.isRestricted()) {
            openDataviewNavLink = new NavLink();
            openDataviewNavLink.setText(i18n.applicationToolbar_openDataview());
            openDataviewNavLink.addClickHandler(openDataviewClickHandler);
            centrifugeDropdown.add(openDataviewNavLink);

            newDataviewNavLink = new NavLink();
            newDataviewNavLink.setText(i18n.applicationToolbar_newDataview());
            newDataviewNavLink.addClickHandler(newDataviewClickHandler);
            centrifugeDropdown.add(newDataviewNavLink);

            newFromTemplateLink = new NavLink();
            newFromTemplateLink.setText(i18n.applicationToolbar_newDataviewFromTemplate());
            newFromTemplateLink.addClickHandler(newFromTemplateClickHandler);
            centrifugeDropdown.add(newFromTemplateLink);
        }

        // saves
        centrifugeDropdown.add(new Divider());

        centrifugeDropdown.add(saveNavLink);
        centrifugeDropdown.add(saveAsNavLink);
        centrifugeDropdown.add(saveAsTemplateNavLink);
        centrifugeDropdown.add(renameDataView);

        centrifugeDropdown.add(new Divider());

        centrifugeDropdown.add(exportThisNavLink);
        centrifugeDropdown.add(exportKMLNavLink);

        centrifugeDropdown.add(new Divider());
        centrifugeDropdown.add(deleteDataView);
        centrifugeDropdown.add(new Divider());

        centrifugeDropdown.add(refreshDataSources);
        centrifugeDropdown.add(closeDataView);

        DataViewDef myMeta = dataViewIn.getMeta();
        boolean myReadOnlyFlag = (null != dataViewPresenter) ? dataViewPresenter.isReadOnly() : false;
        boolean myDeleteFlag = (null != dataViewPresenter) ? dataViewPresenter.isRemovable() : false;
        boolean myOwnershipFlag = (null != dataViewPresenter) ? dataViewPresenter.getOwnership() : false;
        boolean myIsAdmin = (null != myInfo) ? myInfo.isAdmin() : false;
        exportThisNavLink.setDisabled(!ConnectorSupport.getInstance().canExport(myMeta));
        renameDataView.setDisabled(!myOwnershipFlag);
        shareDataView.setDisabled(!(myOwnershipFlag || myIsAdmin));
        saveNavLink.setDisabled(myReadOnlyFlag);
        openFieldList.setDisabled(myReadOnlyFlag);
        editDataSources.setDisabled(myReadOnlyFlag || (!ConnectorSupport.getInstance().canEnterSourceEditor(myMeta)));
        refreshDataSources.setDisabled(myReadOnlyFlag);
        manageLinkupDef.setDisabled(myReadOnlyFlag);
        deleteDataView.setDisabled(!myDeleteFlag);

    }

    private void createManagementDropdown(UserSecurityInfo userInfoIn) {
        managementDropdown.setIcon(IconType.GEAR);

        shareResourcesNavLink = new NavLink();
        shareResourcesNavLink.setText(i18n.applicationToolbar_manageResources());
        shareResourcesNavLink.addClickHandler(shareResourcesClickHandler);

        systemAdministration = new NavLink();
        systemAdministration.setText(i18n.applicationToolbar_systemAdministration());
        systemAdministration.addClickHandler(systemAdministrationClickHandler);

        exportNavLink = new NavLink();
        exportNavLink.setText(i18n.applicationToolbar_exportResources());
        exportNavLink.addClickHandler(new HandleImportExport().exportClickHandler);

        importNavLink = new NavLink();
        importNavLink.setText(i18n.applicationToolbar_importResources());
        importNavLink.addClickHandler(new HandleImportExport().importClickHandler);

        manageFiltersNavLink = new NavLink();
        manageFiltersNavLink.setText(i18n.applicationToolbar_manageResourceFilters());
        manageFiltersNavLink.addClickHandler(editFiltersClickHandler);

        if (userInfoIn.getIconAdmin()) {

            editIconsNavLink = new NavLink();
            editIconsNavLink.setText(i18n.applicationToolbar_manageIcons());
            editIconsNavLink.addClickHandler(event -> {
                try {
                    IconManager iconManager = new IconManager();
                    iconManager.show();
                } catch (Exception myException) {
                    Dialog.showException("ApplicationToolbar", myException);
                }
            });
        }

        editThemeNavLink = new NavLink();
        editThemeNavLink.setText(i18n.applicationToolbar_editThemes());
        editThemeNavLink.addClickHandler(editThemeHandler);

        installTableNavLink = new NavLink();
        installTableNavLink.setText(i18n.applicationToolbar_newInstalledTable());
        installTableNavLink.addClickHandler(InstallDataSourceClickHandler);

        editTemplateNavLink = new NavLink();
        editTemplateNavLink.setText(i18n.applicationToolbar_editTemplate());
        editTemplateNavLink.addClickHandler(editTemplateClickHandler);

        installFunctionNavLink = new NavLink();
        installFunctionNavLink.setText(i18n.applicationToolbar_installFunction());
        installFunctionNavLink.addClickHandler(installFunctionClickHandler);

        managementDropdown.add(shareResourcesNavLink);
        managementDropdown.add(manageFiltersNavLink);
        if(!userInfoIn.isRestricted()) {
            if (userInfoIn.getIconAdmin()) {
                managementDropdown.add(editIconsNavLink);
            }
            managementDropdown.add(editThemeNavLink);
            managementDropdown.add(new Divider());
            managementDropdown.add(importNavLink);
            managementDropdown.add(exportNavLink);
            managementDropdown.add(new Divider());
            managementDropdown.add(editTemplateNavLink);
            managementDropdown.add(installTableNavLink);
        }
        if (userInfoIn.isAdmin() || userInfoIn.isSecurity()) {
            managementDropdown.add(new Divider());
            if (userInfoIn.isAdmin()) {
                managementDropdown.add(installFunctionNavLink);
            }
            managementDropdown.add(systemAdministration);
        }
    }

    public void finalizeMenus(UserSecurityInfo userInfoIn) {
        createManagementDropdown(userInfoIn);
        createMainCentrifugeDropdown(userInfoIn);
        tMessengerButton.setIcon(IconType.COMMENT_ALT);
        tMessengerButton.getElement().setAttribute("font-size", "20px");
        tMessengerButton.getElement().getStyle().setProperty("background", "none");
        tMessengerButton.getElement().getStyle().setProperty("border", "none");
        tMessengerButton.getElement().getStyle().setProperty("color", "white");
        tMessengerButton.getElement().getStyle().setProperty("textShadow", "none");
        tMessengerButton.getElement().getStyle().setProperty("verticalAlign", "baseline");
        SpanElement caret = Document.get().createSpanElement();
        tMessengerButton.getElement().appendChild(caret);
        caret.setClassName("caret");
        caret.getStyle().setProperty("borderTopColor", "white");
        caret.getStyle().setProperty("borderBottomColor", "white");
        caret.getStyle().setMarginLeft(2.0, Style.Unit.PX);
        tMessengerButton.addClickHandler(event -> {
            messagesDialog.show();
        });
        createNewMessageAlert();


        loggedInUserDropDown.setTitle(userInfoIn.getName());
        loggedInUserDropDown.setIcon(IconType.USER);
        loggedInUserDropDown.getElement().setAttribute("font-size", "20px");
        helpDropDown.setIcon(IconType.QUESTION_SIGN);
        helpDropDown.getElement().setAttribute("font-size", "20px");
    }

    @Override
    protected void createMainCentrifugeDropdown(UserSecurityInfo userInfoIn) {
        centrifugeDropdown.clear();
        centrifugeDropdown.setVisible(false);
    }

    @Override
    public void enableDataMenus(DataView dataViewIn) {
        // the idea i guess is to clear the main dropdown of the other options and reinitialize it with no dv
        if (null != dataViewIn) {
            createDataviewDropdown(dataViewIn);
        } else {
            createMainCentrifugeDropdown(WebMain.injector.getMainPresenter().getUserInfo());
            dataviewDropdown.setVisible(false);
        }
    }

    public void toggleMessagesMenu(MainView.Mode mode) {
        if (!(mode.equals(MainView.Mode.ANALYSIS))) {
            tMessengerButton.setVisible(false);
            newMessageAlert.setVisible(false);
        } else {
            tMessengerButton.setVisible(true);
        }
    }

    public void setHeaderText(String textIn) {

        header.setText(textIn);
    }

    @Override
    public void enableMenus() {

        dseView = null;
        centrifugeDropdown.setVisible(true);
        enableDataMenus((null != dataViewPresenter) ? dataViewPresenter.getDataView() : null);
        enableMessagesMenus((null != dataViewPresenter) ? dataViewPresenter.getDataView() : null);
        emptyCentrifuge.setText("");
        emptyCentrifuge.setVisible(false);
    }

    @Override
    public void disableMenus() {
        dseView = null;
        centrifugeDropdown.setVisible(false);
        dataviewDropdown.setVisible(false);
        emptyCentrifuge.setText("");
        emptyCentrifuge.setVisible(false);
    }

    @Override
    public void setDataView(AbstractDataViewPresenter presenter) {
        this.dataViewPresenter = presenter;
        if (null != presenter) {
            dataviewDropdown.setVisible(true);
            dataviewDropdown.setText(i18n.applicationToolbar_configurationDropdown());
            header.setVisible(true);
            header.addStyleName("dvName");
            if (presenter.getOwnership()) {

                header.setText(DataViewInNewTab.genTitle(presenter.getUuid(), presenter.getDisplayName()));

            } else {

                header.setText(DataViewInNewTab.genTitle(presenter.getUuid(), presenter.getDisplayName(), presenter.getOwner()));
            }
            header.getElement().setTitle(presenter.getName());

        } else {
            dataviewDropdown.setVisible(false);
            createMainCentrifugeDropdown(WebMain.injector.getMainPresenter().getUserInfo());
            header.setVisible(false);
        }
    }

    @Override
    public void abortDataView() {

        String myUuid = (null != dataViewPresenter) ? dataViewPresenter.getUuid() : null;

        //close without saving
        WebMain.injector.getEventBus().fireEvent(new CloseDataViewEvent(myUuid, true));
        clearDataView();
    }

    void clearDataView() {
        this.dataViewPresenter = null;

        createMainCentrifugeDropdown(WebMain.injector.getMainPresenter().getUserInfo());
        dataviewDropdown.setVisible(false);
        dataviewDropdown.setText("");
        messagesDialog.hide();

        header.setVisible(false);
        header.setText("");
    }

    private void createNewMessageAlert() {

        newMessageAlert.setIcon(IconType.EXCLAMATION);
        newMessageAlert.getElement().setAttribute("font-size", "10px");
        newMessageAlert.getElement().getStyle().setProperty("background", "none");
        newMessageAlert.getElement().getStyle().setProperty("border", "none");
        newMessageAlert.getElement().getStyle().setProperty("color", "red");
        newMessageAlert.getElement().getStyle().setProperty("right", "7px");
        newMessageAlert.getElement().getStyle().setProperty("top", "3px");
        newMessageAlert.getElement().getStyle().setProperty("position", "absolute");
        newMessageAlert.getElement().getStyle().setProperty("textShadow", "none");
        newMessageAlert.getElement().getStyle().setProperty("verticalAlign", "baseline");
        newMessageAlert.addClickHandler(event -> {
            messagesDialog.show();
        });
        newMessageAlert.setVisible(false);
    }

    @Override
    public void setNewMessageAlertVisible(boolean exists) {
        if (exists) {
            if (tMessengerButton.isVisible()) {
                newMessageAlert.setVisible(true);
            }
            else {
                newMessageAlert.setVisible(false);
            }
        }
        newMessageAlert.setVisible(exists);
    }

    @UiHandler("openFieldList")
    protected void handleFieldsClick(ClickEvent event){
        try {
            FieldList fieldList = new FieldList(dataViewPresenter, null);
            fieldList.show();
        } catch (Exception myException) {
            Dialog.showException("ApplicationToolbar", myException);
        }
    }

    @Override
    public Widget asWidget() {

        try {

            Widget component = uiBinder.createAndBindUi(this);

            return component;

        } catch (Exception myException) {

            Dialog.showException("ApplicationToolbar", myException);
        }
        return null;
    }

    interface MyUiBinder extends UiBinder<Widget, ApplicationToolbar> {
    }

}
