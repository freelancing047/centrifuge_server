package csi.client.gwt.widget.boot;

import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.widget.WatchBox;
import csi.client.gwt.widget.WatchBoxInterface;

/**
 * Created by centrifuge on 9/14/2016.
 */
public abstract class WatchingParent extends WatchBoxSource implements CanBeShownParent {


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    protected static final CentrifugeConstants _constants = CentrifugeConstantsLocator.get();

    protected WatchingParent _this = this;

    private CanBeShownParent canBeShownParent;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                   Abstract Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public abstract void show();


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public WatchingParent() {

        super(null);
    }

    public WatchingParent(CanBeShownParent parentIn) {

        super(parentIn);
        canBeShownParent = parentIn;
    }

    public void show(CanBeShownParent parentIn) {

        canBeShownParent = parentIn;
        setParent(parentIn);
        show();
    }

    public void showWithResults(KnowsParent childIn) {

        retrieveResults(childIn);
        if (null != childIn) {

            childIn.destroy();
        }
        show();
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Protected Methods                                   //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    protected CanBeShownParent getParent() {

        return canBeShownParent;
    }

    protected void retrieveResults(KnowsParent childIn) {

        // DO NOTHING -- Overriding method required to perform retrieval
    }
}
