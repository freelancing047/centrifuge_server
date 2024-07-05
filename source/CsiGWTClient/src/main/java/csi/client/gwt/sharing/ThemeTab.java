package csi.client.gwt.sharing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

import csi.client.gwt.WebMain;
import csi.client.gwt.csiwizard.AdHocEditLauncher;
import csi.client.gwt.events.TemplateNameChangeEvent;
import csi.client.gwt.events.TemplateNameChangeEventHandler;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.theme.editor.ThemeEditorPresenter;
import csi.client.gwt.util.Display;
import csi.client.gwt.util.DownloadHelper;
import csi.client.gwt.vortex.AbstractVortexEventHandler;
import csi.client.gwt.vortex.VortexFuture;
import csi.client.gwt.widget.boot.DecisionDialog;
import csi.client.gwt.widget.boot.Dialog;
import csi.server.common.dto.SharingDisplay;
import csi.server.common.dto.user.UserSecurityInfo;
import csi.server.common.enumerations.AclResourceType;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.model.themes.Theme;
import csi.server.common.model.themes.graph.GraphTheme;
import csi.server.common.model.themes.map.MapTheme;
import csi.server.common.service.api.ExportActionsServiceProtocol;
import csi.server.common.service.api.ThemeActionsServiceProtocol;

/**
 * Created by centrifuge on 7/8/2015.
 */
public class ThemeTab extends ResourceTab {

    private static final CentrifugeConstants _constants = CentrifugeConstantsLocator.get();

    private ThemeEditorPresenter themeEditorPresenter = new ThemeEditorPresenter();
    private DecisionDialog dialog;

    public ThemeTab(ResourceSharingView parentIn) {

        super(parentIn, "sharing.ThemeTab");
        wireInHandlers();

        renameButton.setVisible(false);
    }

    protected String getResourceTypeString() {

        return "Theme";
    }

    protected String getResourceTypePluralString() {

        return "Themes";
    }

    protected AclResourceType getResourceType() {

        return AclResourceType.THEME;
    }

    protected ClickHandler getExportClickHandler() {

        return handleExportRequest;
    }

    
    protected ClickHandler getRenameClickHandler() {

        return null;
    }

    protected ClickHandler getDeleteClickHandler() {

        return buildDeleteDialog(handleDeleteConfirmed);
    }

    protected ClickHandler handleDeleteConfirmed = new ClickHandler() {
        public void onClick(ClickEvent eventIn) {

            final List<String> myItemList = new ArrayList<String>();
            List<SharingDisplay> mySelectedGridList = _grid.getSelectionModel().getSelection();

            _selectionMap = new HashMap<String, SharingDisplay>();

            for (int i = 0; mySelectedGridList.size() > i; i++) {

                SharingDisplay myItem = mySelectedGridList.get(i);

                _selectionMap.put(myItem.getUuid(), myItem);
                myItemList.add(myItem.getUuid());
            }

            VortexFuture<Void> future = WebMain.injector.getVortex().createFuture();
            try {

                watchBox.show(_constants.resourceTab_deleteResources());
                future.execute(ThemeActionsServiceProtocol.class).deleteThemes(myItemList);

                future.addEventHandler(new AbstractVortexEventHandler<Void>(){
                    @Override
                    public boolean onError(Throwable myException) {

                        watchBox.hide();
                        Display.error("ThemeTab", 1, myException);
                        return true;
                    }

                    @Override
                    public void onSuccess(Void result) {
                        for (String myItem : myItemList) {

                            SharingDisplay myInfo = _selectionMap.get(myItem);

                            if (null != myInfo) {

                                mySelectedGridList.remove(myInfo);
                                _grid.getStore().remove(myInfo);
                            }
                        }

                        watchBox.hide();

                    }});
            } catch (CentrifugeException e) {

                watchBox.hide();
                Display.error("ThemeTab", 2, e);
            }
        }
    };

    protected ClickHandler getEditClickHandler() {

        return new ClickHandler(){

            @Override
            public void onClick(ClickEvent event) {

                List<SharingDisplay> mySelectedGridList = _grid.getSelectionModel().getSelection();
                SharingDisplay shareInfo = mySelectedGridList.get(0);
                
                VortexFuture<Theme> future = WebMain.injector.getVortex().createFuture();
                future.addEventHandler(new AbstractVortexEventHandler<Theme>() {
                    @Override
                    public void onSuccess(Theme theme) {

                        ThemeEditorPresenter presenter = new ThemeEditorPresenter();
                        presenter.openEditorForTheme(theme);
                        
                        
                    }
        
                    @Override
                    public boolean onError(Throwable t) {
                        
                        return false;
                    }
                });
                try {
                    future.execute(ThemeActionsServiceProtocol.class).findTheme(shareInfo.getUuid());
                } catch (CentrifugeException e) {
                    
                };
            }};
    }

    private ClickHandler handleCreateRequest = new ClickHandler() {

        public void onClick(ClickEvent eventIn) {

            try {

                ThemeEditorPresenter presenter = new ThemeEditorPresenter();
                presenter.editTheme(null, new GraphTheme());

            } catch (Exception myException) {

                Display.error("ThemeTab", 3, myException);
            }
        }
    };

    private ClickHandler handleAlternateRequest = new ClickHandler() {

        public void onClick(ClickEvent eventIn) {

            try {

                ThemeEditorPresenter presenter = new ThemeEditorPresenter();
                presenter.editTheme(null, new MapTheme());

            } catch (Exception myException) {

                Display.error("ThemeTab", 4, myException);
            }
        }
    };

    protected ClickHandler getCreateClickHandler() {

        return handleCreateRequest;
    }

    @Override
    protected ClickHandler getAlternateClickHandler() {

        return handleAlternateRequest;
    }

    protected ClickHandler getClassificationHandler() {

        return null;
    }

    protected ClickHandler getLaunchClickHandler() {

        return null;
    }

    protected String getEditButtonLabel() {

        return Dialog.txtEditButton;
    }

    protected String getCreateButtonLabel() {

        return "New Graph Theme";
    }

    @Override
    protected String getAlternateButtonLabel() {

        return "New Map Theme";
    }

    @Override
    protected void wireInHandlers() {

        super.wireInHandlers();

        WebMain.injector.getEventBus().addHandler(TemplateNameChangeEvent.type, new TemplateNameChangeEventHandler() {

            @Override
            public void onTemplateNameChange(TemplateNameChangeEvent eventIn) {

                if (null != eventIn) {

                    renameLocalResourceEntry(eventIn.getUuid(), eventIn.getName(), eventIn.getRemarks());
                }
            }
        });
    }

    @Override
    public IconType getIconType() {

        return IconType.SMILE;
    }
}
