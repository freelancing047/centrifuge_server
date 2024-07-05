package csi.client.gwt.widget.boot;

/**
 * Created by centrifuge on 3/1/2016.
 */
public interface CanBeShownParent extends IsWatching {

    public void showWithResults(KnowsParent childIn);
    public void show();
    public void hide();
}
