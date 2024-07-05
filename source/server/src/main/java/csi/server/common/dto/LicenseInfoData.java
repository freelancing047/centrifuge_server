package csi.server.common.dto;



import com.google.gwt.user.client.rpc.IsSerializable;


public class LicenseInfoData implements IsSerializable {

    public Boolean licenseExpires;
    public Boolean expirationEnabled;
    public Boolean userExpiresByDefault;
    public long userExpirationDuration;
    public String userExpirationOn;
}
