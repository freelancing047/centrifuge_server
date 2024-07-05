package csi.client.gwt.csiwizard.widgets;

import com.github.gwtbootstrap.client.ui.constants.IconType;

import com.google.gwt.user.client.ui.Widget;
import csi.client.gwt.widget.boot.AbstractCsiTab;
import csi.client.gwt.widget.ui.ResizeableAbsolutePanel;
import csi.server.common.exception.CentrifugeException;

import java.util.List;

/**
 * Created by centrifuge on 7/28/2015.
 */
public class CsiTabWidget extends AbstractCsiTab {


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                      GUI Objects                                       //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    String _tabLabel;
    IconType _tabIcon;
    Widget _widget;
    AbstractInputWidget inputWidget;
    ResizeableAbsolutePanel _panel;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    int _topMargin = -5;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public CsiTabWidget(Widget widgetIn, String tabLabelIn, IconType tabIconIn) {

        super();

        _panel = new ResizeableAbsolutePanel();
        add(_panel);
        setWidget(widgetIn);
        setIcon(tabIconIn);
        setLabel(tabLabelIn);
    }

    public Widget getWidget() {

        return _widget;
    }

    public void setLabel(String tabLabelIn) {

        _tabLabel = tabLabelIn;
    }

    @Override
    public void setIcon(IconType tabIconIn) {

        _tabIcon = tabIconIn;
        super.setIcon(_tabIcon);
    }

    public void setWidget(Widget widgetIn) {

        if (null != _widget) {

            _panel.remove(_widget);
        }
        inputWidget = null;
        _widget = widgetIn;
        if (null != _widget) {

            if (_widget instanceof AbstractInputWidget) {

                inputWidget = (AbstractInputWidget)_widget;
            }
            _panel.add(_widget);
        }
    }

    public void setPixelSize(int widthIn, int HeightIn) {

        _panel.setPixelSize(widthIn, HeightIn);
        if (null != _widget) {

            _widget.setPixelSize(widthIn - 15, HeightIn - 10);
            _panel.setWidgetPosition(_widget, 10, _topMargin);
        }
    }

    public void setTopMargin(int marginIn) {

        _topMargin = marginIn;
    }

    @Override
    public String getHeadingText() {
        return _tabLabel;
    }

    @Override
    public IconType getIconType() {
        return _tabIcon;
    }

    public String getText() throws CentrifugeException {

        return (null != inputWidget) ? inputWidget.getText() : null;
    }

    public void resetValue() {

        if (null != inputWidget) {

            inputWidget.resetValue();
        }
    }

    public boolean isValid() {

        return (null != inputWidget) ? inputWidget.isValid() : true;
    }

    public boolean atReset() {
        return (null != inputWidget) ? inputWidget.atReset() : false;
    }

    public void grabFocus() {

        if (null != inputWidget) {

            inputWidget.grabFocus();
        }
    }

    public int getRequiredHeight() {

        return (null != inputWidget) ? inputWidget.getRequiredHeight() : 0;
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                   Protected Methods                                    //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    protected void layoutDisplay() {

        if (null != inputWidget) {

            inputWidget.layoutDisplay();
        }
    }
}
