package csi.client.gwt.mapper.data_model;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.safehtml.shared.UriUtils;

import csi.client.gwt.dataview.resources.DataSourceClientUtil;
import csi.client.gwt.mapper.menus.MappingItem;
import csi.server.common.enumerations.CsiDataType;
import csi.server.common.enumerations.JdbcDriverType;


/**
 * Created by centrifuge on 3/25/2016.
 */
public abstract class SelectionDataAccess<T> extends BasicDragItem implements MappingItem {

    public abstract int getOrdinal();
    public abstract String getGroupDisplayName();
    public abstract String getItemDisplayName();
    public abstract CsiDataType getItemDataType();
    public abstract JdbcDriverType getGroupType();
    public abstract void setMapped(boolean mappedIn);
    public abstract boolean isMapped();

    public abstract T getData();

    public SelectionDataAccess(String idIn) {

        super(idIn);
    }

    public String getName() {

        return getItemDisplayName();
    }

    public CsiDataType getType() {

        return getItemDataType();
    }

    public ImageResource getDataTypeImage() {

        return DataSourceClientUtil.get(this.getItemDataType());
    }

    public SafeUri getDataTypeImageHtml() {

        ImageResource myImage = getDataTypeImage();

        return (null != myImage) ? myImage.getSafeUri() : UriUtils.fromSafeConstant("");
    }

    public CsiDataType getCastToDataType() {

        return getItemDataType();
    }

    public CsiDataType getCastToType() {

        return getCastToDataType();
    }

    public ImageResource getCastToTypeImage() {

        return DataSourceClientUtil.get(this.getCastToType());
    }

    public SafeUri getCastToTypeImageHtml() {

        ImageResource myImage = getCastToTypeImage();

        return (null != myImage) ? myImage.getSafeUri() : UriUtils.fromSafeConstant("");
    }

    public ImageResource getGroupImage() {

        return DataSourceClientUtil.get(this.getGroupType(), false);
    }

    public SafeUri getGroupImageHtml() {

        ImageResource myImage = getGroupImage();

        return (null != myImage) ? myImage.getSafeUri() : UriUtils.fromSafeConstant("");
    }
}

