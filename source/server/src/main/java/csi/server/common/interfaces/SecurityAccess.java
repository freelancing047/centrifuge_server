package csi.server.common.interfaces;

import csi.server.common.model.security.CapcoInfo;
import csi.server.common.model.security.SecurityTagsInfo;

/**
 * Created by centrifuge on 5/17/2017.
 */
public interface SecurityAccess {

    public SecurityTagsInfo getSecurityTagsInfo();
    public void setSecurityTagsInfo(SecurityTagsInfo securityTagsInfoIn);
    public CapcoInfo getCapcoInfo();
    public void setCapcoInfo(CapcoInfo capcoInfoIn);
    public String getSecurityBanner(String bannerPrefix, String bannerDelimiter,
                                    String bannerSubDelimiter, String bannerSuffix, String tagItemPrefix);
    public String getSecurityBanner(String defaultBannerIn);
    public String getSecurityBannerAbr(String defaultBannerIn);
    public String getSecurityBanner(String defaultBannerIn, String tagBannerIn);
    public String getSecurityBannerAbr(String defaultBannerIn, String tagBannerIn);
    public String getSecurityPortion();
}
