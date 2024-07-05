package csi.client.gwt.viz.shared.menu;

import com.github.gwtbootstrap.client.ui.Dropdown;
import com.github.gwtbootstrap.client.ui.base.UnorderedList;
import com.google.gwt.dom.client.Style;

/**
 * Created by centrifuge on 4/25/2016.
 */
public class CsiDropdown extends Dropdown {

    String _myWidth = null;

    String _myHeight = null;

    public CsiDropdown() {

        super();
    }

    public CsiDropdown(String captionIn) {

        super(captionIn);
    }

    public void setScrolling(int heightIn) {

        UnorderedList myMenu = getMenuWiget();

        myMenu.getElement().getStyle().setOverflowY(Style.Overflow.SCROLL);
        myMenu.setHeight(Integer.toString(heightIn) + "px");
    }

    public void cancelScrolling() {

        UnorderedList myMenu = getMenuWiget();

        myMenu.getElement().getStyle().setOverflowY(Style.Overflow.HIDDEN);
        myMenu.setHeight("");
    }
}
