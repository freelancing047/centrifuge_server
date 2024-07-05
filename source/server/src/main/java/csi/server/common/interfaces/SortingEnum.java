package csi.server.common.interfaces;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by centrifuge on 3/16/2018.
 */
public interface SortingEnum<T extends SortingEnum> {

    public int ordinal();
    public String getLabel();
    public String getColumn();
    public String getDirection();
    public T getPartner();
}
