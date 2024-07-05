package csi.client.gwt.viz.graph.surface.tooltip;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

import csi.client.gwt.WebMain;
import csi.client.gwt.viz.graph.surface.GraphSurface;
import csi.client.gwt.viz.graph.surface.MouseHandler;
import csi.client.gwt.vortex.AbstractVortexEventHandler;
import csi.client.gwt.vortex.VortexFuture;
import csi.config.advanced.graph.LineBehavior;
import csi.server.common.dto.graph.gwt.FindItemDTO;

public class ToolTipManager {

    private static final int AUTO_CLOSE_DELAY = 1500;
    private GraphSurface surface;
    private List<ToolTip> toolTips = Lists.newArrayList();
    private ToolTip tempToolTip = null;
    private TooltipCloseCommand closeCommand = null;
    private LineBehavior showOnHoverOnly = WebMain.getClientStartupInfo().getGraphAdvConfig().getTooltips().getDefaultLineBehavior();

    // TODO:keep pushing enum over boolean through stack
    public void setShowOnHoverOnly(boolean showOnHoverOnly) {
        this.showOnHoverOnly = showOnHoverOnly ? LineBehavior.HOVER : LineBehavior.ALWAYS;
    }

    public boolean showOnHoverOnly() {
        return showOnHoverOnly == LineBehavior.HOVER;
    }

    public ToolTipManager(GraphSurface surface) {
        this.surface = surface;
    }

    public void createTooltip(final int mouseX, final int mouseY) {
        
        VortexFuture<FindItemDTO> findItemAt = surface.getGraph().getModel().findItemAt(mouseX, mouseY, true);
        findItemAt.addEventHandler(new AbstractVortexEventHandler<FindItemDTO>() {

            @Override
            public void onSuccess(FindItemDTO result) {
                ToolTipManager.this.createTooltip(result, mouseX, mouseY);
            }

            @Override
            public boolean onError(Throwable t) {
                return false;
            }
        });
    }

    public void createTooltip(FindItemDTO result, int mouseX, int mouseY) {
        ToolTip toolTip = null;
        try{
            if (result != null) {
                for (ToolTip tip : toolTips) {
                    if (result.getItemKey().equals(tip.getID())) {
                        return;// we do not need to create a second tooltip.
                    }
                }

                if(tempToolTip != null && tempToolTip.getID().equals(result.getItemKey())){
                    removeToolTip(tempToolTip);
                    tempToolTip = null;
                }
                if(surface != null){
                    if(!result.getItemType().equals("node")){
                        if(result.getX() != 0) {
                            mouseX = result.getX().intValue();
                        }
                        if(result.getY() != 0) {
                            mouseY = result.getY().intValue();
                        }
                    }

                    toolTip = new ToolTip(surface, result, mouseX, mouseY, null, false);
                    toolTips.add(toolTip);

                    toolTip.getView().setDragContainer(surface.asWidget());
                    if(surface.getView() != null)
                        surface.getView().addTooltip(toolTip.getWidget(), mouseX, mouseY);
                }
            }
        } catch(Exception exception){
            if(toolTip != null){
                toolTips.remove(toolTip);
            }
            //For debugging
//                    String stack = "";
//                    for(int ii = 0; ii<exception.getStackTrace().length; ii++){
//                        stack += exception.getStackTrace()[ii];
//                    }
//                    Window.alert(stack);
        }
    }

    public void removeToolTip(ToolTip toolTip) {

        if(tempToolTip != null){
            toolTips.remove(tempToolTip);
            tempToolTip.getWidget().removeFromParent();
        }
        toolTips.remove(toolTip);
        toolTip.getWidget().removeFromParent();

        surface.getView().drawToolTipLines();
    }

    public void removeToolTip(String itemKey){
        ToolTip toolTip = findToolTip(itemKey);
        if(toolTip != null) {
            removeToolTip(toolTip);
        }
        
    }

    private ToolTip findToolTip(String itemKey) {
        for(ToolTip tip : toolTips){
            if(tip.getID().equals(itemKey)) {
                return tip;
            }
        }
        return null;
    }

    public void removeAllToolTips() {
        for (ToolTip toolTip : toolTips) {
            toolTip.getWidget().removeFromParent();
        }
        toolTips.clear();
        surface.getView().drawToolTipLines();
    }

    public List<ToolTip> getToolTips() {
        return toolTips;
    }

    public void createTempTooltip(final int mouseX, final int mouseY) {
        final ClickHandler clickHandler = new ClickHandler(){

            @Override
            public void onClick(ClickEvent event) {
                createTooltip(mouseX, mouseY);
            }};
        VortexFuture<FindItemDTO> findItemAt = surface.getGraph().getModel().findItemAt(mouseX, mouseY, false);
        findItemAt.addEventHandler(new AbstractVortexEventHandler<FindItemDTO>() {

            @Override
            public void onSuccess(FindItemDTO result) {
                ToolTip toolTip = null;

                try{
                    if (result != null) {
                        for (ToolTip tip : toolTips) {
                            if (result.getItemKey().equals(tip.getID())) {
                                return;// we do not need to create a second tooltip.
                            }
                        }
                        if(surface != null){
                            
                            if(result.directionMap != null)
                                result.directionMap.clear();

                            toolTip = new ToolTip(surface, result, mouseX, mouseY, clickHandler, result.isMoreDetails());
                            //toolTips.add(toolTip);
                            
                            if(tempToolTip != null){
                                removeToolTip(tempToolTip);
                            }
                            
                            tempToolTip = toolTip;
                            
                            toolTip.getView().setDragContainer(surface.asWidget());
                            if(surface.getView() != null)
                                surface.getView().addTooltip(toolTip.getWidget(), mouseX, mouseY);
                            
                            closeCommand = new TooltipCloseCommand(result.getItemKey()){
                                
                                @Override
                                public boolean execute() {
                                    if(tempToolTip != null && 
                                            tempToolTip.getID().equals(getTooltipId())){
                                        MouseHandler mouse = surface.getMouseHandler();
                                        int x = mouse.getMouseX();
                                        int y = mouse.getMouseY();
                                        if(tempToolTip.isWithin(x,y)){
                                            return true;
                                        } else {
                                            removeToolTip(tempToolTip);
                                        }
                                    }
                                    
                                    return false;
                                }
                            };
                            Scheduler.get().scheduleFixedDelay(closeCommand, AUTO_CLOSE_DELAY);
                        }
                    }
                } catch(Exception exception){
                    if(toolTip != null){
                        toolTips.remove(toolTip);
                    }
                    //For debugging
//                    String stack = "";
//                    for(int ii = 0; ii<exception.getStackTrace().length; ii++){
//                        stack += exception.getStackTrace()[ii];
//                    }
//                    Window.alert(stack);
                }
            }

            @Override
            public boolean onError(Throwable t) {
                return false;
            }
        });
    }

    public TooltipCloseCommand getCloseCommand() {
        return closeCommand;
    }

    public void setCloseCommand(TooltipCloseCommand closeCommand) {
        this.closeCommand = closeCommand;
    }


    public void hide(List<Integer> result) {
        List<ToolTip> toRemove = new ArrayList<ToolTip>();
        for (ToolTip toolTip : toolTips) {
            if(result.contains(toolTip.getModel().getID().get())){
                toolTip.getWidget().removeFromParent();
                toRemove.add(toolTip);
            }
        }
        toolTips.removeAll(toRemove);
        surface.getView().drawToolTipLines();
    }

    public void hideCorrespondingTooltips(List<Integer> result) {
        List<ToolTip> toRemove = new ArrayList<ToolTip>();
        for (ToolTip toolTip : toolTips) {
            if(result.contains(toolTip.getModel().getID().get())){
                toolTip.getWidget().removeFromParent();
                toRemove.add(toolTip);
            }
        }
        toolTips.removeAll(toRemove);
        surface.getView().drawToolTipLines();
    }

    public void createTooltip(FindItemDTO item) {
        VortexFuture<FindItemDTO> findItemAt = surface.getGraph().getModel().getItem(item);
        findItemAt.addEventHandler(new AbstractVortexEventHandler<FindItemDTO>() {

            @Override
            public void onSuccess(FindItemDTO result) {
                ToolTipManager.this.createTooltip(result, item.getDisplayX().intValue(), item.getDisplayY().intValue());
            }

            @Override
            public boolean onError(Throwable t) {
                return false;
            }
        });
    }
}
