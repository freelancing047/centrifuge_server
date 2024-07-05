package csi.client.gwt.viz.graph.settings;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.gwt.activity.shared.Activity;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Panel;

import csi.client.gwt.WebMain;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.util.name.UniqueNameUtil;
import csi.client.gwt.validation.feedback.StringValidationFeedback;
import csi.client.gwt.validation.multi.ValidationAndFeedbackPair;
import csi.client.gwt.validation.validator.NotBlankValidator;
import csi.client.gwt.validation.validator.VisualizationUniqueNameValidator;
import csi.client.gwt.viz.graph.link.LinkProxy;
import csi.client.gwt.viz.graph.node.NodeProxy;
import csi.client.gwt.viz.graph.node.NodeProxyFactory;
import csi.client.gwt.viz.graph.settings.fielddef.FieldProxy;
import csi.client.gwt.viz.shared.settings.AbstractSettingsPresenter;
import csi.client.gwt.viz.shared.settings.SettingsActionCallback;
import csi.client.gwt.viz.shared.settings.VisualizationSettingsModal;
import csi.client.gwt.vortex.AbstractVortexEventHandler;
import csi.client.gwt.vortex.Callback;
import csi.client.gwt.vortex.Vortex;
import csi.client.gwt.vortex.VortexEventHandler;
import csi.client.gwt.vortex.VortexFuture;
import csi.client.gwt.widget.boot.Dialog;
import csi.server.common.dto.SelectionListData.ResourceBasics;
import csi.server.common.model.FieldDef;
import csi.server.common.model.FieldType;
import csi.server.common.model.attribute.AttributeDef;
import csi.server.common.model.themes.graph.GraphTheme;
import csi.server.common.model.themes.graph.LinkStyle;
import csi.server.common.model.themes.graph.NodeStyle;
import csi.server.common.model.visualization.graph.BundleDef;
import csi.server.common.model.visualization.graph.BundleOp;
import csi.server.common.model.visualization.graph.LinkDef;
import csi.server.common.model.visualization.graph.NodeDef;
import csi.server.common.model.visualization.graph.RelGraphViewDef;
import csi.server.common.service.api.ThemeActionsServiceProtocol;
import csi.server.common.service.api.VisualizationActionsServiceProtocol;
import csi.shared.core.visualization.graph.GraphLayout;

public class GraphSettings extends AbstractSettingsPresenter<RelGraphViewDef> {

    private static String defaultTheme = WebMain.getClientStartupInfo().getGraphAdvConfig().getDefaultTheme();

    interface Presenter extends Activity {

    }

    interface View extends IsWidget {

        void addNode(NodeProxy nodeProxy);

        VisualizationSettingsModal getModal();

        void redraw();

        void showDetails();

        void setVisualizationTitle(String visualizationTitle);

        void setTheme(GraphTheme graphTheme);

        void setRenderThreshold(int renderThreshold);

        void setLoadOnStartup(boolean value);

        void setLoadAfterSave(boolean value);

        void setCurrentBackgroundColor(String color);

        void hide();

        void addTheme(ResourceBasics string);

        void addLink(LinkProxy linkProxy);

        void removeAllNodes();

        Panel getDetailsContainer(NodeProxy nodeProxy);

        void clearThemes();

        void clear();

        void show();

    }

    private View view;
    private boolean isLoaded;
    private GraphSettingsActivityManager activityManager;
    private GraphSettingsModel model;

    public GraphSettings(SettingsActionCallback<RelGraphViewDef> settingsActionCallback) {
        super(settingsActionCallback);
        GraphSettingsActivityMapper activityMapper = new GraphSettingsActivityMapper(this);
        EventBus eventBus = new SimpleEventBus();
        activityManager = new GraphSettingsActivityManager(activityMapper, eventBus);
        model = new GraphSettingsModel(this);
    }

    public void addLink(LinkProxy linkProxy) {
        checkNotNull(linkProxy);
        List<LinkDef> linkDefs = model.getLinkDefs();
        linkDefs.add(linkProxy.getLinkDef());
        model.setLinkDefs(linkDefs);
    }

    public void addLink(NodeProxy node1, NodeProxy node2) {
        for (LinkDef linkDef : model.getLinkDefs()) {
            String node1ID = node1.getNodeDef().getUuid();
            String defID1 = linkDef.getNodeDef1().getUuid();
            String node2ID = node2.getNodeDef().getUuid();
            String defID2 = linkDef.getNodeDef2().getUuid();
            if (node1ID.equals(defID1)) {
                if (node2ID.equals(defID2)) {
                    return;
                }
            }
            if (node2ID.equals(defID1)) {
                if (node1ID.equals(defID2)) {
                    return;
                }
            }
        }
        activityManager.setActivity(new CreateLink(this, node1, node2));
    }

    public void addNode(FieldProxy fieldProxy) {
        activityManager.setActivity(new CreateNode(this, fieldProxy));
    }
    
    public void addNode(FieldDef fieldDef) {
        activityManager.setActivity(new CreateNode(this, fieldDef));

    }

    public void addNode(NodeProxy nodeProxy) {
        checkNotNull(nodeProxy);
        List<NodeDef> nodeDefs = model.getNodeDefs();
        nodeDefs.add(nodeProxy.getNodeDef());
        model.setNodeDefs(nodeDefs);

    }

    @Override
    protected void bindUI() {
        model.setRelGraphViewDef(visualizationDef);
        view = new GraphSettingsPanel(this);
        vizSettings = view.getModal();
    }

    @Override
    protected RelGraphViewDef createNewVisualizationDef() {
        RelGraphViewDef def = new RelGraphViewDef();
        def.setBroadcastListener(WebMain.getClientStartupInfo().isListeningByDefault());
        String layout = WebMain.getClientStartupInfo().getGraphInitialLayout();
        if(layout != null && def.getLayout() == null){
            try{
                def.setLayout(GraphLayout.valueOf(layout));
            } catch(Exception exception){
                //layout configuration is bad
            }
        }
        
        String themeName = defaultTheme;
        if(themeName != null && def.getThemeUuid() == null){
            try{
                def.setThemeUuid(themeName);
            } catch(Exception exception){
                //layout configuration is bad
            }
        }
        
        String name = UniqueNameUtil.getDistinctName(UniqueNameUtil.getVisualizationNames(dataViewPresenter), CentrifugeConstantsLocator.get().graph_defaultName());
        def.setName(name);
        
        return def;
    }

    public void deleteLink(LinkProxy linkProxy) {
        model.getLinkDefs().remove(linkProxy.getLinkDef());

    }

    public void deleteNode(NodeProxy nodeProxy) {
        model.getNodeDefs().remove(nodeProxy.getNodeDef());
        List<LinkDef> linkDefs = Lists.newArrayList(model.getLinkDefs());
        for (LinkDef linkDef : linkDefs) {
            if (linkDef.getNodeDef1().getUuid().equalsIgnoreCase(nodeProxy.getNodeDef().getUuid())) {
                model.getLinkDefs().remove(linkDef);
            } else if (linkDef.getNodeDef2().getUuid().equalsIgnoreCase(nodeProxy.getNodeDef().getUuid())) {
                model.getLinkDefs().remove(linkDef);
            }
        }
        for (BundleDef bundleDef : getBundleDefs()) {
            List<BundleOp> opsToRemove = Lists.newArrayList();
            for (BundleOp bundleOp : bundleDef.getOperations()) {
                if (bundleOp.getNodeDef().getUuid() == nodeProxy.getUuid()) {
                    opsToRemove.add(bundleOp);
                }
            }
            for (BundleOp bundleOp : opsToRemove) {
                bundleDef.getOperations().remove(bundleOp);
            }
        }
    }

    private boolean debounce;
    private GraphTheme graphTheme;

    public void editLink(LinkProxy linkProxy) {
        //FIXME: would like to control business logic through Activities, but this should work.
        if(!debounce) {
            activityManager.setActivity(new EditLink(linkProxy, this));
            debounce = true;
            Scheduler.get().scheduleFixedDelay(new Scheduler.RepeatingCommand() {
                @Override
                public boolean execute() {
                    debounce = false;
                    return false;
                }
            }, 200);
        }
    }

    public void editNode(NodeProxy nodeProxy) {
        //FIXME: would like to control business logic through Activities, but this should work.
        if(!debounce){
            activityManager.setActivity(new EditNode(nodeProxy, this));
            debounce = true;
            Scheduler.get().scheduleFixedDelay(new Scheduler.RepeatingCommand() {
                @Override
                public boolean execute() {
                    debounce = false;
                    return false;
                }
            },200);
        }
    }

    public List<BundleDef> getBundleDefs() {
        return model.getBundleDefs();
    }

    public Collection<LinkProxy> getLinkProxies() {
        List<LinkProxy> proxies = Lists.newArrayList();
        Collection<LinkDef> linkDefs = model.getLinkDefs();
        for (LinkDef linkDef : linkDefs) {
            proxies.add(LinkProxy.get(linkDef));
        }
        return proxies;
    }

    GraphSettingsModel getModel() {
        return model;
    }

    public Collection<NodeProxy> getNodeProxies() {
        Collection<NodeDef> nodeDefs = model.getNodeDefs();
        checkNotNull(nodeDefs);
        List<NodeProxy> proxies = Lists.newArrayList();
        for (NodeDef nodeDef : nodeDefs) {
            proxies.add(nodeProxyFactory.create(nodeDef));
        }
        return proxies;
    }
    
    public GraphTheme getCurrentTheme(){
        if(graphTheme != null && getThemeUuid() != null && graphTheme.getUuid().equals(getThemeUuid()))
            return graphTheme;
        else 
            return null;
    }
    
    HashMap<String, NodeStyle> map = new HashMap<String, NodeStyle>();
    
    public NodeStyle getNodeStyle(String field){
        NodeStyle nodeStyle = null;
        if(getCurrentTheme() != null){
            nodeStyle = map.get(field);
            if(nodeStyle == null){
                populateNodeMap();
                nodeStyle = map.get(field);
            }
        } else {
            return null;
        }
        
        return nodeStyle;
    }

    private void populateNodeMap() {
        map.clear();

        if(getCurrentTheme() != null && getCurrentTheme().getNodeStyles() != null){
            for(NodeStyle nodeStyle: getCurrentTheme().getNodeStyles()){
                if(nodeStyle.getFieldNames() != null)
                    for(String field: nodeStyle.getFieldNames()){
                        map.put(field, nodeStyle);
                    }
            }
        }
    }

    HashMap<String, LinkStyle> linkMap = new HashMap<String, LinkStyle>();
    
    public LinkStyle getLinkStyle(String field){
        LinkStyle linkStyle = null;
        if(getCurrentTheme() != null){
            linkStyle = linkMap.get(field);
            if(linkStyle == null){
                populateLinkMap();
                linkStyle = linkMap.get(field);
            }
        } else {
            return null;
        }
        
        return linkStyle;
    }

    private void populateLinkMap() {
        linkMap.clear();
        if(getCurrentTheme() != null && getCurrentTheme().getLinkStyles() != null){
            for(LinkStyle linkStyle: getCurrentTheme().getLinkStyles()){
                if(linkStyle.getFieldNames() != null)
                    for(String field: linkStyle.getFieldNames()){
                        linkMap.put(field, linkStyle);
                    }
            }
        }
    }

    public VortexFuture<GraphTheme> getTheme() {
        final VortexFuture<GraphTheme> future = WebMain.injector.getVortex().createFuture();
        if(getThemeUuid() == null || getThemeUuid().isEmpty()){
            Scheduler.get().scheduleDeferred(new ScheduledCommand() {
                
                @Override
                public void execute() {
                    future.fireSuccess(null);
                }
            });
        } else if(defaultTheme == null || !defaultTheme.equals(getThemeUuid())){
            future.execute(ThemeActionsServiceProtocol.class).findGraphTheme(getThemeUuid());
        } else {
            
        }
        future.addEventHandler(new VortexEventHandler<GraphTheme>(){

            @Override
            public void onSuccess(GraphTheme result) {
                if(result == null){
                    graphTheme = null;
                    model.setThemeId(null);
                    model.setOptionSetName(null);
                } else {
                    graphTheme = result;
                    model.setThemeId(result.getUuid());
                    model.setOptionSetName(result.getName());
                }
                

                populateLinkMap();
                populateNodeMap();
            }

            @Override
            public boolean onError(Throwable t) {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public void onUpdate(int taskProgess, String taskMessage) {
                // TODO Auto-generated method stub
                
            }

            @Override
            public void onCancel() {
                // TODO Auto-generated method stub
                
            }});
      
        return future;
    }

    public String getThemeUuid() {
        return model.getThemeUuid();
    }

    public String getUuid() {
        return model.getUuid();
    }

    View getView() {
        return view;
    }

    @Override
    protected void initiateValidator() {
        NotBlankValidator notBlankValidator = new NotBlankValidator(model);
        VisualizationUniqueNameValidator visualizationUniqueNameValidator = new VisualizationUniqueNameValidator(
                getDataViewDef().getModelDef().getVisualizations(), model, getVisualizationDef().getUuid());

        validator.addValidationAndFeedback(new ValidationAndFeedbackPair(notBlankValidator, StringValidationFeedback.getEmptyVisualizationFeedback()));
        validator.addValidationAndFeedback(new ValidationAndFeedbackPair(visualizationUniqueNameValidator, StringValidationFeedback.getDuplicateVisualizationFeedback()));
    }

    void setLoaded(boolean loaded) {
        isLoaded = loaded;
    }

    public void setTheme(ResourceBasics value) {
        if(value != null){
            model.setOptionSetName(value.getName());
            model.setThemeId(value.getUuid());
           
        } else {

            model.setOptionSetName(null);
            model.setThemeId("NONE");
        }
        
    }
    
    public void setThemeName(String name) {
        model.setOptionSetName(name);
    }
    
    public void setThemeUuid(String uuid)   {
        model.setThemeId(uuid);
        activityManager.setActivity(new ApplyTheme(this));
    }

    @Override
    public void show() {
        if (isLoaded) {
            activityManager.setActivity(new ShowGraphSettings(this));
        } else {
            // activityManager.setActivity(new LoadGraphSettings(this));
            super.show();
            setLoaded(true);
            activityManager.setActivity(new ShowGraphSettings(this));
        }
    }

    void showDetails(NodeProxy nodeProxy) {
        activityManager.setActivity(new ShowNodeTooltip(nodeProxy, this));
    }

    // This is what happens when you press the save button.
    @Override
    protected void saveVisualizationToServer() {
        boolean refreshOnSuccess = true;
        boolean isStructural = true;
        model.apply(getVisualizationDef());
        getVisualizationDef().setState(null);
        VortexFuture<Void> future = getVisualization().saveSettings(refreshOnSuccess, isStructural);
        future.addEventHandler(new AbstractVortexEventHandler<Void>() {

            @Override
            public void onSuccess(Void result) {
                vizSettings.hide();
                settingsActionCallback.onSaveComplete(getVisualizationDef(), vizSettings.isSuppressLoadOnSave());
            }
            
            @Override
            public boolean onError(Throwable t) {
                vizSettings.enable();
                return false;
            }
        });

    }

    private NodeProxyFactory nodeProxyFactory = new NodeProxyFactory();

    public NodeProxyFactory getNodeProxyFactory() {
        return nodeProxyFactory;
    }

    @Override
    protected void addVisualizationToServer() {
        Vortex vortex = WebMain.injector.getVortex();
        model.apply(getVisualizationDef());
        //        FieldDef fielddef = new FieldDef();
        //        fielddef.setStaticText("http://localhost:9090/Centrifuge/images/flag.png");
        //        fielddef.setFieldType(FieldType.STATIC);
        //        getVisualizationDef().getNodeDefs().get(0).getAttributeDefs().add(new AttributeDef("csi.internal.Icon", fielddef));
        // FIXME: Show some indicator for activity.
        try {

            vortex.execute(new Callback<Void>() {

                @Override
                public void onSuccess(Void result) {
                    vizSettings.hide();
                    settingsActionCallback.onSaveComplete(getVisualizationDef(), vizSettings.isSuppressLoadOnSave());
                }
            }, VisualizationActionsServiceProtocol.class).addVisualization(getVisualizationDef(), getDataViewUuid(),
                    getWorksheetUuid());

        } catch (Exception myException) {

            Dialog.showException(myException);
        }
    }

    public static String getStaticTextFromAttributeDef(AttributeDef def) {
        if (def != null) {
            FieldDef fieldDef = def.getFieldDef();
            if (fieldDef != null) {
                if (fieldDef.getFieldType().equals(FieldType.STATIC)) {
                    String staticText = fieldDef.getStaticText();
                    if (!Strings.isNullOrEmpty(staticText)) {
                        return staticText;
                    }
                }
            }
        }
        return null;
    }
}