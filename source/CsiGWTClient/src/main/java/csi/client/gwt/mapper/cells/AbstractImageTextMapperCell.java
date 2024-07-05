package csi.client.gwt.mapper.cells;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeUri;
import com.sencha.gxt.core.client.XTemplates;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public abstract class AbstractImageTextMapperCell<T> extends AbstractCell<T> {

    interface Template1 extends XTemplates {

        @XTemplate("<img width=\"16\" height=\"15\" src=\"{dataUriIn}\"/></span>")
        SafeHtml html(SafeUri dataUriIn);
    }

    interface Template2 extends XTemplates {

        @XTemplate("<span title=\"{valueIn}\">&nbsp;&nbsp{valueIn}</span>")
        SafeHtml html(String valueIn);
    }

    interface Template3 extends XTemplates {

        @XTemplate("<span title=\"{valueIn}\">&nbsp;&nbsp;<img width=\"16\" height=\"15\" src=\"{dataUriIn}\"/>&nbsp;&nbsp;{valueIn}</span>")
        SafeHtml html(SafeUri dataUriIn, String valueIn);
    }

    interface Template6 extends XTemplates {

        @XTemplate("<span title=\"{groupIn}.{valueIn}\">&nbsp;&nbsp;{groupIn}.{valueIn}</span>")
        SafeHtml html(String valueIn, String groupIn);
    }

    interface Template7 extends XTemplates {

        @XTemplate("<span title=\"{groupIn}.{valueIn}\">&nbsp;&nbsp;<img width=\"16\" height=\"15\" src=\"{dataUriIn}\"/>&nbsp;&nbsp;{groupIn}.{valueIn}</span>")
        SafeHtml html(SafeUri dataUriIn, String valueIn, String groupIn);
    }

    protected static Template1 template1 = GWT.create(Template1.class);
    protected static Template2 template2 = GWT.create(Template2.class);
    protected static Template3 template3 = GWT.create(Template3.class);
    protected static Template6 template6 = GWT.create(Template6.class);
    protected static Template7 template7 = GWT.create(Template7.class);
}