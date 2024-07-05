package csi.client.gwt.widget.cells.readonly;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.sencha.gxt.core.client.XTemplates;

/**
 * Created by centrifuge on 8/7/2017.
 */
public class ConstantCell<T> extends AbstractCell<T> {


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


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private static final Template1 _template1 = GWT.create(Template1.class);
    private static final Template2 _template2 = GWT.create(Template2.class);

    private final String _constantLabel;
    private final String _constantSymbol;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public ConstantCell(String lableIn, String symbolIn) {

        _constantLabel = lableIn;
        _constantSymbol = symbolIn;
    }

    public ConstantCell(String symbolIn) {

        _constantLabel = null;
        _constantSymbol = symbolIn;
    }

    @Override
    public void render(Cell.Context contextIn, T itemIn, SafeHtmlBuilder bufferIn) {

        if (null != _constantLabel) {

            bufferIn.append(_template2.display(_constantLabel, _constantSymbol));

        } else {

            bufferIn.append(_template1.display(_constantSymbol));
        }
    }
}
