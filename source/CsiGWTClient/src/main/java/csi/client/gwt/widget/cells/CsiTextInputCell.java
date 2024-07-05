package csi.client.gwt.widget.cells;

import com.google.gwt.cell.client.Cell.Context;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.sencha.gxt.cell.core.client.form.TextInputCell;
import com.sencha.gxt.cell.core.client.form.TextInputCell.TextFieldAppearance;
import com.sencha.gxt.core.client.dom.XElement;

/**
 * Created by centrifuge on 8/8/2017.
 */
public class CsiTextInputCell extends TextInputCell {


    public interface NameCallBack {

        public void onNameChange(Object keyIn, String nameIn);
    }

    private NameCallBack _nameCallBack;

    public CsiTextInputCell(NameCallBack callBackIn) {

        super();
        _nameCallBack = callBackIn;
    }

    @Override
    public void finishEditing(Element parentIn, String valueIn, Object keyIn, ValueUpdater<String> valueUpdaterIn) {

        super.finishEditing(parentIn, valueIn, keyIn, valueUpdaterIn);
        if (null != _nameCallBack) {

            _nameCallBack.onNameChange(keyIn, valueIn);
        }
    }

    @Override
    public TextFieldAppearance getAppearance() {

        return super.getAppearance();
    }

    @Override
    public void onBrowserEvent(Context contextIn, Element parentIn, String valueIn,
                               NativeEvent eventIn, ValueUpdater<String> valueUpdaterIn) {

        super.onBrowserEvent(contextIn, parentIn, valueIn, eventIn, valueUpdaterIn);
    }

    @Override
    public void render(Context contextIn, String valueIn, SafeHtmlBuilder builderIn) {

        super.render(contextIn, valueIn, builderIn);
    }

    @Override
    public void setSize(XElement parentIn, int widthIn, int heightIn) {

        super.setSize(parentIn, widthIn, heightIn);
    }

}
