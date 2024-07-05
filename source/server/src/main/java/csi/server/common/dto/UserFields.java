package csi.server.common.dto;


import com.google.gwt.user.client.rpc.IsSerializable;


public class UserFields implements IsSerializable {

    public String name;
    public String remark;
    public String password;
    public String email;
    public String admin;
    public String id;
    public String expiration;
    public boolean isPerpetual = true;

    // These are not updatable by the client.
    public boolean isLastPerpetualAdmin = false;
    public boolean isExpired;

    public String groups;
    // public String version; // used in optimistic locking by Hibernate
}
