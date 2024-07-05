package csi.client.gwt.widget.list_boxes;

import csi.server.common.dto.SelectionListData.ExtendedDisplayInfo;
import csi.server.common.enumerations.DisplayMode;
import csi.server.common.util.DisplayableObject;

/**
 * Created by centrifuge on 1/18/2016.
 */
public class CsiStringStoreItem<T> extends DisplayableObject implements ExtendedDisplayInfo {

    private T _value = null;
    String _key = null;
    String _label = null;
    String _title = null;
    String _description = null;

    public CsiStringStoreItem() {

        super(DisplayMode.NORMAL);
    }

    public CsiStringStoreItem(String keyIn, T valueIn, String labelIn) {

        this(keyIn, valueIn, labelIn, DisplayMode.NORMAL);
    }

    public CsiStringStoreItem(String keyIn, T valueIn, String labelIn, String titleIn) {

        this(keyIn, valueIn, labelIn, titleIn, DisplayMode.NORMAL);
    }

    public CsiStringStoreItem(String keyIn, T valueIn, String labelIn, String titleIn, String descriptionIn) {

        this(keyIn, valueIn, labelIn, titleIn, descriptionIn, DisplayMode.NORMAL);
    }

    public CsiStringStoreItem(String keyIn, T valueIn, String labelIn, DisplayMode modeIn) {

        this(keyIn, valueIn, labelIn, null, null, modeIn);
    }

    public CsiStringStoreItem(String keyIn, T valueIn, String labelIn, String titleIn, DisplayMode modeIn) {

        this(keyIn, valueIn, labelIn, titleIn, null, modeIn);
    }

    public CsiStringStoreItem(String keyIn, T valueIn, String labelIn, String titleIn, String descriptionIn, DisplayMode modeIn) {

        super(modeIn);

        _value = valueIn;
        _key = keyIn;
        _label = labelIn;
        _title = titleIn;
        _description = descriptionIn;
    }

    public void setValue(T valueIn) {

        _value = valueIn;
    }

    public T getValue() {

        return _value;
    }

    @Override
    public String getKey() {
        return _key;
    }

    @Override
    public String getParentString() {
        return null;
    }

    @Override
    public String getDisplayString() {

        return _label;
    }

    @Override
    public String getTitleString() {

        return _title;
    }

    @Override
    public String getDescriptionString() {

        return _description;
    }
}
