package csi.client.gwt.csiwizard.widgets;

import java.util.List;

import com.github.gwtbootstrap.client.ui.TabLink;
import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.github.gwtbootstrap.client.ui.resources.Bootstrap;

import csi.client.gwt.widget.boot.CsiTabPanel;
import csi.client.gwt.widget.boot.Dialog;

/**
 * Created by centrifuge on 7/28/2015.
 */
public class NestedTabPanelWidget extends CsiTabWidget {

    private CsiTabPanel _tabCollection;
    private int _widgetWidth;
    private int _widgetHeight;
    private int _delta = Dialog.intIconSize;
    private Bootstrap.Tabs _mode;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public NestedTabPanelWidget(String tabLabelIn,
                                IconType tabIconIn) {
        super(null, tabLabelIn, tabIconIn);

        try {

            _tabCollection = new CsiTabPanel(Bootstrap.Tabs.RIGHT);

            add(_tabCollection);

        } catch (Exception myException) {

            Dialog.showException("NestedTabPanelWidget", myException);
        }
    }

    @Override
    public void setPixelSize(int widthIn, int HeightIn) {

        try {

//        super.asWidget().setPixelSize(widthIn, HeightIn);

            List<TabLink> myList = _tabCollection.getLinks();

            _widgetWidth = widthIn - _delta;
            _widgetHeight = HeightIn;

            if (null != myList) {

                for (TabLink myWidget : myList) {

                    myWidget.setPixelSize(_widgetWidth, _widgetHeight);
                }
            }

        } catch (Exception myException) {

            Dialog.showException("NestedTabPanelWidget", myException);
        }
    }
}
