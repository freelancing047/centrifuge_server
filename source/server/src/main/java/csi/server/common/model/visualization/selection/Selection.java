package csi.server.common.model.visualization.selection;

import java.io.Serializable;

/**
 * @author Centrifuge Systems, Inc.
 */
public interface Selection extends Serializable {

    public boolean isCleared();

    public void clearSelection();

    public void setFromSelection(Selection selection);
    
    public Selection copy();
}
