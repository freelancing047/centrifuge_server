package csi.security.jaas.spi;

import java.security.cert.X509Certificate;

import javax.net.ssl.X509TrustManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DummyTrustmanager implements X509TrustManager {
   private static final Logger LOG = LogManager.getLogger(DummyTrustmanager.class);

   public void checkClientTrusted(X509Certificate[] xcs, String string) {
      LOG.info("<> <> <>  DummyTrustmanager::checkClientTrusted()");
   }

   public void checkServerTrusted(X509Certificate[] xcs, String string) {
      LOG.info("<> <> <>  DummyTrustmanager::checkServerTrusted()");
   }

   public X509Certificate[] getAcceptedIssuers() {
      return new java.security.cert.X509Certificate[0];
   }
}
