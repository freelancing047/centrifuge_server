package csi.client.gwt.mapper.data_model;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.safehtml.shared.UriUtils;

import csi.client.gwt.dataview.resources.DataSourceClientUtil;
import csi.server.common.enumerations.ComparingToken;
import csi.server.common.enumerations.CsiDataType;

/**
 * Created by centrifuge on 3/28/2016.
 */
public class SelectionPair<S extends SelectionDataAccess<?>, T extends SelectionDataAccess<?>> extends BasicDragItem {

    S _leftItem;
    T _rightItem;
    CsiDataType _castToType;
    ComparingToken _comparingToken;

    public SelectionPair(String idIn, S leftItemIn, T rightItemIn) {

        super(idIn);

        _leftItem = leftItemIn;
        _rightItem = rightItemIn;
        _castToType = (null != leftItemIn)
                            ? leftItemIn.getCastToType()
                            : (null != _rightItem)
                                    ? _rightItem.getCastToType()
                                    : CsiDataType.Unsupported;
        _comparingToken = ComparingToken.EQ;
    }

    public SelectionPair(String idIn, S leftItemIn, T rightItemIn,
                         CsiDataType castToTypeIn, ComparingToken comparingTokenIn) {

        super(idIn);

        _leftItem = leftItemIn;
        _rightItem = rightItemIn;
        _castToType = (null != castToTypeIn)
                            ? castToTypeIn
                            : (null != leftItemIn)
                                    ? leftItemIn.getCastToType()
                                    : (null != _rightItem)
                                            ? _rightItem.getCastToType()
                                            : CsiDataType.Unsupported;
        _comparingToken = (null != comparingTokenIn) ? comparingTokenIn : ComparingToken.EQ;
    }

    public String getKey() {

        return getLeftKey() + getRightKey();
    }

    public String getLeftKey() {

        String myKey = (null != _leftItem) ? _leftItem.getKey() : "";

        return (null != myKey) ? myKey : "";
    }

    public String getRightKey() {

        String myKey = (null != _rightItem) ? _rightItem.getKey() : "";

        return (null != myKey) ? myKey : "";
    }

    public String getLeftGroupDisplayName() {

        String myGroup = (null != _leftItem) ? _leftItem.getGroupDisplayName() : "";

        return (null != myGroup) ? myGroup : "";
    }

    public String getLeftItemDisplayName() {

        String myItem = (null != _leftItem) ? _leftItem.getItemDisplayName() : "";

        return (null != myItem) ? myItem : "";
    }

    public CsiDataType getLeftItemDataType() {

        CsiDataType myType = (null != _leftItem) ? _leftItem.getItemDataType() : CsiDataType.Unsupported;

        return (null != myType) ? myType : CsiDataType.Unsupported;
    }

    public ImageResource getLeftDataTypeImage() {

        return (null != _leftItem) ? _leftItem.getDataTypeImage() : null;
    }

    public SafeUri getLeftDataTypeImageHtml() {

        return (null != _leftItem) ? _leftItem.getDataTypeImageHtml() : UriUtils.fromSafeConstant("");
    }

    public CsiDataType getLeftCastToType() {

        CsiDataType myType = (null != _leftItem) ? _leftItem.getCastToType() : CsiDataType.Unsupported;

        return (null != myType) ? myType : CsiDataType.Unsupported;
    }

    public ImageResource getLeftCastToTypeImage() {

        return (null != _leftItem) ? _leftItem.getCastToTypeImage() : null;
    }

    public SafeUri getLeftCastToTypeImageHtml() {

        return (null != _leftItem) ? _leftItem.getCastToTypeImageHtml() : UriUtils.fromSafeConstant("");
    }

    public ImageResource getLeftGroupImage() {

        return (null != _leftItem) ? _leftItem.getGroupImage() : null;
    }

    public SafeUri getLeftGroupImageHtml() {

        return (null != _leftItem) ? _leftItem.getGroupImageHtml() : UriUtils.fromSafeConstant("");
    }

    public S getLeftData() {

        return _leftItem;
    }

    public String getRightGroupDisplayName() {

        String myGroup = (null != _rightItem) ? _rightItem.getGroupDisplayName() : "";

        return (null != myGroup) ? myGroup : "";
    }

    public String getRightItemDisplayName() {

        String myItem = (null != _rightItem) ? _rightItem.getItemDisplayName() : "";

        return (null != myItem) ? myItem : "";
    }

    public CsiDataType getRightItemDataType() {

        CsiDataType myType = (null != _rightItem) ? _rightItem.getItemDataType() : CsiDataType.Unsupported;

        return (null != myType) ? myType : CsiDataType.Unsupported;
    }

    public ImageResource getRightDataTypeImage() {

        return (null != _rightItem) ? _rightItem.getDataTypeImage() : null;
    }

    public SafeUri getRightDataTypeImageHtml() {

        return (null != _rightItem) ? _rightItem.getDataTypeImageHtml() : UriUtils.fromSafeConstant("");
    }

    public CsiDataType getRightCastToType() {

        CsiDataType myType = (null != _rightItem) ? _rightItem.getCastToType() : CsiDataType.Unsupported;

        return (null != myType) ? myType : CsiDataType.Unsupported;
    }

    public ImageResource getRightCastToTypeImage() {

        return (null != _rightItem) ? _rightItem.getCastToTypeImage() : null;
    }

    public SafeUri getRightCastToTypeImageHtml() {

        return (null != _rightItem) ? _rightItem.getCastToTypeImageHtml() : UriUtils.fromSafeConstant("");
    }

    public ImageResource getRightGroupImage() {

        return (null != _rightItem) ? _rightItem.getGroupImage() : null;
    }

    public SafeUri getRightGroupImageHtml() {

        return (null != _rightItem) ? _rightItem.getGroupImageHtml() : UriUtils.fromSafeConstant("");
    }

    public void setCastToType(CsiDataType typeIn) {

        _castToType = typeIn;
    }

    public CsiDataType getCastToType() {

        return _castToType;
    }

    public ImageResource getCastToTypeImage() {

        return DataSourceClientUtil.get(_castToType);
    }

    public SafeUri getCastToTypeImageHtml() {

        ImageResource myImage = getCastToTypeImage();

        return (null != myImage) ? myImage.getSafeUri() : UriUtils.fromSafeConstant("");
    }

    public void setComparingToken(ComparingToken comparingTokenIn) {

        _comparingToken = comparingTokenIn;
    }

    public ComparingToken getComparingToken() {

        return _comparingToken;
    }

    public T getRightData() {

        return _rightItem;
    }

    public void setMapped(boolean mapedIn) {

        if (null != _rightItem) {

            _rightItem.setMapped(mapedIn);
        }
    }
}
