package csi.client.gwt.widget.boot;

import com.github.gwtbootstrap.client.ui.Tab;
import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;

import csi.client.gwt.widget.WatchBox;
import csi.client.gwt.widget.WatchBoxInterface;

public abstract class AbstractCsiTab extends Tab {


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                      GUI Objects                                       //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    protected WatchBoxInterface watchBox = WatchBox.getInstance();


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public AbstractCsiTab() {
        super();

        try {

            asTabLink().setTablePane(new TabReferencingTabPane(this));
            DeferredCommand.add(new Command() {
                public void execute() {
                    labelTab();
                }
            });

        } catch (Exception myException) {

            Dialog.showException("AbstractCsiTab", myException);
        }
    }
    
    public void labelTab() {

        try {

            setHeading(getHeadingText());
            if (getIconType() != null) {
                setIcon(getIconType());
            }

        } catch (Exception myException) {

            Dialog.showException("AbstractCsiTab", myException);
        }
    }

    public void clearAllPanelWidgets() {

        try {

            for (int i = 0; i < getTabPane().getWidgetCount(); i++) {
                getTabPane().remove(0);
            }

        } catch (Exception myException) {

            Dialog.showException("AbstractCsiTab", myException);
        }
    }

    /**
     * @return Tab heading text
     */
    public abstract String getHeadingText();

    /**
     * @return Icon to display to the left of the tab.
     */
    public abstract IconType getIconType();

    /**
     * Called when tab is clicked on (or on first render, if this tab is the default one).
     */
    public void onShow() {
        // Noop
    }

    /**
     * Called when tab looses focus.
     */
    public void onHide() {
        // Noop
    }
}
