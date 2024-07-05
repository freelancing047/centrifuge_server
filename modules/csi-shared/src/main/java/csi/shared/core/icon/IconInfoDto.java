package csi.shared.core.icon;

import java.io.Serializable;

public class IconInfoDto implements Serializable {

    private Integer count = 0;
    private boolean editAccess = false;
    
    public boolean hasEditAccess() {
        return editAccess;
    }
    public void setEditAccess(boolean hasEditAccess) {
        this.editAccess = hasEditAccess;
    }
    public Integer getCount() {
        return count;
    }
    public void setCount(Integer count) {
        this.count = count;
    }
    
}
