package csi.client.gwt.viz;

import com.google.gwt.user.client.ui.HasName;

import csi.client.gwt.viz.shared.chrome.VizChrome;
import csi.client.gwt.vortex.VortexFuture;
import csi.server.common.model.visualization.VisualizationDef;
import csi.server.common.model.visualization.VisualizationType;
import csi.server.common.model.visualization.selection.Selection;
import csi.shared.core.imaging.ImagingRequest;

/**
 * All visualizations should extend this interface.
 * All visualizations should draw to surface. 
 * All visualizations must have VizChrome that provides a surface.
 * 
 */
public interface Visualization extends HasName {

    public VisualizationType getType();

    /**
     * @return Returns the chrome for the visualization
     * 
     */
    public VizChrome getChrome();

    public void setReadOnly();

    public void setLimitedMenu(boolean limitedMenu);

    public void setChrome(VizChrome vizChrome);

    public String getUuid();

    public boolean hasSelection();

    public void setBroadcastListener(boolean listener);
    
    public boolean isBroadcastListener();

    /**
     * The visualization should save non-configuration state (such as selection state) to its visualization definition
     * object. This method is called as part of saveSetting().
     */
    public void saveViewStateToVisualizationDef();
    
    /**
     * @param refreshOnSuccess true to ask visualization to refresh the visualization after the save.
     * @return
     */
    public VortexFuture<Void> saveSettings(boolean refreshOnSuccess);

    /**
     * Requests the visualization to remove itself from its parent.
     */
    public void removeFromParent();
    
    public void delete();
    
    public VisualizationDef getVisualizationDef();

    public boolean isImagingCapable();
    
    /**
     * @return Imaging request to render the image for this visualization.
     */
    public ImagingRequest getImagingRequest();
    
    /**
     * Load this visualization. When this method is called, the chrome has already been set and viz-def has been 
     * fetched.
     */
    public void loadVisualization();

    public void applySelection(Selection selection);

    public void load();
    
    /**
     * Ask the visualization to reload its settings and refresh its view.
     */
    public void reload();

    VortexFuture<Void> saveSettings(boolean refreshOnSuccess, boolean isStructural);

    VortexFuture<Void> saveSettings(boolean refreshOnSuccess, boolean isStructural, boolean clearTransient);

    public String getDataViewUuid();

	public void broadcastNotify(String string);

	public boolean isViewLoaded();

    public void saveOldSelection(Selection selection);

    public void clearBroadcastNotification();



}
