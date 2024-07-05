package csi.client.gwt.widget.cells.readonly;

import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.sencha.gxt.core.client.XTemplates;

/**
 * Created by centrifuge on 9/8/2016.
 */
public class CsiTitleCell extends TextCell {

    int _limit = 0;

    public CsiTitleCell() {

        super();
    }

    public CsiTitleCell(int limitIn) {

        super();
        _limit = limitIn;
    }

    interface DisplayTemplate extends XTemplates {

        @XTemplate("<span title=\"{valueOneIn}\">&nbsp;&nbsp;{valueTwoIn}</span>")
        SafeHtml display(String valueOneIn, String valueTwoIn);
    }

    private static final DisplayTemplate myTemplate = GWT.create(DisplayTemplate.class);

    @Override
    public void render(Context contextIn, String valueIn, SafeHtmlBuilder htmlBuilderIn) {

        if (null != valueIn) {

            if ((0 < _limit) && (valueIn.length() > _limit)) {

                htmlBuilderIn.append(myTemplate.display(valueIn, valueIn.substring(0, _limit) + " . . ."));

            } else {

                htmlBuilderIn.append(myTemplate.display(valueIn, valueIn));
            }
        }
    }
}
