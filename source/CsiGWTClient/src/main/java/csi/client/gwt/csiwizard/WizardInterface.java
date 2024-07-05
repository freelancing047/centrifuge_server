package csi.client.gwt.csiwizard;

import csi.client.gwt.widget.boot.CanBeShownParent;
import csi.client.gwt.widget.boot.IsWatching;
import csi.client.gwt.widget.boot.KnowsParent;

/**
 * Created by centrifuge on 6/9/2017.
 */
public interface WizardInterface extends CanBeShownParent {

    public void cancel();
    public void destroy();
    public void clickPrior();
}
