package csi.server.common.interfaces;

import com.google.gwt.user.client.rpc.IsSerializable;
import csi.server.common.dto.FieldListAccess;
import csi.server.common.model.FieldDef;

import java.util.List;

/**
 * Created by centrifuge on 6/27/2017.
 */
public interface FieldDefSource extends IsSerializable {

    public List<FieldDef> getFieldDefs();
    public void setFieldDefs(List<FieldDef> listIn);
    public FieldListAccess getFieldListAccess();
    public boolean isSorted();
}
