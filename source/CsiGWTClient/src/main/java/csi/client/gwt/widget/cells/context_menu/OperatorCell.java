package csi.client.gwt.widget.cells.context_menu;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.BrowserEvents;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeUri;
import com.sencha.gxt.core.client.XTemplates;
import csi.client.gwt.events.GridClickEvent;
import csi.client.gwt.events.GridClickEventHandler;
import csi.client.gwt.util.FieldDefUtils;
import csi.client.gwt.widget.DataTypeCallback;
import csi.client.gwt.widget.OperatorCallback;
import csi.client.gwt.widget.OperatorMenu;
import csi.client.gwt.widget.boot.Dialog;
import csi.server.common.enumerations.ComparingToken;

/**
 * Created by centrifuge on 8/3/2017.
 */
public class OperatorCell extends AbstractCell<ComparingToken> {


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                  Embedded Interfaces                                   //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    interface Template1 extends XTemplates {

        @XTemplate("<span title=\"{labelIn}\">{labelIn}</span>")
        SafeHtml display(String labelIn);
    }
    interface Template2 extends XTemplates {

        @XTemplate("<span title=\"{labelIn}\">&nbsp;&nbsp;{symbolIn}&nbsp;&nbsp;</span>")
        SafeHtml display(String labelIn, String symbolIn);
    }
    interface MenuTemplate1 extends XTemplates {

        @XTemplate("<span title=\"{labelIn}\">{labelIn}<img width=\"16\" height=\"15\" src=\"{chevronIn}\" /></span>")
        SafeHtml display(String labelIn, SafeUri chevronIn);
    }
    interface MenuTemplate2 extends XTemplates {

        @XTemplate("<span title=\"{labelIn}\">&nbsp;&nbsp;{symbolIn}&nbsp;&nbsp;<img width=\"16\" height=\"15\" src=\"{chevronIn}\" /></span>")
        SafeHtml display(String labelIn, String symbolIn, SafeUri chevronIn);
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private static final Template1 _template1 = GWT.create(Template1.class);
    private static final Template2 _template2 = GWT.create(Template2.class);
    private static final MenuTemplate1 _menuTemplate1 = GWT.create(MenuTemplate1.class);
    private static final MenuTemplate2 _menuTemplate2 = GWT.create(MenuTemplate2.class);

    private boolean _useLabels = false;
    private OperatorCallback _callback = null;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public OperatorCell() {

        super(BrowserEvents.CLICK, BrowserEvents.CONTEXTMENU);
    }

    public OperatorCell(OperatorCallback callbackIn) {

        super(BrowserEvents.CLICK, BrowserEvents.CONTEXTMENU);

        _callback = callbackIn;
    }

    public OperatorCell(boolean useLabelsIn) {

        super(BrowserEvents.CLICK, BrowserEvents.CONTEXTMENU);

        _useLabels = useLabelsIn;
    }

    public OperatorCell(boolean useLabelsIn, OperatorCallback callbackIn) {

        super(BrowserEvents.CLICK, BrowserEvents.CONTEXTMENU);

        _useLabels = useLabelsIn;
        _callback = callbackIn;
    }

    public void setCallback(OperatorCallback callbackIn) {

        _callback = callbackIn;
    }

    @Override
    public void render(Context contextIn, ComparingToken itemIn, SafeHtmlBuilder bufferIn) {

        if (null != _callback) {

            SafeUri myChevron = FieldDefUtils.getMenuChevron().getSafeUri();
            if (_useLabels) {

                bufferIn.append(_menuTemplate1.display(itemIn.getLabel(), myChevron));

            } else {

                bufferIn.append(_menuTemplate2.display(itemIn.getLabel(), itemIn.getSymbol(), myChevron));
            }

        } else {

            if (_useLabels) {

                bufferIn.append(_template1.display(itemIn.getLabel()));

            } else {

                bufferIn.append(_template2.display(itemIn.getLabel(), itemIn.getSymbol()));
            }
        }
    }

    @Override
    public void onBrowserEvent(com.google.gwt.cell.client.Cell.Context contextIn, Element parentIn,
                               ComparingToken valueIn, NativeEvent eventIn, ValueUpdater<ComparingToken> valueUpdaterIn)
    {
        try {

            String eventType = eventIn.getType();

            if ((null != _callback) && BrowserEvents.CLICK.equals(eventType)) {

                (new OperatorMenu(_callback, contextIn.getIndex())).showAt(eventIn.getClientX(),
                        eventIn.getClientY());

            } else if ((null != _callback) && BrowserEvents.CONTEXTMENU.equals(eventType)) {

                (new OperatorMenu(_callback, contextIn.getIndex())).showAt(eventIn.getClientX(),
                                                                                    eventIn.getClientY());
            }

        } catch (Exception myException) {

            Dialog.showException(myException);
        }
    }
}
