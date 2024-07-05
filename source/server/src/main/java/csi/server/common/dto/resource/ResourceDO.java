package csi.server.common.dto.resource;



import java.util.Date;
import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;


public class ResourceDO implements IsSerializable {

    public String uuid;
    public List<String> permissions;
    public String name;
    public String remarks;
    public Date createDate;
    public Date lastOpenDate;
    public Date lastUpdateDate;
    public Boolean spinoff;

    public ResourceDO() {
    }

}