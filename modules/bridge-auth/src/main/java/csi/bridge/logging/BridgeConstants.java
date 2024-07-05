package csi.bridge.logging;

import javax.xml.namespace.QName;

public interface BridgeConstants {

    String ALS_WSDL_URL_LOCAL = "META-INF/services/als.wsdl";
    String ALS_SERVICE = "ALSService";
    String ALS_NS = "http://oculusinfo.com/ncompass/als/3.0";

    String CSI_URN = "urn:centrifuge:server";
    String CSI_SERVER = "Centrifuge Server";
    String CSI_SERVER_VERSION = "1.6.0 Mako";

    String AUTHENTICATED_USER = "csi::user::name";

    QName ALS_SERVICE_NAME = new QName(ALS_NS, ALS_SERVICE);
}