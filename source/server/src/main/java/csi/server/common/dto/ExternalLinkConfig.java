package csi.server.common.dto;

import com.google.gwt.user.client.rpc.IsSerializable;

import csi.config.AbstractConfigurationSettings;

public class ExternalLinkConfig extends AbstractConfigurationSettings implements IsSerializable{

    private  String gettingStartedGuideUrl = "https://CentrifugeSystems.com/downloads/Centrifuge-Analytics-User-Guide-361.pdf";
    private  String tutorialUrl = "https://CentrifugeSystems.com/downloads/Centrifuge-Analytics-Initial-Tutorial-361.pdf";
    private  String centrifugeCompanyHomeUrl = "https://www.CentrifugeSystems.com";


    public  String getGettingStartedGuideUrl() {
        return gettingStartedGuideUrl;
    }
    public  void setGettingStartedGuideUrl(String gettingStartedGuideUrl) {
        this.gettingStartedGuideUrl = gettingStartedGuideUrl;
    }
    public  String getTutorialUrl() {
        return tutorialUrl;
    }
    public  void setTutorialUrl(String tutorialUrl) {
        this.tutorialUrl = tutorialUrl;
    }
    public  String getCentrifugeCompanyHomeUrl() {
        return centrifugeCompanyHomeUrl;
    }
    public  void setCentrifugeCompanyHomeUrl(String centrifugeCompanyHomeUrl) {
        this.centrifugeCompanyHomeUrl = centrifugeCompanyHomeUrl;
    }


}
