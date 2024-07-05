package csi.client.gwt.viz.graph.node.settings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

import csi.client.gwt.WebMain;
import csi.client.gwt.viz.graph.node.settings.appearance.AppearanceTab;
import csi.client.gwt.viz.graph.node.settings.appearance.NodeIcon;
import csi.client.gwt.viz.graph.node.settings.appearance.NodeShape;
import csi.client.gwt.viz.graph.node.settings.appearance.NodeTransparency;
import csi.client.gwt.viz.graph.node.settings.bundle.BundleSpecificationPresenter;
import csi.client.gwt.viz.graph.node.settings.bundle.BundleSpecificationPresenterImpl;
import csi.client.gwt.viz.graph.node.settings.tooltip.NodeTooltip;
import csi.client.gwt.viz.graph.node.settings.tooltip.TooltipFunction;
import csi.client.gwt.viz.graph.node.settings.type.NodeType;
import csi.client.gwt.viz.graph.shared.AbstractGraphObjectScale.ScaleMode;
import csi.client.gwt.vortex.AbstractVortexEventHandler;
import csi.client.gwt.vortex.VortexFuture;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.graphics.shapes.ShapeType;
import csi.server.common.model.FieldDef;
import csi.server.common.service.api.IconActionsServiceProtocol;
import csi.shared.core.color.ClientColorHelper;
import csi.shared.core.color.ClientColorHelper.Color;

public class ShowNodeSettings extends AbstractActivity {

    private NodeSettings nodeSettings;
    private BundleSpecificationPresenter bundleSpecificationPresenter;

    public ShowNodeSettings(NodeSettings nodeSettings) {
        this.nodeSettings = nodeSettings;
        setBundleSpecificationPresenter(new BundleSpecificationPresenterImpl(nodeSettings));
    }

    public void setColor(Color color) {
        NodeSettingsDialog view = nodeSettings.getView();
        NodeSettingsModel model = nodeSettings.getModel();
        model.getShape().setColor(color);
        view.getAppearanceTab().setColor(color);
        view.getAppearanceTab().setPreviewRenderedNode(model.getRenderedNode());
    }

    public void setIcon(String icon) {
        NodeSettingsDialog view = nodeSettings.getView();
        NodeSettingsModel model = nodeSettings.getModel();
        model.getIcon().setName(icon);        
//        final VortexFuture<String> vortexFuture = WebMain.injector.getVortex().createFuture();
//        vortexFuture.addEventHandler(new AbstractVortexEventHandler<String>() {
//
//          @Override
//          public void onSuccess(String result) {
//              
//              if(nodeSettings.getView() != null)
//                  nodeSettings.getView().getAppearanceTab().setIcon(result);
//              
//          }});
//        
//        try {
//            vortexFuture.execute(IconActionsServiceProtocol.class).getDataUrlImage(icon);
//        } catch (CentrifugeException e) {
//            
//        }
      
        view.getAppearanceTab().setPreviewRenderedNode(model.getRenderedNode());
    }

    public void updateLabelSettings(boolean value) {
        NodeSettingsDialog view = nodeSettings.getView();
        NodeSettingsModel model = nodeSettings.getModel();
        model.setHideLabels(value == false);
        view.getAppearanceTab().enableLabelSettings(value);
        if(!value){
            model.getLabel().setFixed(true);
            model.getLabel().setText(null);
        }
        
        if (model.getLabel().isFixed()) {
            view.getAppearanceTab().setLabelText(model.getLabel().getText());
            setLabelFixed(true);
        } else {
            view.getAppearanceTab().setLabelText(model.getName());// TODO: Should be something reasonable
            setLabelFixed(false);
            view.getAppearanceTab().setLabelField(model.getLabel().getField());
        }
        view.getAppearanceTab().enableAllLabelSettings(value);
    }

    public void updateSizeSettings(boolean value) {
        NodeSettingsDialog view = nodeSettings.getView();
        NodeSettingsModel model = nodeSettings.getModel();
        model.getSize().setEnabled(value);
        view.getAppearanceTab().enableAllScaleOptions(value);
    }

    public void updateName(String name) {
        NodeSettingsModel model = nodeSettings.getModel();
        model.setName(name);
    }

    @Override
    public void start(AcceptsOneWidget panel, EventBus eventBus) {
        nodeSettings.getView().setPresenter(this);
        final NodeSettingsDialog view = nodeSettings.getView();
        final NodeSettingsModel model = nodeSettings.getModel();
        // Name
        view.getAppearanceTab().setName(model.getName());
        // Scale
        switch (model.getSize().getMode()) {

            case DYNAMIC:
                view.getAppearanceTab().setSizeByMetric();
                view.getAppearanceTab().setSizeMetric(model.getSize().getMeasure());
                break;
            case FIXED:
                view.getAppearanceTab().setSizeFixed();
                view.getAppearanceTab().scaleValue(model.getSize().getValue());
                break;
            case COMPUTED:
                view.getAppearanceTab().setSizeByComputed();
                view.getAppearanceTab().setSizeComputedField(model.getSize().getField());
                view.getAppearanceTab().setSizeComputedFunction(model.getSize().getFunction());
                break;
        }

        updateSizeSettings(model.getSize().isEnabled());
                // Scale
        NodeTransparency transparency = model.getTransparency();
        switch (transparency.getMode()) {

            case DYNAMIC:
                view.getAppearanceTab().setTransparencyByMetric();
                view.getAppearanceTab().setTransparencyField(transparency.getMeasure());
                break;
            case FIXED:
                view.getAppearanceTab().setTransparencyByFixed();
                view.getAppearanceTab().setTransparencyValue(transparency.getValue());
                break;
            case COMPUTED:
                view.getAppearanceTab().setTransparencyByComputed();
                view.getAppearanceTab().setTransparencyComputedField(transparency.getField());
                view.getAppearanceTab().setTransparencyComputedFunction(transparency.getFunction());
                break;
        }

        updateTransparencySettings(transparency.isEnabled());
        // Label
        updateLabelSettings(model.hasLabel());
        // Type
        updateTypeSettings();
        // Identity
        updateIdentitySettings();
        // Shape
        updateShapeList();
        view.getAppearanceTab().setShape(model.getShape().getType());
        view.getAppearanceTab().setColor(model.getShape().getColor());
        updateShapeSettings(model.getShape().isEnabled(), false);
        view.getAppearanceTab().enableHideShapeCheckBox(true);
        view.getAppearanceTab().enableShapePicker(model.getShape().isEnabled());
        view.getAppearanceTab().setShapeCheckBox(model.getShape().isEnabled());
        // Color
        updateColorSettings(model.getShape().isColorEnabled(), false);
        view.getAppearanceTab().setColorCheckBox(model.getShape().isColorEnabled());
        // Icon
        updateIconSettings(model.getIcon().isEnabled(), false);
        
        
        // Business logic regarding themes
        if (model.getType() != null && model.getType().getText() != null && nodeSettings.getTheme() != null 
                && (nodeSettings.getTheme().findNodeStyle(model.getType().getText()) != null )) {
                //view.getAppearanceTab().enableHideShapeCheckBox(false);
                //view.getAppearanceTab().enableAllShapeOptions(false);
                //view.getAppearanceTab().enableHideIconCheckBox(false);
            
        } 
        
        List<NodeTooltip> tooltips = model.getTooltips();
        for (NodeTooltip nodeTooltip : tooltips) {
            view.addTooltip(nodeTooltip);
        }
        view.setBundles(model.getBundles());
        
        // Preview
        view.getAppearanceTab().setPreviewRenderedNode(model.getImage());
        view.show();
    }

    private void updateIdentitySettings() {
        NodeSettingsDialog view = nodeSettings.getView();
        NodeSettingsModel model = nodeSettings.getModel();
        NodeIdentity identity = model.getIdentity();
        if (identity.isFixed()) {
            view.getAppearanceTab().setIdentityText(identity.getText());
            setIdentityFixed(true);
        } else {
            view.getAppearanceTab().setIdentityText(model.getName());// TODO: Should be something reasonable
            setIdentityFixed(false);
            view.getAppearanceTab().setIdentityField(identity.getFieldDef());
        }
    }

    public void updateTransparencySettings(boolean enabled) {

        NodeSettingsDialog view = nodeSettings.getView();
        NodeSettingsModel model = nodeSettings.getModel();
        model.getTransparency().setEnabled(enabled);
        view.getAppearanceTab().enableAllTransparencyOptions(enabled);
    }

    public void updateIconSettings(boolean value, boolean render) {
        NodeSettingsDialog view = nodeSettings.getView();
        NodeSettingsModel model = nodeSettings.getModel();
        NodeIcon icon = model.getIcon();
        icon.setEnabled(value);
        final AppearanceTab appearanceTab = view.getAppearanceTab();
        if (render) {
            appearanceTab.setPreviewRenderedNode(model.getRenderedNode());
        }
        
        appearanceTab.setIconCheckBox(value);
        appearanceTab.setIconFixed(icon.isFixed());
        
        if(icon.isFixed() && icon.isEnabled() && icon.getName() != null && !icon.getName().isEmpty()){
            final VortexFuture<String> futureTask = WebMain.injector.getVortex().createFuture();
            try {
                futureTask.execute(IconActionsServiceProtocol.class).getDataUrlImage(icon.getName());
            } catch (CentrifugeException e) {
                
            }
            futureTask.addEventHandler(new AbstractVortexEventHandler<String>() {

                @Override
                public void onSuccess(String result) {

                    appearanceTab.setIcon(result);
                }
                
                @Override
                public boolean onError(Throwable t){
                    return true;
                }
            });
        }
        
        if (icon.getFieldDef()!= null) {
            appearanceTab.setIconField(icon.getFieldDef());
        }
    }

    public void setShape(ShapeType shapeType) {
        NodeSettingsDialog view = nodeSettings.getView();
        NodeSettingsModel model = nodeSettings.getModel();
        model.getShape().setType(shapeType);
        view.getAppearanceTab().setPreviewRenderedNode(model.getRenderedNode());
    }

    public void updateShapeSettings(boolean value, boolean render) {
        NodeSettingsDialog view = nodeSettings.getView();
        NodeSettingsModel model = nodeSettings.getModel();
        view.getAppearanceTab().enableAllShapeOptions(value);
        model.getShape().setEnabled(value);
        if(value){
            if(render)
                view.getAppearanceTab().setPreviewRenderedNode(model.getRenderedNode());
        } else {
            setShape(model.getNodeProxy().getShape());
        }
    }
    
    public void updateColorSettings(boolean value, boolean render) {
        NodeSettingsDialog view = nodeSettings.getView();
        NodeSettingsModel model = nodeSettings.getModel();
        model.getShape().setColorEnabled(value);
        
        if(value){
            if(render)
                view.getAppearanceTab().setPreviewRenderedNode(model.getRenderedNode());
        } else {
            setColor(ClientColorHelper.get().make(model.getNodeProxy().getColor()));
        }
    }

//    private void updateIconCategoryList() {
//
//        final VortexFuture<List<String>> vortexFuture = WebMain.injector.getVortex().createFuture();
//        vortexFuture.addEventHandler(new AbstractVortexEventHandler<List<String>>() {
//
//            @Override
//            public void onSuccess(List<String> result) {
//                NodeSettingsDialog view = nodeSettings.getView();
//                NodeSettingsModel model = nodeSettings.getModel();
//                ArrayList<String> categories = Lists.newArrayList();
//                for (String folderPath : result) {
//                    String[] splitFolderPath = folderPath.split("/");
//                    String categoryName = splitFolderPath[splitFolderPath.length - 1];
//                    categories.add(categoryName);
//                }
//                java.util.Collections.sort(categories);
//                view.getAppearanceTab().setIconCategories(categories);
//
//                if (Strings.isNullOrEmpty(model.getIcon().getCategory())) {
//                    if (categories.size() > 0) {
//                        model.getIcon().setCategory(categories.get(0));
//                    }
//                }
//                view.getAppearanceTab().setIconCategory(model.getIcon().getCategory());
//                updateIconList();
//            }
//        });
//        vortexFuture.execute(FileActionsServiceProtocol.class).getApplicationResourceDirectories(
//                "icons/" + nodeSettings.getTheme().getIconRoot());
//    }

//    private void updateIconList() {
//
//        final VortexFuture<List<String>> vortexFuture = WebMain.injector.getVortex().createFuture();
//        vortexFuture.addEventHandler(new AbstractVortexEventHandler<List<String>>() {
//
//            @Override
//            public void onSuccess(List<String> result) {
//                NodeSettingsDialog view = nodeSettings.getView();
//                NodeSettingsModel model = nodeSettings.getModel();
//                ArrayList<String> icons = Lists.newArrayList();
//                for (String filePath : result) {
//                    String[] splitFilePath = filePath.split("/");
//                    String filename = splitFilePath[splitFilePath.length - 1];
//                    icons.add(filename.substring(0, filename.length() - 4));
//                }
//                java.util.Collections.sort(icons);
//                view.getAppearanceTab().setIcons(icons);
//
//                if (icons.contains((model.getIcon().getName())) == false) {
//                    if (icons.size() > 0) {
//                        model.getIcon().setName(icons.get(0));
//                    }
//                }
//                view.getAppearanceTab().setIcon(model.getIcon().getName());
//                view.getAppearanceTab().setPreviewRenderedNode(model.getRenderedNode());
//            }
//        });
//        NodeSettingsModel model = nodeSettings.getModel();
//        vortexFuture.execute(FileActionsServiceProtocol.class).getApplicationResourceFiles(
//                "icons/" + nodeSettings.getTheme().getIconRoot() + "/" + model.getIcon().getCategory());
//    }

    private void updateShapeList() {
        NodeSettingsDialog view = nodeSettings.getView();
        NodeSettingsModel model = nodeSettings.getModel();
        List<ShapeType> shapes = new ArrayList<ShapeType>(Arrays.asList(ShapeType.nodeShapeWheel));
        if ((shapes != null) && (shapes.size() > 0)) {
            if ((shapes.size() > 1) && (shapes.indexOf(ShapeType.NONE) < 0)) {
                shapes.add(ShapeType.NONE);
            }
            view.getAppearanceTab().setShapes(shapes);
        }
        if (model.getShape() == null) {// not sure this can happen
            if ((shapes != null) && (shapes.size() > 0)) {
                model.getShape().setType(shapes.get(0));
            }
        }
        view.getAppearanceTab().setShape(model.getShape().getType());
    }

    public void save() {
        nodeSettings.save();
    }

    public void cancel() {
        NodeSettingsDialog view = nodeSettings.getView();
        view.close();
        nodeSettings.close();
    }

    public void delete() {
        nodeSettings.delete();
        NodeSettingsDialog view = nodeSettings.getView();
        view.close();
        nodeSettings.close();
    }

    public void setStaticScaleValue(double newValue) {
        NodeSettingsModel model = nodeSettings.getModel();
        model.getSize().setValue(newValue);
        nodeSettings.getView().getAppearanceTab().scaleValue(model.getSize().getValue());

    }
    public void setStaticTransparencyValue(double newValue) {
        NodeSettingsModel model = nodeSettings.getModel();
        model.getTransparency().setValue(newValue);
        nodeSettings.getView().getAppearanceTab().setTransparencyValue(model.getTransparency().getValue());

    }

    public void setSizeByFixed() {
        NodeSettingsModel model = nodeSettings.getModel();
            model.getSize().setMode(ScaleMode.FIXED);
        NodeSettingsDialog view = nodeSettings.getView();
        view.getAppearanceTab().setSizeFixed();
    }
    public void setSizeByDynamic() {
        NodeSettingsModel model = nodeSettings.getModel();
            model.getSize().setMode(ScaleMode.DYNAMIC);
        NodeSettingsDialog view = nodeSettings.getView();
        view.getAppearanceTab().setSizeByMetric();
    }
    public void setSizeByComputed() {
        NodeSettingsModel model = nodeSettings.getModel();
            model.getSize().setMode(ScaleMode.COMPUTED);
        NodeSettingsDialog view = nodeSettings.getView();
        view.getAppearanceTab().setSizeByComputed();
    }

    public void setScaleMeasure(SizingAttribute sizingAttribute) {
        NodeSettingsModel model = nodeSettings.getModel();
        model.getSize().setMode(ScaleMode.DYNAMIC);
        model.getSize().setMeasure(sizingAttribute);
    }

    public void setLabelFixed(Boolean value) {
        NodeSettingsDialog view = nodeSettings.getView();
        NodeSettingsModel model = nodeSettings.getModel();
        model.getLabel().setFixed(value);
        view.getAppearanceTab().setlabelFixed(value);
    }

    public void setLabelText(String value) {
        NodeSettingsDialog view = nodeSettings.getView();
        NodeSettingsModel model = nodeSettings.getModel();
        model.getLabel().setText(value);
        view.getAppearanceTab().setLabelText(value);
    }

    public void setLabelField(FieldDef value) {
        nodeSettings.getModel().getLabel().setField(value);
    }


    public void setTypeField(FieldDef value) {
        nodeSettings.getModel().getType().setFieldDef(value);
    }

    public void updateTypeSettings() {
        NodeSettingsDialog view = nodeSettings.getView();
        NodeSettingsModel model = nodeSettings.getModel();
        NodeType nodeType = model.getType();
        if (nodeType.isFixed()) {
            view.getAppearanceTab().setTypeText(nodeType.getText());
            setTypeFixed(true);
            if(nodeSettings.getModel().getIdentity().getFieldDef() != null){
                view.getAppearanceTab().setTypeField(nodeSettings.getModel().getIdentity().getFieldDef());
            }
        } else {
            view.getAppearanceTab().setTypeText(model.getName());// TODO: Should be something reasonable
            setTypeFixed(false);
            view.getAppearanceTab().setTypeField(nodeType.getFieldDef());
        }
    }

    public void setTypeFixed(boolean fixed) {
        NodeSettingsDialog view = nodeSettings.getView();
        NodeSettingsModel model = nodeSettings.getModel();
        model.getType().setFixed(fixed);
        view.getAppearanceTab().setTypeFixed(fixed);
        if(fixed){
            NodeType type = model.getType();
            if(type.getText() == null){
                type.setText(model.getName());
                view.getAppearanceTab().setTypeText(type.getText());
            }
        }
    }

    public void setTypeText(String value) {
        NodeSettingsDialog view = nodeSettings.getView();
        NodeSettingsModel model = nodeSettings.getModel();
        model.getType().setText(value);
        view.getAppearanceTab().setTypeText(value);
    }

    public BundleSpecificationPresenter getBundleSpecificationPresenter() {
        return bundleSpecificationPresenter;
    }

    public void setBundleSpecificationPresenter(BundleSpecificationPresenter bundleSpecificationPresenter) {
        this.bundleSpecificationPresenter = bundleSpecificationPresenter;
    }

    public void setIconFixed(boolean fixed) {
        NodeSettingsDialog view = nodeSettings.getView();
        NodeSettingsModel model = nodeSettings.getModel();
        model.getIcon().setFixed(fixed);
        view.getAppearanceTab().setIconFixed(fixed);
        view.getAppearanceTab().setPreviewRenderedNode(model.getRenderedNode());
    }

    public void setIconField(FieldDef value) {
        nodeSettings.getModel().getIcon().setFieldDef(value);
    }

    public void setTransparencyByDynamic() {
        NodeSettingsModel model = nodeSettings.getModel();
        model.getTransparency().setMode(ScaleMode.DYNAMIC);
        NodeSettingsDialog view = nodeSettings.getView();
        view.getAppearanceTab().setTransparencyByMetric();
    }

    public void setTransparencyByFixed() {
        NodeSettingsModel model = nodeSettings.getModel();
        model.getTransparency().setMode(ScaleMode.FIXED);
        NodeSettingsDialog view = nodeSettings.getView();
        view.getAppearanceTab().setTransparencyByFixed();

    }

    public void setTransparencyByComputed() {
        NodeSettingsModel model = nodeSettings.getModel();
        model.getTransparency().setMode(ScaleMode.COMPUTED);
        NodeSettingsDialog view = nodeSettings.getView();
        view.getAppearanceTab().setTransparencyByComputed();
    }


    public void setSizeField(FieldDef value) {
        NodeSettingsModel model = nodeSettings.getModel();
        model.getSize().setField(value);
        NodeSettingsDialog view = nodeSettings.getView();
        view.getAppearanceTab().setSizeField(model.getSize().getField());

    }
    public void setTransparencyField(FieldDef value) {
        NodeSettingsModel model = nodeSettings.getModel();
        model.getTransparency().setField(value);
        NodeSettingsDialog view = nodeSettings.getView();
        view.getAppearanceTab().setTransparencyComputedField(model.getTransparency().getField());
    }

    public void setSizeFunction(TooltipFunction tooltipFunction) {
        NodeSettingsModel model = nodeSettings.getModel();
        model.getSize().setFunction(tooltipFunction);
        NodeSettingsDialog view = nodeSettings.getView();
        view.getAppearanceTab().setSizeComputedFunction(model.getSize().getFunction());
    }

    public void setTransparencyFunction(TooltipFunction value) {
        NodeSettingsModel model = nodeSettings.getModel();
        model.getTransparency().setFunction(value);
        NodeSettingsDialog view = nodeSettings.getView();
        view.getAppearanceTab().setTransparencyComputedFunction(model.getTransparency().getFunction());
    }

    public void setTransparencyMeasure(SizingAttribute sizingAttribute) {
        NodeSettingsModel model = nodeSettings.getModel();
        model.getTransparency().setMode(ScaleMode.DYNAMIC);
        model.getTransparency().setMeasure(sizingAttribute);
    }

    public void setIdentityField(FieldDef value) {
        NodeIdentity identity = nodeSettings.getModel().getIdentity();
        identity.setFieldDef(value);
        NodeSettingsDialog view = nodeSettings.getView();
        view.getAppearanceTab().setIdentityField(nodeSettings.getModel().getIdentity().getFieldDef());
    }

    public void setIdentityText(String value) {
        NodeSettingsDialog view = nodeSettings.getView();
        NodeSettingsModel model = nodeSettings.getModel();
        model.getIdentity().setText(value);
        view.getAppearanceTab().setIdentityText(model.getIdentity().getText());
    }

    public void setIdentityFixed(boolean fixed) {
        NodeSettingsDialog view = nodeSettings.getView();
        NodeSettingsModel model = nodeSettings.getModel();
        model.getIdentity().setFixed(fixed);
        view.getAppearanceTab().setIdentityFixed(fixed);
        if(fixed){
            NodeIdentity identity = model.getIdentity();
            if(identity.getText() == null){
                identity.setText(model.getName());
                view.getAppearanceTab().setIdentityText(identity.getText());
            }
        }
    }

    public void updateColorSettings(Boolean value, boolean render) {
        NodeSettingsDialog view = nodeSettings.getView();
        NodeSettingsModel model = nodeSettings.getModel();
        NodeShape shape = model.getShape();
        shape.setColorEnabled(value);
        AppearanceTab appearanceTab = view.getAppearanceTab();
        if(value){
            if(render)
                view.getAppearanceTab().setPreviewRenderedNode(model.getRenderedNode());
        } else {
            setColor(ClientColorHelper.get().make(model.getNodeProxy().getColor()));
        }
        appearanceTab.setColorCheckBox(value);
    }
}
