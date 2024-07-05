package csi.client.gwt.theme.editor;

import java.util.List;

import csi.client.gwt.WebMain;
import csi.client.gwt.csi_resource.ExportDialog;
import csi.client.gwt.dataview.AbstractDataViewPresenter;
import csi.client.gwt.etc.AbstractInfrastructureAwarePresenter;
import csi.client.gwt.mainapp.MainPresenter;
import csi.client.gwt.theme.editor.graph.GraphThemeEditorPresenter;
import csi.client.gwt.theme.editor.map.MapThemeEditorPresenter;
import csi.client.gwt.util.Display;
import csi.client.gwt.util.DownloadHelper;
import csi.client.gwt.vortex.AbstractVortexEventHandler;
import csi.client.gwt.vortex.Callback;
import csi.client.gwt.vortex.VortexFuture;
import csi.client.gwt.widget.boot.Dialog;
import csi.server.common.dto.SelectionListData.ResourceBasics;
import csi.server.common.dto.user.UserSecurityInfo;
import csi.server.common.enumerations.AclResourceType;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.model.themes.Theme;
import csi.server.common.model.themes.graph.GraphTheme;
import csi.server.common.model.themes.map.MapTheme;
import csi.server.common.model.visualization.VisualizationType;
import csi.server.common.service.api.ThemeActionsServiceProtocol;
import csi.server.common.util.Format;

public class ThemeEditorPresenter extends AbstractInfrastructureAwarePresenter{

    private MainPresenter _mainPresenter = null;
    private ThemeEditorPanel view;
    private GraphThemeEditorPresenter graphThemeEditorPresenter = new GraphThemeEditorPresenter(this);
    private MapThemeEditorPresenter mapThemeEditorPresenter = new MapThemeEditorPresenter(this);
    private List<String> fieldNames = null;
    private String fileName = null;

    public void openEditorForTheme(Theme theme){

        try {

            switch(theme.getVisualizationType()){
                case RELGRAPH_V2:
                    editGraphTheme(theme);
                    break;
                case MAP_CHART:
                    editMapTheme(theme);
                    break;
                default:
                    throw new CentrifugeException("Unable to edit theme of type "
                                                    + Format.value(theme.getVisualizationType()));
            }

        } catch (Exception myException) {

            Display.error("ThemeEditorPresenter", 1, myException);
        }
    }

    private void editGraphTheme(Theme theme) {
        graphThemeEditorPresenter.edit(theme);
    }

    private void editMapTheme(Theme theme) {
        mapThemeEditorPresenter.edit(theme);
    }

    public ThemeEditorPanel getView(){
        if(view == null){
            view = new ThemeEditorPanel();
            view.setPresenter(this);
            populateThemeNames();
        }
        return view;
    }

    public void deleteTheme(String uuid){
        VortexFuture<Void> future = WebMain.injector.getVortex().createFuture();
        future.addEventHandler(new AbstractVortexEventHandler<Void>() {
            @Override
            public void onSuccess(Void result) {

                Display.cancelWatchBox();
                populateThemeNames();

            }

            @Override
            public boolean onError(Throwable t) {

                Dialog.showException(t);
                return false;
            }
        });
        try {

            Display.showWatchBox("Deleting theme on server.");
            future.execute(ThemeActionsServiceProtocol.class).deleteTheme(uuid);

        } catch (CentrifugeException myException) {

            Display.error("ThemeEditorPresenter", 1, myException);
        };
    }

    public void editTheme(String uuid, Theme theme){

        if(uuid == null){

            final VisualizationType myVizType = theme.getVisualizationType();

            if (myVizType == VisualizationType.MAP_CHART) {

                MapTheme mapTheme = new MapTheme();
                mapTheme.setName("Map Theme");
                mapTheme.setOwner(getMainPresenter().getUserName());
                editMapTheme(mapTheme);

            } else if ((myVizType == VisualizationType.RELGRAPH) || (myVizType == VisualizationType.RELGRAPH_V2)) {

                GraphTheme graphTheme = new GraphTheme();
                graphTheme.setName("Graph Theme");
                graphTheme.setOwner(getMainPresenter().getUserName());
                editGraphTheme(graphTheme);

            } else {

                Display.error("Edit Theme", Format.value(Format.value(myVizType)) + " themes are not currently supported.");
            }

            //List<NodeStyle> nodeStyles = new ArrayList<NodeStyle>();
            //graphTheme.setNodeStyles(nodeStyles);
            //            List<LinkStyle> linkStyles = new ArrayList<LinkStyle>();
            //            graphTheme.setLinkStyles(linkStyles);
            //            NodeStyle nodeStyle = new NodeStyle();
            //            nodeStyle.setColor(256);
            //            nodeStyle.setShape(ShapeType.DIAMOND);
            //            nodeStyle.setName("Node Style 1");
            //            nodeStyle.getFieldNames().add("Field A");
            //            nodeStyle.getFieldNames().add("Field B");
            //            nodeStyle.getFieldNames().add("Field C");
        } else {

            VortexFuture<Theme> future = WebMain.injector.getVortex().createFuture();
            future.addEventHandler(new AbstractVortexEventHandler<Theme>() {
                @Override
                public void onSuccess(Theme theme) {

                    Display.cancelWatchBox();
                    openEditorForTheme(theme);
                }

                @Override
                public boolean onError(Throwable myException) {

                    Display.cancelWatchBox();
                    Display.error("ThemeEditorPresenter", 2, myException);
                    return false;
                }
            });
            try {

                Display.showWatchBox("Retrieving theme information.");
                future.execute(ThemeActionsServiceProtocol.class).findTheme(uuid);

            } catch (Exception myException) {

                Display.cancelWatchBox();
                Display.error("ThemeEditorPresenter", 3, myException);
            };
        }

    }

    public void populateThemeNames() {
        VortexFuture<List<ResourceBasics>> future = WebMain.injector.getVortex().createFuture();
        future.addEventHandler(new AbstractVortexEventHandler<List<ResourceBasics>>() {
            @Override
            public void onSuccess(List<ResourceBasics> result) {

                Display.cancelWatchBox();
                getView().updateGrid(result);
            }

            @Override
            public boolean onError(Throwable myException) {

                Display.cancelWatchBox();
                Display.error("ThemeEditorPresenter", 4, myException);
                return false;
            }
        });
        try {

            Display.showWatchBox("Retrieving theme names.");
            future.execute(ThemeActionsServiceProtocol.class).listThemes();

        } catch (Exception myException) {

            Display.cancelWatchBox();
            Display.error("ThemeEditorPresenter", 5, myException);
        };
    }

    public List<String> getFieldNames() {
        return fieldNames;
    }

    public void setFieldNames(List<String> fieldNames) {
        this.fieldNames = fieldNames;
    }
    public void exportTheme(final ResourceBasics theme) {
        if (null != theme.getUuid()) {

            try {
/*
                YesNoDialog dialog = new YesNoDialog(i18n.themeExportTitle(), //$NON-NLS-1$
                        i18n.themeExportWarning(theme.getName()), event -> {
                            Display.showWatchBox("Preparing theme for export.");
                            fileName = (theme.getName());
                            WebMain.injector.getVortex().execute(downloadCallback, ExportActionsServiceProtocol.class).exportTheme(theme.getUuid());
                        });
                dialog.show();
*/
                (new ExportDialog(theme.getDisplayString(), AclResourceType.THEME)).show();

            } catch (Exception myException) {

                Display.cancelWatchBox();
                Display.error("ThemeEditorPresenter", 6, myException);
            }
        }
    }

    private Callback<String> downloadCallback = new Callback<String>() {
        @Override
        public void onSuccess(String fileToken) {

            Display.cancelWatchBox();

            if (null != fileToken) {

                DownloadHelper.download(fileName, ".TM.zip", fileToken);

            } else {

                Display.error("Unable to format export file contents.");
            }
        }
    };

    private MainPresenter getMainPresenter() {

        if (null == _mainPresenter) {

            _mainPresenter = WebMain.injector.getMainPresenter();
        }
        return _mainPresenter;
    }
}
