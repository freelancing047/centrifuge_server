package csi.server.common.dto.SelectionListData;


import java.io.Serializable;

import csi.server.common.enumerations.DisplayMode;

public interface ExtendedInfo extends Serializable {

    public String getKey();
    public String getParentString();
    public String getDisplayString();
    public String getTitleString();
    public String getDescriptionString();
    public DisplayMode getDisplayMode();
    public boolean isDisabled();
    public boolean isSpecial();
    public boolean isError();
    public boolean isComponent();
    public void enable();
    public void disable();
    public int getOrdinal();
}
