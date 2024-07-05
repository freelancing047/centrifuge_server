package csi.client.gwt.csiwizard.widgets;

import com.google.gwt.dom.client.Style;

import csi.client.gwt.util.BooleanResponse;
import csi.client.gwt.widget.boot.Dialog;
import csi.server.common.dto.SelectionListData.ExtendedDisplayInfo;
import csi.server.common.exception.CentrifugeException;

/**
 * Created by centrifuge on 9/12/2016.
 */
public class DualPairedListWidget<S extends ExtendedDisplayInfo, T extends ExtendedDisplayInfo> extends AbstractInputWidget {

    PairedListWidget<S> _topWidget;
    PairedListWidget<T> _bottomWidget;

    protected String[] _choices;
    protected boolean _localControl;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public DualPairedListWidget() {

        this(null, null);
    }

    public DualPairedListWidget(BooleanResponse handlerIn) {

        this(handlerIn, null);
    }

    public DualPairedListWidget(BooleanResponse handlerIn, String[] choicesIn, boolean localControlIn) {

        this(handlerIn, null, choicesIn, localControlIn);
    }

    public DualPairedListWidget(BooleanResponse topHandlerIn, BooleanResponse bottomHandlerIn) {

        this(topHandlerIn, bottomHandlerIn, null, false);
    }

    public DualPairedListWidget(BooleanResponse topHandlerIn, BooleanResponse bottomHandlerIn,
                                String[] choicesIn, boolean localControlIn) {

        _choices = choicesIn;
        _localControl = localControlIn;
        _topWidget = new PairedListWidget<S>(_choices, _localControl, topHandlerIn);
        _bottomWidget = new PairedListWidget<T>(_choices, _localControl, bottomHandlerIn);

        add(_topWidget);
        add(_bottomWidget);
    }

    public PairedListWidget<S> getTopWidget() {

        return _topWidget;
    }

    public PairedListWidget<T> getBottomWidget() {

        return _bottomWidget;
    }

    public void forceList(int indexIn) {

        if ((null != _choices) && (!_localControl)) {

            _topWidget.forceList(indexIn);
            _bottomWidget.forceList(indexIn);
        }
    }

    @Override
    public String getText() throws CentrifugeException {
        return null;
    }

    @Override
    public void resetValue() {

        _topWidget.resetValue();
        _bottomWidget.resetValue();
    }

    @Override
    public boolean isValid() {

        return _topWidget.isValid() && _bottomWidget.isValid();
    }

    @Override
    public boolean atReset() {

        return _topWidget.atReset() && _bottomWidget.atReset();
    }

    @Override
    public void grabFocus() {

    }

    @Override
    public int getRequiredHeight() {
        return _topWidget.getRequiredHeight() + _bottomWidget.getRequiredHeight() + Dialog.intMargin;
    }

    @Override
    protected void layoutDisplay() {

        int myWidgetHeight = (getHeight() - Dialog.intMargin) / 2;
        int myWidgetWidth = getWidth();
        int myBottomWidgetTop = myWidgetHeight + (Dialog.intMargin / 2);

        _topWidget.setPixelSize(myWidgetWidth, myWidgetHeight);
        setWidgetTopHeight(_topWidget, 0, Style.Unit.PX, myWidgetHeight, Style.Unit.PX);
        setWidgetLeftWidth(_topWidget, 0, Style.Unit.PX, myWidgetWidth, Style.Unit.PX);

        _bottomWidget.setPixelSize(myWidgetWidth, myWidgetHeight);
        setWidgetTopHeight(_bottomWidget, myBottomWidgetTop, Style.Unit.PX, myWidgetHeight, Style.Unit.PX);
        setWidgetLeftWidth(_bottomWidget, 0, Style.Unit.PX, myWidgetWidth, Style.Unit.PX);
    }
}
